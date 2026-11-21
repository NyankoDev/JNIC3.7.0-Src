package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class AllocationTransformer extends OpcodeTransformer {
   public AllocationTransformer() {
      super(true, 187, 188, 189, 197);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder source = new StringBuilder();
      int stackSizeBefore = stack.sizeBefore();
      int opcode = instruction.getOpcode();
      if (opcode <= 188) {
         if (opcode == 187) {
            int stackSizeBefore2 = stack.sizeBefore();
            Type objectType = JniTypes.OBJECT_TYPE;
            String stackValue = JniTypes.stackVariable("stack", stackSizeBefore2, objectType);
            source.append(stackValue);
            source.append(" = ");
            String[] callArguments = new String[1];
            String classReference = context.getClassCache().classReference(((TypeInsnNode)instruction).desc);
            callArguments[0] = classReference;
            String allocObjectCall = context.jniCall("AllocObject", callArguments);
            source.append(allocObjectCall);
            source.append(";\n");
            return source.toString();
         }

         if (opcode != 188) {
            throw new AssertionError();
         }

         Type objectType2 = JniTypes.OBJECT_TYPE;
         String stackValue2 = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType2);
         source.append(stackValue2);
         source.append(" = ");
         Type elementType = JniTypes.newArrayType((IntInsnNode)instruction);
         Type intType = Type.INT_TYPE;
         String stackValue3 = JniTypes.stackVariable("stack", stackSizeBefore - 1, intType);
         String arrayAllocation = this.allocateArray(context, elementType, stackValue3);
         source.append(arrayAllocation);
         source.append(";\n");
         return source.toString();
      } else {
         if (opcode == 189) {
            Type objectType3 = JniTypes.OBJECT_TYPE;
            String stackValue4 = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType3);
            source.append(stackValue4);
            source.append(" = ");
            Type objectType4 = Type.getObjectType(((TypeInsnNode)instruction).desc);
            Type intType2 = Type.INT_TYPE;
            String stackValue5 = JniTypes.stackVariable("stack", stackSizeBefore - 1, intType2);
            String arrayAllocation2 = this.allocateArray(context, objectType4, stackValue5);
            source.append(arrayAllocation2);
            source.append(";\n");
            return source.toString();
         }

         if (opcode != 197) {
            throw new AssertionError();
         }

         MultiANewArrayInsnNode arrayInstruction = (MultiANewArrayInsnNode)instruction;
         Type type = Type.getType(arrayInstruction.desc);
         int dims = arrayInstruction.dims;
         source.append("{\n");
         String indentation = this.indentation(2);
         source.append(indentation);
         source.append("jsize dim[] = {");
         IntStream intStream = IntStream.range(stackSizeBefore - 1 - dims + 1, stackSizeBefore - 1 + 1);
         IntFunction<String> arrayFactory = index -> {
            Type intType3 = Type.INT_TYPE;
            return JniTypes.stackVariable("stack", index, intType3);
         };
         String joinedValues = intStream.mapToObj(arrayFactory).collect(Collectors.joining(", "));
         source.append(joinedValues);
         source.append("};");
         int dimensionIndex = 0;
         int dims2 = arrayInstruction.dims;

         while (dimensionIndex < dims2) {
            label49: {
               String indentation2 = this.indentation(dimensionIndex + 2);
               source.append(indentation2);
               Type objectType5 = JniTypes.OBJECT_TYPE;
               String stackValue6 = JniTypes.stackVariable("stack", dimensionIndex + stackSizeBefore - 1 - dims + 1, objectType5);
               source.append(stackValue6);
               source.append(" = ");
               Type type2 = this.componentType(type, dimensionIndex + 1);
               String literalText = "]";
               String expression = "dim[" + dimensionIndex + literalText;
               String arrayAllocation3 = this.allocateArray(context, type2, expression);
               source.append(arrayAllocation3);
               source.append(";\n");
               if (dimensionIndex <= 0) {
                  if (arrayInstruction.dims - 1 <= dimensionIndex) {
                     break label49;
                  }
               } else {
                  String indentation3 = this.indentation(dimensionIndex + 2);
                  source.append(indentation3);
                  String[] callArguments2 = new String[3];
                  Type objectType6 = JniTypes.OBJECT_TYPE;
                  String stackValue7 = JniTypes.stackVariable("stack", stackSizeBefore - 1 - dims + 1 + dimensionIndex - 1, objectType6);
                  callArguments2[0] = stackValue7;
                  String expression2 = "d" + (dimensionIndex - 1);
                  callArguments2[1] = expression2;
                  Type objectType7 = JniTypes.OBJECT_TYPE;
                  String stackValue8 = JniTypes.stackVariable("stack", stackSizeBefore - 1 - dims + 1 + dimensionIndex, objectType7);
                  callArguments2[2] = stackValue8;
                  String setObjectArrayElementCall = context.jniCall("SetObjectArrayElement", callArguments2);
                  source.append(setObjectArrayElementCall);
                  source.append(";\n");
                  if (arrayInstruction.dims - 1 <= dimensionIndex) {
                     break label49;
                  }
               }

               String indentation4 = this.indentation(dimensionIndex + 2);
               source.append(indentation4);
               source.append("for(jsize d");
               source.append(dimensionIndex);
               source.append(" = 0; d");
               source.append(dimensionIndex);
               source.append(" < dim[");
               source.append(dimensionIndex);
               source.append("]; d");
               source.append(dimensionIndex);
               source.append("++) {\n");
               int dims3 = arrayInstruction.dims;
               int nextIndex = dimensionIndex + 1;
               dims2 = dims3;
               dimensionIndex = nextIndex;
               continue;
            }

            int dims4 = arrayInstruction.dims;
            int nextIndex2 = dimensionIndex + 1;
            dims2 = dims4;
            dimensionIndex = nextIndex2;
         }

         for (int nestedDimension = arrayInstruction.dims - 2; nestedDimension >= 0; nestedDimension--) {
            String indentation5 = this.indentation(nestedDimension + 2);
            source.append(indentation5);
            source.append("}\n");
         }

         source.append("    }\n;\n");
         return source.toString();
      }
   }

   private String allocateArray(CompilationContext context, Type type, String text) {
      int typeSort = type.getSort();
      if (typeSort == 10) {
         String[] callArguments = new String[]{text, null, null};
         String classReference = context.getClassCache().classReference(type.getInternalName());
         callArguments[1] = classReference;
         callArguments[2] = "NULL";
         return context.jniCall("NewObjectArray", callArguments);
      } else if (typeSort != 9) {
         String typeSuffix = JniTypes.jniTypeSuffix(type);
         String expression = "New" + typeSuffix + "Array";
         String[] callArguments2 = new String[]{text};
         return context.jniCall(expression, callArguments2);
      } else {
         String[] callArguments3 = new String[]{text, null, null};
         String classReference2 = context.getClassCache().classReference(type.getInternalName());
         callArguments3[1] = classReference2;
         callArguments3[2] = "NULL";
         return context.jniCall("NewObjectArray", callArguments3);
      }
   }

   private Type componentType(Type type, int index) {
      return Type.getType(type.getDescriptor().substring(index));
   }

   private String indentation(int index) {
      Supplier<String> factory = () -> "    ";
      return Stream.generate(factory).limit(index).collect(Collectors.joining());
   }
}
