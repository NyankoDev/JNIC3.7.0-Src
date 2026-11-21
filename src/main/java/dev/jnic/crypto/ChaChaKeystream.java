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
      byte[] var7 = text.getBytes(StandardCharsets.US_ASCII);
      if (var7.length != 16) {
         throw new IllegalArgumentException("invalid magic length");
      }

      Pack.littleEndianToInt(var7, 0, this.state, 0, 4);
      Pack.littleEndianToInt(key, 0, this.state, 4, 8);
      this.state[12] = 0;
      Pack.littleEndianToInt(nonce, 0, this.state, 13, 3);
   }

   public static ChaChaKeystream create(String text, SecureRandom secureRandom) {
      byte[] var2 = new byte[32];
      byte[] var3 = new byte[12];
      secureRandom.nextBytes(var2);
      secureRandom.nextBytes(var3);
      return new ChaChaKeystream(text, var2, var3);
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
         int[] var9 = this.state;
         int var11 = var9[12]++;
         this.blockOffset = 0;
         int var15 = this.blockOffset;
         int var17 = Math.min(index2, 64 - var15);
         int var21 = this.blockOffset;
         System.arraycopy((Object)this.block, var21, (Object)bytes, index, var17);
         int var24 = this.blockOffset;
         this.blockOffset = var24 + var17;
         int var27 = this.position;
         this.position = var27 + var17;
         return var17;
      } else {
         int var8 = this.blockOffset;
         int var10 = Math.min(index2, 64 - var8);
         int var14 = this.blockOffset;
         System.arraycopy((Object)this.block, var14, (Object)bytes, index, var10);
         int var28 = this.blockOffset;
         this.blockOffset = var28 + var10;
         int var20 = this.position;
         this.position = var20 + var10;
         return var10;
      }
   }

   @Override
   public int read() {
      int var3 = this.blockOffset;
      if (64 <= var3) {
         this.generateBlock();
         int[] var6 = this.state;
         int var8 = var6[12]++;
         this.blockOffset = 0;
         int var12 = this.position++;
         byte[] var21 = this.block;
         int var17 = this.blockOffset++;
         byte var20 = var21[var17];
         return 0xFF & (var20 & 0xFF) << 24 >> 24;
      } else {
         int var5 = this.position++;
         byte[] var10000 = this.block;
         int var10 = this.blockOffset++;
         byte var13 = var10000[var10];
         return 0xFF & (var13 & 0xFF) << 24 >> 24;
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
