package dev.jnic.utils;

public class BytePatternSearch {
   public static int indexOf(byte[] bytes, byte[] bytes2) {
      int[] words = failureTable(bytes2);
      int byteCount = bytes.length;
      int index = 0;

      for (int index2 = 0; index2 < byteCount; index2++) {
         while (index != 0 && bytes[index2] != bytes2[index]) {
            index = words[index - 1];
         }

         if (bytes[index2] == bytes2[index]) {
            index++;
         }

         if (index == bytes2.length) {
            return index2 + 1 - index;
         }
      }

      return -1;
   }

   private static int[] failureTable(byte[] bytes) {
      int[] words = new int[bytes.length];
      int index = 0;

      for (int index2 = 1; index2 < bytes.length; index2++) {
         while (index != 0 && bytes[index2] != bytes[index]) {
            index = words[index - 1];
         }

         if (bytes[index2] == bytes[index]) {
            index++;
         }

         words[index2] = index;
      }

      return words;
   }
}
