package dev.jnic.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class NullnessFrame extends AliasFrame<NullnessValue> {
   private AliasSet branchAliases;

   public NullnessFrame(int index, int index2) {
      super(index, index2);
   }

   public NullnessFrame(Frame<? extends NullnessValue> frame) {
      super(frame);
      this.init(frame);
   }

   private void refineAliases(AliasSet aliasSet, NullnessValue nullnessValue) {
      for (int slotIndex = aliasSet.nextSetBit(0); slotIndex >= 0; slotIndex = aliasSet.nextSetBit(slotIndex + 1)) {
         this.setSlot(slotIndex, nullnessValue);
      }
   }

   @Override
   public void initJumpTarget(int index, LabelNode labelNode) {
      if (labelNode == null) {
         if (index != 198) {
            if (index == 199) {
               AliasSet branchAliases2 = this.branchAliases;
               NullnessValue nullValue = NullnessValue.NULL;
               this.refineAliases(branchAliases2, nullValue);
            }
         } else {
            AliasSet branchAliases3 = this.branchAliases;
            NullnessValue nonNullValue = NullnessValue.NOT_NULL;
            this.refineAliases(branchAliases3, nonNullValue);
         }
      } else if (index != 198) {
         if (index == 199) {
            AliasSet branchAliases4 = this.branchAliases;
            NullnessValue nonNullValue2 = NullnessValue.NOT_NULL;
            this.refineAliases(branchAliases4, nonNullValue2);
         }
      } else {
         AliasSet branchAliases5 = this.branchAliases;
         NullnessValue nullValue2 = NullnessValue.NULL;
         this.refineAliases(branchAliases5, nullValue2);
      }
   }

   @Override
   public void execute(AbstractInsnNode instruction, Interpreter interpreter) throws AnalyzerException {
      label146: {
         AliasSet aliasGroup;
         label116: {
            int topSlot;
            label115: {
               label114: {
                  label121: {
                     topSlot = this.topSlot();
                     int opcode;
                     if (instruction.getOpcode() != 198) {
                        if (instruction.getOpcode() == 199) {
                           AliasSet aliasGroup2;
                           label86: {
                              AliasSet aliasGroup3 = this.aliasSet(topSlot);
                              this.branchAliases = aliasGroup3;
                              int opcode2 = instruction.getOpcode();
                              if (Integer.compareUnsigned(opcode2 - 180, 15) <= 0) {
                                 switch (opcode2 - 180) {
                                    case 0:
                                       aliasGroup2 = this.aliasSet(topSlot);
                                       break label86;
                                    case 1:
                                       break;
                                    case 2:
                                    case 3:
                                    case 5:
                                       instruction = (MethodInsnNode)instruction;
                                       int argumentTypeCount = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                       aliasGroup2 = this.aliasSet(topSlot - argumentTypeCount);
                                       break label86;
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    case 11:
                                    case 12:
                                    case 13:
                                       break label146;
                                    case 10:
                                    case 14:
                                    case 15:
                                       aliasGroup2 = this.aliasSet(topSlot);
                                       break label86;
                                    default:
                                       throw new AssertionError();
                                 }
                              } else {
                                 if (Integer.compareUnsigned(opcode2 + -46, 7) > 0) {
                                    if (Integer.compareUnsigned(opcode2 + -79, 7) > 0) {
                                       break label146;
                                    }

                                    switch (opcode2 + -79) {
                                       case 0:
                                       case 1:
                                       case 2:
                                       case 3:
                                       case 4:
                                       case 5:
                                       case 6:
                                       case 7:
                                          aliasGroup2 = this.aliasSet(topSlot - 2);
                                          break label86;
                                       default:
                                          throw new AssertionError();
                                    }
                                 }

                                 switch (opcode2 + -46) {
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                       break;
                                    default:
                                       throw new AssertionError();
                                 }
                              }

                              aliasGroup2 = this.aliasSet(topSlot - 1);
                           }

                           super.execute(instruction, interpreter);
                           if (aliasGroup2 != null) {
                              NullnessValue nonNullValue = NullnessValue.NOT_NULL;
                              this.refineAliases(aliasGroup2, nonNullValue);
                           }

                           return;
                        }

                        this.branchAliases = null;
                        opcode = instruction.getOpcode();
                        if (Integer.compareUnsigned(opcode - 180, 15) <= 0) {
                           switch (opcode - 180) {
                              case 0:
                                 break label115;
                              case 1:
                                 break label114;
                              case 2:
                              case 3:
                              case 5:
                                 instruction = (MethodInsnNode)instruction;
                                 int argumentTypeCount2 = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                 aliasGroup = this.aliasSet(topSlot - argumentTypeCount2);
                                 break label116;
                              case 4:
                              case 6:
                              case 7:
                              case 8:
                              case 9:
                              case 11:
                              case 12:
                              case 13:
                                 break label146;
                              case 10:
                              case 14:
                              case 15:
                                 break label121;
                              default:
                                 throw new AssertionError();
                           }
                        }

                        if (Integer.compareUnsigned(opcode + -46, 7) <= 0) {
                           switch (opcode + -46) {
                              case 0:
                              case 1:
                              case 2:
                              case 3:
                              case 4:
                              case 5:
                              case 6:
                              case 7:
                                 break label114;
                              default:
                                 throw new AssertionError();
                           }
                        }
                     } else {
                        AliasSet aliasGroup4 = this.aliasSet(topSlot);
                        this.branchAliases = aliasGroup4;
                        opcode = instruction.getOpcode();
                        if (Integer.compareUnsigned(opcode - 180, 15) <= 0) {
                           switch (opcode - 180) {
                              case 0:
                                 break label115;
                              case 1:
                                 break label114;
                              case 2:
                              case 3:
                              case 5:
                                 instruction = (MethodInsnNode)instruction;
                                 int argumentTypeCount3 = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                 aliasGroup = this.aliasSet(topSlot - argumentTypeCount3);
                                 break label116;
                              case 4:
                              case 6:
                              case 7:
                              case 8:
                              case 9:
                              case 11:
                              case 12:
                              case 13:
                                 break label146;
                              case 10:
                              case 14:
                              case 15:
                                 break label121;
                              default:
                                 throw new AssertionError();
                           }
                        }

                        if (Integer.compareUnsigned(opcode + -46, 7) <= 0) {
                           switch (opcode + -46) {
                              case 0:
                              case 1:
                              case 2:
                              case 3:
                              case 4:
                              case 5:
                              case 6:
                              case 7:
                                 aliasGroup = this.aliasSet(topSlot - 1);
                                 break label116;
                              default:
                                 throw new AssertionError();
                           }
                        }
                     }

                     if (Integer.compareUnsigned(opcode + -79, 7) > 0) {
                        break label146;
                     }

                     switch (opcode + -79) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                           aliasGroup = this.aliasSet(topSlot - 2);
                           break label116;
                        default:
                           throw new AssertionError();
                     }
                  }

                  aliasGroup = this.aliasSet(topSlot);
                  break label116;
               }

               aliasGroup = this.aliasSet(topSlot - 1);
               break label116;
            }

            aliasGroup = this.aliasSet(topSlot);
         }

         super.execute(instruction, interpreter);
         if (aliasGroup != null) {
            NullnessValue nonNullValue2 = NullnessValue.NOT_NULL;
            this.refineAliases(aliasGroup, nonNullValue2);
         }

         return;
      }

      super.execute(instruction, interpreter);
   }
}
