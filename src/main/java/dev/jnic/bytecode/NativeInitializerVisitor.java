package dev.jnic.bytecode;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class NativeInitializerVisitor extends ClassVisitor {
   public static final String INITIALIZER_METHOD = new String("$jnicClinit");
   public static final String LOADER_METHOD = new String("$jnicLoader");
   private final String loaderClassName;
   private String className;
   private boolean hasInitializer = false;

   public NativeInitializerVisitor(ClassVisitor classVisitor, String loaderClassName) {
      super(589824, classVisitor);
      this.loaderClassName = loaderClassName;
   }

   @Override
   public void visit(int index, int index2, String text, String text2, String text3, String[] values) {
      super.visit(index, index2, text, text2, text3, values);
      this.className = text;
   }

   @Override
   public MethodVisitor visitMethod(int index, String literalText, String text, String text2, String[] values) {
      if ("<clinit>".equals(literalText)) {
         literalText = "$jnicClinit";
         this.hasInitializer = true;
         index += 4096;
      }

      return this.cv.visitMethod(index, literalText, text, text2, values);
   }

   @Override
   public void visitEnd() {
      super.visitMethod(4361, "$jnicLoader", "()V", null, null).visitEnd();
      MethodVisitor methodVisitor = super.visitMethod(9, "<clinit>", "()V", null, null);
      methodVisitor.visitCode();
      String loaderClassName2 = this.loaderClassName;
      methodVisitor.visitMethodInsn(184, loaderClassName2, "init", "()V", false);
      String className2 = this.className;
      methodVisitor.visitMethodInsn(184, className2, "$jnicLoader", "()V", false);
      if (this.hasInitializer) {
         String className3 = this.className;
         methodVisitor.visitMethodInsn(184, className3, "$jnicClinit", "()V", false);
         methodVisitor.visitInsn(177);
         methodVisitor.visitMaxs(0, 0);
         methodVisitor.visitEnd();
         super.visitEnd();
      } else {
         methodVisitor.visitInsn(177);
         methodVisitor.visitMaxs(0, 0);
         methodVisitor.visitEnd();
         super.visitEnd();
      }
   }
}
