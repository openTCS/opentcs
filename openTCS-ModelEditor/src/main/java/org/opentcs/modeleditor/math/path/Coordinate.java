/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

/**
 * A generic 2-tuple of double values, usable for 2D coordinates and vectors, for instance.
 */
public class Coordinate {

  /**
   * The X coordinate.
   */
  private final double x;
  /**
   * The Y coordinate.
   */
  private final double y;

  /**
   * Creates a new instance.
   *
   * @param x The X coordinate.
   * @param y The Y coordinate.
   */
  public Coordinate(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the x coordinate.
   *
   * @return x
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the y coordinate.
   *
   * @return y
   */
  public double getY() {
    return y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Coordinate)) {
      return false;
    }
    Coordinate other = (Coordinate) obj;
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
    return (int) (x * y);
  }

  @Override
  public String toString() {
    return "Coordinate{" + "x=" + x + ", y=" + y + '}';
  }

}
