/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.math;

import java.math.BigInteger;

/**
 * Provides helper methods for conversions between various representations of
 * numbers/data.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ByteConversions {

  /**
   * Prevents creation of instances.
   */
  private ByteConversions() {
  }

  /**
   * Returns the hexadecimal representation of the given integer.
   *
   * @param num The integer to be represented.
   * @return The hexadecimal representation of the given integer.
   */
  public static String hexVal(int num) {
    return String.format("%02X", num);
  }

  /**
   * Returns the given byte's value interpreted as unsigned.
   *
   * @param input The byte to be interpreted as an unsigned value.
   * @return An <code>int</code> representing the given byte's value when
   * interpreted as unsigned.
   */
  public static int byteAsUnsigned(byte input) {
    return input & 0xff;
  }

  /**
   * Returns the given word (two bytes, big-endian) interpreted as an unsigned
   * value.
   *
   * @param bytes The word to be interpreted as an unsigned value.
   * <code>bytes[0]</code> will be used as the most significant byte,
   * <code>bytes[1]</code> as the least significant byte.
   * @return An <code>int</code> representing the given word when interpreted as
   * an unsigned value.
   */
  public static int wordAsUnsigned(byte[] bytes) {
    return wordAsUnsigned(bytes, 0);
  }

  /**
   * Returns two bytes of the given byte array interpreted as an
   * unsigned value.
   *
   * @param bytes The word to be interpreted as an unsigned value.
   * @param offset First of the two bytes that contain the number
   * @return An <code>int</code> representing the given word when interpreted as
   * an unsigned value.
   */
  public static int wordAsUnsigned(byte[] bytes, int offset) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length < offset + 2) {
      throw new IllegalArgumentException("bytes.length < " + (offset + 2));
    }

    // Interpret the bytes as unsigned values and put them together.
    int msb = byteAsUnsigned(bytes[offset]);      // MSB first/left
    int lsb = byteAsUnsigned(bytes[offset + 1]);  // LSB last/right

    return (msb << 8) + lsb;
  }

  /**
   * Returns the given word (two bytes, little-endian) interpreted as an
   * unsigned value.
   *
   * @param bytes The word to be interpreted as an unsigned value.
   * <code>bytes[1]</code> will be used as the most significant byte,
   * <code>bytes[0]</code> as the least significant byte.
   * @return An <code>int</code> representing the given word when interpreted as
   * an unsigned value.
   */
  public static int wordAsUnsignedLittleEndian(byte[] bytes) {
    return wordAsUnsignedLittleEndian(bytes, 0);
  }

  /**
   * Returns two bytes of the given byte array interpreted as an
   * unsigned value (little-endian).
   *
   * @param bytes The word to be interpreted as an unsigned value.
   * @param offset First of the two bytes that contain the number
   * @return An <code>int</code> representing the given word when interpreted as
   * an unsigned value.
   */
  public static int wordAsUnsignedLittleEndian(byte[] bytes, int offset) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length < offset + 2) {
      throw new IllegalArgumentException("bytes.length < " + (offset + 2));
    }

    // Interpret the bytes as unsigned values and put them together.
    int lsb = byteAsUnsigned(bytes[offset]);     // LSB first/left
    int msb = byteAsUnsigned(bytes[offset + 1]); // MSB last/right

    return (msb << 8) + lsb;
  }

  /**
   * Returns the given word (two bytes, big-endian) interpreted as a signed
   * value.
   *
   * @param bytes The word to be interpreted as a signed value.
   * <code>bytes[0]</code> will be used as the most significant byte,
   * <code>bytes[1]</code> as the least significant byte.
   * @return A <code>short</code> representing the given word when interpreted
   * as a signed value.
   */
  public static short wordAsSigned(byte[] bytes) {
    return wordAsSigned(bytes, 0);
  }

  /**
   * Returns the word (two bytes, big-endian) at the given offset in the given
   * byte array interpreted as a signed value.
   *
   * @param bytes The byte array containing the word to be interpreted as a
   * signed value.
   * <code>bytes[offset]</code> will be used as the most significant byte,
   * <code>bytes[offset + 1]</code> as the least significant byte.
   * @param offset The word's offset in the byte array.
   * @return A <code>short</code> representing the given word when interpreted
   * as a signed value.
   */
  public static short wordAsSigned(byte[] bytes, int offset) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length < offset + 2) {
      throw new IllegalArgumentException("bytes.length < " + (offset + 2));
    }

    // Interpret the bytes as unsigned values and put them together.
    short msb = (short) byteAsUnsigned(bytes[offset]);
    short lsb = (short) byteAsUnsigned(bytes[offset + 1]);

    return (short) ((msb << 8) + lsb);
  }

  /**
   * Returns the given word (two bytes, little-endian) interpreted as a
   * signed value.
   *
   * @param bytes The word to be interpreted as a signed value.
   * <code>bytes[1]</code> will be used as the most significant byte,
   * <code>bytes[0]</code> as the least significant byte.
   * @return A <code>short</code> representing the given word when interpreted
   * as a signed value.
   */
  public static short wordAsSignedLittleEndian(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length != 2) {
      throw new IllegalArgumentException("bytes.length != 2: " + bytes.length);
    }

    // Interpret the bytes as unsigned values and put them together.
    short lsb = (short) byteAsUnsigned(bytes[0]);
    short msb = (short) byteAsUnsigned(bytes[1]);

    return (short) ((msb << 8) + lsb);
  }

  /**
   * Returns the given double word (four bytes, big-endian) interpreted as an
   * unsigned value.
   *
   * @param bytes The double word to be interpreted as an unsigned value.
   * @return A <code>long</code> representing the given double word when
   * interpreted as an unsigned value.
   */
  public static long doubleWordAsUnsigned(byte[] bytes) {
    return doubleWordAsUnsigned(bytes, 0);
  }

  /**
   * Returns the double word (four bytes, big-endian) at the given offset in the
   * given byte array interpreted as an unsigned value.
   *
   * @param bytes The byte array containing the double word to be interpreted as
   * an unsigned value.
   * @param offset The double word's offset in the byte array.
   * @return A <code>long</code> representing the given double word when
   * interpreted as an unsigned value.
   */
  public static long doubleWordAsUnsigned(byte[] bytes, int offset) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length < offset + 4) {
      throw new IllegalArgumentException("bytes.length < " + (offset + 4));
    }

    // Interpret the bytes as unsigned values and put them together.
    long msb = byteAsUnsigned(bytes[offset]);
    long byte2 = byteAsUnsigned(bytes[offset + 1]);
    long byte3 = byteAsUnsigned(bytes[offset + 2]);
    long lsb = byteAsUnsigned(bytes[offset + 3]);

    return (msb << 24) + (byte2 << 16) + (byte3 << 8) + lsb;
  }

  /**
   * Returns the given double word (four bytes, little-endian) interpreted as an
   * unsigned value.
   *
   * @param bytes The double word to be interpreted as an unsigned value.
   * @return A <code>long</code> representing the given double word when
   * interpreted as an unsigned value.
   */
  public static long doubleWordAsUnsignedLittleEndian(byte[] bytes) {
    return doubleWordAsUnsignedLittleEndian(bytes, 0);
  }

  /**
   * Returns four bytes of the given byte array interpreted as an
   * unsigned value (four bytes, little-endian).
   *
   * @param bytes The double word to be interpreted as an unsigned value.
   * @param offset First of the four bytes that contain the number
   * @return A <code>long</code> representing the given double word when
   * interpreted as an unsigned value.
   */
  public static long doubleWordAsUnsignedLittleEndian(byte[] bytes, int offset) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length < offset + 4) {
      throw new IllegalArgumentException("bytes.length < " + (offset + 4));
    }

    // Interpret the bytes as unsigned values and put them together.
    long lsb = byteAsUnsigned(bytes[offset]);
    long byte3 = byteAsUnsigned(bytes[offset + 1]);
    long byte2 = byteAsUnsigned(bytes[offset + 2]);
    long msb = byteAsUnsigned(bytes[offset + 3]);

    return (msb << 24) + (byte2 << 16) + (byte3 << 8) + lsb;
  }

  /**
   * Returns the given double word (four bytes, big-endian) interpreted as a
   * signed value.
   *
   * @param bytes The double word to be interpreted as a signed value.
   * @return An <code>int</code> representing the given double word when
   * interpreted as a signed value.
   */
  public static int doubleWordAsSigned(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length != 4) {
      throw new IllegalArgumentException("bytes.length != 4: " + bytes.length);
    }

    // Interpret the bytes as unsigned values and put them together.
    int msb = byteAsUnsigned(bytes[0]);
    int byte2 = byteAsUnsigned(bytes[1]);
    int byte3 = byteAsUnsigned(bytes[2]);
    int lsb = byteAsUnsigned(bytes[3]);

    return (msb << 24) + (byte2 << 16) + (byte3 << 8) + lsb;
  }

  /**
   * Returns the given double word (four bytes, little-endian) interpreted as a
   * signed value.
   *
   * @param bytes The double word to be interpreted as a signed value.
   * @return An <code>int</code> representing the given double word when
   * interpreted as a signed value.
   */
  public static int doubleWordAsSignedLittleEndian(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    if (bytes.length != 4) {
      throw new IllegalArgumentException("bytes.length != 4: " + bytes.length);
    }

    // Interpret the bytes as unsigned values and put them together.
    int lsb = byteAsUnsigned(bytes[0]);
    int byte3 = byteAsUnsigned(bytes[1]);
    int byte2 = byteAsUnsigned(bytes[2]);
    int msb = byteAsUnsigned(bytes[3]);

    return (msb << 24) + (byte2 << 16) + (byte3 << 8) + lsb;
  }

  /**
   * Encodes the lowest two bytes of an integer to an unsigned word
   * (big-endian).
   *
   * @param value The integer to be encoded as an unsigned, big-endian word.
   * @return The lowest two bytes of the given integer in big-endian order
   * (i.e. the most significant byte first).
   */
  public static byte[] intToUnsignedWord(int value) {
    byte[] result = new byte[2];
    result[0] = (byte) ((value >>> 8) & 0xff);
    result[1] = (byte) (value & 0xff);

    return result;
  }

  /**
   * Encodes the lowest two bytes of an integer to an unsigned word
   * (little-endian).
   *
   * @param value The integer to be encoded as an unsigned, little-endian word.
   * @return The lowest two bytes of the given integer in little-endian order
   * (i.e. the least significant byte first).
   */
  public static byte[] intToUnsignedWordLittleEndian(int value) {
    byte[] result = new byte[2];
    result[1] = (byte) ((value >>> 8) & 0xff);
    result[0] = (byte) (value & 0xff);

    return result;
  }

  /**
   * Encodes the lowest two bytes of an integer to a signed word
   * (big-endian).
   *
   * @param value The integer to be encoded as a signed, big-endian word.
   * @return The lowest two bytes of the given integer in big-endian order
   * (i.e. the most significant byte first).
   */
  public static byte[] intToSignedWord(int value) {
    byte[] result = new byte[2];
    result[0] = (byte) ((value >> 8) & 0xff);
    result[1] = (byte) (value & 0xff);

    return result;
  }

  /**
   * Encodes the lowest two bytes of an integer to a signed word
   * (little-endian).
   *
   * @param value The integer to be encoded as an signed, little-endian word.
   * @return The lowest two bytes of the given integer in little-endian order
   * (i.e. the least significant byte first).
   */
  public static byte[] intToSignedWordLittleEndian(int value) {
    byte[] result = new byte[2];
    result[1] = (byte) ((value >> 8) & 0xff);
    result[0] = (byte) (value & 0xff);

    return result;
  }

  /**
   * Encodes the lowest four bytes of a long integer to an unsigned double-word
   * (big-endian).
   *
   * @param value The long integer to be encoded as an unsigned, big-endian
   * double-word.
   * @return The lowest four bytes of the given long integer in big-endian order
   * (i.e. the most significant byte first).
   */
  public static byte[] longToUnsignedDoubleWord(long value) {
    byte[] result = new byte[4];
    result[0] = (byte) ((value >>> 24) & 0xff);
    result[1] = (byte) ((value >>> 16) & 0xff);
    result[2] = (byte) ((value >>> 8) & 0xff);
    result[3] = (byte) (value & 0xff);

    return result;
  }

  /**
   * Encodes the lowest four bytes of a long integer to an unsigned double-word
   * (big-endian).
   *
   * @param value The long integer to be encoded as an unsigned, big-endian
   * double-word.
   * @return The lowest four bytes of the given long integer in big-endian order
   * (i.e. the most significant byte first).
   */
  public static byte[] longToUnsignedDoubleWordLittleEndian(long value) {
    byte[] result = new byte[4];
    result[0] = (byte) (value & 0xff);
    result[1] = (byte) ((value >>> 8) & 0xff);
    result[2] = (byte) ((value >>> 16) & 0xff);
    result[3] = (byte) ((value >>> 24) & 0xff);

    return result;
  }

  /**
   * Returns a byte array with the given integer values.
   *
   * @param intarray Array with integer numbers
   * @return A byte array with the given integer values.
   */
  public static byte[] intArrayToByteArray(int[] intarray) {
    byte[] ba = new byte[intarray.length];

    for (int i = 0; i < intarray.length; i++) {
      ba[i] = (byte) intarray[i];
    }

    return ba;
  }

  /**
   * Returns a byte array as a String, formatted as hexadecimal digits with
   * leading zeroes.
   *
   * @param bytes The bytes to be formatted hexadecimally.
   * @return A String, representing the given byte array in hexadecimal digits.
   */
  public static String byteArrayToHexString(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        result.append(' ');
      }

      result.append(String.format("%02X", Byte.valueOf(bytes[i])));
    }

    return result.toString();
  }

  /**
   * Returns an array of integer numbers as a String, formatted as decimal
   * digits with leading zeroes, separated by a space.
   *
   * @param integers The integer numbers to be formatted hexadecimally.
   * @return A String, representing the given byte array in hexadecimal digits.
   */
  public static String intArrayToString(int[] integers) {
    if (integers == null) {
      throw new NullPointerException("integers is null");
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < integers.length; i++) {
      if (i > 0) {
        result.append(' ');
      }

      result.append(integers[i]);
    }

    return result.toString();
  }

  /**
   * Returns an array of integer numbers as a String, formatted as hexadecimal
   * digits with leading zeroes, separated by a space.
   *
   * @param integers The integer numbers to be formatted hexadecimally.
   * @return A String, representing the given byte array in hexadecimal digits.
   */
  public static String intArrayToHexString(int[] integers) {
    return intArrayToHexString(integers, ' ');
  }

  /**
   * Returns an array of integer numbers as a String, formatted as hexadecimal
   * digits with leading zeroes, separated by the [separator].
   *
   * @param integers The integer numbers to be formatted hexadecimally.
   * @param separator The character to be used to separate the integers.
   * @return A String, representing the given byte array in hexadecimal digits.
   */
  public static String intArrayToHexString(int[] integers, char separator) {
    if (integers == null) {
      throw new NullPointerException("ints is null");
    }

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < integers.length; i++) {
      if (i > 0) {
        result.append(separator);
      }

      result.append(String.format("%02X", Integer.valueOf(integers[i])));
    }

    return result.toString();
  }

  /**
   * Returns a hex string as byte array.
   *
   * @param hexString The string to be represented as byte array.
   * @return A byte array, representing the given hexadecimal digits numeric.
   */
  public static byte[] hexStringToByteArray(String hexString) {
    byte[] result = new byte[hexString.length() / 2];

    for (int i = 0; i < (hexString.length() / 2); i++) {
      byte firstNibble =
          Byte.parseByte(hexString.substring(2 * i, 2 * i + 1), 16);
      byte secondNibble =
          Byte.parseByte(hexString.substring(2 * i + 1, 2 * i + 2), 16);
      byte finalByte = (byte) (secondNibble | (firstNibble << 4));
      result[i] = finalByte;
    }

    return result;
  }

  /**
   * Returns a decimal string as byte array.
   *
   * @param decString The string to be represented as byte array.
   * @return A byte array, representing the given decimal digits numeric.
   */
  public static byte[] decStringToByteArray(String decString) {
    byte[] result = new byte[decString.length() / 2];

    for (int i = 0; i < (decString.length() / 2); i++) {
      byte firstNibble =
          Byte.parseByte(decString.substring(2 * i, 2 * i + 1), 10);
      byte secondNibble =
          Byte.parseByte(decString.substring(2 * i + 1, 2 * i + 2), 10);
      byte finalByte = (byte) (secondNibble | (firstNibble << 4));
      result[i] = finalByte;
    }

    return result;
  }

  /**
   * Convert a byte array to a string of binary.
   * @param bytes The byte array to be converted.
   * @return The string of binary.
   */
  public static String byteToBinary(byte[] bytes) {
    String result = "";
    BigInteger integer = new BigInteger(bytes);
    result = integer.toString(2);

    return result;
  }

  /**
   * Returns the packed BCD representation of the four least significant
   * decimals of the given integer value as an array of (two) bytes.
   * <b>Fehlerhaft!</b> 2010-02-05 H.Huber
   *
   * @param value The value for which to return the packed BCD representation.
   * @return An array of two bytes containing the packed BCD representation of
   * the four least significant decimals of the given integer value.
   */
  public static byte[] intTo4PackedBCD(int value) {
    byte[] result = new byte[2];
    byte curByte;
    int curValue;

    curByte = (byte) (value % 10);
    curValue = value / 10;
    curByte |= (value % 10) << 4;
    result[1] = curByte;
    curValue /= 10;
    curByte = (byte) (value % 10);
    curValue /= 10;
    curByte |= (value % 10) << 4;
    result[0] = curByte;

    return result;
  }
}
