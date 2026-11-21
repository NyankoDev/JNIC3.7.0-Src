package dev.jnic.bytecode;

import java.util.HashMap;
import java.util.Map;

public class TranslatedMethodIndex {
   private final Map<String, TranslatedClassMethods> classes = new HashMap<>();

   public void registerMethod(String className, String methodName, String descriptor) {
      Map classes2 = this.classes;
      TranslatedClassMethods translatedClassMethods = new TranslatedClassMethods();
      classes2.putIfAbsent((String)className, (TranslatedClassMethods)translatedClassMethods);
      this.classes.get(className).methods.add((MethodSignature)(new MethodSignature(methodName, descriptor)));
   }

   public boolean containsMethod(String className, String methodName, String descriptor) {
      return this.classes.containsKey(className) ? this.classes.get(className).methods.contains(new MethodSignature(methodName, descriptor)) : false;
   }

   public boolean containsClass(String text) {
      return this.classes.containsKey(text);
   }
}
