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
      IntStream intStream = Arrays.stream(values);
      BitSet opcodes2 = this.opcodes;
      intStream.forEach(opcodes2::set);
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
