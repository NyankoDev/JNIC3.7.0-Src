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
      int var7 = stack.sizeBefore();
      int var8 = stack.sizeBefore();
      StringBuilder var6 = new StringBuilder();
      int var9 = instruction.getOpcode();
      if (Integer.compareUnsigned(var9 + -153, 46) <= 0) {
         JumpInsnNode var14;
         String value17;
         label50: {
            JumpInsnNode var15;
            String value80;
            label49: {
               JumpInsnNode var16;
               String value78;
               switch (var9 + -153) {
                  case 0:
                     Type var11 = Type.INT_TYPE;
                     String value12 = JniTypes.stackVariable("stack", var7 - 1, var11);
                     var6.append(value12);
                     var6.append(" == ");
                     String value38 = "0";
                     var6.append(value38);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 1:
                     Type var21 = Type.INT_TYPE;
                     String value39 = JniTypes.stackVariable("stack", var7 - 1, var21);
                     var6.append(value39);
                     var6.append(" != ");
                     String value40 = "0";
                     var6.append(value40);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 2:
                     Type var22 = Type.INT_TYPE;
                     String value41 = JniTypes.stackVariable("stack", var7 - 1, var22);
                     var6.append(value41);
                     var6.append(" < ");
                     String value42 = "0";
                     var6.append(value42);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 3:
                     Type var23 = Type.INT_TYPE;
                     String value43 = JniTypes.stackVariable("stack", var7 - 1, var23);
                     var6.append(value43);
                     var6.append(" >= ");
                     String value44 = "0";
                     var6.append(value44);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 4:
                     Type var24 = Type.INT_TYPE;
                     String value45 = JniTypes.stackVariable("stack", var7 - 1, var24);
                     var6.append(value45);
                     var6.append(" > ");
                     String value46 = "0";
                     var6.append(value46);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 5:
                     Type var25 = Type.INT_TYPE;
                     String value47 = JniTypes.stackVariable("stack", var7 - 1, var25);
                     var6.append(value47);
                     var6.append(" <= ");
                     String value48 = "0";
                     var6.append(value48);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 6:
                     Type var26 = Type.INT_TYPE;
                     String value49 = JniTypes.stackVariable("stack", var8 + -2, var26);
                     var6.append(value49);
                     var6.append(" == ");
                     Type var50 = Type.INT_TYPE;
                     String value51 = JniTypes.stackVariable("stack", var7 - 1, var50);
                     var6.append(value51);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 7:
                     Type var27 = Type.INT_TYPE;
                     String value52 = JniTypes.stackVariable("stack", var8 + -2, var27);
                     var6.append(value52);
                     var6.append(" != ");
                     Type var53 = Type.INT_TYPE;
                     String value54 = JniTypes.stackVariable("stack", var7 - 1, var53);
                     var6.append(value54);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 8:
                     Type var28 = Type.INT_TYPE;
                     String value55 = JniTypes.stackVariable("stack", var8 + -2, var28);
                     var6.append(value55);
                     var6.append(" < ");
                     Type var56 = Type.INT_TYPE;
                     String value57 = JniTypes.stackVariable("stack", var7 - 1, var56);
                     var6.append(value57);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 9:
                     Type var29 = Type.INT_TYPE;
                     String value58 = JniTypes.stackVariable("stack", var8 + -2, var29);
                     var6.append(value58);
                     var6.append(" >= ");
                     Type var59 = Type.INT_TYPE;
                     String value60 = JniTypes.stackVariable("stack", var7 - 1, var59);
                     var6.append(value60);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 10:
                     Type var30 = Type.INT_TYPE;
                     String value61 = JniTypes.stackVariable("stack", var8 + -2, var30);
                     var6.append(value61);
                     var6.append(" > ");
                     Type var62 = Type.INT_TYPE;
                     String value63 = JniTypes.stackVariable("stack", var7 - 1, var62);
                     var6.append(value63);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 11:
                     Type var31 = Type.INT_TYPE;
                     String value64 = JniTypes.stackVariable("stack", var8 + -2, var31);
                     var6.append(value64);
                     var6.append(" <= ");
                     Type var65 = Type.INT_TYPE;
                     String value66 = JniTypes.stackVariable("stack", var7 - 1, var65);
                     var6.append(value66);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 12:
                     String[] var32 = new String[2];
                     Type var10 = JniTypes.OBJECT_TYPE;
                     String value67 = JniTypes.stackVariable("stack", var8 + -2, var10);
                     var32[0] = value67;
                     Type var68 = JniTypes.OBJECT_TYPE;
                     String value69 = JniTypes.stackVariable("stack", var7 - 1, var68);
                     var32[1] = value69;
                     String value70 = context.jniCall("IsSameObject", var32);
                     var6.append(value70);
                     value80 = var6.toString();
                     var15 = (JumpInsnNode)instruction;
                     break label49;
                  case 13:
                     String value33 = "!";
                     var6.append(value33);
                     String[] var34 = new String[2];
                     Type var20 = JniTypes.OBJECT_TYPE;
                     String value71 = JniTypes.stackVariable("stack", var8 + -2, var20);
                     var34[0] = value71;
                     Type var72 = JniTypes.OBJECT_TYPE;
                     String value81 = JniTypes.stackVariable("stack", var7 - 1, var72);
                     var34[1] = value81;
                     String value82 = context.jniCall("IsSameObject", var34);
                     var6.append(value82);
                     value17 = var6.toString();
                     var14 = (JumpInsnNode)instruction;
                     break label50;
                  case 14:
                     if (!context.getOptions().isFlowObfuscationEnabled()) {
                        String value73 = "1";
                        var6.append(value73);
                        String value83 = var6.toString();
                        LabelNode var74 = ((JumpInsnNode)instruction).label;
                        return jumps.jumpIf(value83, var74) + "\n";
                     }

                     String value75 = context.getLiterals().encryptedInteger(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
                     var6.append(value75);
                     String value35 = var6.toString();
                     LabelNode var76 = ((JumpInsnNode)instruction).label;
                     return jumps.jumpIf(value35, var76) + "\n";
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
                     Type var36 = JniTypes.OBJECT_TYPE;
                     String value77 = JniTypes.stackVariable("stack", var7 - 1, var36);
                     var6.append(value77);
                     var6.append(" == NULL");
                     value78 = var6.toString();
                     var16 = (JumpInsnNode)instruction;
                     break;
                  case 46:
                     Type var37 = JniTypes.OBJECT_TYPE;
                     String value79 = JniTypes.stackVariable("stack", var7 - 1, var37);
                     var6.append(value79);
                     var6.append(" != NULL");
                     value78 = var6.toString();
                     var16 = (JumpInsnNode)instruction;
               }

               LabelNode var19 = var16.label;
               return jumps.jumpIf(value78, var19) + "\n";
            }

            LabelNode var18 = var15.label;
            return jumps.jumpIf(value80, var18) + "\n";
         }

         LabelNode var13 = var14.label;
         return jumps.jumpIf(value17, var13) + "\n";
      } else {
         throw new AssertionError();
      }
   }
}
