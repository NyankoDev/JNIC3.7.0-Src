package dev.jnic.transformers.intrinsics;

import dev.jnic.transformers.InstructionTransformer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public abstract class IntrinsicTransformer implements InstructionTransformer {
   private final int opcode;
   private final String owner;
   private final String methodName;
   private final String descriptor;

   protected IntrinsicTransformer(int opcode, String owner, String methodName, String descriptor) {
      this.opcode = opcode;
      this.owner = owner;
      this.methodName = methodName;
      this.descriptor = descriptor;
   }

   @Override
   public boolean accepts(AbstractInsnNode instruction) {
      if (instruction.getOpcode() == this.opcode && instruction instanceof MethodInsnNode) {
         MethodInsnNode invocation = (MethodInsnNode)instruction;
         if ((this.owner == null || invocation.owner.equals(this.owner))
            && (this.methodName == null || invocation.name.equals(this.methodName))
            && (this.descriptor == null || invocation.desc.equals(this.descriptor))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canThrow() {
      return true;
   }
}
