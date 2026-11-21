package dev.jnic.crypto;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.bouncycastle.util.Pack;

public class ChaChaKeystream extends InputStream {
   private final byte[] key;
   private final byte[] nonce;
   private final int[] state = new int[32];
   private final byte[] block = new byte[64];
   private int blockOffset = 64;
   private int position = 0;

   private ChaChaKeystream(String text, byte[] key, byte[] nonce) {
      if (key.length != 32) {
         throw new IllegalArgumentException("invalid key length " + key.length);
      }

      if (nonce.length != 12) {
         throw new IllegalArgumentException("invalid nonce length " + nonce.length);
      }

      this.key = key;
      this.nonce = nonce;
      byte[] magicBytes = text.getBytes(StandardCharsets.US_ASCII);
      if (magicBytes.length != 16) {
         throw new IllegalArgumentException("invalid magic length");
      }

      Pack.littleEndianToInt(magicBytes, 0, this.state, 0, 4);
      Pack.littleEndianToInt(key, 0, this.state, 4, 8);
      this.state[12] = 0;
      Pack.littleEndianToInt(nonce, 0, this.state, 13, 3);
   }

   public static ChaChaKeystream create(String text, SecureRandom secureRandom) {
      byte[] buffer = new byte[32];
      byte[] buffer2 = new byte[12];
      secureRandom.nextBytes(buffer);
      secureRandom.nextBytes(buffer2);
      return new ChaChaKeystream(text, buffer, buffer2);
   }

   public byte[] getKey() {
      return this.key;
   }

   public byte[] getNonce() {
      return this.nonce;
   }

   @Override
   public int read(byte[] bytes, int index, int index2) {
      if (this.blockOffset >= 64) {
         this.generateBlock();
         int[] state2 = this.state;
         int previousWord = state2[12]++;
         this.blockOffset = 0;
         int blockOffset2 = this.blockOffset;
         int readCount = Math.min(index2, 64 - blockOffset2);
         int blockOffset3 = this.blockOffset;
         System.arraycopy((Object)this.block, blockOffset3, (Object)bytes, index, readCount);
         int blockOffset4 = this.blockOffset;
         this.blockOffset = blockOffset4 + readCount;
         int position2 = this.position;
         this.position = position2 + readCount;
         return readCount;
      } else {
         int blockOffset5 = this.blockOffset;
         int readCount2 = Math.min(index2, 64 - blockOffset5);
         int blockOffset6 = this.blockOffset;
         System.arraycopy((Object)this.block, blockOffset6, (Object)bytes, index, readCount2);
         int blockOffset7 = this.blockOffset;
         this.blockOffset = blockOffset7 + readCount2;
         int position3 = this.position;
         this.position = position3 + readCount2;
         return readCount2;
      }
   }

   @Override
   public int read() {
      int blockOffset2 = this.blockOffset;
      if (64 <= blockOffset2) {
         this.generateBlock();
         int[] state2 = this.state;
         int previousWord = state2[12]++;
         this.blockOffset = 0;
         int previousPosition = this.position++;
         byte[] block2 = this.block;
         int previousBlockOffset = this.blockOffset++;
         byte byteValue = block2[previousBlockOffset];
         return 0xFF & (byteValue & 0xFF) << 24 >> 24;
      } else {
         int previousPosition2 = this.position++;
         byte[] block3 = this.block;
         int previousBlockOffset2 = this.blockOffset++;
         byte byteValue2 = block3[previousBlockOffset2];
         return 0xFF & (byteValue2 & 0xFF) << 24 >> 24;
      }
   }

   public int getPosition() {
      return this.position;
   }

   private void generateBlock() {
      int[] working = new int[16];

      for (int index = 0; index < 16; index++) {
         working[index] = this.state[index];
      }

      for (int round = 0; round < 10; round++) {
         quarterRound(working, 0, 4, 8, 12);
         quarterRound(working, 1, 5, 9, 13);
         quarterRound(working, 2, 6, 10, 14);
         quarterRound(working, 3, 7, 11, 15);
         quarterRound(working, 0, 5, 10, 15);
         quarterRound(working, 1, 6, 11, 12);
         quarterRound(working, 2, 7, 8, 13);
         quarterRound(working, 3, 4, 9, 14);
      }

      for (int index = 0; index < 16; index++) {
         int value = working[index] + this.state[index];
         int offset = index * 4;
         this.block[offset] = (byte)value;
         this.block[offset + 1] = (byte)(value >>> 8);
         this.block[offset + 2] = (byte)(value >>> 16);
         this.block[offset + 3] = (byte)(value >>> 24);
      }
   }

   private static void quarterRound(int[] working, int a, int b, int c, int d) {
      working[a] += working[b];
      working[d] = Integer.rotateLeft(working[d] ^ working[a], 16);
      working[c] += working[d];
      working[b] = Integer.rotateLeft(working[b] ^ working[c], 12);
      working[a] += working[b];
      working[d] = Integer.rotateLeft(working[d] ^ working[a], 8);
      working[c] += working[d];
      working[b] = Integer.rotateLeft(working[b] ^ working[c], 7);
   }
}
