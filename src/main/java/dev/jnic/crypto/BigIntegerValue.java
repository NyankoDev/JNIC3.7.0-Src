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
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.add(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue subtract(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.subtract(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue multiply(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.multiply(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue divide(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.divide(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue[] divideAndRemainder(BigIntegerValue bigIntegerValue) {
      return wrapResults(this.asBigInteger().divideAndRemainder(bigIntegerValue.asBigInteger()));
   }

   public BigIntegerValue remainder(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.remainder(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue pow(int index) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.pow(index), leftOperand, this, null, null);
   }

   public BigIntegerValue gcd(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.gcd(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue abs() {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.abs(), leftOperand, this, null, null);
   }

   public BigIntegerValue negate() {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.negate(), leftOperand, this, null, null);
   }

   public int signum() {
      return this.asBigInteger().signum();
   }

   public BigIntegerValue mod(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.mod(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue modPow(BigIntegerValue bigIntegerValue, BigIntegerValue bigIntegerValue2) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.modPow(rightOperand = bigIntegerValue.asBigInteger(), bigIntegerValue2.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue modInverse(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.modInverse(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue shiftLeft(int index) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.shiftLeft(index), leftOperand, this, null, null);
   }

   public BigIntegerValue shiftRight(int index) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.shiftRight(index), leftOperand, this, null, null);
   }

   public BigIntegerValue and(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.and(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue or(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.or(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue xor(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.xor(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue not() {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.not(), leftOperand, this, null, null);
   }

   public BigIntegerValue andNot(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.andNot(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public boolean testBit(int index) {
      return this.asBigInteger().testBit(index);
   }

   public BigIntegerValue setBit(int bit) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.setBit(bit), leftOperand, this, null, null);
   }

   public BigIntegerValue clearBit(int index) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.clearBit(index), leftOperand, this, null, null);
   }

   public BigIntegerValue flipBit(int index) {
      BigInteger leftOperand = this.asBigInteger();
      return wrapResult(leftOperand.flipBit(index), leftOperand, this, null, null);
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
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.min(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
   }

   public BigIntegerValue max(BigIntegerValue bigIntegerValue) {
      BigInteger leftOperand = this.asBigInteger();
      BigInteger rightOperand;
      return wrapResult(leftOperand.max(rightOperand = bigIntegerValue.asBigInteger()), leftOperand, this, rightOperand, bigIntegerValue);
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
