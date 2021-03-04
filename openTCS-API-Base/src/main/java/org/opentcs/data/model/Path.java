/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.data.TCSObjectReference;

/**
 * A Path is a connection between two Points.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Path
    extends TCSResource<Path>
    implements Serializable, Cloneable {

  /**
   * A reference to the point which this point originates in.
   */
  private TCSObjectReference<Point> sourcePoint;
  /**
   * A reference to the point which this point ends in.
   */
  private TCSObjectReference<Point> destinationPoint;
  /**
   * The length of this path (in mm).
   */
  private long length = 1;
  /**
   * An explicit (unitless) weight that can be used to influence routing. The
   * higher the value, the more travelling this path costs.
   */
  private long routingCost = 1;
  /**
   * The absolute maximum allowed forward velocity on this path (in mm/s).
   * A value of 0 (default) means forward movement is not allowed on this path.
   */
  private int maxVelocity;
  /**
   * The absolute maximum allowed reverse velocity on this path (in mm/s).
   * A value of 0 (default) means reverse movement is not allowed on this path.
   */
  private int maxReverseVelocity;
  /**
   * A flag for marking this path as locked (i.e. to prevent vehicles from
   * using it).
   */
  private boolean locked;

  /**
   * Creates a new Path.
   *
   * @param objectID The new path's object ID.
   * @param name The new path's name.
   * @param srcPoint A reference to this path's starting point.
   * @param destPoint A reference to this path's destination point.
   */
  public Path(int objectID, String name, TCSObjectReference<Point> srcPoint,
              TCSObjectReference<Point> destPoint) {
    super(objectID, name);
    sourcePoint = Objects.requireNonNull(srcPoint, "srcPoint is null");
    destinationPoint = Objects.requireNonNull(destPoint, "destPoint is null");
  }

  // Methods not declared in any interface start here
  /**
   * Return the length of this path (in mm).
   *
   * @return The length of this path (in mm).
   */
  public long getLength() {
    return length;
  }

  /**
   * Set the length of this path (in mm).
   *
   * @param newLength The new length of this path (in mm). Must be a positive
   * value.
   */
  public void setLength(long newLength) {
    if (newLength <= 0) {
      throw new IllegalArgumentException("newLength is not a positive value");
    }
    length = newLength;
  }

  /**
   * Returns the routing cost of this path (unitless). The higher the value, the
   * more travelling this path costs.
   *
   * @return The routing cost of this path (unitless).
   */
  public long getRoutingCost() {
    return routingCost;
  }

  /**
   * Sets the routing cost of this path (unitless), an explicit weight that can
   * be used to influence routing. The higher the value, the more travelling
   * this path costs.
   *
   * @param newCost The new routing cost (unitless). Must be a positive value.
   */
  public void setRoutingCost(long newCost) {
    if (newCost <= 0) {
      throw new IllegalArgumentException("newCost <= 0: " + newCost);
    }
    routingCost = newCost;
  }

  /**
   * Returns a reference to the point which this path originates in.
   *
   * @return A reference to the point which this path originates in.
   */
  public TCSObjectReference<Point> getSourcePoint() {
    return sourcePoint;
  }

  /**
   * Returns a reference to the point which this path ends in.
   *
   * @return A reference to the point which this path ends in.
   */
  public TCSObjectReference<Point> getDestinationPoint() {
    return destinationPoint;
  }

  /**
   * Return the maximum allowed forward velocity (in mm/s) for this path.
   *
   * @return The maximum allowed forward velocity (in mm/s). A value of 0 means
   * forward movement is not allowed on this path.
   */
  public int getMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Set the maximum allowed forward velocity (in mm/s).
   *
   * @param newVelocity The new maximum allowed velocity for this path
   * (in mm/s). May not be a negative value.
   * @throws IllegalArgumentException If <code>newVelocity</code> is negative.
   */
  public void setMaxVelocity(int newVelocity) {
    if (newVelocity < 0) {
      throw new IllegalArgumentException("newVelocity is negative");
    }
    maxVelocity = newVelocity;
  }

  /**
   * Return the maximum allowed reverse velocity (in mm/s) for this path.
   *
   * @return The maximum allowed reverse velocity (in mm/s). A value of 0 means
   * reverse movement is not allowed on this path.
   */
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Set the maximum allowed reverse velocity (in mm/s).
   *
   * @param newVelocity The new maximum allowed reverse velocity for this path
   * (in mm/s). May not be a negative value.
   * @throws IllegalArgumentException If <code>newVelocity</code> is negative.
   */
  public void setMaxReverseVelocity(int newVelocity) {
    if (newVelocity < 0) {
      throw new IllegalArgumentException("newVelocity is negative");
    }
    maxReverseVelocity = newVelocity;
  }

  /**
   * Return the lock status of this path (i.e. whether this path my be used by
   * vehicles or not).
   *
   * @return <code>true</code> if this path is currently locked (i.e. it may not
   * be used by vehicles), else <code>false</code>.
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Lock or unlock this path.
   *
   * @param newLocked If <code>true</code>, this path will be locked when the
   * method call returns; if <code>false</code>, this path will be unlocked.
   */
  public void setLocked(boolean newLocked) {
    locked = newLocked;
  }

  /**
   * Checks whether this path is navigable in forward direction.
   *
   * @return <code>true</code> if, and only if, this path is not locked and its
   * maximum forward velocity is not zero.
   */
  public boolean isNavigableForward() {
    return !locked && maxVelocity != 0;
  }

  /**
   * Checks whether this path is navigable in backward/reverse direction.
   *
   * @return <code>true</code> if, and only if, this path is not locked and its
   * maximum reverse velocity is not zero.
   */
  public boolean isNavigableReverse() {
    return !locked && maxReverseVelocity != 0;
  }

  /**
   * Checks whether this path is navigable towards the given point.
   *
   * @param navPoint The point.
   * @return If <code>navPoint</code> is this path's destination point, returns
   * <code>isNavigableForward()</code>; if <code>navPoint</code> is this path's
   * source point, returns <code>isNavigableReverse()</code>.
   * @throws IllegalArgumentException If the given point is neither the source
   * point nor the destination point of this path.
   */
  public boolean isNavigableTo(TCSObjectReference<Point> navPoint)
      throws IllegalArgumentException {
    if (Objects.equals(navPoint, destinationPoint)) {
      return isNavigableForward();
    }
    else if (Objects.equals(navPoint, sourcePoint)) {
      return isNavigableReverse();
    }
    else {
      throw new IllegalArgumentException(
          navPoint + " is not an end point of " + this);
    }
  }

  @Override
  public Path clone() {
    Path clone = (Path) super.clone();
    clone.sourcePoint = sourcePoint.clone();
    clone.destinationPoint = destinationPoint.clone();
    return clone;
  }
}
