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
      int var3 = type.getSort();
      if (var3 == 10) {
         return OBJECT_TYPE;
      } else {
         return var3 != 9 ? type : OBJECT_TYPE;
      }
   }

   public static String eraseMethodDescriptor(String text) {
      Type[] var2 = Type.getArgumentTypes(text);
      Type var10000 = eraseReferenceType(Type.getReturnType(text));
      Stream<Type> stream3 = Arrays.stream(var2);
      Function<Type, Type> function4 = RuntimeLoaderGenerator::eraseReferenceType;
      Stream<Type> stream5 = stream3.map(function4);
      IntFunction<Type[]> function6 = Type[]::new;
      return Type.getMethodDescriptor(var10000, stream5.toArray(function6));
   }

   public static String methodHandleDescriptor(String text) {
      Type var1 = eraseReferenceType(Type.getReturnType(text));
      Type[] var2 = Type.getArgumentTypes(text);
      Type[] var3 = new Type[var2.length + 1];
      Type var4 = Type.getObjectType("java/lang/invoke/MethodHandle");
      var3[0] = var4;
      int var7 = 0;

      while (true) {
         int var6 = var2.length;
         if (var7 >= var6) {
            return Type.getMethodDescriptor(var1, var3);
         }

         Type var9 = eraseReferenceType(var2[var7]);
         var3[var7 + 1] = var9;
         var7++;
      }
   }

   private static void emitLoader(String text, int index, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      new RuntimeLoaderGenerator().writeRuntimeLoader(text, index, text2, map, classVisitor, bytes);
   }

   private static void lambda$visitLoader$1(MethodVisitor methodVisitor, String text, TargetPlatform targetPlatform, NativeLibrarySlice nativeLibrarySlice) {
      Label var4 = new Label();
      Label var5 = new Label();
      methodVisitor.visitVarInsn(25, 0);
      String value6 = targetPlatform.getOsName();
      methodVisitor.visitLdcInsn(value6);
      methodVisitor.visitMethodInsn(182, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
      methodVisitor.visitJumpInsn(153, var4);

      for (String var7 : targetPlatform.getArchitectures()) {
         methodVisitor.visitVarInsn(25, 1);
         methodVisitor.visitLdcInsn(var7);
         methodVisitor.visitMethodInsn(182, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
         methodVisitor.visitJumpInsn(154, var5);
      }

      methodVisitor.visitJumpInsn(167, var4);
      methodVisitor.visitLabel(var5);
      Long var8 = nativeLibrarySlice.getStartOffset();
      methodVisitor.visitLdcInsn(var8);
      methodVisitor.visitVarInsn(55, 2);
      Long var9 = nativeLibrarySlice.getEndOffset();
      methodVisitor.visitLdcInsn(var9);
      methodVisitor.visitVarInsn(55, 4);
      String var10 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var10, "Ljava/nio/ByteBuffer;");
      Integer var11 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 0);
      methodVisitor.visitLdcInsn(var11);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var12 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var12, "Ljava/nio/ByteBuffer;");
      Integer var13 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 4);
      methodVisitor.visitLdcInsn(var13);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var14 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var14, "Ljava/nio/ByteBuffer;");
      Integer var15 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 8);
      methodVisitor.visitLdcInsn(var15);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var16 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var16, "Ljava/nio/ByteBuffer;");
      Integer var17 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 12);
      methodVisitor.visitLdcInsn(var17);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var18 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var18, "Ljava/nio/ByteBuffer;");
      Integer var19 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 16);
      methodVisitor.visitLdcInsn(var19);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var20 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var20, "Ljava/nio/ByteBuffer;");
      Integer var21 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 20);
      methodVisitor.visitLdcInsn(var21);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var22 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var22, "Ljava/nio/ByteBuffer;");
      Integer var23 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 24);
      methodVisitor.visitLdcInsn(var23);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      String var24 = new String("z");
      methodVisitor.visitFieldInsn(178, text, var24, "Ljava/nio/ByteBuffer;");
      Integer var25 = Pack.littleEndianToInt(nativeLibrarySlice.getKey(), 28);
      methodVisitor.visitLdcInsn(var25);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
      methodVisitor.visitLabel(var4);
   }

   public void writeRuntimeLoader(String text, int index, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      writeRuntimeHeader(text, index, classVisitor);
      writeInit(text, text2, map, classVisitor, bytes);
      this.writeInitializer(text, text2, map, classVisitor, bytes);
      classVisitor.visitEnd();
   }

   private static void writeInit(String text, String text2, Map map, ClassVisitor classVisitor, byte[] bytes) {
      MethodVisitor var5 = classVisitor.visitMethod(9, "init", "()V", null, null);
      var5.visitCode();
      var5.visitInsn(177);
      var5.visitMaxs(0, 0);
      var5.visitEnd();
   }

   private void writeInitializer(String text, String text2, Map<?, ?> map, ClassVisitor classVisitor, byte[] bytes) {
      MethodVisitor var6 = classVisitor.visitMethod(9, "<clinit>", "()V", null, null);
      var6.visitCode();
      Label var23 = new Label();
      Label var22 = new Label();
      Label var21 = new Label();
      var6.visitTryCatchBlock(var23, var22, var21, "java/io/IOException");
      Label var19 = new Label();
      Label var9 = new Label();
      Label var7 = new Label();
      var6.visitTryCatchBlock(var19, var9, var7, "java/lang/Throwable");
      Label var13 = new Label();
      Label var14 = new Label();
      Label var8 = new Label();
      var6.visitTryCatchBlock(var13, var14, var8, "java/lang/Throwable");
      Label var20 = new Label();
      Label var15 = new Label();
      Label var10 = new Label();
      var6.visitTryCatchBlock(var20, var15, var10, "java/lang/Throwable");
      Label var16 = new Label();
      Label var17 = new Label();
      Label var11 = new Label();
      var6.visitTryCatchBlock(var16, var17, var11, "java/lang/Throwable");
      Label var24 = new Label();
      Label var18 = new Label();
      Label var12 = new Label();
      var6.visitTryCatchBlock(var24, var18, var12, "java/io/IOException");
      new Label();
      var6.visitIntInsn(16, 64);
      var6.visitMethodInsn(184, "java/nio/ByteBuffer", "allocateDirect", "(I)Ljava/nio/ByteBuffer;", false);
      var6.visitFieldInsn(178, "java/nio/ByteOrder", "LITTLE_ENDIAN", "Ljava/nio/ByteOrder;");
      var6.visitMethodInsn(182, "java/nio/ByteBuffer", "order", "(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;", false);
      var6.visitFieldInsn(179, text, "z", "Ljava/nio/ByteBuffer;");
      var6.visitLdcInsn("os.name");
      var6.visitMethodInsn(184, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
      var6.visitMethodInsn(182, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
      var6.visitVarInsn(58, 0);
      var6.visitLdcInsn("os.arch");
      var6.visitMethodInsn(184, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
      var6.visitMethodInsn(182, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
      var6.visitVarInsn(58, 1);
      var6.visitInsn(9);
      var6.visitVarInsn(55, 2);
      var6.visitInsn(9);
      var6.visitVarInsn(55, 4);

      for (Entry var26 : map.entrySet()) {
         Object var37 = var26.getValue();
         lambda$visitLoader$1(var6, text, (TargetPlatform)var26.getKey(), (NativeLibrarySlice)var37);
      }

      var6.visitVarInsn(22, 2);
      var6.visitVarInsn(22, 4);
      var6.visitInsn(148);
      var6.visitJumpInsn(154, var23);
      var6.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      var6.visitInsn(89);
      var6.visitTypeInsn(187, "java/lang/StringBuilder");
      var6.visitInsn(89);
      var6.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      var6.visitLdcInsn("Platform not supported: ");
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitVarInsn(25, 0);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitLdcInsn("/");
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitVarInsn(25, 1);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      var6.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      var6.visitInsn(191);
      var6.visitLabel(var23);
      var6.visitLdcInsn("lib");
      var6.visitInsn(1);
      var6.visitMethodInsn(184, "java/io/File", "createTempFile", "(Ljava/lang/String;Ljava/lang/String;)Ljava/io/File;", false);
      var6.visitVarInsn(58, 6);
      var6.visitVarInsn(25, 6);
      var6.visitMethodInsn(182, "java/io/File", "deleteOnExit", "()V", false);
      var6.visitVarInsn(25, 6);
      var6.visitMethodInsn(182, "java/io/File", "exists", "()Z", false);
      var6.visitJumpInsn(154, var22);
      var6.visitTypeInsn(187, "java/io/IOException");
      var6.visitInsn(89);
      var6.visitMethodInsn(183, "java/io/IOException", "<init>", "()V", false);
      var6.visitInsn(191);
      var6.visitLabel(var22);
      Label var39 = new Label();
      var6.visitJumpInsn(167, var39);
      var6.visitLabel(var21);
      var6.visitVarInsn(58, 7);
      var6.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      var6.visitInsn(89);
      var6.visitLdcInsn("Failed to create temp file");
      var6.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      var6.visitInsn(191);
      var6.visitLabel(var39);
      var6.visitIntInsn(17, 2048);
      var6.visitIntInsn(188, 8);
      var6.visitVarInsn(58, 7);
      var6.visitLabel(var24);
      var6.visitTypeInsn(187, text);
      var6.visitInsn(89);
      var6.visitLdcInsn(Type.getObjectType(text));
      var6.visitLdcInsn("/" + text2);
      var6.visitMethodInsn(182, "java/lang/Class", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
      var6.visitMethodInsn(184, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
      var6.visitTypeInsn(192, "java/io/InputStream");
      var6.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;)V", false);
      var6.visitVarInsn(58, 8);
      var6.visitLabel(var20);
      var6.visitTypeInsn(187, "java/io/FileOutputStream");
      var6.visitInsn(89);
      var6.visitVarInsn(25, 6);
      var6.visitMethodInsn(183, "java/io/FileOutputStream", "<init>", "(Ljava/io/File;)V", false);
      var6.visitVarInsn(58, 9);
      var6.visitLabel(var19);
      var6.visitInsn(9);
      var6.visitVarInsn(55, 10);
      Label var38 = new Label();
      var6.visitLabel(var38);
      var6.visitVarInsn(22, 10);
      var6.visitVarInsn(22, 2);
      var6.visitInsn(148);
      Label var34 = new Label();
      var6.visitJumpInsn(156, var34);
      var6.visitVarInsn(25, 8);
      var6.visitVarInsn(22, 2);
      var6.visitVarInsn(22, 10);
      var6.visitInsn(101);
      var6.visitMethodInsn(182, "java/io/InputStream", "skip", "(J)J", false);
      var6.visitVarInsn(55, 12);
      var6.visitVarInsn(22, 12);
      var6.visitInsn(9);
      var6.visitInsn(148);
      Label var36 = new Label();
      var6.visitJumpInsn(157, var36);
      var6.visitTypeInsn(187, "java/io/IOException");
      var6.visitInsn(89);
      var6.visitTypeInsn(187, "java/lang/StringBuilder");
      var6.visitInsn(89);
      var6.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      var6.visitLdcInsn("failed to skip: ");
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitVarInsn(22, 10);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      var6.visitMethodInsn(183, "java/io/IOException", "<init>", "(Ljava/lang/String;)V", false);
      var6.visitInsn(191);
      var6.visitLabel(var36);
      var6.visitVarInsn(22, 10);
      var6.visitVarInsn(22, 12);
      var6.visitInsn(97);
      var6.visitVarInsn(55, 10);
      var6.visitJumpInsn(167, var38);
      var6.visitLabel(var34);
      var6.visitVarInsn(22, 10);
      var6.visitVarInsn(22, 4);
      var6.visitInsn(148);
      var6.visitJumpInsn(156, var9);
      var6.visitVarInsn(25, 8);
      var6.visitVarInsn(25, 7);
      var6.visitInsn(3);
      var6.visitVarInsn(25, 7);
      var6.visitInsn(190);
      var6.visitInsn(133);
      var6.visitVarInsn(22, 4);
      var6.visitVarInsn(22, 10);
      var6.visitInsn(101);
      var6.visitMethodInsn(184, "java/lang/Math", "min", "(JJ)J", false);
      var6.visitInsn(136);
      var6.visitMethodInsn(182, "java/io/InputStream", "read", "([BII)I", false);
      var6.visitVarInsn(54, 12);
      var6.visitVarInsn(25, 9);
      var6.visitVarInsn(25, 7);
      var6.visitInsn(3);
      var6.visitVarInsn(21, 12);
      var6.visitMethodInsn(182, "java/io/OutputStream", "write", "([BII)V", false);
      var6.visitVarInsn(22, 10);
      var6.visitVarInsn(21, 12);
      var6.visitInsn(133);
      var6.visitInsn(97);
      var6.visitVarInsn(55, 10);
      var6.visitJumpInsn(167, var34);
      var6.visitLabel(var9);
      var6.visitVarInsn(25, 9);
      var6.visitMethodInsn(182, "java/io/OutputStream", "close", "()V", false);
      var6.visitJumpInsn(167, var15);
      var6.visitLabel(var7);
      var6.visitVarInsn(58, 10);
      var6.visitLabel(var13);
      var6.visitVarInsn(25, 9);
      var6.visitMethodInsn(182, "java/io/OutputStream", "close", "()V", false);
      var6.visitLabel(var14);
      Label var30 = new Label();
      var6.visitJumpInsn(167, var30);
      var6.visitLabel(var8);
      var6.visitVarInsn(58, 11);
      var6.visitVarInsn(25, 10);
      var6.visitVarInsn(25, 11);
      var6.visitMethodInsn(182, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
      var6.visitLabel(var30);
      var6.visitVarInsn(25, 10);
      var6.visitInsn(191);
      var6.visitLabel(var15);
      var6.visitVarInsn(25, 8);
      var6.visitMethodInsn(182, "java/io/InputStream", "close", "()V", false);
      var6.visitJumpInsn(167, var18);
      var6.visitLabel(var10);
      var6.visitVarInsn(58, 9);
      var6.visitLabel(var16);
      var6.visitVarInsn(25, 8);
      var6.visitMethodInsn(182, "java/io/InputStream", "close", "()V", false);
      var6.visitLabel(var17);
      Label var31 = new Label();
      var6.visitJumpInsn(167, var31);
      var6.visitLabel(var11);
      var6.visitVarInsn(58, 10);
      var6.visitVarInsn(25, 9);
      var6.visitVarInsn(25, 10);
      var6.visitMethodInsn(182, "java/lang/Throwable", "addSuppressed", "(Ljava/lang/Throwable;)V", false);
      var6.visitLabel(var31);
      var6.visitVarInsn(25, 9);
      var6.visitInsn(191);
      var6.visitLabel(var18);
      Label var32 = new Label();
      var6.visitJumpInsn(167, var32);
      var6.visitLabel(var12);
      var6.visitVarInsn(58, 8);
      var6.visitTypeInsn(187, "java/lang/UnsatisfiedLinkError");
      var6.visitInsn(89);
      var6.visitTypeInsn(187, "java/lang/StringBuilder");
      var6.visitInsn(89);
      var6.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V", false);
      var6.visitLdcInsn("Failed to extract file: ");
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitVarInsn(25, 8);
      var6.visitMethodInsn(182, "java/io/IOException", "getMessage", "()Ljava/lang/String;", false);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      var6.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
      var6.visitMethodInsn(183, "java/lang/UnsatisfiedLinkError", "<init>", "(Ljava/lang/String;)V", false);
      var6.visitInsn(191);
      var6.visitLabel(var32);
      ByteBuffer var33 = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

      for (byte var28 = 0; var28 < 12; var28 += 4) {
         writeBufferWord(var6, text, var33.getInt(var28));
      }

      int var40 = ThreadLocalRandom.current().nextInt(5);

      for (int var29 = 0; var29 < var40; var29++) {
         writeBufferWord(var6, text, ThreadLocalRandom.current().nextInt());
      }

      var6.visitVarInsn(25, 6);
      var6.visitMethodInsn(182, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
      var6.visitMethodInsn(184, "java/lang/System", "load", "(Ljava/lang/String;)V", false);
      var6.visitInsn(177);
      var6.visitMaxs(9, 14);
      var6.visitEnd();
   }

   private static void writeBufferWord(MethodVisitor methodVisitor, String text, int index) {
      methodVisitor.visitFieldInsn(178, text, "z", "Ljava/nio/ByteBuffer;");
      methodVisitor.visitLdcInsn(index);
      methodVisitor.visitMethodInsn(182, "java/nio/ByteBuffer", "putInt", "(I)Ljava/nio/ByteBuffer;", false);
      methodVisitor.visitInsn(87);
   }

   public static Map generateClasses(String text, String text2, int index, Map map, String text3, byte[] bytes) {
      HashMap var6 = new HashMap();
      RuntimeNameRemapper var7 = new RuntimeNameRemapper(text);
      HashMap var8 = new HashMap();
      copyRuntimeClasses(var7, var8, var6);
      RuntimeClassWriter var9 = new RuntimeClassWriter(text2, var7, var8);
      emitLoader(text2, index, text3, map, new ClassRemapper(var9, var7), bytes);
      var6.put(text2 + ".class", var9.toByteArray());
      return var6;
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
      try (InputStream var4 = Objects.requireNonNull(RuntimeLoaderGenerator.class.getResourceAsStream("/" + text + ".class"))) {
         ClassReader var5 = new ClassReader(var4);
         ClassWriter var6 = new ClassWriter(0);
         map.put(var5.getClassName(), var5);
         var5.accept(new ClassRemapper(var6, remapper), 2);
         map2.put(remapper.mapType(var5.getClassName()) + ".class", var6.toByteArray());
      } catch (IOException var12) {
         throw new UncheckedIOException(var12);
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
      MethodVisitor var2 = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;I[B)V", null, new String[]{"java/io/IOException"});
      var2.visitCode();
      var2.visitVarInsn(25, 0);
      var2.visitMethodInsn(183, "java/io/InputStream", "<init>", "()V", false);
      var2.visitVarInsn(25, 0);
      var2.visitTypeInsn(187, "org/tukaani/xz/LZMA2InputStream");
      var2.visitInsn(89);
      var2.visitVarInsn(25, 1);
      var2.visitVarInsn(21, 2);
      var2.visitVarInsn(25, 3);
      var2.visitMethodInsn(183, "org/tukaani/xz/LZMA2InputStream", "<init>", "(Ljava/io/InputStream;I[B)V", false);
      var2.visitFieldInsn(181, text, "decoder", "Ljava/io/InputStream;");
      var2.visitInsn(177);
      var2.visitMaxs(6, 4);
      var2.visitEnd();
   }

   private static void writeDefaultConstructor(String text, int index, ClassVisitor classVisitor) {
      MethodVisitor var3 = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;)V", null, new String[]{"java/io/IOException"});
      var3.visitCode();
      var3.visitVarInsn(25, 0);
      var3.visitVarInsn(25, 1);
      var3.visitLdcInsn(index);
      var3.visitInsn(1);
      var3.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;I[B)V", false);
      var3.visitInsn(177);
      var3.visitMaxs(4, 2);
      var3.visitEnd();
   }

   private static void writeSizedConstructor(String text, ClassVisitor classVisitor) {
      MethodVisitor var2 = classVisitor.visitMethod(1, "<init>", "(Ljava/io/InputStream;I)V", null, new String[]{"java/io/IOException"});
      var2.visitCode();
      var2.visitVarInsn(25, 0);
      var2.visitVarInsn(25, 1);
      var2.visitVarInsn(21, 2);
      var2.visitInsn(1);
      var2.visitMethodInsn(183, text, "<init>", "(Ljava/io/InputStream;I[B)V", false);
      var2.visitInsn(177);
      var2.visitMaxs(4, 3);
      var2.visitEnd();
   }

   private static void writeDelegate(String text, ClassVisitor classVisitor, String text2, String text3) {
      MethodVisitor var4 = classVisitor.visitMethod(1, text2, text3, null, new String[]{"java/io/IOException"});
      var4.visitCode();
      var4.visitVarInsn(25, 0);
      var4.visitFieldInsn(180, text, "decoder", "Ljava/io/InputStream;");
      int var5 = 1;

      for (Type var9 : Type.getArgumentTypes(text3)) {
         var4.visitVarInsn(var9.getOpcode(21), var5);
         var5 += var9.getSize();
      }

      var4.visitMethodInsn(182, "java/io/InputStream", text2, text3, false);
      var4.visitInsn(Type.getReturnType(text3).getOpcode(172));
      var4.visitMaxs(var5, var5);
      var4.visitEnd();
   }
}
