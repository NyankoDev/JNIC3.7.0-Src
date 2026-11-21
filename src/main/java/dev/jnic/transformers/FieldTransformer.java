package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.logging.JnicLogger;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldTransformer extends OpcodeTransformer {
   public FieldTransformer() {
      super(true, 178, 179, 180, 181);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      if (instruction != null && !(instruction instanceof FieldInsnNode)) {
         throw new ClassCastException("");
      }

      FieldInsnNode fieldInstruction = (FieldInsnNode)instruction;
      Type type = Type.getType(fieldInstruction.desc);
      int stackSizeBefore = stack.sizeBefore();
      int stackSizeBefore2 = stack.sizeBefore();
      int stackSizeBefore3 = stack.sizeBefore();
      if (fieldInstruction.owner.startsWith("dev/jnic/")) {
         if (fieldInstruction.name == null) {
            throw new NullPointerException("");
         }

         if (fieldInstruction.name.equals("$jnicEnabled") && fieldInstruction.desc.equals("Z")) {
            JnicLogger.INSTANCE.debug("Replaced $jnicEnabled access in " + ownerName);
            return JniTypes.stackVariable("stack", stackSizeBefore3, type) + " = 1;\n";
         }
      }

      switch (instruction.getOpcode()) {
         case 178:
            return JniTypes.stackVariable("stack", stackSizeBefore3, type) + " = " + context.readStaticField(fieldInstruction.owner, fieldInstruction.name, fieldInstruction.desc) + ";\n";
         case 179:
            return context.writeStaticField(fieldInstruction.owner, fieldInstruction.name, fieldInstruction.desc, JniTypes.stackVariable("stack", stackSizeBefore2 - 1, type)) + ";\n";
         case 180:
            return JniTypes.stackVariable("stack", stackSizeBefore2 - 1, type)
               + " = "
               + context.readField(fieldInstruction.owner, fieldInstruction.name, fieldInstruction.desc, JniTypes.stackVariable("stack", stackSizeBefore2 - 1, Type.getObjectType("java/lang/Object")))
               + ";\n";
         case 181:
            return context.writeField(
                  fieldInstruction.owner,
                  fieldInstruction.name,
                  fieldInstruction.desc,
                  JniTypes.stackVariable("stack", stackSizeBefore - 2, Type.getObjectType("java/lang/Object")),
                  JniTypes.stackVariable("stack", stackSizeBefore2 - 1, type)
               )
               + ";\n";
         default:
            throw new AssertionError();
      }
   }
}
