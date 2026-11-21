package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.concurrent.ThreadLocalRandom;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

public class JumpTransformer extends OpcodeTransformer {
   public JumpTransformer() {
      super(false, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 198, 199, 167);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int stackSize = stack.sizeBefore();
      int operandCount = stack.sizeBefore();
      StringBuilder source = new StringBuilder();
      int opcode = instruction.getOpcode();
      if (Integer.compareUnsigned(opcode + -153, 46) <= 0) {
         JumpInsnNode jumpInstruction;
         String generatedSource;
         label50: {
            JumpInsnNode jumpInstruction2;
            String generatedSource2;
            label49: {
               JumpInsnNode jumpInstruction3;
               String generatedSource3;
               switch (opcode + -153) {
                  case 0:
                     Type intType = Type.INT_TYPE;
                     String stackValue = JniTypes.stackVariable("stack", stackSize - 1, intType);
                     source.append(stackValue);
                     source.append(" == ");
                     String zeroLiteral = "0";
                     source.append(zeroLiteral);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 1:
                     Type intType2 = Type.INT_TYPE;
                     String stackValue2 = JniTypes.stackVariable("stack", stackSize - 1, intType2);
                     source.append(stackValue2);
                     source.append(" != ");
                     String zeroLiteral2 = "0";
                     source.append(zeroLiteral2);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 2:
                     Type intType3 = Type.INT_TYPE;
                     String stackValue3 = JniTypes.stackVariable("stack", stackSize - 1, intType3);
                     source.append(stackValue3);
                     source.append(" < ");
                     String zeroLiteral3 = "0";
                     source.append(zeroLiteral3);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 3:
                     Type intType4 = Type.INT_TYPE;
                     String stackValue4 = JniTypes.stackVariable("stack", stackSize - 1, intType4);
                     source.append(stackValue4);
                     source.append(" >= ");
                     String zeroLiteral4 = "0";
                     source.append(zeroLiteral4);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 4:
                     Type intType5 = Type.INT_TYPE;
                     String stackValue5 = JniTypes.stackVariable("stack", stackSize - 1, intType5);
                     source.append(stackValue5);
                     source.append(" > ");
                     String zeroLiteral5 = "0";
                     source.append(zeroLiteral5);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 5:
                     Type intType6 = Type.INT_TYPE;
                     String stackValue6 = JniTypes.stackVariable("stack", stackSize - 1, intType6);
                     source.append(stackValue6);
                     source.append(" <= ");
                     String zeroLiteral6 = "0";
                     source.append(zeroLiteral6);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 6:
                     Type intType7 = Type.INT_TYPE;
                     String stackValue7 = JniTypes.stackVariable("stack", operandCount + -2, intType7);
                     source.append(stackValue7);
                     source.append(" == ");
                     Type intType8 = Type.INT_TYPE;
                     String stackValue8 = JniTypes.stackVariable("stack", stackSize - 1, intType8);
                     source.append(stackValue8);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 7:
                     Type intType9 = Type.INT_TYPE;
                     String stackValue9 = JniTypes.stackVariable("stack", operandCount + -2, intType9);
                     source.append(stackValue9);
                     source.append(" != ");
                     Type intType10 = Type.INT_TYPE;
                     String stackValue10 = JniTypes.stackVariable("stack", stackSize - 1, intType10);
                     source.append(stackValue10);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 8:
                     Type intType11 = Type.INT_TYPE;
                     String stackValue11 = JniTypes.stackVariable("stack", operandCount + -2, intType11);
                     source.append(stackValue11);
                     source.append(" < ");
                     Type intType12 = Type.INT_TYPE;
                     String stackValue12 = JniTypes.stackVariable("stack", stackSize - 1, intType12);
                     source.append(stackValue12);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 9:
                     Type intType13 = Type.INT_TYPE;
                     String stackValue13 = JniTypes.stackVariable("stack", operandCount + -2, intType13);
                     source.append(stackValue13);
                     source.append(" >= ");
                     Type intType14 = Type.INT_TYPE;
                     String stackValue14 = JniTypes.stackVariable("stack", stackSize - 1, intType14);
                     source.append(stackValue14);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 10:
                     Type intType15 = Type.INT_TYPE;
                     String stackValue15 = JniTypes.stackVariable("stack", operandCount + -2, intType15);
                     source.append(stackValue15);
                     source.append(" > ");
                     Type intType16 = Type.INT_TYPE;
                     String stackValue16 = JniTypes.stackVariable("stack", stackSize - 1, intType16);
                     source.append(stackValue16);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 11:
                     Type intType17 = Type.INT_TYPE;
                     String stackValue17 = JniTypes.stackVariable("stack", operandCount + -2, intType17);
                     source.append(stackValue17);
                     source.append(" <= ");
                     Type intType18 = Type.INT_TYPE;
                     String stackValue18 = JniTypes.stackVariable("stack", stackSize - 1, intType18);
                     source.append(stackValue18);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 12:
                     String[] callArguments = new String[2];
                     Type objectType = JniTypes.OBJECT_TYPE;
                     String stackValue19 = JniTypes.stackVariable("stack", operandCount + -2, objectType);
                     callArguments[0] = stackValue19;
                     Type objectType2 = JniTypes.OBJECT_TYPE;
                     String stackValue20 = JniTypes.stackVariable("stack", stackSize - 1, objectType2);
                     callArguments[1] = stackValue20;
                     String isSameObjectCall = context.jniCall("IsSameObject", callArguments);
                     source.append(isSameObjectCall);
                     generatedSource2 = source.toString();
                     jumpInstruction2 = (JumpInsnNode)instruction;
                     break label49;
                  case 13:
                     String literalText = "!";
                     source.append(literalText);
                     String[] callArguments2 = new String[2];
                     Type objectType3 = JniTypes.OBJECT_TYPE;
                     String stackValue21 = JniTypes.stackVariable("stack", operandCount + -2, objectType3);
                     callArguments2[0] = stackValue21;
                     Type objectType4 = JniTypes.OBJECT_TYPE;
                     String stackValue22 = JniTypes.stackVariable("stack", stackSize - 1, objectType4);
                     callArguments2[1] = stackValue22;
                     String isSameObjectCall2 = context.jniCall("IsSameObject", callArguments2);
                     source.append(isSameObjectCall2);
                     generatedSource = source.toString();
                     jumpInstruction = (JumpInsnNode)instruction;
                     break label50;
                  case 14:
                     if (!context.getOptions().isFlowObfuscationEnabled()) {
                        String oneLiteral = "1";
                        source.append(oneLiteral);
                        String generatedSource4 = source.toString();
                        LabelNode targetLabel = ((JumpInsnNode)instruction).label;
                        return jumps.jumpIf(generatedSource4, targetLabel) + "\n";
                     }

                     String encryptedValue = context.getLiterals().encryptedInteger(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
                     source.append(encryptedValue);
                     String generatedSource5 = source.toString();
                     LabelNode targetLabel2 = ((JumpInsnNode)instruction).label;
                     return jumps.jumpIf(generatedSource5, targetLabel2) + "\n";
                  case 15:
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                  case 30:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 35:
                  case 36:
                  case 37:
                  case 38:
                  case 39:
                  case 40:
                  case 41:
                  case 42:
                  case 43:
                  case 44:
                  default:
                     throw new AssertionError();
                  case 45:
                     Type objectType5 = JniTypes.OBJECT_TYPE;
                     String stackValue23 = JniTypes.stackVariable("stack", stackSize - 1, objectType5);
                     source.append(stackValue23);
                     source.append(" == NULL");
                     generatedSource3 = source.toString();
                     jumpInstruction3 = (JumpInsnNode)instruction;
                     break;
                  case 46:
                     Type objectType6 = JniTypes.OBJECT_TYPE;
                     String stackValue24 = JniTypes.stackVariable("stack", stackSize - 1, objectType6);
                     source.append(stackValue24);
                     source.append(" != NULL");
                     generatedSource3 = source.toString();
                     jumpInstruction3 = (JumpInsnNode)instruction;
               }

               LabelNode targetLabel3 = jumpInstruction3.label;
               return jumps.jumpIf(generatedSource3, targetLabel3) + "\n";
            }

            LabelNode targetLabel4 = jumpInstruction2.label;
            return jumps.jumpIf(generatedSource2, targetLabel4) + "\n";
         }

         LabelNode targetLabel5 = jumpInstruction.label;
         return jumps.jumpIf(generatedSource, targetLabel5) + "\n";
      } else {
         throw new AssertionError();
      }
   }
}
