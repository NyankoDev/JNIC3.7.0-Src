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
      int position = this.keystream.getPosition();
      ChaChaKeystream keystream2 = this.keystream;

      byte[] bytes;
      try {
         bytes = keystream2.readNBytes(4);
      } catch (IOException ioFailure) {
         throw new RuntimeException(ioFailure);
      }

      int keyWord = Pack.littleEndianToInt(bytes, 0);
      return "(" + (keyWord ^ index) + " ^ *((int32_t *) (jnic_buf + " + position + ")))";
   }

   private String encryptedBytes(byte[] bytes) {
      int position = this.keystream.getPosition();
      ChaChaKeystream keystream2 = this.keystream;
      int byteCount = bytes.length;

      byte[] bytes2;
      try {
         bytes2 = keystream2.readNBytes(byteCount);
      } catch (IOException ioFailure) {
         throw new RuntimeException(ioFailure);
      }

      StringBuilder builder = new StringBuilder();
      builder.append("jnic_decrypt((char[]) {");
      int byteCount2 = bytes.length;
      int index = 0;
      int byteCount3 = byteCount2;

      while (index < byteCount3) {
         byte byteValue = bytes[index];
         byte byteValue2 = bytes2[index];
         builder.append((byteValue2 & 255 ^ byteValue & 255) << 24 >> 24);
         builder.append(", ");
         int byteCount4 = bytes.length;
         byteCount2 = index + 1;
         byteCount3 = byteCount4;
         index = byteCount2;
      }

      builder.append("0}, ");
      builder.append(position);
      builder.append(", ");
      int byteCount5 = bytes.length;
      builder.append(byteCount5);
      String closingParenthesis = ")";
      builder.append(closingParenthesis);
      return builder.toString();
   }

   private String characterArray(String text) {
      StringBuilder builder = new StringBuilder();
      builder.append("(unsigned short[]) {");
      int characterIndex = 0;

      while (text != null) {
         int textLength = text.length();
         if (characterIndex >= textLength) {
            String literalText = "}";
            builder.append(literalText);
            return builder.toString();
         }

         if (characterIndex == 0) {
            String characterLiteral = Integer.toString(text.charAt(0));
            builder.append(characterLiteral);
            characterIndex = 1;
         } else {
            builder.append(", ");
            String characterLiteral2 = Integer.toString(text.charAt(characterIndex));
            builder.append(characterLiteral2);
            characterIndex++;
         }
      }

      throw new NullPointerException("");
   }

   public String asciiString(String text) {
      if (!this.context.getOptions().isStringObfuscationEnabled()) {
         return "\"" + text + "\"";
      }

      Charset usAscii = StandardCharsets.US_ASCII;
      byte[] encodedBytes = text.getBytes(usAscii);
      return this.encryptedBytes(encodedBytes);
   }

   public String utf16String(String text) {
      if (!this.context.getOptions().isStringObfuscationEnabled()) {
         return this.characterArray(text);
      }

      Charset utf16le = StandardCharsets.UTF_16LE;
      byte[] encodedBytes = text.getBytes(utf16le);
      return this.encryptedBytes(encodedBytes);
   }
}
