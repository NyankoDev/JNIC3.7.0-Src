package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class LdcTransformer extends OpcodeTransformer {
   public LdcTransformer() {
      super(true, 18);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      if (instruction.getOpcode() != 18) {
         throw new AssertionError();
      }

      Object var6 = ((LdcInsnNode)instruction).cst;
      return "stack" + stack.sizeBefore() + "." + constantSlot(var6) + " = " + context.constant(ownerName, var6) + ";\n";
   }

   private static char constantSlot(Object value) {
      if (value instanceof String || value instanceof Type || value instanceof Handle) {
         return 'l';
      } else if (value instanceof Integer) {
         return 'i';
      } else if (value instanceof Long) {
         return 'j';
      } else if (value instanceof Float) {
         return 'f';
      } else if (value instanceof Double) {
         return 'd';
      } else if (value instanceof ConstantDynamic) {
         throw new IllegalArgumentException("ConstantDynamic not supported");
      } else {
         throw new AssertionError("ldc of " + value);
      }
   }
}
