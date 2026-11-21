package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;

public class ConstantTransformer extends OpcodeTransformer {
   public ConstantTransformer() {
      super(false, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 1);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder source = new StringBuilder();
      int stackSizeBefore = stack.sizeBefore();
      source.append("stack");
      source.append(stackSizeBefore);
      String memberSeparator = ".";
      source.append(memberSeparator);
      int opcode = instruction.getOpcode();
      if (Integer.compareUnsigned(opcode - 1, 16) <= 0) {
         IntInsnNode integerInstruction;
         StringBuilder builder;
         switch (opcode - 1) {
            case 0:
               String referenceSlot = "l";
               source.append(referenceSlot);
               source.append(" = NULL;\n");
               return source.toString();
            case 1:
               String integerSlot = "i";
               source.append(integerSlot);
               source.append(" = -1;\n");
               return source.toString();
            case 2:
               String integerSlot2 = "i";
               source.append(integerSlot2);
               source.append(" = ");
               String zeroLiteral = "0";
               source.append(zeroLiteral);
               source.append(";\n");
               return source.toString();
            case 3:
               String integerSlot3 = "i";
               source.append(integerSlot3);
               source.append(" = ");
               String oneLiteral = "1";
               source.append(oneLiteral);
               source.append(";\n");
               return source.toString();
            case 4:
               String integerSlot4 = "i";
               source.append(integerSlot4);
               source.append(" = ");
               String literalText = "2";
               source.append(literalText);
               source.append(";\n");
               return source.toString();
            case 5:
               String integerSlot5 = "i";
               source.append(integerSlot5);
               source.append(" = ");
               String literalText2 = "3";
               source.append(literalText2);
               source.append(";\n");
               return source.toString();
            case 6:
               String integerSlot6 = "i";
               source.append(integerSlot6);
               source.append(" = ");
               String literalText3 = "4";
               source.append(literalText3);
               source.append(";\n");
               return source.toString();
            case 7:
               String integerSlot7 = "i";
               source.append(integerSlot7);
               source.append(" = ");
               String literalText4 = "5";
               source.append(literalText4);
               source.append(";\n");
               return source.toString();
            case 8:
               String longSlot = "j";
               source.append(longSlot);
               source.append(" = 0L;\n");
               return source.toString();
            case 9:
               String longSlot2 = "j";
               source.append(longSlot2);
               source.append(" = 1L;\n");
               return source.toString();
            case 10:
               String floatSlot = "f";
               source.append(floatSlot);
               source.append(" = 0.0f;\n");
               return source.toString();
            case 11:
               String floatSlot2 = "f";
               source.append(floatSlot2);
               source.append(" = 1.0f;\n");
               return source.toString();
            case 12:
               String floatSlot3 = "f";
               source.append(floatSlot3);
               source.append(" = 2.0f;\n");
               return source.toString();
            case 13:
               String doubleSlot = "d";
               source.append(doubleSlot);
               source.append(" = 0.0;\n");
               return source.toString();
            case 14:
               String doubleSlot2 = "d";
               source.append(doubleSlot2);
               source.append(" = 1.0;\n");
               return source.toString();
            case 15:
               String integerSlot8 = "i";
               source.append(integerSlot8);
               builder = source.append(" = ");
               integerInstruction = (IntInsnNode)instruction;
               break;
            case 16:
               String integerSlot9 = "i";
               source.append(integerSlot9);
               builder = source.append(" = ");
               integerInstruction = (IntInsnNode)instruction;
               break;
            default:
               throw new AssertionError();
         }

         int constantValue = integerInstruction.operand;
         builder.append(constantValue);
         source.append(";\n");
         return source.toString();
      } else {
         throw new AssertionError();
      }
   }
}
