/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;

/**
 * A generic 3-tuple of long integer values, usable for 3D coordinates and vectors, for instance.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Triple
    implements Serializable {

  /**
   * The X coordinate.
   */
  private long x;
  /**
   * The Y coordinate.
   */
  private long y;
  /**
   * The Z coordinate.
   */
  private long z;

  /**
   * Creates a new Triple with the given values.
   *
   * @param x The X coordinate.
   * @param y The Y coordinate.
   * @param z The Z coordindate.
   */
  public Triple(long x, long y, long z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns the x coordinate.
   *
   * @return x
   */
  public long getX() {
    return x;
  }

  /**
   * Returns the y coordinate.
   *
   * @return y
   */
  public long getY() {
    return y;
  }

  /**
   * Returns the z coordinate.
   *
   * @return z
   */
  public long getZ() {
    return z;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Triple)) {
      return false;
    }
    Triple other = (Triple) obj;
    if (this.x != other.x) {
      return false;
    }
    if (this.y != other.y) {
      return false;
    }
    if (this.z != other.z) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return (int) (x ^ y ^ z);
  }

  @Override
  public String toString() {
    return "Triple{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
