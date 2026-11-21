package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import java.util.Locale;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ArrayTransformer extends OpcodeTransformer {
   static final boolean ASSERTIONS_DISABLED = ArrayTransformer.class.desiredAssertionStatus() ^ true;

   public ArrayTransformer() {
      super(true, 46, 47, 48, 49, 50, 51, 52, 53, 79, 80, 81, 82, 83, 84, 85, 86, 190);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      if (instruction.getOpcode() != 51 && instruction.getOpcode() != 84) {
         int opcode = instruction.getOpcode();
         if (opcode >= 46 && opcode <= 53) {
            int loadKind = opcode - 46;
            String arrayReference = arrayVariable(stack.sizeBefore() - 2, 'l');
            String arrayIndex = arrayVariable(stack.sizeBefore() - 1, 'i');
            if (loadKind == 4) {
               String elementTarget = arrayVariable(stack.sizeAfter() - 1, 'l');
               String[] callArguments = new String[]{arrayReference, arrayIndex};
               return elementTarget + " = " + context.jniCall("GetObjectArrayElement", callArguments) + ";\n";
            } else {
               String readTemporary = "temp0." + "ijfdlbcs".charAt(loadKind);
               String jniCallExpression = context.jniCall("Get" + arrayType(loadKind) + "ArrayRegion", arrayReference, arrayIndex, "1", "&" + readTemporary);
               char readSlot = loadKind >= 5 ? 'i' : "ijfd".charAt(loadKind);
               return jniCallExpression + "; " + arrayVariable(stack.sizeAfter() - 1, readSlot) + " = " + readTemporary + ";\n";
            }
         } else if (opcode >= 79 && opcode <= 86) {
            int storeKind = opcode - 79;
            String storeArray = arrayVariable(stack.sizeBefore() - 3, 'l');
            String storeIndex = arrayVariable(stack.sizeBefore() - 2, 'i');
            char writeSlot = storeKind >= 5 ? 'i' : "ijfdl".charAt(storeKind);
            String storeValue = arrayVariable(stack.sizeBefore() - 1, writeSlot);
            if (storeKind == 4) {
               String[] callArguments2 = new String[]{storeArray, storeIndex, storeValue};
               return context.jniCall("SetObjectArrayElement", callArguments2) + ";\n";
            } else {
               String elementTypeName = arrayType(storeKind);
               String writeTemporary = "temp0." + "ijfdlbcs".charAt(storeKind);
               return writeTemporary
                  + " = (j"
                  + elementTypeName.toLowerCase(Locale.ROOT)
                  + ") "
                  + storeValue
                  + "; "
                  + context.jniCall("Set" + elementTypeName + "ArrayRegion", storeArray, storeIndex, "1", "&" + writeTemporary)
                  + ";\n";
            }
         } else if (opcode == 190) {
            int topStackIndex = stack.sizeBefore() - 1;
            String lengthTarget = arrayVariable(topStackIndex, 'i');
            String[] callArguments3 = new String[]{arrayVariable(topStackIndex, 'l')};
            return lengthTarget + " = " + context.jniCall("GetArrayLength", callArguments3) + ";\n";
         } else {
            throw new AssertionError();
         }
      } else {
         return emitByteOrBooleanArray(context, stack.sizeBefore(), stack.sizeAfter(), instruction.getOpcode());
      }
   }

   private static String arrayVariable(int index, char value) {
      if (index < 0) {
         throw new AssertionError();
      } else {
         return "stack" + index + "." + value;
      }
   }

   private static String arrayType(int index) {
      String literalText;
      switch (index) {
         case 0:
            literalText = "Int";
            break;
         case 1:
            literalText = "Long";
            break;
         case 2:
            literalText = "Float";
            break;
         case 3:
            literalText = "Double";
            break;
         case 4:
         default:
            throw new AssertionError();
         case 5:
            literalText = "Byte";
            break;
         case 6:
            literalText = "Char";
            break;
         case 7:
            literalText = "Short";
      }

      return literalText;
   }

   private static String emitByteOrBooleanArray(CompilationContext context, int index, int index2, int index3) {
      boolean matches = index3 == 84;
      String expression = "stack" + (index - (matches ? 3 : 2)) + ".l";
      String expression2 = "stack" + (index - (matches ? 2 : 1)) + ".i";
      String classReference = context.getClassCache().classReference("[Z");
      String isInstanceOfCall = context.jniCall("IsInstanceOf", expression, classReference);
      String literalText = matches ? "SetBooleanArrayRegion" : "GetBooleanArrayRegion";
      String jniCallExpression = context.jniCall(literalText, expression, expression2, "1", "&temp0.z");
      String literalText2 = matches ? "SetByteArrayRegion" : "GetByteArrayRegion";
      String jniCallExpression2 = context.jniCall(literalText2, expression, expression2, "1", "&temp0.b");
      String expression3 = matches ? "temp0.z = (jboolean) (stack" + (index - 1) + ".i & 1); " + jniCallExpression + ";" : jniCallExpression + "; stack" + (index2 - 1) + ".i = temp0.z;";
      String expression4 = matches ? "temp0.b = (jbyte) stack" + (index - 1) + ".i; " + jniCallExpression2 + ";" : jniCallExpression2 + "; stack" + (index2 - 1) + ".i = temp0.b;";
      return "if (" + isInstanceOfCall + ") { " + expression3 + " } else { " + expression4 + " }\n";
   }
}
