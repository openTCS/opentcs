/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication.tcp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Takes a stream of bytes and constructs objects from it.
 *
 * @param <E> The type of objects created from the byte stream.
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class ByteSequenceDissector<E> {
  /**
   * Unprocessed data left from previous calls of
   * <code>addIncomingBytes()</code>.
   */
  protected byte[] processingData = new byte[0];
  /**
   * A queue of objects constructed from the byte stream received so far.
   */
  private final Queue<E> objects = new LinkedList<>();

  /**
   * Adds data to this dissector's internal buffer.
   *
   * @param newData The data to be added.
   */
  public abstract void addIncomingBytes(byte[] newData);

  /**
   * Clears the internal buffer.
   */
  public final void reset() {
    processingData = new byte[0];
  }

  /**
   * Checks if this dissector has identified/parsed any objects in the byte
   * stream.
   *
   * @return <code>true</code> if, and only if, there are objects in the
   * queue.
   */
  public final boolean hasObjects() {
    synchronized (objects) {
      return !objects.isEmpty();
    }
  }

  /**
   * Returns the next object in the queue (if any).
   *
   * @return The next object in the queue, or <code>null</code>, if this
   * dissector's queue is empty.
   */
  public final E getNextObject() {
    synchronized (objects) {
      return objects.poll();
    }
  }

  /**
   * Adds an object to this tokenizer's queue.
   *
   * @param newObject The object to be added to the queue.
   */
  protected final void addObject(E newObject) {
    if (newObject == null) {
      throw new NullPointerException("newObject is null");
    }

    synchronized (objects) {
      objects.add(newObject);
    }
  }

  /**
   * Merges the given data with the data we already have and returns the result.
   *
   * @param newData The new data to be appended.
   * @return The merged data.
   */
  protected final byte[] mergeData(byte[] newData) {
    if (newData == null) {
      throw new NullPointerException("newData is null");
    }

    // If there's data left from the previous call, merge it with the new data.
    int offset = processingData.length;
    byte[] data;

    if (offset != 0) {
      int length = newData.length + offset;
      data = new byte[length];
      System.arraycopy(processingData, 0, data, 0, offset);
      System.arraycopy(newData, 0, data, offset, newData.length);
    }
    else {
      data = Arrays.copyOf(newData, newData.length);
    }

    return data;
  }
}
