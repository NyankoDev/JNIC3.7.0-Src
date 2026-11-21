package dev.jnic.runtime;

import dev.jnic.archive.NativeLibrarySlice;
import dev.jnic.compiler.TargetPlatform;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import org.bouncycastle.util.Pack;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class RuntimeLoaderGenerator {
   public static final int LOADER_HEADER_SIZE = 13;
   private static final Type OBJECT_TYPE = Type.getObjectType(new String("java/lang/Object"));

   private static Type eraseReferenceType(Type type) {
      int typeSort = type.getSort();
      if (typeSort == 10) {
         return OBJECT_TYPE;
      } else {
         return typeSort != 9 ? type : OBJECT_TYPE;
      }
   }

   public static String eraseMethodDescriptor(String text) {
      Type[] argumentTypes = Type.getArgumentTypes(text);
      Type type = eraseReferenceType(Type.getReturnType(text));
      Stream<Type> stream3 = Arrays.stream(argumentTypes);
      Function<Type, Type> eraseReferenceTypeMapper = RuntimeLoaderGenerator::eraseReferenceType;
      Stream<Type> stream5 = stream3.map(eraseReferenceTypeMapper);
      IntFunction<Type[]> initMapper = Type[]::new;
      return Type.getMethodDescriptor(type, stream5.toArray(initMapper));
   }

   public static String methodHandleDescriptor(String text) {
      Type type = eraseReferenceType(Type.getReturnType(text));
      Type[] argumentTypes = Type.getArgumentTypes(text);
      Type[] types = new Type[argumentTypes.length + 1];
      Type objectType = Type.getObjectType("java/lang/invoke/MethodHandle");
      types[0] = objectType;
      int index = 0;

      while (true) {
         int argumentTypeCount = argumentTypes.length;
         if (index >= argumentTypeCount) {
            return Type.getMethodDescriptor(type, types);
         }

         Type type2 = eraseReferenceType(argumentTypes[index]);
         types[index + 1] = type2;
         index++;
      }
   }

   private static void emitLoader(String text, int index, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      new RuntimeLoaderGenerator().writeRuntimeLoader(text, index, text2, map, classVisitor, bytes);
   }

   private static void lambda$visitLoader$1(MethodVisitor methodVisitor, String text, TargetPlatform targetPlatform, NativeLibrarySlice nativeLibrarySlice) {
      Label label = new Label();
      Label label2 = new Label();
      methodVisitor.visitVarInsn(25, 0);
      String osName = targetPlatform.getOsName();
      methodVisitor.visitLdcInsn(osName);
      methodVisitor.visitMethodInsn(182, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
      methodVisitor.visitJumpInsn(153, label);

      for (String constant : targetPlatform.getArchitectures()) {
         methodVisitor.visitVarInsn(25, 1);
         methodVisitor.visitLdcInsn(constant);
         methodVisitor.visitMethodInsn(182, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
         methodVisitor.visitJumpInsn(154, label2);
      }

      methodVisitor.visitJumpInsn(167, label);
      methodVisitor.visitLabel(label2);
      Long startOffset = nativeLibrarySlice.getStartOffset();
      methodVisitor.visitLdcInsn(startOffset);
      methodVisitor.visitVarInsn(55, 2);
      Long endOffset = nativeLibrarySlice.getEndOffset();
      methodVisitor.visitLdcInsn(endOffset);
      methodVisitor.visitVarInsn(55, 4);
      String memberName = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName, "Ljava/nio/ByteBuffer;");
      Integer keyWord = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 0);
      methodVisitor.visitLdcInsn(keyWord);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName2 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName2, "Ljava/nio/ByteBuffer;");
      Integer keyWord2 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 4);
      methodVisitor.visitLdcInsn(keyWord2);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName3 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName3, "Ljava/nio/ByteBuffer;");
      Integer keyWord3 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 8);
      methodVisitor.visitLdcInsn(keyWord3);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName4 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName4, "Ljava/nio/ByteBuffer;");
      Integer keyWord4 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 12);
      methodVisitor.visitLdcInsn(keyWord4);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName5 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName5, "Ljava/nio/ByteBuffer;");
      Integer keyWord5 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 16);
      methodVisitor.visitLdcInsn(keyWord5);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName6 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName6, "Ljava/nio/ByteBuffer;");
      Integer keyWord6 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 20);
      methodVisitor.visitLdcInsn(keyWord6);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName7 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName7, "Ljava/nio/ByteBuffer;");
      Integer keyWord7 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 24);
      methodVisitor.visitLdcInsn(keyWord7);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String memberName8 = new String("z");
      methodVisitor.visitFieldInsn(178, text, memberName8, "Ljava/nio/ByteBuffer;");
      Integer keyWord8 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 28);
      methodVisitor.visitLdcInsn(keyWord8);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      methodVisitor.visitLabel(label);
   }

   public void writeRuntimeLoader(String text, int index, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      writeRuntimeHeader(text, index, classVisitor);
      writeInit(text, text2, map, classVisitor, bytes);
      this.writeInitializer(text, text2, map, classVisitor, bytes);
      classVisitor.visitEnd();
   }

   private static void writeInit(String text, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(9, "init", "()V", null, null);
      methodVisitor.visitCode();
      methodVisitor.visitInsn(177);
      methodVisitor.visitMaxs(0, 0);
      methodVisitor.visitEnd();
   }

   private void writeInitializer(String text, String text2, Map<?, ?> map, ClassVisitor classVisitor, byte[] bytes) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(9, "<clinit>", "()V", null, null);
      methodVisitor.visitCode();
      Label label = new Label();
      Label label2 = new Label();
      Label label3 = new Label();
      methodVisitor.visitTryCatchBlock(label, label2, label3, "java/io/IOException");
      Label label4 = new Label();
      Label label5 = new Label();
      Label label6 = new Label();
      methodVisitor.visitTryCatchBlock(label4, label5, label6, "java/lang/Throwable");
      Label label7 = new Label();
      Label label8 = new Label();
      Label label9 = new Label();
      methodVisitor.visitTryCatchBlock(label7, label8, label9, "java/lang/Throwable");
      Label label10 = new Label();
      Label label11 = new Label();
      Label label12 = new Label();
      methodVisitor.visitTryCatchBlock(label10, label11, label12, "java/lang/Throwable");
      Label label13 = new Label();
      Label label14 = new Label();
      Label label15 = new Label();
      methodVisitor.visitTryCatchBlock(label13, label14, label15, "java/lang/Throwable");
      Label label16 = new Label();
      Label label17 = new Label();
      Label label18 = new Label();
      methodVisitor.visitTryCatchBlock(label16, label17, label18, "java/io/IOException");
      new Label();
      methodVisitor.visitIntInsn(16, 64);
      methodVisitor.visitMethodInsn(184, "java/nio/ByteBuffer", "allocateDirect", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitFieldInsn(178, "java/nio/ByteOrder", "LITTLE_ENDIAN", "Ljava/nio/ByteOrder;");
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "order", "(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitFieldInsn(179, text, "z", "Ljava/nio/ByteBuffer;");
      methodVisitor.visitLdcInsn("os.name");
      methodVisitor.visitMethodInsn(184, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
      methodVisitor.visitVarInsn(58, 0);
      methodVisitor.visitLdcInsn("os.arch");
      methodVisitor.visitMethodInsn(184, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
      methodVisitor.visitVarInsn(58, 1);
      methodVisitor.visitInsn(9);
      methodVisitor.visitVarInsn(55, 2);
      methodVisitor.visitInsn(9);
      methodVisitor.visitVarInsn(55, 4);

      for (Entry entry : map.entrySet()) {
         Object entryValue = entry.getValue();
         lambda$visitLoader$1(methodVisitor, text, (TargetPlatform)entry.getKey(), (NativeLibrarySlice)entryValue);
      }

      methodVisitor.visitVarInsn(22, 2);
      methodVisitor.visitVarInsn(22, 4);
      methodVisitor.visitInsn(148);
      methodVisitor.visitJumpInsn(154, label);
      methodVisitor.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      methodVisitor.visitInsn(89);
      methodVisitor.visitTypeInsn(187, "java/lang/StringBuilder");
      methodVisitor.visitInsn(89);
      methodVisitor.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      methodVisitor.visitLdcInsn("Platform not supported: ");
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitLdcInsn("/");
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitVarInsn(25, 1);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label);
      methodVisitor.visitLdcInsn("lib");
      methodVisitor.visitInsn(1);
      methodVisitor.visitMethodInsn(184, "java/io/File", "createTempFile", "(Ljava/lang/String;Ljava/lang/String;)Ljava/io/File;", false);
      methodVisitor.visitVarInsn(58, 6);
      methodVisitor.visitVarInsn(25, 6);
      methodVisitor.visitMethodInsn(182, "java/io/File", "deleteOnExit", "()V", false);
      methodVisitor.visitVarInsn(25, 6);
      methodVisitor.visitMethodInsn(182, "java/io/File", "exists", "()Z", false);
      methodVisitor.visitJumpInsn(154, label2);
      methodVisitor.visitTypeInsn(187, "java/io/IOException");
      methodVisitor.visitInsn(89);
      methodVisitor.visitMethodInsn(183, "java/io/IOException", "<init>", "()V", false);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label2);
      Label label19 = new Label();
      methodVisitor.visitJumpInsn(167, label19);
      methodVisitor.visitLabel(label3);
      methodVisitor.visitVarInsn(58, 7);
      methodVisitor.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      methodVisitor.visitInsn(89);
      methodVisitor.visitLdcInsn("Failed to create temp file");
      methodVisitor.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label19);
      methodVisitor.visitIntInsn(17, 2048);
      methodVisitor.visitIntInsn(188, 8);
      methodVisitor.visitVarInsn(58, 7);
      methodVisitor.visitLabel(label16);
      methodVisitor.visitTypeInsn(187, text);
      methodVisitor.visitInsn(89);
      methodVisitor.visitLdcInsn(Type.getObjectType(text));
      methodVisitor.visitLdcInsn("/" + text2);
      methodVisitor.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
      methodVisitor.visitMethodInsn(184, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
      methodVisitor.visitTypeInsn(192, "java/io/InputStream");
      methodVisitor.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;)V", false);
      methodVisitor.visitVarInsn(58, 8);
      methodVisitor.visitLabel(label10);
      methodVisitor.visitTypeInsn(187, "java/io/FileOutputStream");
      methodVisitor.visitInsn(89);
      methodVisitor.visitVarInsn(25, 6);
      methodVisitor.visitMethodInsn(183, "java/io/FileOutputStream", "<init>", "(Ljava/io/File;)V", false);
      methodVisitor.visitVarInsn(58, 9);
      methodVisitor.visitLabel(label4);
      methodVisitor.visitInsn(9);
      methodVisitor.visitVarInsn(55, 10);
      Label label20 = new Label();
      methodVisitor.visitLabel(label20);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitVarInsn(22, 2);
      methodVisitor.visitInsn(148);
      Label label21 = new Label();
      methodVisitor.visitJumpInsn(156, label21);
      methodVisitor.visitVarInsn(25, 8);
      methodVisitor.visitVarInsn(22, 2);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitInsn(101);
      methodVisitor.visitMethodInsn(182, "java/io/InputStream", "skip", "(J)J", false);
      methodVisitor.visitVarInsn(55, 12);
      methodVisitor.visitVarInsn(22, 12);
      methodVisitor.visitInsn(9);
      methodVisitor.visitInsn(148);
      Label label22 = new Label();
      methodVisitor.visitJumpInsn(157, label22);
      methodVisitor.visitTypeInsn(187, "java/io/IOException");
      methodVisitor.visitInsn(89);
      methodVisitor.visitTypeInsn(187, "java/lang/StringBuilder");
      methodVisitor.visitInsn(89);
      methodVisitor.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      methodVisitor.visitLdcInsn("failed to skip: ");
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(183, "java/io/IOException", "<init>", "(Ljava/lang/String;)V", false);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label22);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitVarInsn(22, 12);
      methodVisitor.visitInsn(97);
      methodVisitor.visitVarInsn(55, 10);
      methodVisitor.visitJumpInsn(167, label20);
      methodVisitor.visitLabel(label21);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitVarInsn(22, 4);
      methodVisitor.visitInsn(148);
      methodVisitor.visitJumpInsn(156, label5);
      methodVisitor.visitVarInsn(25, 8);
      methodVisitor.visitVarInsn(25, 7);
      methodVisitor.visitInsn(3);
      methodVisitor.visitVarInsn(25, 7);
      methodVisitor.visitInsn(190);
      methodVisitor.visitInsn(133);
      methodVisitor.visitVarInsn(22, 4);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitInsn(101);
      methodVisitor.visitMethodInsn(184, "java/lang/Math", "min", "(JJ)J", false);
      methodVisitor.visitInsn(136);
      methodVisitor.visitMethodInsn(182, "java/io/InputStream", "read", "([BII)I", false);
      methodVisitor.visitVarInsn(54, 12);
      methodVisitor.visitVarInsn(25, 9);
      methodVisitor.visitVarInsn(25, 7);
      methodVisitor.visitInsn(3);
      methodVisitor.visitVarInsn(21, 12);
      methodVisitor.visitMethodInsn(182, "java/io/OutputStream", "write", "([BII)V", false);
      methodVisitor.visitVarInsn(22, 10);
      methodVisitor.visitVarInsn(21, 12);
      methodVisitor.visitInsn(133);
      methodVisitor.visitInsn(97);
      methodVisitor.visitVarInsn(55, 10);
      methodVisitor.visitJumpInsn(167, label21);
      methodVisitor.visitLabel(label5);
      methodVisitor.visitVarInsn(25, 9);
      methodVisitor.visitMethodInsn(182, "java/io/OutputStream", "close", "()V", false);
      methodVisitor.visitJumpInsn(167, label11);
      methodVisitor.visitLabel(label6);
      methodVisitor.visitVarInsn(58, 10);
      methodVisitor.visitLabel(label7);
      methodVisitor.visitVarInsn(25, 9);
      methodVisitor.visitMethodInsn(182, "java/io/OutputStream", "close", "()V", false);
      methodVisitor.visitLabel(label8);
      Label label23 = new Label();
      methodVisitor.visitJumpInsn(167, label23);
      methodVisitor.visitLabel(label9);
      methodVisitor.visitVarInsn(58, 11);
      methodVisitor.visitVarInsn(25, 10);
      methodVisitor.visitVarInsn(25, 11);
      methodVisitor.visitMethodInsn(182, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
      methodVisitor.visitLabel(label23);
      methodVisitor.visitVarInsn(25, 10);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label11);
      methodVisitor.visitVarInsn(25, 8);
      methodVisitor.visitMethodInsn(182, "java/io/InputStream", "close", "()V", false);
      methodVisitor.visitJumpInsn(167, label17);
      methodVisitor.visitLabel(label12);
      methodVisitor.visitVarInsn(58, 9);
      methodVisitor.visitLabel(label13);
      methodVisitor.visitVarInsn(25, 8);
      methodVisitor.visitMethodInsn(182, "java/io/InputStream", "close", "()V", false);
      methodVisitor.visitLabel(label14);
      Label label24 = new Label();
      methodVisitor.visitJumpInsn(167, label24);
      methodVisitor.visitLabel(label15);
      methodVisitor.visitVarInsn(58, 10);
      methodVisitor.visitVarInsn(25, 9);
      methodVisitor.visitVarInsn(25, 10);
      methodVisitor.visitMethodInsn(182, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
      methodVisitor.visitLabel(label24);
      methodVisitor.visitVarInsn(25, 9);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label17);
      Label label25 = new Label();
      methodVisitor.visitJumpInsn(167, label25);
      methodVisitor.visitLabel(label18);
      methodVisitor.visitVarInsn(58, 8);
      methodVisitor.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      methodVisitor.visitInsn(89);
      methodVisitor.visitTypeInsn(187, "java/lang/StringBuilder");
      methodVisitor.visitInsn(89);
      methodVisitor.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      methodVisitor.visitLdcInsn("Failed to extract file: ");
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitVarInsn(25, 8);
      methodVisitor.visitMethodInsn(182, "java/io/IOException", "getMessage", "()Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      methodVisitor.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      methodVisitor.visitInsn(191);
      methodVisitor.visitLabel(label25);
      ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

      for (byte index = 0; index < 12; index += 4) {
         writeBufferWord(methodVisitor, text, byteBuffer.getInt(index));
      }

      int randomValue = ThreadLocalRandom.current().nextInt(5);

      for (int index2 = 0; index2 < randomValue; index2++) {
         writeBufferWord(methodVisitor, text, ThreadLocalRandom.current().nextInt());
      }

      methodVisitor.visitVarInsn(25, 6);
      methodVisitor.visitMethodInsn(182, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
      methodVisitor.visitMethodInsn(184, "java/lang/System", "load", "(Ljava/lang/String;)V", false);
      methodVisitor.visitInsn(177);
      methodVisitor.visitMaxs(9, 14);
      methodVisitor.visitEnd();
   }

   private static void writeBufferWord(MethodVisitor methodVisitor, String text, int index) {
      methodVisitor.visitFieldInsn(178, text, "z", "Ljava/nio/ByteBuffer;");
      methodVisitor.visitLdcInsn(index);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
   }

   public static Map generateClasses(String text, String text2, int index, Map map, String text3, byte[] bytes) {
      HashMap entries = new HashMap();
      RuntimeNameRemapper runtimeNameRemapper = new RuntimeNameRemapper(text);
      HashMap entries2 = new HashMap();
      copyRuntimeClasses(runtimeNameRemapper, entries2, entries);
      RuntimeClassWriter runtimeClassWriter = new RuntimeClassWriter(text2, runtimeNameRemapper, entries2);
      emitLoader(text2, index, text3, map, new ClassRemapper(runtimeClassWriter, runtimeNameRemapper), bytes);
      entries.put(text2 + ".class", runtimeClassWriter.toByteArray());
      return entries;
   }

   private static void copyRuntimeClasses(Remapper remapper, Map<String, ClassReader> map, Map<String, byte[]> map2) {
      copyRuntimeClass("org/tukaani/xz/ArrayCache", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/BasicArrayCache", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/BasicArrayCache$1", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/BasicArrayCache$CacheMap", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/BasicArrayCache$CyclicStack", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/BasicArrayCache$LazyHolder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/CorruptedInputException", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/LZMA2InputStream", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/XZIOException", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lz/LZDecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMACoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMACoder$LengthCoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMACoder$LiteralCoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMACoder$LiteralCoder$LiteralSubcoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMADecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMADecoder$1", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMADecoder$LengthDecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMADecoder$LiteralDecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/LZMADecoder$LiteralDecoder$LiteralSubdecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/lzma/State", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/rangecoder/RangeCoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/rangecoder/RangeDecoder", remapper, map, map2);
      copyRuntimeClass("org/tukaani/xz/rangecoder/RangeDecoderFromBuffer", remapper, map, map2);
   }

   private static void copyRuntimeClass(String text, Remapper remapper, Map<String, ClassReader> map, Map<String, byte[]> map2) {
      try (InputStream input = Objects.requireNonNull(RuntimeLoaderGenerator.class.getResourceAsStream("/" + text + ".class"))) {
         ClassReader reader = new ClassReader(input);
         ClassWriter writer = new ClassWriter(0);
         map.put(reader.getClassName(), reader);
         reader.accept(new ClassRemapper(writer, remapper), 2);
         map2.put(remapper.mapType(reader.getClassName()) + ".class", writer.toByteArray());
      } catch (IOException ioFailure) {
         throw new UncheckedIOException(ioFailure);
      }
   }

   private static void writeRuntimeHeader(String text, int index, ClassVisitor classVisitor) {
      classVisitor.visit(51, 33, text, null, "java/io/InputStream", null);
      classVisitor.visitField(9, "z", "Ljava/nio/ByteBuffer;", null, null).visitEnd();
      classVisitor.visitField(2, "V", "Ljava/lang/String;", null, RuntimeLoaderGenerator.class.getPackage().getImplementationVersion()).visitEnd();
      classVisitor.visitField(18, "decoder", "Ljava/io/InputStream;", null, null).visitEnd();
      writeDecoderConstructor(text, classVisitor);
      writeDefaultConstructor(text, index, classVisitor);
      writeSizedConstructor(text, classVisitor);
      writeDelegate(text, classVisitor, "read", "()I");
      writeDelegate(text, classVisitor, "read", "([BII)I");
      writeDelegate(text, classVisitor, "available", "()I");
      writeDelegate(text, classVisitor, "close", "()V");
   }

   private static void writeDecoderConstructor(String text, ClassVisitor classVisitor) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;I[B)V", null, new String[]{"java/io/IOException"});
      methodVisitor.visitCode();
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitMethodInsn(183, "java/io/InputStream", "<init>", "()V", false);
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitTypeInsn(187, "org/tukaani/xz/LZMA2InputStream");
      methodVisitor.visitInsn(89);
      methodVisitor.visitVarInsn(25, 1);
      methodVisitor.visitVarInsn(21, 2);
      methodVisitor.visitVarInsn(25, 3);
      methodVisitor.visitMethodInsn(183, "org/tukaani/xz/LZMA2InputStream", "<init>", "(Ljava/io/InputStream;I[B)V", false);
      methodVisitor.visitFieldInsn(181, text, "decoder", "Ljava/io/InputStream;");
      methodVisitor.visitInsn(177);
      methodVisitor.visitMaxs(6, 4);
      methodVisitor.visitEnd();
   }

   private static void writeDefaultConstructor(String text, int index, ClassVisitor classVisitor) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;)V", null, new String[]{"java/io/IOException"});
      methodVisitor.visitCode();
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitVarInsn(25, 1);
      methodVisitor.visitLdcInsn(index);
      methodVisitor.visitInsn(1);
      methodVisitor.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;I[B)V", false);
      methodVisitor.visitInsn(177);
      methodVisitor.visitMaxs(4, 2);
      methodVisitor.visitEnd();
   }

   private static void writeSizedConstructor(String text, ClassVisitor classVisitor) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;I)V", null, new String[]{"java/io/IOException"});
      methodVisitor.visitCode();
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitVarInsn(25, 1);
      methodVisitor.visitVarInsn(21, 2);
      methodVisitor.visitInsn(1);
      methodVisitor.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;I[B)V", false);
      methodVisitor.visitInsn(177);
      methodVisitor.visitMaxs(4, 3);
      methodVisitor.visitEnd();
   }

   private static void writeDelegate(String text, ClassVisitor classVisitor, String text2, String text3) {
      MethodVisitor methodVisitor = classVisitor.visitMethod(1, text2, text3, null, new String[]{"java/io/IOException"});
      methodVisitor.visitCode();
      methodVisitor.visitVarInsn(25, 0);
      methodVisitor.visitFieldInsn(180, text, "decoder", "Ljava/io/InputStream;");
      int localIndex = 1;

      for (Type type : Type.getArgumentTypes(text3)) {
         methodVisitor.visitVarInsn(type.getOpcode(21), localIndex);
         localIndex += type.getSize();
      }

      methodVisitor.visitMethodInsn(182, "java/io/InputStream", text2, text3, false);
      methodVisitor.visitInsn(Type.getReturnType(text3).getOpcode(172));
      methodVisitor.visitMaxs(localIndex, localIndex);
      methodVisitor.visitEnd();
   }
}
