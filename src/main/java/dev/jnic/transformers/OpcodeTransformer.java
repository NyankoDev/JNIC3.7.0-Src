package dev.jnic.transformers;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;
import org.objectweb.asm.tree.AbstractInsnNode;

public abstract class OpcodeTransformer implements InstructionTransformer {
   private final boolean canThrow;
   private final BitSet opcodes;

   public OpcodeTransformer(boolean canThrow, int... values) {
      this.canThrow = canThrow;
      this.opcodes = new BitSet(256);
      IntStream var10000 = Arrays.stream(values);
      BitSet capturedValue3 = this.opcodes;
      var10000.forEach(capturedValue3::set);
   }

   @Override
   public boolean accepts(AbstractInsnNode instruction) {
      return this.opcodes.get(instruction.getOpcode());
   }

   @Override
   public boolean canThrow() {
      return this.canThrow;
   }
}
