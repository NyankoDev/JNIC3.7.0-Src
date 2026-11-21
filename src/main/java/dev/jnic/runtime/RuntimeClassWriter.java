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

   private String superName(String text) {
      Objects.requireNonNull(text, "");
      if (text.equals(this.loaderClassName)) {
         return "java/io/InputStream";
      } else if (text.equals("java/io/InputStream")) {
         return "java/lang/Object";
      } else if (this.classes.get(text) == null) {
         return null;
      } else {
         Object var2 = this.classes.get(text);
         if (var2 != null && !(var2 instanceof ClassReader)) {
            throw new ClassCastException("");
         } else {
            return ((ClassReader)var2).getSuperName();
         }
      }
   }

   private Set ancestors(String value1) {
      HashSet var6 = new HashSet();
      var6.add(value1);

      while (true) {
         value1 = this.superName(value1);
         if (value1 == null) {
            return var6;
         }

         var6.add(value1);
      }
   }

   @Override
   protected String getCommonSuperClass(String text, String text2) {
      String value7 = this.remapper.originalName(text);
      String value8 = this.remapper.originalName(text2);
      Set var9 = this.ancestors(value8);
      if (var9.contains(value7)) {
         return text;
      }

      do {
         value7 = this.superName(value7);
         if (value7 == null) {
            return super.getCommonSuperClass(text, text2);
         }
      } while (!var9.contains(value7));

      return this.remapper.originalName(value7);
   }
}
