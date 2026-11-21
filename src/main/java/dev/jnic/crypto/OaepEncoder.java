package dev.jnic.crypto;

import java.util.Random;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.util.Pack;

public class OaepEncoder {
   private final byte[] labelHash;
   private final SHA1Digest digest;
   private final RsaEngine engine;
   private final Random random;
   private static final String INPUT_TOO_LONG = "input data too long";

   public OaepEncoder(RsaEngine engine, Random random) {
      this.engine = engine;
      this.digest = new SHA1Digest();
      this.labelHash = new byte[]{-38, 57, -93, -18, 94, 107, 75, 13, 50, 85, -65, -17, -107, 96, 24, -112, -81, -40, 7, 9};
      this.random = random;
   }

   public int inputSize() {
      int var10000 = this.engine.inputSize();
      int var4 = this.labelHash.length;
      return var10000 + ~(var4 + var4);
   }

   public int outputSize() {
      return this.engine.outputSize();
   }

   public byte[] encodeBlock(byte[] bytes, int index, int index2) {
      if (index2 > this.inputSize()) {
         throw new IllegalArgumentException("input data too long");
      }

      byte[] var4 = new byte[this.inputSize() + 1 + 2 * this.labelHash.length];
      System.arraycopy(bytes, index, var4, var4.length - index2, index2);
      var4[var4.length - index2 - 1] = 1;
      System.arraycopy(this.labelHash, 0, var4, this.labelHash.length, this.labelHash.length);
      byte[] var5 = new byte[this.labelHash.length];
      this.random.nextBytes(var5);
      byte[] var6 = this.maskGenerationFunction(var5, 0, var5.length, var4.length - this.labelHash.length);

      for (int var7 = this.labelHash.length; var7 != var4.length; var7++) {
         var4[var7] ^= var6[var7 - this.labelHash.length];
      }

      System.arraycopy(var5, 0, var4, 0, this.labelHash.length);
      byte[] var8 = this.maskGenerationFunction(var4, this.labelHash.length, var4.length - this.labelHash.length, this.labelHash.length);

      for (int var9 = 0; var9 != this.labelHash.length; var9++) {
         var4[var9] ^= var8[var9];
      }

      return this.engine.process(var4, var4.length);
   }

   private byte[] maskGenerationFunction(byte[] bytes, int index, int index2, int index3) {
      byte[] var5 = new byte[index3];
      byte[] var6 = new byte[this.digest.getDigestSize()];
      byte[] var7 = new byte[4];
      int var8 = 0;
      this.digest.reset();

      while (var8 < index3 / var6.length) {
         Pack.intToBigEndian(var8, var7, 0);
         this.digest.update(bytes, index, index2);
         this.digest.update(var7, 0, var7.length);
         this.digest.doFinal(var6, 0);
         System.arraycopy(var6, 0, var5, var8 * var6.length, var6.length);
         var8++;
      }

      if (var8 * var6.length < index3) {
         Pack.intToBigEndian(var8, var7, 0);
         this.digest.update(bytes, index, index2);
         this.digest.update(var7, 0, var7.length);
         this.digest.doFinal(var6, 0);
         System.arraycopy(var6, 0, var5, var8 * var6.length, var5.length - var8 * var6.length);
      }

      return var5;
   }
}
