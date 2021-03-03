/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a single load handling device on a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoadHandlingDevice
    implements Serializable {

  /**
   * A name/label for this device.
   */
  private final String label;
  /**
   * A flag indicating whether this device is filled to its maximum capacity or
   * not.
   */
  private final boolean full;

  /**
   * Creates a new LoadHandlingDevice.
   *
   * @param label The device's name/label.
   * @param full A flag indicating whether this device is filled to its maximum
   * capacity or not.
   */
  public LoadHandlingDevice(String label, boolean full) {
    this.label = Objects.requireNonNull(label, "label is null");
    this.full = full;
  }

  /**
   * Creates a new LoadHandlingDevice as a copy of the given one.
   *
   * @param original The instance to be copied.F
   */
  public LoadHandlingDevice(LoadHandlingDevice original) {
    this.label = original.label;
    this.full = original.full;
  }

  /**
   * Returns this load handling device's name/label.
   *
   * @return This load handling device's name/label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns a flag indicating whether this device is filled to its maximum
   * capacity or not.
   *
   * @return A flag indicating whether this device is filled to its maximum
   * capacity or not.
   */
  public boolean isFull() {
    return full;
  }

  @Override
  public String toString() {
    return label + ":" + full;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof LoadHandlingDevice)) {
      return false;
    }
    final LoadHandlingDevice other = (LoadHandlingDevice) obj;
    if (!Objects.equals(this.label, other.label)) {
      return false;
    }
    if (this.full != other.full) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.label);
    hash = 23 * hash + (this.full ? 1 : 0);
    return hash;
  }
}
