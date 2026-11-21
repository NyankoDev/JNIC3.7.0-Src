package dev.jnic.bytecode;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodReference {
   private final String owner;
   private final String name;
   private final String descriptor;

   public MethodReference(String owner, String name, String descriptor) {
      this.owner = owner;
      this.name = name;
      this.descriptor = descriptor;
   }

   public static MethodReference of(ClassNode owner, MethodNode method) {
      String var2 = owner.name;
      String var3 = method.name;
      String var4 = method.desc;
      return new MethodReference(var2, var3, var4);
   }

   @Override
   public boolean equals(Object value) {
      if (value instanceof MethodReference) {
         MethodReference var2 = (MethodReference)value;
         if (this.owner.equals(var2.owner) && this.name.equals(var2.name) && this.descriptor.equals(var2.descriptor)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      int var2 = this.owner.hashCode();
      int var4 = this.name.hashCode();
      return this.descriptor.hashCode() + (var4 + var2 * 1667) * 1667;
   }

   String getOwner() {
      return this.owner;
   }

   public String getName() {
      return this.name;
   }

   public String getDescriptor() {
      return this.descriptor;
   }
}
