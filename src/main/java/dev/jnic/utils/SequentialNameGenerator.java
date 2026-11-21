package dev.jnic.utils;

public class SequentialNameGenerator {
   private int nextId = 0;
   private static final String PREFIX = "jnic_";

   public SequentialNameGenerator() {
      this(0);
   }

   private SequentialNameGenerator(int index) {
   }

   public String nextName() {
      int var1 = this.nextId++;
      return PREFIX + var1;
   }
}
