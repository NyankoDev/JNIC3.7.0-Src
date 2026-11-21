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
      String memberName = owner.name;
      String memberName2 = method.name;
      String descriptor2 = method.desc;
      return new MethodReference(memberName, memberName2, descriptor2);
   }

   @Override
   public boolean equals(Object value) {
      if (value instanceof MethodReference) {
         MethodReference methodReference = (MethodReference)value;
         if (this.owner.equals(methodReference.owner) && this.name.equals(methodReference.name) && this.descriptor.equals(methodReference.descriptor)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hashCode = this.owner.hashCode();
      int hashCode2 = this.name.hashCode();
      return this.descriptor.hashCode() + (hashCode2 + hashCode * 1667) * 1667;
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
