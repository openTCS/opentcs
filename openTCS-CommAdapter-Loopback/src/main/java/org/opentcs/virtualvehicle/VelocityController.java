/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates the velocity of a vehicle depending on the length of the way and
 * the time it has moved already.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Iryna Felko (Fraunhofer IML)
 */
public class VelocityController {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VelocityController.class);
  /**
   * The maximum deceleration of the vehicle (in mm/s<sup>2</sup>).
   */
  private int maxDeceleration;
  /**
   * The maximum acceleration of the vehicle (in mm/s<sup>2</sup>).
   */
  private int maxAcceleration;
  /**
   * The maximum reverse velocity of the vehicle (in mm/s).
   */
  private int maxRevVelocity;
  /**
   * The maximum forward velocity of the vehicle (in mm/s).
   */
  private int maxFwdVelocity;
  /**
   * The current acceleration (in mm/s<sup>2</sup>).
   */
  private int currentAcceleration;
  /**
   * The current velocity (in mm/s).
   */
  private int currentVelocity;
  /**
   * The current position (in mm from the beginning of the current way entry).
   */
  private int currentPosition;
  /**
   * The current time, relative to the point of time at which this velocity
   * controller was created.
   */
  private long currentTime;
  /**
   * This controller's processing queue.
   */
  private final Queue<WayEntry> wayEntries = new LinkedList<>();
  /**
   * A set of velocity listeners.
   */
  private final Set<VelocityListener> velocityListeners = new HashSet<>();
  /**
   * True, if the vehicle has been paused, e.g. via the kernel gui
   * or a by a client message.
   */
  private boolean paused;

  /**
   * Creates a new VelocityController.
   *
   * @param maxDecel The maximum deceleration of the vehicle (in
   * mm/s<sup>2</sup>).
   * @param maxAccel The maximum acceleration of the vehicle (in
   * mm/s<sup>2</sup>).
   * @param maxRevVelo The maximum reverse velocity of the vehicle (in mm/s).
   * @param maxFwdVelo The maximum forward velocity of the vehicle (in mm/s).
   */
  public VelocityController(int maxDecel,
                            int maxAccel,
                            int maxRevVelo,
                            int maxFwdVelo) {
    maxDeceleration = maxDecel;
    maxAcceleration = maxAccel;
    maxRevVelocity = maxRevVelo;
    maxFwdVelocity = maxFwdVelo;
    paused = false;
  }

  /**
   * Returns the maximum deceleration.
   *
   * @return The maximum deceleration
   */
  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDeceleration The new maximum deceleration
   */
  public void setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
  }

  /**
   * Returns the maximum acceleration.
   *
   * @return The maximum acceleration
   */
  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  /**
   * Sets the maximum acceleration.
   *
   * @param maxAcceleration The new maximum acceleration
   */
  public void setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  /**
   * Returns the maximum reverse velocity.
   *
   * @return The maximum reverse velocity
   */
  public int getMaxRevVelocity() {
    return maxRevVelocity;
  }

  /**
   * Sets the maximum reverse velocity.
   *
   * @param maxRevVelocity The new maximum reverse velocity
   */
  public void setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
  }

  /**
   * Returns the maximum forward velocity.
   *
   * @return The maximum forward velocity
   */
  public int getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  /**
   * Sets the maximum forward velocity.
   *
   * @param maxFwdVelocity The new maximum forward velocity
   */
  public void setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
  }

  /**
   * Returns whether the vehicle is paused.
   *
   * @return paused
   */
  public boolean isVehiclePaused() {
    return paused;
  }

  /**
   * Pause the vehicle (i.e. set it's velocity to zero).
   *
   * @param pause True, if vehicle shall be paused. False, otherwise.
   */
  public void setVehiclePaused(boolean pause) {
    paused = pause;
  }

  /**
   * Adds a velocity listener to this vehicle controller's set of listeners.
   *
   * @param listener The velocity listener to be added.
   */
  public void addVelocityListener(VelocityListener listener) {
    if (listener == null) {
      throw new NullPointerException("listener is null");
    }
    velocityListeners.add(listener);
  }

  /**
   * Removes a velocity listener from this vehicle controller's set of listeners.
   *
   * @param listener The velocity listener to be removed.
   */
  public void removeVelocityListener(VelocityListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("listener is null");
    }
    velocityListeners.remove(listener);
  }

  /**
   * Returns this controller's current velocity.
   *
   * @return This controller's current velocity.
   */
  public int getCurrentVelocity() {
    return currentVelocity;
  }

  /**
   * Returns the vehicle's current position (in mm from the beginning of the
   * current way entry.
   *
   * @return The vehicle's current position (in mm from the beginning of the
   * current way entry.
   */
  public int getCurrentPosition() {
    return currentPosition;
  }

  /**
   * Returns the current time, relative to to the point of time at which this
   * controller was started.
   *
   * @return The current time, relative to to the point of time at which this
   * controller was started.
   */
  public long getCurrentTime() {
    return currentTime;
  }

  /**
   * Adds a way entry to this vehicle controller's processing queue.
   *
   * @param newEntry The way entry to add.
   */
  public void addWayEntry(WayEntry newEntry) {
    if (newEntry == null) {
      throw new NullPointerException("newEntry is null");
    }
    wayEntries.add(newEntry);
  }

  /**
   * Returns the way entry this velocity controller is currently processing.
   *
   * @return The way entry this velocity controller is currently processing. If
   * the processing queue is currently empty, <code>null</code> is returned.
   */
  public WayEntry getCurrentWayEntry() {
    return wayEntries.peek();
  }

  /**
   * Returns <code>true</code> if, and only if, there are way entries to be
   * processed in this velocity controller's queue.
   *
   * @return <code>true</code> if, and only if, there are way entries to be
   * processed in this velocity controller's queue.
   */
  public boolean hasWayEntries() {
    return !wayEntries.isEmpty();
  }

  /**
   * Increase this controller's current time by the given value and simulate
   * the events that would happen in this time frame.
   *
   * @param dt The time by which to advance this controller (in milliseconds).
   * Must be at least 1.
   */
  public void advanceTime(int dt) {
    if (dt < 1) {
      throw new IllegalArgumentException("dt is less than 1");
    }
    final int oldPosition = currentPosition;
    final int oldVelocity = currentVelocity;
    final Iterator<WayEntry> wayEntryIter = wayEntries.iterator();
    final WayEntry curWayEntry
        = wayEntryIter.hasNext() ? wayEntryIter.next() : null;
    final int targetVelocity;
    final long accelerationDistance;
    if (curWayEntry == null || paused) {
      targetVelocity = 0;
      accelerationDistance = 1;
      currentAcceleration = 0;
      currentVelocity = 0;
    }
    else {
      final int maxVelocity;
      final Vehicle.Orientation orientation = curWayEntry.vehicleOrientation;
      switch (orientation) {
        case FORWARD:
          maxVelocity = maxFwdVelocity;
          break;
        case BACKWARD:
          maxVelocity = maxRevVelocity;
          break;
        default:
          LOG.warn("Unhandled orientation: {}, assuming forward.", orientation);
          maxVelocity = maxFwdVelocity;
      }
      targetVelocity = Math.min(curWayEntry.targetVelocity, maxVelocity);
      // Accelerate as quickly as possible.
      accelerationDistance = 10;
      // Recompute the acceleration to reach/keep the desired velocity.
      currentAcceleration
          = (currentVelocity == targetVelocity) ? 0
              : suitableAcceleration(targetVelocity, accelerationDistance);
      // Recompute current velocity.
      currentVelocity = oldVelocity + currentAcceleration * dt / 1000;
      // Recompute current position.
      currentPosition = oldPosition + oldVelocity * dt / 1000
          + currentAcceleration * dt * dt / 1000000 / 2;
      // Check if we have left the way entry and entered the next.
      if (currentPosition >= curWayEntry.length) {
        currentPosition -= curWayEntry.length;
        wayEntries.poll();
      }
    }
    // The given time has now passed.
    currentTime += dt;
    // Let the listeners know about the new velocity value.
    for (VelocityListener curListener : velocityListeners) {
      curListener.addVelocityValue(currentVelocity);
    }
  }

  /**
   * Returns the acceleration (in mm/s<sup>2</sup>) needed for reaching a given
   * velocity exactly after travelling a given distance (respecting the current
   * velocity).
   *
   * @param targetVelocity The desired velocity (in mm/s).
   * @param travelDistance The distance after which the desired velocity is
   * supposed to be reached (in mm). Must be a positive value.
   * @return The acceleration needed for reaching the given velocity after
   * travelling the given distance.
   */
  int suitableAcceleration(final int targetVelocity, final long travelDistance) {
    if (travelDistance < 1) {
      throw new IllegalArgumentException("travelDistance is less than 1");
    }
    final double v_current = currentVelocity;
    final double v_target = targetVelocity;
    final double s = travelDistance;
    // Compute travelling time.
    // XXX Divide by zero if (v_current == -v_target), especially if both are 0!
    final double t = s / (v_current + (v_target - v_current) / 2);
    LOG.debug("t = " + t
        + "; s = " + s
        + "; v_current = " + v_current
        + "; v_target = " + v_target);
    // Compute acceleration.
    int result = (int) ((v_target - v_current) / t);
    LOG.debug("result = " + result);
    if (result > maxAcceleration) {
      result = maxAcceleration;
    }
    else if (result < maxDeceleration) {
      result = maxDeceleration;
    }
    return result;
  }

  /**
   * An entry in a vehicle controller's processing queue.
   */
  public static class WayEntry
      implements Serializable {

    /**
     * The length of the way to drive (in mm).
     */
    private final long length;
    /**
     * The target velocity on this way (in mm/s).
     */
    private final int targetVelocity;
    /**
     * The name of the destination point.
     */
    private final String destPointName;
    /**
     * The vehicle's orientation on this way.
     */
    private final Vehicle.Orientation vehicleOrientation;

    /**
     * Creates a new WayEntry.
     *
     * @param length The length of the way to drive (in mm).
     * @param maxVelocity The maximum velocity on this way (in mm/s).
     * @param destPointName The name of the destination point.
     * @param orientation The vehicle's orientation on this way.
     */
    public WayEntry(long length,
                    int maxVelocity,
                    String destPointName,
                    Vehicle.Orientation orientation) {
      checkArgument(length > 0, "length is not > 0 but %s", length);
      this.length = length;
      if (maxVelocity < 1) {
        LOG.warn("maxVelocity is zero or negative, setting to 100");
        this.targetVelocity = 100;
      }
      else {
        this.targetVelocity = maxVelocity;
      }
      this.destPointName = requireNonNull(destPointName, "destPointName");
      this.vehicleOrientation = requireNonNull(orientation, "vehicleOrientation");
    }

    /**
     * Returns the name of the destination point.
     *
     * @return The name of the destination point.
     */
    public String getDestPointName() {
      return destPointName;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof WayEntry) {
        WayEntry other = (WayEntry) o;
        return other.length == length
            && other.targetVelocity == targetVelocity
            && destPointName.equals(other.destPointName)
            && vehicleOrientation.equals(other.vehicleOrientation);
      }
      else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return (int) (length ^ (length >>> 32))
          ^ targetVelocity
          ^ destPointName.hashCode()
          ^ vehicleOrientation.hashCode();
    }
  }
}
