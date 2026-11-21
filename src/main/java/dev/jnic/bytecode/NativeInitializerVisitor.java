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
   public MethodVisitor visitMethod(int index, String value2, String text, String text2, String[] values) {
      if ("<clinit>".equals(value2)) {
         value2 = "$jnicClinit";
         this.hasInitializer = true;
         index += 4096;
      }

      return this.cv.visitMethod(index, value2, text, text2, values);
   }

   @Override
   public void visitEnd() {
      super.visitMethod(4361, "$jnicLoader", "()V", null, null).visitEnd();
      MethodVisitor var31 = super.visitMethod(9, "<clinit>", "()V", null, null);
      var31.visitCode();
      String var32 = this.loaderClassName;
      var31.visitMethodInsn(184, var32, "init", "()V", false);
      String var47 = this.className;
      var31.visitMethodInsn(184, var47, "$jnicLoader", "()V", false);
      if (this.hasInitializer) {
         String var63 = this.className;
         var31.visitMethodInsn(184, var63, "$jnicClinit", "()V", false);
         var31.visitInsn(177);
         var31.visitMaxs(0, 0);
         var31.visitEnd();
         super.visitEnd();
      } else {
         var31.visitInsn(177);
         var31.visitMaxs(0, 0);
         var31.visitEnd();
         super.visitEnd();
      }
   }
}
