// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import java.io.Serializable;

/**
 * A transfer object describing generic 2-tuple of long integer values.
 */
public class CoupleCreationTO
    implements
      Serializable {

  private final long x;
  private final long y;

  /**
   * Creates a new instance.
   *
   * @param x The X coordinate.
   * @param y The Y coordinate.
   */
  public CoupleCreationTO(long x, long y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the x coordinate.
   *
   * @return The x coordinate.
   */
  public long getX() {
    return x;
  }

  /**
   * Creates a copy of this object, with the given x coordinate.
   *
   * @param x The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public CoupleCreationTO withX(long x) {
    return new CoupleCreationTO(x, y);
  }

  /**
   * Returns the y coordinate.
   *
   * @return The y coordinate.
   */
  public long getY() {
    return y;
  }

  /**
   * Creates a copy of this object, with the given y coordinate.
   *
   * @param y The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public CoupleCreationTO withY(long y) {
    return new CoupleCreationTO(x, y);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CoupleCreationTO)) {
      return false;
    }
    CoupleCreationTO other = (CoupleCreationTO) obj;
    if (this.x != other.x) {
      return false;
    }
    if (this.y != other.y) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return (int) (x ^ y);
  }

  @Override
  public String toString() {
    return "CoupleCreationTO{" + "x=" + x + ", y=" + y + '}';
  }
}
