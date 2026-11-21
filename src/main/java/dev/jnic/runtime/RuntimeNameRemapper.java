package dev.jnic.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.objectweb.asm.commons.Remapper;

public class RuntimeNameRemapper extends Remapper {
   private final String runtimePackage;
   private final Map<String, String> names = new HashMap<>();

   public RuntimeNameRemapper(String runtimePackage) {
      this.runtimePackage = runtimePackage;
   }

   @Override
   public String map(String internalName) {
      if (internalName.startsWith("org/tukaani/xz/")) {
         Map names2 = this.names;
         Function<String, String> mapper = textx -> {
            String candidateName;
            do {
               candidateName = this.runtimePackage + "/" + randomName(1);
            } while (this.names.containsValue(candidateName));

            return candidateName;
         };
         String mappedName = (String)names2.computeIfAbsent((String)internalName, mapper);
         this.names.put(mappedName, (String)internalName);
         return mappedName;
      } else {
         return internalName;
      }
   }

   public String originalName(String mappedName) {
      return this.names.getOrDefault(mappedName, (String)mappedName);
   }

   public static String randomName(int index) {
      StringBuilder builder = new StringBuilder(index);

      for (int index2 = 0; index2 < index; index2++) {
         builder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(ThreadLocalRandom.current().nextInt(52)));
      }

      return builder.toString();
   }
}
