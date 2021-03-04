/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A generic 3-tuple of long integer values, usable for 3D coordinates and vectors, for instance.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class Triple
    implements Serializable,
               Cloneable {

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
   * Creates a new Triple with all values set to 0.
   *
   * @deprecated Use {@link #Triple(long, long, long)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public Triple() {
  }

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
   * Creates a new Triple with values copied from the given one.
   *
   * @param original The Triple from which to copy the values.
   * @deprecated Not useful with immutable instances. Use {@link #Triple(long, long, long)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public Triple(Triple original) {
    this(original.x, original.y, original.z);
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
   * Sets the new x coordinate.
   *
   * @param x The new x coordinate.
   * @return This instance.
   * @deprecated Instances should be immutable. Use {@link #Triple(long, long, long)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public Triple setX(long x) {
    this.x = x;
    return this;
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
   * Sets the new y coordinate.
   *
   * @param y The new y coordinate.
   * @return This instance.
   * @deprecated Instances should be immutable. Use {@link #Triple(long, long, long)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public Triple setY(long y) {
    this.y = y;
    return this;
  }

  /**
   * Returns the z coordinate.
   *
   * @return z
   */
  public long getZ() {
    return z;
  }

  /**
   * Sets the new z coordinate.
   *
   * @param z The new z coordinate.
   * @return This instance.
   * @deprecated Instances should be immutable. Use {@link #Triple(long, long, long)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public Triple setZ(long z) {
    this.z = z;
    return this;
  }

  @Override
  public Triple clone() {
    try {
      Triple clone = (Triple) super.clone();
      return clone;
    }
    catch (CloneNotSupportedException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
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
