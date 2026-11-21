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
      VarInsnNode localInstruction = (VarInsnNode)instruction;
      int opcode = instruction.getOpcode();
      boolean matches = opcode >= 21 && opcode <= 25;
      char character = localSlot(opcode);
      String expression = "local" + localInstruction.var + "." + character;
      String expression2 = "stack" + (stack.sizeBefore() - Boolean.compare(!matches, false)) + "." + character;
      return matches ? expression2 + " = " + expression + ";\n" : expression + " = " + expression2 + ";\n";
   }

   private static char localSlot(int index) {
      char character;
      switch (index) {
         case 21:
         case 54:
            character = 'i';
            break;
         case 22:
         case 55:
            character = 'j';
            break;
         case 23:
         case 56:
            character = 'f';
            break;
         case 24:
         case 57:
            character = 'd';
            break;
         case 25:
         case 58:
            character = 'l';
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

      return character;
   }
}
