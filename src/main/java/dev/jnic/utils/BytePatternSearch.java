package dev.jnic.utils;

public class BytePatternSearch {
   public static int indexOf(byte[] bytes, byte[] bytes2) {
      int[] var3 = failureTable(bytes2);
      int var4 = bytes.length;
      int var5 = 0;

      for (int var6 = 0; var6 < var4; var6++) {
         while (var5 != 0 && bytes[var6] != bytes2[var5]) {
            var5 = var3[var5 - 1];
         }

         if (bytes[var6] == bytes2[var5]) {
            var5++;
         }

         if (var5 == bytes2.length) {
            return var6 + 1 - var5;
         }
      }

      return -1;
   }

   private static int[] failureTable(byte[] bytes) {
      int[] var1 = new int[bytes.length];
      int var3 = 0;

      for (int var4 = 1; var4 < bytes.length; var4++) {
         while (var3 != 0 && bytes[var4] != bytes[var3]) {
            var3 = var1[var3 - 1];
         }

         if (bytes[var4] == bytes[var3]) {
            var3++;
         }

         var1[var4] = var3;
      }

      return var1;
   }
}
