package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;

public class ArithmeticTransformer extends OpcodeTransformer {
   public ArithmeticTransformer() {
      super(
         false,
         96,
         97,
         98,
         99,
         100,
         101,
         102,
         103,
         104,
         105,
         106,
         107,
         108,
         109,
         110,
         111,
         112,
         113,
         114,
         115,
         116,
         117,
         118,
         119,
         120,
         121,
         122,
         123,
         124,
         125,
         126,
         127,
         128,
         129,
         130,
         131,
         132,
         148,
         149,
         150,
         151,
         152
      );
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var6 = stack.sizeBefore() - 2;
      int var7 = stack.sizeBefore() - 1;
      int var8 = instruction.getOpcode();
      if (var8 >= 96 && var8 <= 115) {
         char var9 = "ijfd".charAt(var8 & 3);
         String value10 = arithmeticVariable("stack", var6, var9);
         String value11 = arithmeticVariable("stack", var7, var9);
         String value12 = var9 == 'i' ? "(unsigned int) " : (var9 == 'j' ? "(unsigned long long) " : "");
         String var10000;
         switch ((var8 - 96) / 4) {
            case 0:
               var10000 = value10 + " = " + value12 + value10 + " + " + value11 + ";\n";
               break;
            case 1:
               var10000 = value10 + " = " + value12 + value10 + " - " + value11 + ";\n";
               break;
            case 2:
               var10000 = value10 + " = " + value12 + value10 + " * " + value11 + ";\n";
               break;
            case 3:
               var10000 = arithmeticDivision(value10, value11, var9, false);
               break;
            case 4:
               var10000 = arithmeticDivision(value10, value11, var9, true);
               break;
            default:
               throw new AssertionError();
         }

         return var10000;
      } else if (var8 >= 116 && var8 <= 119) {
         char var13 = "ijfd".charAt(var8 & 3);
         String value18 = arithmeticVariable("stack", var7, var13);
         return value18 + " = -" + (var13 == 'i' ? "(unsigned int) " : (var13 == 'j' ? "(unsigned long long) " : "")) + value18 + ";\n";
      } else if (var8 >= 120 && var8 <= 125) {
         boolean var14 = (var8 & 1) != 0;
         String value19 = arithmeticVariable("stack", var6, (char)(var14 ? 'j' : 'i'));
         String var23 = "(" + arithmeticVariable("stack", var7, 'i') + " & " + (var14 ? "0x3f" : "0x1f") + ")";
         return var8 >= 124
            ? value19 + " = " + (var14 ? "(jlong) ((unsigned long long)" : "(jint) ((unsigned int)") + value19 + " >> " + var23 + ");\n"
            : value19
               + " = "
               + (var8 < 122 ? (var14 ? "(unsigned long long) " : "(unsigned int) ") : "")
               + value19
               + (var8 < 122 ? " << " : " >> ")
               + var23
               + ";\n";
      } else if (var8 >= 126 && var8 <= 131) {
         int var15 = (var8 & 1) == 0 ? 105 : 106;
         String value20 = arithmeticVariable("stack", var6, (char)var15);
         String value24 = arithmeticVariable("stack", var7, (char)var15);
         String value26 = var8 < 128 ? " & " : (var8 < 130 ? " | " : " ^ ");
         return value20 + " = " + value20 + value26 + value24 + ";\n";
      } else if (var8 == 132) {
         IincInsnNode var16 = (IincInsnNode)instruction;
         String value21 = arithmeticVariable("local", var16.var, 'i');
         return value21 + " = (unsigned int) " + value21 + " + " + var16.incr + ";\n";
      } else if (var8 >= 148 && var8 <= 152) {
         int var17 = var8 == 148 ? 106 : (var8 <= 150 ? 102 : 100);
         String value22 = arithmeticVariable("stack", var6, (char)var17);
         String value25 = arithmeticVariable("stack", var7, (char)var17);
         String var27 = "stack" + var6 + ".i = " + value22 + " < " + value25 + " ? -1 : " + value22 + " > " + value25 + " ? 1 : ";
         return var8 == 148 ? var27 + "0;\n" : var27 + value22 + " == " + value25 + " ? 0 : " + ((var8 & 1) == 0 ? "1" : "-1") + ";\n";
      } else {
         throw new AssertionError();
      }
   }

   private static String arithmeticVariable(String text, int index, char value) {
      if (index < 0) {
         throw new AssertionError();
      } else {
         return text + index + "." + value;
      }
   }

   private static String arithmeticDivision(String text, String text2, char value, boolean enabled) {
      if (value != 'f' && value != 'd') {
         String value4 = value == 'i' ? "(jint) 0x80000000" : "(jlong) 0x8000000000000000LL";
         return "if ("
            + text2
            + " == 0) {\n    jclass exception = (*env)->FindClass(env, \"java/lang/ArithmeticException\");\n    if (exception != NULL) { (*env)->ThrowNew(env, exception, \"/ by zero\"); (*env)->DeleteLocalRef(env, exception); }\n} else "
            + text
            + " = "
            + text
            + " == "
            + value4
            + " && "
            + text2
            + " == -1 ? "
            + (enabled ? "0" : text)
            + " : "
            + text
            + (enabled ? " % " : " / ")
            + text2
            + ";\n";
      } else {
         return enabled ? text + " = fmod(" + text + ", " + text2 + ");\n" : text + " = " + text + " / " + text2 + ";\n";
      }
   }
}
