package dev.jnic.utils;

import java.util.regex.Pattern;

public class GlobPatterns {
   public static Pattern compile(String text) {
      if (text == null) {
         throw new NullPointerException("");
      }

      int textLength = text.length();
      StringBuilder builder = new StringBuilder(textLength + textLength);
      char[] characters = text.toCharArray();
      int characterCount = characters.length;
      int index = 0;

      while (index < characterCount) {
         char character = characters[index];
         if ((character & '\uffff') << 16 >> 16 == 63) {
            builder.append(".?");
            index++;
         } else if ((character & '\uffff') != 42) {
            String quotedCharacter = Pattern.quote(String.valueOf((char)(character & 65535)));
            builder.append(quotedCharacter);
            index++;
         } else {
            builder.append(".*");
            index++;
         }
      }

      return Pattern.compile(builder.toString());
   }
}
