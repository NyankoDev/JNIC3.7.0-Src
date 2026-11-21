package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LocalVariableTransformer extends OpcodeTransformer {
   public LocalVariableTransformer() {
      super(false, 21, 22, 23, 24, 25, 54, 55, 56, 57, 58);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      VarInsnNode var6 = (VarInsnNode)instruction;
      int var7 = instruction.getOpcode();
      boolean var8 = var7 >= 21 && var7 <= 25;
      char var9 = localSlot(var7);
      String var10 = "local" + var6.var + "." + var9;
      String var11 = "stack" + (stack.sizeBefore() - Boolean.compare(!var8, false)) + "." + var9;
      return var8 ? var11 + " = " + var10 + ";\n" : var10 + " = " + var11 + ";\n";
   }

   private static char localSlot(int index) {
      char var10000;
      switch (index) {
         case 21:
         case 54:
            var10000 = 'i';
            break;
         case 22:
         case 55:
            var10000 = 'j';
            break;
         case 23:
         case 56:
            var10000 = 'f';
            break;
         case 24:
         case 57:
            var10000 = 'd';
            break;
         case 25:
         case 58:
            var10000 = 'l';
            break;
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
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         default:
            throw new AssertionError();
      }

      return var10000;
   }
}
