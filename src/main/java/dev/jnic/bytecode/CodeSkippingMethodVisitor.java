package dev.jnic.bytecode;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

public class CodeSkippingMethodVisitor extends MethodVisitor {
   CodeSkippingMethodVisitor(MethodVisitor methodVisitor) {
      super(589824, methodVisitor);
   }

   @Override
   public void visitFrame(int index, int index2, Object[] values, int index3, Object[] values2) {
   }

   @Override
   public void visitInsn(int index) {
   }

   @Override
   public void visitIntInsn(int index, int index2) {
   }

   @Override
   public void visitVarInsn(int index, int index2) {
   }

   @Override
   public void visitTypeInsn(int index, String text) {
   }

   @Override
   public void visitFieldInsn(int index, String text, String text2, String text3) {
   }

   @Override
   public void visitMethodInsn(int index, String text, String text2, String text3, boolean enabled) {
   }

   @Override
   public void visitInvokeDynamicInsn(String text, String text2, Handle handle, Object... values) {
   }

   @Override
   public void visitJumpInsn(int index, Label label) {
   }

   @Override
   public void visitLabel(Label label) {
   }

   @Override
   public void visitLdcInsn(Object value) {
   }

   @Override
   public void visitIincInsn(int index, int index2) {
   }

   @Override
   public void visitTableSwitchInsn(int index, int index2, Label label, Label... values) {
   }

   @Override
   public void visitLookupSwitchInsn(Label label, int[] values, Label[] values2) {
   }

   @Override
   public void visitMultiANewArrayInsn(String text, int index) {
   }

   @Override
   public AnnotationVisitor visitInsnAnnotation(int index, TypePath typePath, String text, boolean enabled) {
      return null;
   }

   @Override
   public void visitTryCatchBlock(Label label, Label label2, Label label3, String text) {
   }

   @Override
   public AnnotationVisitor visitTryCatchAnnotation(int index, TypePath typePath, String text, boolean enabled) {
      return null;
   }

   @Override
   public void visitLocalVariable(String text, String text2, String text3, Label label, Label label2, int index) {
   }

   @Override
   public AnnotationVisitor visitLocalVariableAnnotation(
      int index, TypePath typePath, Label[] values, Label[] values2, int[] values3, String text, boolean enabled
   ) {
      return null;
   }

   @Override
   public void visitLineNumber(int index, Label label) {
   }

   @Override
   public void visitMaxs(int index, int index2) {
   }

   static {
      int[] var0 = new int[]{0, 0, 0, 0, 0};
   }
}
