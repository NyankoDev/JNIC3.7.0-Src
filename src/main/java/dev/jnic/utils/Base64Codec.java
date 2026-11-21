package dev.jnic.utils;

import java.nio.ByteBuffer;
import java.util.Base64;

public class Base64Codec {
   public static byte[] encode(byte[] bytes) {
      return Base64.getEncoder().encode(bytes);
   }

   public static String encodeToString(byte[] bytes) {
      return Base64.getEncoder().encodeToString(bytes);
   }

   public int encode(byte[] bytes, byte[] bytes2) {
      return Base64.getEncoder().encode(bytes, bytes2);
   }

   public ByteBuffer encode(ByteBuffer byteBuffer) {
      return Base64.getEncoder().encode(byteBuffer);
   }

   public static byte[] decode(byte[] bytes) {
      return Base64.getDecoder().decode(bytes);
   }

   public static byte[] decode(String text) {
      return Base64.getDecoder().decode(text);
   }

   public int decode(byte[] bytes, byte[] bytes2) {
      return Base64.getDecoder().decode(bytes, bytes2);
   }

   public ByteBuffer decode(ByteBuffer byteBuffer) {
      return Base64.getDecoder().decode(byteBuffer);
   }
}
