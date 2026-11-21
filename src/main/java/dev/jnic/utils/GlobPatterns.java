package dev.jnic.utils;

import java.util.regex.Pattern;

public class GlobPatterns {
   public static Pattern compile(String text) {
      if (text == null) {
         throw new NullPointerException("");
      }

      int var1 = text.length();
      StringBuilder var7 = new StringBuilder(var1 + var1);
      char[] var2 = text.toCharArray();
      int var3 = var2.length;
      int var4 = 0;

      while (var4 < var3) {
         char var6 = var2[var4];
         if ((var6 & '\uffff') << 16 >> 16 == 63) {
            var7.append(".?");
            var4++;
         } else if ((var6 & '\uffff') != 42) {
            String value8 = Pattern.quote(String.valueOf((char)(var6 & 65535)));
            var7.append(value8);
            var4++;
         } else {
            var7.append(".*");
            var4++;
         }
      }

      return Pattern.compile(var7.toString());
   }
}
