package dev.jnic.bytecode;

class MethodSignature {
   private final String name;
   private final String descriptor;

   MethodSignature(String name, String descriptor) {
      this.name = name;
      this.descriptor = descriptor;
   }

   @Override
   public boolean equals(Object value) {
      if (value instanceof MethodSignature) {
         MethodSignature methodSignature = (MethodSignature)value;
         if (this.name.equals(methodSignature.name) && this.descriptor.equals(methodSignature.descriptor)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hashCode = this.name.hashCode();
      return this.descriptor.hashCode() + hashCode * 1667;
   }
}
