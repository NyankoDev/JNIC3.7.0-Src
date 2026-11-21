package dev.jnic.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class RuntimeClassWriter extends ClassWriter {
   private final String loaderClassName;
   private final RuntimeNameRemapper remapper;
   private final Map<String, ClassReader> classes;

   public RuntimeClassWriter(String loaderClassName, RuntimeNameRemapper remapper, Map<String, ClassReader> classes) {
      super(2);
      this.loaderClassName = loaderClassName;
      this.remapper = remapper;
      this.classes = classes;
   }

   private String superName(String className) {
      Objects.requireNonNull(className, "");
      if (className.equals(this.loaderClassName)) {
         return "java/io/InputStream";
      } else if (className.equals("java/io/InputStream")) {
         return "java/lang/Object";
      } else if (this.classes.get(className) == null) {
         return null;
      } else {
         Object classReader = this.classes.get(className);
         if (classReader != null && !(classReader instanceof ClassReader)) {
            throw new ClassCastException("");
         } else {
            return ((ClassReader)classReader).getSuperName();
         }
      }
   }

   private Set ancestors(String className) {
      HashSet elements = new HashSet();
      elements.add(className);

      while (true) {
         className = this.superName(className);
         if (className == null) {
            return elements;
         }

         elements.add(className);
      }
   }

   @Override
   protected String getCommonSuperClass(String firstType, String secondType) {
      String leftName = this.remapper.originalName(firstType);
      String rightName = this.remapper.originalName(secondType);
      Set elements = this.ancestors(rightName);
      if (elements.contains(leftName)) {
         return firstType;
      }

      do {
         leftName = this.superName(leftName);
         if (leftName == null) {
            return super.getCommonSuperClass(firstType, secondType);
         }
      } while (!elements.contains(leftName));

      return this.remapper.originalName(leftName);
   }
}
