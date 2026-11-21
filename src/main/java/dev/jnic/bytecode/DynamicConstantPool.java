package dev.jnic.bytecode;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class DynamicConstantPool extends ClassVisitor {
   private final ClassWriter writer;
   private final Map<ConstantDynamic, ConstantDynamic> constants = new IdentityHashMap<>();
   private final Map<String, Integer> originalNameTypes = new HashMap<>();
   private int nextConstantId;

   private DynamicConstantPool(ClassWriter writer) {
      super(Opcodes.ASM9, writer);
      this.writer = writer;
   }

   public static byte[] write(ClassNode owner) {
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
      DynamicConstantPool pool = new DynamicConstantPool(writer);
      owner.accept(pool);
      byte[] bytes = writer.toByteArray();
      if (!pool.constants.isEmpty()) {
         pool.restoreNames(bytes);
      }
      return bytes;
   }

   @Override
   public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
      return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
         @Override
         public void visitLdcInsn(Object value) {
            super.visitLdcInsn(preserveIdentity(value));
         }

         @Override
         public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrap, Object... arguments) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrap, preserveArguments(arguments));
         }
      };
   }

   private Object[] preserveArguments(Object[] arguments) {
      Object[] preserved = new Object[arguments.length];
      for (int index = 0; index < arguments.length; index++) {
         preserved[index] = this.preserveIdentity(arguments[index]);
      }
      return preserved;
   }

   private Object preserveIdentity(Object value) {
      if (!(value instanceof ConstantDynamic)) {
         return value;
      }
      ConstantDynamic constant = (ConstantDynamic)value;
      ConstantDynamic preserved = this.constants.get(constant);
      if (preserved == null) {
         // ClassReader caches by pool index; ASM's structural deduplication must not merge distinct resolution states.
         String marker = "$jnic$constant$" + this.nextConstantId++;
         Object[] arguments = new Object[constant.getBootstrapMethodArgumentCount()];
         for (int index = 0; index < arguments.length; index++) {
            arguments[index] = this.preserveIdentity(constant.getBootstrapMethodArgument(index));
         }
         preserved = new ConstantDynamic(marker, constant.getDescriptor(), constant.getBootstrapMethod(), arguments);
         this.constants.put(constant, preserved);
         this.originalNameTypes.put(marker, this.writer.newNameType(constant.getName(), constant.getDescriptor()));
      }
      return preserved;
   }

   private void restoreNames(byte[] bytes) {
      ClassReader reader = new ClassReader(bytes);
      char[] buffer = new char[reader.getMaxStringLength()];
      for (int index = 1; index < reader.getItemCount(); index++) {
         int offset = reader.getItem(index);
         if (offset == 0 || reader.readByte(offset - 1) != 17) {
            continue;
         }
         int nameTypeOffset = reader.getItem(reader.readUnsignedShort(offset + 2));
         String marker = reader.readUTF8(nameTypeOffset, buffer);
         Integer originalNameType = this.originalNameTypes.get(marker);
         if (originalNameType == null) {
            throw new IllegalStateException("Missing dynamic constant identity: " + marker);
         }
         // Patch only CONSTANT_Dynamic.name_and_type_index, leaving shared UTF8 entries untouched.
         bytes[offset + 2] = (byte)(originalNameType >>> 8);
         bytes[offset + 3] = (byte)(int)originalNameType;
      }
   }
}
