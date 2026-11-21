package dev.jnic.crypto;

import java.math.BigInteger;
import java.util.Arrays;

public final class RsaEngine {
   private BigIntegerValue modulus;
   private BigIntegerValue exponent;

   public void initialize(BigIntegerValue modulus, BigIntegerValue exponent) {
      this.modulus = modulus;
      this.exponent = exponent;
   }

   public int inputSize() {
      return (this.modulus.asBigInteger().bitLength() + 7) / 8 - 1;
   }

   public int outputSize() {
      return (this.modulus.asBigInteger().bitLength() + 7) / 8;
   }

   public BigIntegerValue convertInput(byte[] input, int offset, int length) {
      byte[] bytes = input;
      if (offset != 0 || length != input.length) {
         bytes = new byte[length];
         System.arraycopy(input, offset, bytes, 0, length);
      }

      return new BigIntegerValue(new BigInteger(1, bytes));
   }

   public byte[] convertOutput(BigIntegerValue value) {
      byte[] result = value.asBigInteger().toByteArray();
      int size = this.outputSize();
      if (result.length > size && result[0] == 0) {
         return Arrays.copyOfRange(result, 1, result.length);
      } else if (result.length < size) {
         byte[] padded = new byte[size];
         System.arraycopy(result, 0, padded, size - result.length, result.length);
         return padded;
      } else {
         return result;
      }
   }

   public BigIntegerValue apply(BigIntegerValue input) {
      return BigIntegerValue.fromBigInteger(input.asBigInteger().modPow(this.exponent.asBigInteger(), this.modulus.asBigInteger()));
   }

   public byte[] process(byte[] bytes, int index) {
      return this.convertOutput(this.apply(this.convertInput(bytes, 0, index)));
   }
}
