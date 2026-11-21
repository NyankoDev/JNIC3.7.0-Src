package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationException;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StackTransformer extends OpcodeTransformer {
   public StackTransformer() {
      super(false, 87, 88, 89, 90, 91, 92, 93, 94, 95);
   }

   private int saveStackValue(StringBuilder stringBuilder, int index, int index2, Type type) {
      String value12 = JniTypes.stackVariable("temp", index2, type);
      stringBuilder.append(value12);
      stringBuilder.append(" = ");
      String value29 = JniTypes.stackVariable("stack", index, type);
      stringBuilder.append(value29);
      stringBuilder.append(";\n    ");
      return type.getSize();
   }

   private void restoreStackValue(StringBuilder stringBuilder, int index, int index2, Type type) {
      String value12 = JniTypes.stackVariable("stack", index, type);
      stringBuilder.append(value12);
      stringBuilder.append(" = ");
      String value29 = JniTypes.stackVariable("temp", index2, type);
      stringBuilder.append(value29);
      stringBuilder.append(";\n    ");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var9 = instruction.getOpcode();
      if (var9 == 87 || var9 == 88) {
         return "\n";
      }

      if (var9 >= 89 && var9 <= 95) {
         int var8 = stack.sizeBefore();
         StringBuilder var6 = new StringBuilder();
         if (var9 == 95) {
            if (this.saveStackValue(var6, var8 - 1, 0, stack.typeBefore(var8 - 1)) == 1
               && this.saveStackValue(var6, var8 - 2, 1, stack.typeBefore(var8 - 2)) == 1) {
               this.restoreStackValue(var6, var8 - 2, 0, stack.typeAfter(var8 - 2));
               this.restoreStackValue(var6, var8 - 1, 1, stack.typeAfter(var8 - 1));
               return var6.toString();
            } else {
               throw new CompilationException("Illegal use of SWAP");
            }
         } else {
            int var7 = var9 < 92 ? 1 : 2;
            int var10 = (var9 - 89) % 3;
            int var11 = this.saveWords(var6, stack, var8, 0, var7, var9);
            int var13 = this.saveWords(var6, stack, var8, var11, var10, var9);
            int var12 = var8 - var11 - var13;

            for (int var14 = var11 - 1; var14 >= 0; var14--) {
               this.restoreStackValue(var6, var12, var14, stack.typeAfter(var12));
               var12++;
            }

            for (int var15 = var11 + var13 - 1; var15 >= var11; var15--) {
               this.restoreStackValue(var6, var12, var15, stack.typeAfter(var12));
               var12++;
            }

            for (int var16 = var11 - 1; var16 >= 0; var16--) {
               this.restoreStackValue(var6, var12, var16, stack.typeAfter(var12));
               var12++;
            }

            return var6.toString();
         }
      } else {
         throw new AssertionError();
      }
   }

   private int saveWords(StringBuilder stringBuilder, OperandStack stack, int index, int index2, int index3, int index4) {
      int var7 = 0;

      int var8;
      for (var8 = 0; var8 < index3; var7++) {
         int var9 = index2 + var7;
         int var10 = index - 1 - var9;
         var8 += this.saveStackValue(stringBuilder, var10, var9, stack.typeBefore(var10));
      }

      if (var8 != index3) {
         throw new CompilationException("Illegal use of " + new String[]{"DUP", "DUP_X1", "DUP_X2", "DUP2", "DUP2_X1", "DUP2_X2"}[index4 - 89]);
      } else {
         return var7;
      }
   }
}
