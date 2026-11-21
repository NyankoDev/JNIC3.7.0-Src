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
      int secondStackIndex = stack.sizeBefore() - 2;
      int topStackIndex = stack.sizeBefore() - 1;
      int opcode = instruction.getOpcode();
      if (opcode >= 96 && opcode <= 115) {
         char character = "ijfd".charAt(opcode & 3);
         String arithmeticOperand = arithmeticVariable("stack", secondStackIndex, character);
         String arithmeticOperand2 = arithmeticVariable("stack", topStackIndex, character);
         String literalText = character == 'i' ? "(unsigned int) " : (character == 'j' ? "(unsigned long long) " : "");
         String expression;
         switch ((opcode - 96) / 4) {
            case 0:
               expression = arithmeticOperand + " = " + literalText + arithmeticOperand + " + " + arithmeticOperand2 + ";\n";
               break;
            case 1:
               expression = arithmeticOperand + " = " + literalText + arithmeticOperand + " - " + arithmeticOperand2 + ";\n";
               break;
            case 2:
               expression = arithmeticOperand + " = " + literalText + arithmeticOperand + " * " + arithmeticOperand2 + ";\n";
               break;
            case 3:
               expression = arithmeticDivision(arithmeticOperand, arithmeticOperand2, character, false);
               break;
            case 4:
               expression = arithmeticDivision(arithmeticOperand, arithmeticOperand2, character, true);
               break;
            default:
               throw new AssertionError();
         }

         return expression;
      } else if (opcode >= 116 && opcode <= 119) {
         char character2 = "ijfd".charAt(opcode & 3);
         String arithmeticOperand3 = arithmeticVariable("stack", topStackIndex, character2);
         return arithmeticOperand3 + " = -" + (character2 == 'i' ? "(unsigned int) " : (character2 == 'j' ? "(unsigned long long) " : "")) + arithmeticOperand3 + ";\n";
      } else if (opcode >= 120 && opcode <= 125) {
         boolean matches = (opcode & 1) != 0;
         String arithmeticOperand4 = arithmeticVariable("stack", secondStackIndex, (char)(matches ? 'j' : 'i'));
         String expression2 = "(" + arithmeticVariable("stack", topStackIndex, 'i') + " & " + (matches ? "0x3f" : "0x1f") + ")";
         return opcode >= 124
            ? arithmeticOperand4 + " = " + (matches ? "(jlong) ((unsigned long long)" : "(jint) ((unsigned int)") + arithmeticOperand4 + " >> " + expression2 + ");\n"
            : arithmeticOperand4
               + " = "
               + (opcode < 122 ? (matches ? "(unsigned long long) " : "(unsigned int) ") : "")
               + arithmeticOperand4
               + (opcode < 122 ? " << " : " >> ")
               + expression2
               + ";\n";
      } else if (opcode >= 126 && opcode <= 131) {
         int shiftSlot = (opcode & 1) == 0 ? 105 : 106;
         String arithmeticOperand5 = arithmeticVariable("stack", secondStackIndex, (char)shiftSlot);
         String arithmeticOperand6 = arithmeticVariable("stack", topStackIndex, (char)shiftSlot);
         String literalText2 = opcode < 128 ? " & " : (opcode < 130 ? " | " : " ^ ");
         return arithmeticOperand5 + " = " + arithmeticOperand5 + literalText2 + arithmeticOperand6 + ";\n";
      } else if (opcode == 132) {
         IincInsnNode increment = (IincInsnNode)instruction;
         String arithmeticOperand7 = arithmeticVariable("local", increment.var, 'i');
         return arithmeticOperand7 + " = (unsigned int) " + arithmeticOperand7 + " + " + increment.incr + ";\n";
      } else if (opcode >= 148 && opcode <= 152) {
         int comparisonSlot = opcode == 148 ? 106 : (opcode <= 150 ? 102 : 100);
         String arithmeticOperand8 = arithmeticVariable("stack", secondStackIndex, (char)comparisonSlot);
         String arithmeticOperand9 = arithmeticVariable("stack", topStackIndex, (char)comparisonSlot);
         String expression3 = "stack" + secondStackIndex + ".i = " + arithmeticOperand8 + " < " + arithmeticOperand9 + " ? -1 : " + arithmeticOperand8 + " > " + arithmeticOperand9 + " ? 1 : ";
         return opcode == 148 ? expression3 + "0;\n" : expression3 + arithmeticOperand8 + " == " + arithmeticOperand9 + " ? 0 : " + ((opcode & 1) == 0 ? "1" : "-1") + ";\n";
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
         String literalText = value == 'i' ? "(jint) 0x80000000" : "(jlong) 0x8000000000000000LL";
         return "if ("
            + text2
            + " == 0) {\n    jclass exception = (*env)->FindClass(env, \"java/lang/ArithmeticException\");\n    if (exception != NULL) { (*env)->ThrowNew(env, exception, \"/ by zero\"); (*env)->DeleteLocalRef(env, exception); }\n} else "
            + text
            + " = "
            + text
            + " == "
            + literalText
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
