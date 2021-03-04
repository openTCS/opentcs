/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * This class provides helper methods for working with streams.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Streams {

  /**
   * The initial size for a growing buffer.
   */
  private static final int INITIAL_BUFFER_SIZE = 10000;

  /**
   * Creates a new Streams.
   */
  private Streams() {
  }

  /**
   * Reads an <code>InputStream</code> until it recognizes a given end tag, and
   * returns a new <code>InputStream</code> containing only the bytes up to and
   * including that end tag.
   *
   * @param inStream The stream to read from.
   * @param endTag The sequence of bytes marking the end of the content to be
   * returned.
   * @param includeEndTag Whether or not to include the end tag in the result.
   * @return A new <code>InputStream</code>, containing only the bytes up to and
   * including the end tag, or <code>null</code>, if the end of the stream was
   * reached and no such tag was found. (The bytes read from the stream are
   * discarded.)
   * @throws IOException If an I/O error occurs while reading from the input
   * stream.
   */
  public static InputStream getInputStreamToEndTag(InputStream inStream,
                                                   byte[] endTag,
                                                   boolean includeEndTag)
      throws IOException {
    requireNonNull(inStream, "inStream");
    requireNonNull(endTag, "endTag");
    checkInRange(endTag.length, 1, Integer.MAX_VALUE, "endTag.length");

    SearchableByteArrayOutputStream localBuffer = new SearchableByteArrayOutputStream();
    boolean endTagFound = false;
    int currentByte = inStream.read();
    while (!endTagFound && currentByte != -1) {
      localBuffer.write(currentByte);
      endTagFound = localBuffer.endsWith(endTag);
      currentByte = inStream.read();
    }

    if (endTagFound) {
      ByteArrayInputStream result;
      byte[] input = localBuffer.toByteArray();

      if (includeEndTag) {
        result = new ByteArrayInputStream(input);
      }
      else {
        result = new ByteArrayInputStream(input, 0, input.length - endTag.length);
      }

      return result;
    }
    else {
      return null;
    }
  }

  /**
   * Reads an <code>InputStream</code> to the end and returns an array
   * containing all bytes read.
   * The given <code>InputStream</code> will be closed when this method returns.
   *
   * @param inStream The stream to read from.
   * @return An array containing all bytes read from the given
   * <code>InputStream</code>. If nothing was read, the returned array's length
   * will be 0.
   * @throws IOException If an I/O error occurs while reading from the input
   * stream.
   */
  public static byte[] getCompleteInputStream(InputStream inStream)
      throws IOException {
    requireNonNull(inStream, "inStream");

    int bufferSize = INITIAL_BUFFER_SIZE;
    int offset = 0;
    byte[] buffer = new byte[bufferSize];

    try {
      int bytesRead = inStream.read(buffer, offset, bufferSize - offset);

      while (bytesRead != -1) {
        offset += bytesRead;
        // Enlarge the buffer if it's full.
        if (offset == bufferSize) {
          bufferSize *= 2;
          buffer = Arrays.copyOf(buffer, bufferSize);
        }

        bytesRead = inStream.read(buffer, offset, bufferSize - offset);
      }
    }
    finally {
      inStream.close();
    }
    // Shrink the buffer to the actual size of the data read.
    if (offset < bufferSize) {
      buffer = Arrays.copyOf(buffer, offset);
    }

    return buffer;
  }

  /**
   * A {@code ByteArrayOutputStream} providing additional methods to scan its
   * content for byte sequences.
   */
  private static class SearchableByteArrayOutputStream
      extends ByteArrayOutputStream {

    /**
     * Creates a new SearchableByteArrayOutputStream.
     */
    public SearchableByteArrayOutputStream() {
    }

    /**
     * Checks if this stream currently ends with the given byte sequence.
     *
     * @param endTag The byte sequence to check for.
     * @return {@code true} if, and only if, this stream currently ends with the
     * given byte sequence.
     */
    public boolean endsWith(byte[] endTag) {
      boolean result = false;

      if (count >= endTag.length) {
        result = true;

        for (int i = 1; i <= endTag.length; i++) {
          if (endTag[endTag.length - i] != buf[count - i]) {
            result = false;
            break;
          }
        }
      }

      return result;
    }
  }
}
