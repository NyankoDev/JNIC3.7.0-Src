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
      for (int var4 = aliasSet.nextSetBit(0); var4 >= 0; var4 = aliasSet.nextSetBit(var4 + 1)) {
         this.setSlot(var4, nullnessValue);
      }
   }

   @Override
   public void initJumpTarget(int index, LabelNode labelNode) {
      if (labelNode == null) {
         if (index != 198) {
            if (index == 199) {
               AliasSet var4 = this.branchAliases;
               NullnessValue var5 = NullnessValue.NULL;
               this.refineAliases(var4, var5);
            }
         } else {
            AliasSet var6 = this.branchAliases;
            NullnessValue var9 = NullnessValue.NOT_NULL;
            this.refineAliases(var6, var9);
         }
      } else if (index != 198) {
         if (index == 199) {
            AliasSet var7 = this.branchAliases;
            NullnessValue var10 = NullnessValue.NOT_NULL;
            this.refineAliases(var7, var10);
         }
      } else {
         AliasSet var8 = this.branchAliases;
         NullnessValue var11 = NullnessValue.NULL;
         this.refineAliases(var8, var11);
      }
   }

   @Override
   public void execute(AbstractInsnNode instruction, Interpreter interpreter) throws AnalyzerException {
      label146: {
         AliasSet var12;
         label116: {
            int var3;
            label115: {
               label114: {
                  label121: {
                     var3 = this.topSlot();
                     int var7;
                     if (instruction.getOpcode() != 198) {
                        if (instruction.getOpcode() == 199) {
                           AliasSet var5;
                           label86: {
                              AliasSet var13 = this.aliasSet(var3);
                              this.branchAliases = var13;
                              int var9 = instruction.getOpcode();
                              if (Integer.compareUnsigned(var9 - 180, 15) <= 0) {
                                 switch (var9 - 180) {
                                    case 0:
                                       var5 = this.aliasSet(var3);
                                       break label86;
                                    case 1:
                                       break;
                                    case 2:
                                    case 3:
                                    case 5:
                                       instruction = (MethodInsnNode)instruction;
                                       int var4 = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                       var5 = this.aliasSet(var3 - var4);
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
                                       var5 = this.aliasSet(var3);
                                       break label86;
                                    default:
                                       throw new AssertionError();
                                 }
                              } else {
                                 if (Integer.compareUnsigned(var9 + -46, 7) > 0) {
                                    if (Integer.compareUnsigned(var9 + -79, 7) > 0) {
                                       break label146;
                                    }

                                    switch (var9 + -79) {
                                       case 0:
                                       case 1:
                                       case 2:
                                       case 3:
                                       case 4:
                                       case 5:
                                       case 6:
                                       case 7:
                                          var5 = this.aliasSet(var3 - 2);
                                          break label86;
                                       default:
                                          throw new AssertionError();
                                    }
                                 }

                                 switch (var9 + -46) {
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

                              var5 = this.aliasSet(var3 - 1);
                           }

                           super.execute(instruction, interpreter);
                           if (var5 != null) {
                              NullnessValue var6 = NullnessValue.NOT_NULL;
                              this.refineAliases(var5, var6);
                           }

                           return;
                        }

                        this.branchAliases = null;
                        var7 = instruction.getOpcode();
                        if (Integer.compareUnsigned(var7 - 180, 15) <= 0) {
                           switch (var7 - 180) {
                              case 0:
                                 break label115;
                              case 1:
                                 break label114;
                              case 2:
                              case 3:
                              case 5:
                                 instruction = (MethodInsnNode)instruction;
                                 int var8 = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                 var12 = this.aliasSet(var3 - var8);
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

                        if (Integer.compareUnsigned(var7 + -46, 7) <= 0) {
                           switch (var7 + -46) {
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
                        AliasSet var14 = this.aliasSet(var3);
                        this.branchAliases = var14;
                        var7 = instruction.getOpcode();
                        if (Integer.compareUnsigned(var7 - 180, 15) <= 0) {
                           switch (var7 - 180) {
                              case 0:
                                 break label115;
                              case 1:
                                 break label114;
                              case 2:
                              case 3:
                              case 5:
                                 instruction = (MethodInsnNode)instruction;
                                 int var10 = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
                                 var12 = this.aliasSet(var3 - var10);
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

                        if (Integer.compareUnsigned(var7 + -46, 7) <= 0) {
                           switch (var7 + -46) {
                              case 0:
                              case 1:
                              case 2:
                              case 3:
                              case 4:
                              case 5:
                              case 6:
                              case 7:
                                 var12 = this.aliasSet(var3 - 1);
                                 break label116;
                              default:
                                 throw new AssertionError();
                           }
                        }
                     }

                     if (Integer.compareUnsigned(var7 + -79, 7) > 0) {
                        break label146;
                     }

                     switch (var7 + -79) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                           var12 = this.aliasSet(var3 - 2);
                           break label116;
                        default:
                           throw new AssertionError();
                     }
                  }

                  var12 = this.aliasSet(var3);
                  break label116;
               }

               var12 = this.aliasSet(var3 - 1);
               break label116;
            }

            var12 = this.aliasSet(var3);
         }

         super.execute(instruction, interpreter);
         if (var12 != null) {
            NullnessValue var11 = NullnessValue.NOT_NULL;
            this.refineAliases(var12, var11);
         }

         return;
      }

      super.execute(instruction, interpreter);
   }
}
