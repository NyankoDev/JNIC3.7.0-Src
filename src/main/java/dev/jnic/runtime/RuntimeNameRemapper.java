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
   public String map(String text) {
      if (text.startsWith("org/tukaani/xz/")) {
         Map var2 = this.names;
         Function<String, String> function5 = textx -> {
            String var2x;
            do {
               var2x = this.runtimePackage + "/" + randomName(1);
            } while (this.names.containsValue(var2x));

            return var2x;
         };
         String var4 = (String)var2.computeIfAbsent((String)text, function5);
         this.names.put(var4, (String)text);
         return var4;
      } else {
         return text;
      }
   }

   public String originalName(String text) {
      return this.names.getOrDefault(text, (String)text);
   }

   public static String randomName(int index) {
      StringBuilder var1 = new StringBuilder(index);

      for (int var2 = 0; var2 < index; var2++) {
         var1.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(ThreadLocalRandom.current().nextInt(52)));
      }

      return var1.toString();
   }
}
