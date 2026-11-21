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
      int inputSize = this.engine.inputSize();
      int labelHashCount = this.labelHash.length;
      return inputSize + ~(labelHashCount + labelHashCount);
   }

   public int outputSize() {
      return this.engine.outputSize();
   }

   public byte[] encodeBlock(byte[] bytes, int index, int index2) {
      if (index2 > this.inputSize()) {
         throw new IllegalArgumentException("input data too long");
      }

      byte[] encodingBuffer = new byte[this.inputSize() + 1 + 2 * this.labelHash.length];
      System.arraycopy(bytes, index, encodingBuffer, encodingBuffer.length - index2, index2);
      encodingBuffer[encodingBuffer.length - index2 - 1] = 1;
      System.arraycopy(this.labelHash, 0, encodingBuffer, this.labelHash.length, this.labelHash.length);
      byte[] encodingBuffer2 = new byte[this.labelHash.length];
      this.random.nextBytes(encodingBuffer2);
      byte[] mask = this.maskGenerationFunction(encodingBuffer2, 0, encodingBuffer2.length, encodingBuffer.length - this.labelHash.length);

      for (int labelHashCount = this.labelHash.length; labelHashCount != encodingBuffer.length; labelHashCount++) {
         encodingBuffer[labelHashCount] ^= mask[labelHashCount - this.labelHash.length];
      }

      System.arraycopy(encodingBuffer2, 0, encodingBuffer, 0, this.labelHash.length);
      byte[] mask2 = this.maskGenerationFunction(encodingBuffer, this.labelHash.length, encodingBuffer.length - this.labelHash.length, this.labelHash.length);

      for (int index3 = 0; index3 != this.labelHash.length; index3++) {
         encodingBuffer[index3] ^= mask2[index3];
      }

      return this.engine.process(encodingBuffer, encodingBuffer.length);
   }

   private byte[] maskGenerationFunction(byte[] bytes, int index, int index2, int index3) {
      byte[] encodingBuffer = new byte[index3];
      byte[] encodingBuffer2 = new byte[this.digest.getDigestSize()];
      byte[] encodingBuffer3 = new byte[4];
      int blockCounter = 0;
      this.digest.reset();

      while (blockCounter < index3 / encodingBuffer2.length) {
         Pack.intToBigEndian(blockCounter, encodingBuffer3, 0);
         this.digest.update(bytes, index, index2);
         this.digest.update(encodingBuffer3, 0, encodingBuffer3.length);
         this.digest.doFinal(encodingBuffer2, 0);
         System.arraycopy(encodingBuffer2, 0, encodingBuffer, blockCounter * encodingBuffer2.length, encodingBuffer2.length);
         blockCounter++;
      }

      if (blockCounter * encodingBuffer2.length < index3) {
         Pack.intToBigEndian(blockCounter, encodingBuffer3, 0);
         this.digest.update(bytes, index, index2);
         this.digest.update(encodingBuffer3, 0, encodingBuffer3.length);
         this.digest.doFinal(encodingBuffer2, 0);
         System.arraycopy(encodingBuffer2, 0, encodingBuffer, blockCounter * encodingBuffer2.length, encodingBuffer.length - blockCounter * encodingBuffer2.length);
      }

      return encodingBuffer;
   }
}
