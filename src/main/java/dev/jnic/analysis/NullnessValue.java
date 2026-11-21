package dev.jnic.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

public enum NullnessValue implements Value {
   NULL,
   NOT_NULL,
   UNKNOWN1,
   UNKNOWN2;

   public static NullnessValue unknownValue(Type type) {
      return type != null && type.getSize() == 2 ? UNKNOWN2 : UNKNOWN1;
   }

   @Override
   public int getSize() {
      switch (this.ordinal()) {
         case 0:
            return 1;
         case 1:
            return 1;
         case 2:
            return 1;
         case 3:
            return 2;
         default:
            throw new AssertionError();
      }
   }

   public NullnessValue merge(NullnessValue nullnessValue) {
      return this != nullnessValue && this != UNKNOWN2 ? UNKNOWN1 : this;
   }

   public NullnessValue invertNullness() {
      if (this == NULL) {
         return NOT_NULL;
      } else {
         return this == NOT_NULL ? NULL : this;
      }
   }

   static {
      NullnessValue[] nullnessValues = new NullnessValue[]{NULL, NOT_NULL, UNKNOWN1, UNKNOWN2};
   }
}
