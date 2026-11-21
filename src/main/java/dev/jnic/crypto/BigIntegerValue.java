package dev.jnic.crypto;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

public final class BigIntegerValue {
   private static final BigIntegerValue[] POSITIVE_CACHE = new BigIntegerValue[17];
   private static final BigIntegerValue[] NEGATIVE_CACHE = new BigIntegerValue[17];
   public static final BigIntegerValue ZERO = new BigIntegerValue(BigInteger.ZERO);
   public static final BigIntegerValue ONE;
   public static final BigIntegerValue TEN;
   final int sign;
   final int[] magnitude;

   public BigIntegerValue(BigInteger value) {
      Objects.requireNonNull(value);
      this.sign = value.signum();
      BigInteger magnitude = value.abs();
      byte[] bytes = magnitude.toByteArray();
      this.magnitude = new int[magnitude.bitLength() + 31 >>> 5];

      for (int i = 0; i < bytes.length; i++) {
         int index = bytes.length - 1 - i;
         if (i / 4 < this.magnitude.length) {
            this.magnitude[this.magnitude.length - 1 - i / 4] = this.magnitude[this.magnitude.length - 1 - i / 4] | (bytes[index] & 255) << i % 4 * 8;
         }
      }
   }

   BigIntegerValue(int[] magnitude, int sign) {
      this.magnitude = Objects.requireNonNull(magnitude);
      this.sign = magnitude.length == 0 ? 0 : sign;
   }

   public BigInteger asBigInteger() {
      byte[] bytes = new byte[this.magnitude.length * 4];

      for (int i = 0; i < this.magnitude.length; i++) {
         for (int j = 0; j < 4; j++) {
            bytes[i * 4 + j] = (byte)(this.magnitude[i] >>> 24 - j * 8);
         }
      }

      return new BigInteger(this.sign, bytes);
   }

   public static BigIntegerValue fromBigInteger(BigInteger value) {
      if (value == BigInteger.ZERO) {
         return ZERO;
      } else if (value == BigInteger.ONE) {
         return ONE;
      } else {
         return value == BigInteger.TEN ? TEN : new BigIntegerValue(value);
      }
   }

   public static BigIntegerValue cachedValue(long value) {
      if (value == 0L) {
         return ZERO;
      } else if (value > 0L && value < POSITIVE_CACHE.length) {
         return POSITIVE_CACHE[(int)value];
      } else {
         return value < 0L && value > -NEGATIVE_CACHE.length ? NEGATIVE_CACHE[(int)(-value)] : new BigIntegerValue(BigInteger.valueOf(value));
      }
   }

   public static BigIntegerValue wrapResult(BigInteger result, BigInteger left, BigIntegerValue originalLeft, BigInteger right, BigIntegerValue originalRight) {
      if (result == left) {
         return originalLeft;
      } else {
         return result == right ? originalRight : fromBigInteger(result);
      }
   }

   public static BigIntegerValue[] wrapResults(BigInteger[] values) {
      BigIntegerValue[] result = new BigIntegerValue[values.length];

      for (int i = 0; i < result.length; i++) {
         result[i] = fromBigInteger(values[i]);
      }

      return result;
   }

   public boolean equalsValue(Object other) {
      if (other instanceof BigIntegerValue) {
         BigIntegerValue value = (BigIntegerValue)other;
         if (this.asBigInteger().equals(value.asBigInteger())) {
            return true;
         }
      }

      return false;
   }

   static {
      for (int i = 1; i < POSITIVE_CACHE.length; i++) {
         POSITIVE_CACHE[i] = new BigIntegerValue(BigInteger.valueOf(i));
         NEGATIVE_CACHE[i] = new BigIntegerValue(BigInteger.valueOf(-i));
      }

      ONE = POSITIVE_CACHE[1];
      TEN = POSITIVE_CACHE[10];
   }

