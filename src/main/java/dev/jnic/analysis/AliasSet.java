package dev.jnic.analysis;

import java.util.BitSet;

public class AliasSet {
   private final BitSet slots;

   public AliasSet(BitSet slots) {
      this.slots = slots;
   }

   public AliasSet() {
      this(new BitSet());
   }

   public static AliasSet singleton(int index) {
      AliasSet var1 = new AliasSet();
      var1.slots.set(index);
      return var1;
   }

   void add(int index) {
      this.slots.set(index);
   }

   void remove(int index) {
      this.slots.clear(index);
   }

   public boolean contains(int index) {
      return this.slots.get(index);
   }

   public boolean isEmpty() {
      return this.slots.isEmpty();
   }

   public int size() {
      return this.slots.cardinality();
   }

   public int nextSetBit(int index) {
      return this.slots.nextSetBit(index);
   }

   AliasSet copy() {
      BitSet var2 = (BitSet)this.slots.clone();
      return new AliasSet(var2);
   }
}
