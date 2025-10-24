// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import java.io.Serializable;

/**
 * A generic 3-tuple of long integer values, usable for 3D coordinates and vectors, for instance.
 */
public class TripleCreationTO
    implements
      Serializable {

  /**
   * The X coordinate.
   */
  private final long x;
  /**
   * The Y coordinate.
   */
  private final long y;
  /**
   * The Z coordinate.
   */
  private final long z;

  /**
   * Creates a new TripleCreationTO with the given values.
   *
   * @param x The X coordinate.
   * @param y The Y coordinate.
   * @param z The Z coordindate.
   */
  public TripleCreationTO(long x, long y, long z) {
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
    if (!(obj instanceof TripleCreationTO)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    TripleCreationTO other = (TripleCreationTO) obj;
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
    return "TripleCreationTO{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
