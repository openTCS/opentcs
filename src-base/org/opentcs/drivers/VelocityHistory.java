/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.logging.Logger;

/**
 * A <code>VelocityListener</code> that keeps velocity values in a ring buffer.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
public final class VelocityHistory {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(VelocityHistory.class.getName());
  /**
   * The actual ring buffer, containing the velocity values.
   */
  private final int[] velocities;
  /**
   * The ring buffer's capacity.
   */
  private final int velocityQueueCapacity;
  /**
   * A divisor for selecting the values that are actually written to the ring
   * buffer.
   */
  private final int divisor;
  /**
   * A counter for the number of velocity values that have been given to this
   * listener so far.
   */
  private int valueCounter;
  /**
   * The index of element in the ring buffer to receive the next value.
   */
  private int writeIndex;

  /**
   * Creates a new instance of VelocityQueueListener.
   *
   * @param queueCapacity This listener's queue capacity.
   * @param newDivisor A divisor for selecting values actually
   * written to the ring buffer. (I.e. only every <em>n</em>th value provided
   * via {@link #addVelocityValue(int) addVelocityValue()} will be written
   * to the ring buffer, where <em>n</em> = <code>newDivisor</code>.
   */
  public VelocityHistory(int queueCapacity, int newDivisor) {
    log.finer("method entry");
    if (queueCapacity < 1) {
      throw new IllegalArgumentException("queueCapacity is less than 1");
    }
    if (newDivisor < 1) {
      throw new IllegalArgumentException("newDivisor is less than 1");
    }
    velocityQueueCapacity = queueCapacity;
    divisor = newDivisor;
    velocities = new int[queueCapacity];
  }

  /**
   * Adds a new velocity value to this history.
   *
   * @param newValue The value to be added.
   */
  public void addVelocityValue(int newValue) {
    log.finer("method entry");
    synchronized (velocities) {
      if (valueCounter % divisor == 0) {
        velocities[writeIndex] = newValue;
        writeIndex = (writeIndex + 1) % velocityQueueCapacity;
      }
      valueCounter++;
    }
  }

  /**
   * Returns a copy of this listener's ring buffer.
   *
   * @return A copy of this listener's ring buffer.
   */
  public int[] getVelocities() {
    log.finer("method entry");
    int[] result = new int[velocityQueueCapacity];
    synchronized (velocities) {
      int firstCount = velocityQueueCapacity - writeIndex;
      System.arraycopy(velocities, writeIndex, result, 0, firstCount);
      if (firstCount != velocityQueueCapacity) {
        int secondCount = velocityQueueCapacity - firstCount;
        System.arraycopy(velocities, 0, result, firstCount, secondCount);
      }
    }
    return result;
  }

  /**
   * Returns this listener's queue capacity.
   *
   * @return This listener's queue capacity.
   */
  public int getQueueSize() {
    log.finer("method entry");
    return velocityQueueCapacity;
  }

  /**
   * Clears this <code>VelocityQueueListener</code>'s queue, i.e. sets all
   * values in the queue to 0.
   */
  public void clear() {
    log.finer("method entry");
    synchronized (velocities) {
      for (int i = 0; i < velocities.length; i++) {
        velocities[i] = 0;
      }
    }
  }
}
