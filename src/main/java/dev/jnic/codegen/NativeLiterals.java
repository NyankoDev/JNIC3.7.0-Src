package dev.jnic.codegen;

import dev.jnic.Jnic;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.crypto.ChaChaKeystream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.Pack;

public class NativeLiterals {
   private final CompilationContext context;
   private final ChaChaKeystream keystream;

   public NativeLiterals(CompilationContext context) {
      this.context = context;
      this.keystream = ChaChaKeystream.create(Jnic.CHACHA_MAGIC, context.getRandom());
   }

   public ChaChaKeystream getKeystream() {
      return this.keystream;
   }

   public String encryptedInteger(int index) {
      int var4 = this.keystream.getPosition();
      ChaChaKeystream var6 = this.keystream;

      byte[] var8;
      try {
         var8 = var6.readNBytes(4);
      } catch (IOException var11) {
         throw new RuntimeException(var11);
      }

      int var10 = Pack.littleEndianToInt(var8, 0);
      return "(" + (var10 ^ index) + " ^ *((int32_t *) (jnic_buf + " + var4 + ")))";
   }

   private String encryptedBytes(byte[] bytes) {
      int var6 = this.keystream.getPosition();
      ChaChaKeystream var8 = this.keystream;
      int var10 = bytes.length;

      byte[] var12;
      try {
         var12 = var8.readNBytes(var10);
      } catch (IOException var105) {
         throw new RuntimeException(var105);
      }

      StringBuilder var14 = new StringBuilder();
      var14.append("jnic_decrypt((char[]) {");
      int var10000 = bytes.length;
      int var48 = 0;
      int var50 = var10000;

      while (var48 < var50) {
         byte var53 = bytes[var48];
         byte var55 = var12[var48];
         var14.append((var55 & 255 ^ var53 & 255) << 24 >> 24);
         var14.append(", ");
         int var74 = bytes.length;
         var10000 = var48 + 1;
         var50 = var74;
         var48 = var10000;
      }

      var14.append("0}, ");
      var14.append(var6);
      var14.append(", ");
      int var100 = bytes.length;
      var14.append(var100);
      String value104 = ")";
      var14.append(value104);
      return var14.toString();
   }

   private String characterArray(String text) {
      StringBuilder var2 = new StringBuilder();
      var2.append("(unsigned short[]) {");
      int var21 = 0;

      while (text != null) {
         int var22 = text.length();
         if (var21 >= var22) {
            String value24 = "}";
            var2.append(value24);
            return var2.toString();
         }

         if (var21 == 0) {
            String value26 = Integer.toString(text.charAt(0));
            var2.append(value26);
            var21 = 1;
         } else {
            var2.append(", ");
            String value41 = Integer.toString(text.charAt(var21));
            var2.append(value41);
            var21++;
         }
      }

      throw new NullPointerException("");
   }

   public String asciiString(String text) {
      if (!this.context.getOptions().isStringObfuscationEnabled()) {
         return "\"" + text + "\"";
      }

      Charset var10 = StandardCharsets.US_ASCII;
      byte[] var12 = text.getBytes(var10);
      return this.encryptedBytes(var12);
   }

   public String utf16String(String text) {
      if (!this.context.getOptions().isStringObfuscationEnabled()) {
         return this.characterArray(text);
      }

      Charset var10 = StandardCharsets.UTF_16LE;
      byte[] var12 = text.getBytes(var10);
      return this.encryptedBytes(var12);
   }
}
