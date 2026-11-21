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
      StringBuilder var6 = new StringBuilder();
      int var7 = stack.sizeBefore();
      var6.append("stack");
      var6.append(var7);
      String value9 = ".";
      var6.append(value9);
      int var12 = instruction.getOpcode();
      if (Integer.compareUnsigned(var12 - 1, 16) <= 0) {
         IntInsnNode var11;
         StringBuilder var34;
         switch (var12 - 1) {
            case 0:
               String value18 = "l";
               var6.append(value18);
               var6.append(" = NULL;\n");
               return var6.toString();
            case 1:
               String value19 = "i";
               var6.append(value19);
               var6.append(" = -1;\n");
               return var6.toString();
            case 2:
               String value20 = "i";
               var6.append(value20);
               var6.append(" = ");
               String value8 = "0";
               var6.append(value8);
               var6.append(";\n");
               return var6.toString();
            case 3:
               String value21 = "i";
               var6.append(value21);
               var6.append(" = ");
               String value13 = "1";
               var6.append(value13);
               var6.append(";\n");
               return var6.toString();
            case 4:
               String value22 = "i";
               var6.append(value22);
               var6.append(" = ");
               String value14 = "2";
               var6.append(value14);
               var6.append(";\n");
               return var6.toString();
            case 5:
               String value23 = "i";
               var6.append(value23);
               var6.append(" = ");
               String value15 = "3";
               var6.append(value15);
               var6.append(";\n");
               return var6.toString();
            case 6:
               String value24 = "i";
               var6.append(value24);
               var6.append(" = ");
               String value16 = "4";
               var6.append(value16);
               var6.append(";\n");
               return var6.toString();
            case 7:
               String value25 = "i";
               var6.append(value25);
               var6.append(" = ");
               String value17 = "5";
               var6.append(value17);
               var6.append(";\n");
               return var6.toString();
            case 8:
               String value26 = "j";
               var6.append(value26);
               var6.append(" = 0L;\n");
               return var6.toString();
            case 9:
               String value27 = "j";
               var6.append(value27);
               var6.append(" = 1L;\n");
               return var6.toString();
            case 10:
               String value28 = "f";
               var6.append(value28);
               var6.append(" = 0.0f;\n");
               return var6.toString();
            case 11:
               String value29 = "f";
               var6.append(value29);
               var6.append(" = 1.0f;\n");
               return var6.toString();
            case 12:
               String value30 = "f";
               var6.append(value30);
               var6.append(" = 2.0f;\n");
               return var6.toString();
            case 13:
               String value31 = "d";
               var6.append(value31);
               var6.append(" = 0.0;\n");
               return var6.toString();
            case 14:
               String value32 = "d";
               var6.append(value32);
               var6.append(" = 1.0;\n");
               return var6.toString();
            case 15:
               String value33 = "i";
               var6.append(value33);
               var34 = var6.append(" = ");
               var11 = (IntInsnNode)instruction;
               break;
            case 16:
               String value35 = "i";
               var6.append(value35);
               var34 = var6.append(" = ");
               var11 = (IntInsnNode)instruction;
               break;
            default:
               throw new AssertionError();
         }

         int var10 = var11.operand;
         var34.append(var10);
         var6.append(";\n");
         return var6.toString();
      } else {
         throw new AssertionError();
      }
   }
}