   public BigIntegerValue add(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.add(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue subtract(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.subtract(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue multiply(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.multiply(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue divide(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.divide(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue[] divideAndRemainder(BigIntegerValue bigIntegerValue) {
      return wrapResults(this.asBigInteger().divideAndRemainder(bigIntegerValue.asBigInteger()));
   }

   public BigIntegerValue remainder(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.remainder(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue pow(int index) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.pow(index), var2, this, null, null);
   }

   public BigIntegerValue gcd(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.gcd(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue abs() {
      BigInteger var1 = this.asBigInteger();
      return wrapResult(var1.abs(), var1, this, null, null);
   }

   public BigIntegerValue negate() {
      BigInteger var1 = this.asBigInteger();
      return wrapResult(var1.negate(), var1, this, null, null);
   }

   public int signum() {
      return this.asBigInteger().signum();
   }

   public BigIntegerValue mod(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.mod(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue modPow(BigIntegerValue bigIntegerValue, BigIntegerValue bigIntegerValue2) {
      BigInteger var3 = this.asBigInteger();
      BigInteger var4;
      return wrapResult(var3.modPow(var4 = bigIntegerValue.asBigInteger(), bigIntegerValue2.asBigInteger()), var3, this, var4, bigIntegerValue);
   }

   public BigIntegerValue modInverse(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.modInverse(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue shiftLeft(int index) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.shiftLeft(index), var2, this, null, null);
   }

   public BigIntegerValue shiftRight(int index) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.shiftRight(index), var2, this, null, null);
   }

   public BigIntegerValue and(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.and(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue or(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.or(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue xor(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.xor(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue not() {
      BigInteger var1 = this.asBigInteger();
      return wrapResult(var1.not(), var1, this, null, null);
   }

   public BigIntegerValue andNot(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.andNot(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public boolean testBit(int index) {
      return this.asBigInteger().testBit(index);
   }

   public BigIntegerValue setBit(int bit) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.setBit(bit), var2, this, null, null);
   }

   public BigIntegerValue clearBit(int index) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.clearBit(index), var2, this, null, null);
   }

   public BigIntegerValue flipBit(int index) {
      BigInteger var2 = this.asBigInteger();
      return wrapResult(var2.flipBit(index), var2, this, null, null);
   }

   public int getLowestSetBit() {
      return this.asBigInteger().getLowestSetBit();
   }

   public int bitLength() {
      return this.asBigInteger().bitLength();
   }

   public int bitCount() {
      return this.asBigInteger().bitCount();
   }

   public int compareTo(BigIntegerValue bigIntegerValue) {
      return this.asBigInteger().compareTo(bigIntegerValue.asBigInteger());
   }

   public BigIntegerValue min(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.min(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   public BigIntegerValue max(BigIntegerValue bigIntegerValue) {
      BigInteger var2 = this.asBigInteger();
      BigInteger var3;
      return wrapResult(var2.max(var3 = bigIntegerValue.asBigInteger()), var2, this, var3, bigIntegerValue);
   }

   @Override
   public int hashCode() {
      return this.asBigInteger().hashCode();
   }

   public String toString(int index) {
      return this.asBigInteger().toString(index);
   }

   @Override
   public String toString() {
      return this.asBigInteger().toString();
   }

   public byte[] toByteArray() {
      return this.asBigInteger().toByteArray();
   }

   public int intValue() {
      return this.asBigInteger().intValue();
   }

   public long longValue() {
      return this.asBigInteger().longValue();
   }

   public long longValueExact() {
      return this.asBigInteger().longValueExact();
   }

   public int intValueExact() {
      return this.asBigInteger().intValueExact();
   }

   @Override
   public boolean equals(Object value) {
      return this.equalsValue(value);
   }

   public static BigIntegerValue valueOf(long value) {
      return cachedValue(value);
   }

   public BigIntegerValue(byte[] bytes) {
      this(new BigInteger(bytes));
   }

   public BigIntegerValue(int index, byte[] bytes) {
      this(new BigInteger(index, bytes));
   }

   public BigIntegerValue(String text, int index) {
      this(new BigInteger(text, index));
   }

   public BigIntegerValue(String text) {
      this(new BigInteger(text));
   }

   public BigIntegerValue(int index, Random random) {
      this(new BigInteger(index, random));
   }
}
