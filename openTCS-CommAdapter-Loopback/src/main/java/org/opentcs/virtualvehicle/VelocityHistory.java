/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import static com.google.common.base.Preconditions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>VelocityListener</code> that keeps velocity values in a ring buffer.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
public class VelocityHistory {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VelocityHistory.class);
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
    checkArgument(queueCapacity >= 1,
                  "queueCapacity is less than 1: %s",
                  queueCapacity);
    checkArgument(newDivisor >= 1, "newDivisor is less than 1: %s", newDivisor);

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
    LOG.debug("method entry");
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
    LOG.debug("method entry");
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
    LOG.debug("method entry");
    return velocityQueueCapacity;
  }

  /**
   * Clears this <code>VelocityQueueListener</code>'s queue, i.e. sets all
   * values in the queue to 0.
   */
  public void clear() {
    LOG.debug("method entry");
    synchronized (velocities) {
      for (int i = 0; i < velocities.length; i++) {
        velocities[i] = 0;
      }
    }
  }
}
