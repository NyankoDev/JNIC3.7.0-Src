package dev.jnic.codegen.jni;

import java.util.Objects;

public class JniMember {
   private final String name;
   private final String descriptor;
   private final boolean isStatic;
   private static final String STATIC_PREFIX = "static ";

   public JniMember(String text, String text2, boolean isStatic) {
      this.name = Objects.requireNonNull(text);
      this.descriptor = Objects.requireNonNull(text2);
      this.isStatic = isStatic;
   }

   public String getName() {
      return this.name;
   }

   public String getDescriptor() {
      return this.descriptor;
   }

   public boolean isStatic() {
      return this.isStatic;
   }

   @Override
   public boolean equals(Object value) {
      if (this != value) {
         if (value instanceof JniMember) {
            JniMember var2 = (JniMember)value;
            if (this.isStatic == var2.isStatic && this.name.equals(var2.name) && this.descriptor.equals(var2.descriptor)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public int hashCode() {
      return (this.name.hashCode() * 31 + this.descriptor.hashCode()) * 31 + Boolean.compare(this.isStatic, false);
   }

   @Override
   public String toString() {
      StringBuilder var10000 = new StringBuilder();
      if (this.isStatic) {
         var10000 = var10000.append("static ");
      }

      return var10000.append(this.name).append(this.descriptor).toString();
   }
}
