package dev.jnic.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public class OperandStack {
   private final Frame<BasicValue> before;
   private final Frame<BasicValue> after;
   private final NullnessFrame nullness;

   public OperandStack(Frame<BasicValue> before, Frame<BasicValue> after, NullnessFrame nullness) {
      if (nullness.getStackSize() != before.getStackSize()) {
         throw new IllegalArgumentException();
      }

      this.before = before;
      this.after = after;
      this.nullness = nullness;
   }

   public int sizeBefore() {
      return this.before.getStackSize();
   }

   public int sizeAfter() {
      return this.after.getStackSize();
   }

   public String referenceTypeBefore(int index) {
      BasicValue var6 = this.before.getStack(index);
      BasicValue var8 = BasicValue.REFERENCE_VALUE;
      if (var6 != var8) {
         return !var6.isReference() ? null : var6.getType().getInternalName();
      } else {
         return null;
      }
   }

   public Type typeBefore(int index) {
      return this.before.getStack(index).getType();
   }

   public Type typeAfter(int index) {
      return this.after.getStack(index).getType();
   }

   public boolean mayBeNull(int index) {
      return this.nullness.getStack(index) != NullnessValue.NOT_NULL;
   }
}
