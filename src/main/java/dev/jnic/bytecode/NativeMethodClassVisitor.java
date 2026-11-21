package dev.jnic.bytecode;

import dev.jnic.logging.JnicLogger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class NativeMethodClassVisitor extends ClassVisitor {
   private final TranslatedMethodIndex translatedMethods;
   private String className;

   public NativeMethodClassVisitor(ClassVisitor classVisitor, TranslatedMethodIndex translatedMethods) {
      super(589824, classVisitor);
      this.translatedMethods = translatedMethods;
   }

   @Override
   public void visit(int index, int index2, String text, String text2, String text3, String[] values) {
      this.className = text;
      super.visit(index, index2, text, text2, text3, values);
   }

   @Override
   public MethodVisitor visitMethod(int index, String text, String text2, String text3, String[] values) {
      String className2 = this.className;
      if (this.translatedMethods.containsMethod(className2, text, text2)) {
         MethodVisitor methodVisitor = this.cv.visitMethod(index | 256, text, text2, text3, values);
         if (methodVisitor == null) {
            return null;
         }

         String expression = "Deleted " + this.className + '.' + text + text2;
         JnicLogger.INSTANCE.debug(expression);
         return new CodeSkippingMethodVisitor(methodVisitor);
      } else {
         return this.cv.visitMethod(index, text, text2, text3, values);
      }
   }

   @Override
   public FieldVisitor visitField(int index, String text, String text2, String text3, Object value) {
      if (this.className.startsWith("dev/jnic/") && text.equals("$jnicEnabled") && text2.equals("Z")) {
         String expression = "Deleted $jnicEnabled field in " + this.className;
         JnicLogger.INSTANCE.debug(expression);
         return null;
      } else {
         return super.visitField(index, text, text2, text3, value);
      }
   }
}
