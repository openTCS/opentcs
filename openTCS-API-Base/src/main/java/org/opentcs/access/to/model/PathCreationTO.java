/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Path.Layout.ConnectionType;
import static org.opentcs.util.Assertions.checkArgument;

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
   * The peripheral operations to be performed when a vehicle travels along this path.
   */
  private List<PeripheralOperationCreationTO> peripheralOperations;
  /**
   * A flag for marking this path as locked (i.e. to prevent vehicles from using it).
   */
  private boolean locked;
  /**
   * The information regarding the grahical representation of this path.
   */
  private Layout layout = new Layout();

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
    this.peripheralOperations = new ArrayList<>();
  }

  private PathCreationTO(String name,
                         @Nonnull String srcPointName,
                         @Nonnull String destPointName,
                         @Nonnull Map<String, String> properties,
                         long length,
                         int maxVelocity,
                         int maxReverseVelocity,
                         List<PeripheralOperationCreationTO> peripheralOperations,
                         boolean locked,
                         @Nonnull Layout layout) {
    super(name, properties);
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    this.destPointName = requireNonNull(destPointName, "destPointName");
    this.length = length;
    this.maxVelocity = maxVelocity;
    this.maxReverseVelocity = maxReverseVelocity;
    this.peripheralOperations = new ArrayList<>(requireNonNull(peripheralOperations,
                                                               "peripheralOperations"));
    this.locked = locked;
    this.layout = requireNonNull(layout, "layout");
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
  }

  /**
   * Returns the peripheral operations to be performed when a vehicle travels along this path.
   *
   * @return The peripheral operations to be performed when a vehicle travels along this path.
   */
  public List<PeripheralOperationCreationTO> getPeripheralOperations() {
    return Collections.unmodifiableList(peripheralOperations);
  }

  /**
   * Creates a copy of this object with the given peripheral operations.
   *
   * @param peripheralOperations The peripheral operations.
   * @return A copy of this object, differing in the given peripheral operations.
   */
  public PathCreationTO withPeripheralOperations(
      @Nonnull List<PeripheralOperationCreationTO> peripheralOperations) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
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
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
  }

  /**
   * Returns the information regarding the grahical representation of this path.
   *
   * @return The information regarding the grahical representation of this path.
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Creates a copy of this object, with the given layout.
   *
   * @param layout The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PathCreationTO withLayout(Layout layout) {
    return new PathCreationTO(getName(),
                              srcPointName,
                              destPointName,
                              getModifiableProperties(),
                              length,
                              maxVelocity,
                              maxReverseVelocity,
                              peripheralOperations,
                              locked,
                              layout);
  }

  @Override
  public String toString() {
    return "PathCreationTO{"
        + "name=" + getName()
        + ", srcPointName=" + srcPointName
        + ", destPointName=" + destPointName
        + ", length=" + length
        + ", maxVelocity=" + maxVelocity
        + ", maxReverseVelocity=" + maxReverseVelocity
        + ", peripheralOperations=" + peripheralOperations
        + ", locked=" + locked
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Contains information regarding the grahical representation of a path.
   */
  public static class Layout
      implements Serializable {

    /**
     * The connection type the path is represented as.
     */
    private final ConnectionType connectionType;
    /**
     * Control points describing the way the path is drawn (if the connection type
     * is {@link ConnectionType#BEZIER}, {@link ConnectionType#BEZIER_3}
     * or {@link ConnectionType#POLYPATH}).
     */
    private final List<Couple> controlPoints;
    /**
     * The ID of the layer on which the path is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(ConnectionType.DIRECT, new ArrayList<>(), 0);
    }

    /**
     * Creates a new instance.
     *
     * @param connectionType The connection type a path is represented as.
     * @param controlPoints Control points describing the way the path is drawn.
     * @param layerId The ID of the layer on which the path is to be drawn.
     */
    public Layout(ConnectionType connectionType, List<Couple> controlPoints, int layerId) {
      this.connectionType = connectionType;
      this.controlPoints = requireNonNull(controlPoints, "controlPoints");
      this.layerId = layerId;
    }

    /**
     * Returns the connection type the path is represented as.
     *
     * @return The connection type the path is represented as.
     */
    public ConnectionType getConnectionType() {
      return connectionType;
    }

    /**
     * Creates a copy of this object, with the given connection type.
     *
     * @param connectionType The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withConnectionType(ConnectionType connectionType) {
      return new Layout(connectionType, controlPoints, layerId);
    }

    /**
     * Returns the control points describing the way the path is drawn.
     * Returns an empty list if connection type is not {@link ConnectionType#BEZIER},
     * {@link ConnectionType#BEZIER_3} or {@link ConnectionType#POLYPATH}.
     *
     * @return The control points describing the way the path is drawn.
     */
    public List<Couple> getControlPoints() {
      return Collections.unmodifiableList(controlPoints);
    }

    /**
     * Creates a copy of this object, with the given control points.
     *
     * @param controlPoints The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withControlPoints(List<Couple> controlPoints) {
      return new Layout(connectionType, controlPoints, layerId);
    }

    /**
     * Returns the ID of the layer on which the path is to be drawn.
     *
     * @return The layer ID.
     */
    public int getLayerId() {
      return layerId;
    }

    /**
     * Creates a copy of this object, with the given layer ID.
     *
     * @param layerId The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLayer(int layerId) {
      return new Layout(connectionType, controlPoints, layerId);
    }

    @Override
    public String toString() {
      return "Layout{"
          + "connectionType=" + connectionType
          + ", controlPoints=" + controlPoints
          + ", layerId=" + layerId
          + '}';
    }
  }
}
