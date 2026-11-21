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
         MethodSignature var2 = (MethodSignature)value;
         if (this.name.equals(var2.name) && this.descriptor.equals(var2.descriptor)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      int var2 = this.name.hashCode();
      return this.descriptor.hashCode() + var2 * 1667;
   }
}
