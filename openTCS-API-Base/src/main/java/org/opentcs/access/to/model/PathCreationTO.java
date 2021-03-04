/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a path in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PathCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The point name this path originates in.
   */
  @Nonnull
  private String srcPointName;
  /**
   * The point name this path ends in.
   */
  @Nonnull
  private String destPointName;
  /**
   * This path's length (in mm).
   */
  private long length = 1;
  /**
   * An explicit (unitless) weight that can be used to influence routing. The higher the value, the
   * more travelling this path costs.
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
   * A flag for marking this path as locked (i.e. to prevent vehicles from using it).
   */
  private boolean locked;

  /**
   * Creates a new instance.
   *
   * @param name The name of this path.
   * @param srcPointName The point name this path originates in.
   * @param destPointName The point name this path ends in.
   */
  public PathCreationTO(@Nonnull String name,
                        @Nonnull String srcPointName,
                        @Nonnull String destPointName) {
    super(name);
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    this.destPointName = requireNonNull(destPointName, "destPointName");
  }

  private PathCreationTO(String name,
                        @Nonnull String srcPointName,
                        @Nonnull String destPointName,
                        @Nonnull Map<String, String> properties,
                        long length,
                        long routingCost,
                        int maxVelocity,
                        int maxReverseVelocity,
                        boolean locked) {
    super(name, properties);
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    this.destPointName = requireNonNull(destPointName, "destPointName");
    this.length = length;
    this.routingCost = routingCost;
    this.maxVelocity = maxVelocity;
    this.maxReverseVelocity = maxReverseVelocity;
    this.locked = locked;
  }

  /**
   * Sets the name of this path.
   *
   * @param name The new name.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public PathCreationTO setName(@Nonnull String name) {
    return (PathCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public PathCreationTO withName(@Nonnull String name) {
    return new PathCreationTO(name,
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the point name this path originates in.
   *
   * @return The point name this path originates in.
   */
  @Nonnull
  public String getSrcPointName() {
    return srcPointName;
  }

  /**
   * Sets the point name this path originates in.
   *
   * @param srcPointName The new point name.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setSrcPointName(@Nonnull String srcPointName) {
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    return this;
  }

  /**
   * Creates a copy of this object with the given point name this path originates in.
   *
   * @param srcPointName The new source point name.
   * @return A copy of this object, differing in the given source point.
   */
  public PathCreationTO withSrcPointName(@Nonnull String srcPointName) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the point name this path ends in.
   *
   * @return The point name this path ends in.
   */
  @Nonnull
  public String getDestPointName() {
    return destPointName;
  }

  /**
   * Sets the point name this path ends in.
   *
   * @param destPointName The new point name.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setDestPointName(@Nonnull String destPointName) {
    this.destPointName = requireNonNull(destPointName, "destPointName");
    return this;
  }

  /**
   * Creates a copy of this object with the given destination point.
   *
   * @param destPointName The new source point.
   * @return A copy of this object, differing in the given value.
   */
  public PathCreationTO withDestPointName(@Nonnull String destPointName) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the length of this path (in mm).
   *
   * @return The length of this path (in mm).
   */
  public long getLength() {
    return length;
  }

  /**
   * Sets the length of this path (in mm).
   *
   * @param length The new length (in mm). Must be a positive value.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setLength(long length) {
    checkArgument(length > 0, "length must be a positive value: " + length);
    this.length = length;
    return this;
  }

  /**
   * Creates a copy of this object with the given path length (in mm).
   *
   * @param length the new length (in mm). Must be a positive value.
   * @return A copy of this object, differing in the given length.
   */
  public PathCreationTO withLength(long length) {
    checkArgument(length > 0, "length must be a positive value: " + length);
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the routing cost of this path (unitless). The higher the value, the more travelling
   * this path costs.
   *
   * @return The routing cost of this path (unitless).
   */
  public long getRoutingCost() {
    return routingCost;
  }

  /**
   * Sets the routing cost of this path (unitless), an explicit weight that can be used to
   * influence routing. The higher the value, the more travelling this path costs.
   *
   * @param routingCost The new routing cost (unitless). Must be a positive value.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setRoutingCost(long routingCost) {
    this.routingCost = routingCost;
    return this;
  }

  /**
   * Creates a copy of this object with the given routing cost (unitless),
   * an explicit weight that can be used to influence routing.
   * The higher the value, the more travelling this path costs.
   *
   * @param routingCost The new routing cost (unitless). Must be a positive value.
   * @return A copy of this object, differing in the given routing cost.
   */
  public PathCreationTO withRoutingCost(long routingCost) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the maximum allowed forward velocity (in mm/s) for this path.
   *
   * @return The maximum allowed forward velocity (in mm/s). A value of 0 means forward movement is
   * not allowed on this path.
   */
  public int getMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Sets the maximum allowed forward velocity (in mm/s) for this path.
   *
   * @param maxVelocity The new maximum allowed velocity (in mm/s). May not be a negative value.
   * @return The modified path.
   * @throws IllegalArgumentException If {@code maxVelocity} is negative.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setMaxVelocity(int maxVelocity) {
    checkArgument(maxVelocity >= 0,
                  "maxVelocity may not be a negative value: " + maxVelocity);
    this.maxVelocity = maxVelocity;
    return this;
  }

  /**
   * Creates a copy of this object with the maximum allowed forward velocity (in mm/s) for this path.
   *
   * @param maxVelocity The new maximum allowed velocity (in mm/s). May not be a negative value.
   * @return A copy of this object, differing in the given maximum velocity.
   */
  public PathCreationTO withMaxVelocity(int maxVelocity) {
    checkArgument(maxVelocity >= 0,
                  "maxVelocity may not be a negative value: " + maxVelocity);
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the maximum allowed reverse velocity (in mm/s) for this path.
   *
   * @return The maximum allowed reverse velocity (in mm/s). A value of 0 means reverse movement is
   * not allowed on this path.
   */
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Sets the maximum allowed reverse velocity (in mm/s) for this path.
   *
   * @param maxReverseVelocity The new maximum allowed reverse velocity (in mm/s). Must not be a
   * negative value.
   * @return The modified path.
   * @throws IllegalArgumentException If {@code maxReverseVelocity} is negative.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setMaxReverseVelocity(int maxReverseVelocity) {
    checkArgument(maxReverseVelocity >= 0,
                  "maxReverseVelocity may not be a negative value: " + maxReverseVelocity);
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }

  /**
   * Creates a copy of this object with the allowed maximum reverse velocity (in mm/s).
   *
   * @param maxReverseVelocity The new maximum allowed reverse velocity (in mm/s). Must not be a
   * negative value.
   * @return A copy of this object, differing in the given maximum reverse velocity.
   */
  public PathCreationTO withMaxReverseVelocity(int maxReverseVelocity) {
    checkArgument(maxReverseVelocity >= 0,
                  "maxReverseVelocity may not be a negative value: " + maxReverseVelocity);
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Returns the lock status of this path (i.e. whether this path my be used by vehicles or not).
   *
   * @return {@code true} if this path is currently locked (i.e. it may not be used by vehicles),
   * else {@code false}.
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Locks or unlocks this path.
   *
   * @param locked If {@code true}, this path will be locked when the method call returns; if
   * {@code false}, this path will be unlocked.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PathCreationTO setLocked(boolean locked) {
    this.locked = locked;
    return this;
  }

  /**
   * Creates a copy of this object that is locked if {@code locked==true} and unlocked otherwise.
   *
   * @param locked If {@code true}, this path will be locked when the method call returns; if
   * {@code false}, this path will be unlocked.
   * @return a copy of this object, differing in the locked attribute.
   */
  public PathCreationTO withLocked(boolean locked) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Sets the properties of this path.
   *
   * @param properties The new properties.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public PathCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (PathCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public PathCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              properties,
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }

  /**
   * Sets a single property of this path.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified path.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public PathCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (PathCreationTO) super.setProperty(key, value);
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public PathCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              propertiesWith(key, value),
                              length,
                              routingCost,
                              maxVelocity,
                              maxReverseVelocity,
                              locked);
  }
}
