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
         int var6 = instruction.getOpcode();
         if (var6 >= 46 && var6 <= 53) {
            int var15 = var6 - 46;
            String value8 = arrayVariable(stack.sizeBefore() - 2, 'l');
            String value9 = arrayVariable(stack.sizeBefore() - 1, 'i');
            if (var15 == 4) {
               String var24 = arrayVariable(stack.sizeAfter() - 1, 'l');
               String[] var12 = new String[]{value8, value9};
               return var24 + " = " + context.jniCall("GetObjectArrayElement", var12) + ";\n";
            } else {
               String var7 = "temp0." + "ijfdlbcs".charAt(var15);
               String value10 = context.jniCall("Get" + arrayType(var15) + "ArrayRegion", value8, value9, "1", "&" + var7);
               char var11 = var15 >= 5 ? 'i' : "ijfd".charAt(var15);
               return value10 + "; " + arrayVariable(stack.sizeAfter() - 1, var11) + " = " + var7 + ";\n";
            }
         } else if (var6 >= 79 && var6 <= 86) {
            int var16 = var6 - 79;
            String value18 = arrayVariable(stack.sizeBefore() - 3, 'l');
            String value19 = arrayVariable(stack.sizeBefore() - 2, 'i');
            char var13 = var16 >= 5 ? 'i' : "ijfdl".charAt(var16);
            String value20 = arrayVariable(stack.sizeBefore() - 1, var13);
            if (var16 == 4) {
               String[] var21 = new String[]{value18, value19, value20};
               return context.jniCall("SetObjectArrayElement", var21) + ";\n";
            } else {
               String value14 = arrayType(var16);
               String var22 = "temp0." + "ijfdlbcs".charAt(var16);
               return var22
                  + " = (j"
                  + value14.toLowerCase(Locale.ROOT)
                  + ") "
                  + value20
                  + "; "
                  + context.jniCall("Set" + value14 + "ArrayRegion", value18, value19, "1", "&" + var22)
                  + ";\n";
            }
         } else if (var6 == 190) {
            int var17 = stack.sizeBefore() - 1;
            String var10000 = arrayVariable(var17, 'i');
            String[] var23 = new String[]{arrayVariable(var17, 'l')};
            return var10000 + " = " + context.jniCall("GetArrayLength", var23) + ";\n";
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
      String var10000;
      switch (index) {
         case 0:
            var10000 = "Int";
            break;
         case 1:
            var10000 = "Long";
            break;
         case 2:
            var10000 = "Float";
            break;
         case 3:
            var10000 = "Double";
            break;
         case 4:
         default:
            throw new AssertionError();
         case 5:
            var10000 = "Byte";
            break;
         case 6:
            var10000 = "Char";
            break;
         case 7:
            var10000 = "Short";
      }

      return var10000;
   }

   private static String emitByteOrBooleanArray(CompilationContext context, int index, int index2, int index3) {
      boolean var4 = index3 == 84;
      String var5 = "stack" + (index - (var4 ? 3 : 2)) + ".l";
      String var6 = "stack" + (index - (var4 ? 2 : 1)) + ".i";
      String value7 = context.getClassCache().classReference("[Z");
      String value8 = context.jniCall("IsInstanceOf", var5, value7);
      String value9 = var4 ? "SetBooleanArrayRegion" : "GetBooleanArrayRegion";
      String value10 = context.jniCall(value9, var5, var6, "1", "&temp0.z");
      String value11 = var4 ? "SetByteArrayRegion" : "GetByteArrayRegion";
      String value12 = context.jniCall(value11, var5, var6, "1", "&temp0.b");
      String var13 = var4 ? "temp0.z = (jboolean) (stack" + (index - 1) + ".i & 1); " + value10 + ";" : value10 + "; stack" + (index2 - 1) + ".i = temp0.z;";
      String var14 = var4 ? "temp0.b = (jbyte) stack" + (index - 1) + ".i; " + value12 + ";" : value12 + "; stack" + (index2 - 1) + ".i = temp0.b;";
      return "if (" + value8 + ") { " + var13 + " } else { " + var14 + " }\n";
   }
}
