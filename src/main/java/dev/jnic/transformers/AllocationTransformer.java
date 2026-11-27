package dev.jnic.transformers;

import static org.objectweb.asm.Opcodes.*;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class AllocationTransformer extends OpcodeTransformer {
   public AllocationTransformer() {
      super(true, NEW, NEWARRAY, ANEWARRAY, MULTIANEWARRAY);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int top = stack.sizeBefore() - 1;
      switch (instruction.getOpcode()) {
         case NEW:
            return JniTypes.stackVariable("stack", stack.sizeBefore(), JniTypes.OBJECT_TYPE) + " = "
               + context.jniCall("AllocObject", context.getClassCache().classReference(((TypeInsnNode)instruction).desc)) + ";\n";
         case NEWARRAY:
            return JniTypes.stackVariable("stack", top, JniTypes.OBJECT_TYPE) + " = "
               + this.allocateArray(context, JniTypes.newArrayType((IntInsnNode)instruction),
                  JniTypes.stackVariable("stack", top, Type.INT_TYPE)) + ";\n";
         case ANEWARRAY:
            return JniTypes.stackVariable("stack", top, JniTypes.OBJECT_TYPE) + " = "
               + this.allocateArray(context, Type.getObjectType(((TypeInsnNode)instruction).desc),
                  JniTypes.stackVariable("stack", top, Type.INT_TYPE)) + ";\n";
         case MULTIANEWARRAY:
            return this.allocateMultiArray(context, (MultiANewArrayInsnNode)instruction, stack.sizeBefore());
         default:
            throw new AssertionError();
      }
   }

   private String allocateMultiArray(CompilationContext context, MultiANewArrayInsnNode instruction, int stackSize) {
      int dimensions = instruction.dims;
      int firstSlot = stackSize - dimensions;
      StringBuilder source = new StringBuilder("{\n");
      source.append(indentation(2)).append("jsize dim[] = {")
         .append(IntStream.range(firstSlot, stackSize)
            .mapToObj(index -> JniTypes.stackVariable("stack", index, Type.INT_TYPE))
            .collect(Collectors.joining(", ")))
         .append("};");

      for (int dimension = 0; dimension < dimensions; dimension++) {
         Type component = Type.getType(instruction.desc.substring(dimension + 1));
         String array = JniTypes.stackVariable("stack", firstSlot + dimension, JniTypes.OBJECT_TYPE);
         source.append(indentation(dimension + 2)).append(array).append(" = ")
            .append(this.allocateArray(context, component, "dim[" + dimension + "]")).append(";\n");
         if (dimension > 0) {
            String parent = JniTypes.stackVariable("stack", firstSlot + dimension - 1, JniTypes.OBJECT_TYPE);
            source.append(indentation(dimension + 2))
               .append(context.jniCall("SetObjectArrayElement", parent, "d" + (dimension - 1), array)).append(";\n");
         }
         if (dimension < dimensions - 1) {
            source.append(indentation(dimension + 2)).append("for(jsize d").append(dimension).append(" = 0; d")
               .append(dimension).append(" < dim[").append(dimension).append("]; d").append(dimension).append("++) {\n");
         }
      }
      for (int dimension = dimensions - 2; dimension >= 0; dimension--) {
         source.append(indentation(dimension + 2)).append("}\n");
      }
      return source.append("    }\n;\n").toString();
   }

   private String allocateArray(CompilationContext context, Type component, String length) {
      if (component.getSort() == Type.OBJECT || component.getSort() == Type.ARRAY) {
         return context.jniCall("NewObjectArray", length, context.getClassCache().classReference(component.getInternalName()), "NULL");
      }
      return context.jniCall("New" + JniTypes.jniTypeSuffix(component) + "Array", length);
   }

   private static String indentation(int level) {
      return "    ".repeat(level);
   }
}
