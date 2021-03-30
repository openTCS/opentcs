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
 * A generic 2-tuple of long integer values, usable for 2D coordinates and vectors, for instance.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class Couple
    implements Serializable {

  /**
   * The X coordinate.
   */
  private final long x;
  /**
   * The Y coordinate.
   */
  private final long y;

  /**
   * Creates a new instance.
   *
   * @param x The X coordinate.
   * @param y The Y coordinate.
   */
  public Couple(long x, long y) {
    this.x = x;
    this.y = y;
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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Couple)) {
      return false;
    }
    Couple other = (Couple) obj;
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
    return "Couple{" + "x=" + x + ", y=" + y + '}';
  }
}
