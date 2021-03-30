/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralOperation;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * Describes a connection between two {@link Point}s which a {@link Vehicle} may traverse.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Path
    extends TCSResource<Path>
    implements Serializable {

  /**
   * A reference to the point which this point originates in.
   */
  private final TCSObjectReference<Point> sourcePoint;
  /**
   * A reference to the point which this point ends in.
   */
  private final TCSObjectReference<Point> destinationPoint;
  /**
   * The length of this path (in mm).
   */
  private final long length;
  /**
   * The absolute maximum allowed forward velocity on this path (in mm/s).
   * A value of 0 (default) means forward movement is not allowed on this path.
   */
  private final int maxVelocity;
  /**
   * The absolute maximum allowed reverse velocity on this path (in mm/s).
   * A value of 0 (default) means reverse movement is not allowed on this path.
   */
  private final int maxReverseVelocity;
  /**
   * The peripheral operations to be performed when a vehicle travels along this path.
   */
  private final List<PeripheralOperation> peripheralOperations;
  /**
   * A flag for marking this path as locked (i.e. to prevent vehicles from using it).
   */
  private final boolean locked;
  /**
   * The information regarding the grahical representation of this path.
   */
  private final Layout layout;

  /**
   * Creates a new Path.
   *
   * @param name The new path's name.
   * @param sourcePoint A reference to this path's starting point.
   * @param destinationPoint A reference to this path's destination point.
   */
  public Path(String name,
              TCSObjectReference<Point> sourcePoint,
              TCSObjectReference<Point> destinationPoint) {
    super(name);
    this.sourcePoint = requireNonNull(sourcePoint, "sourcePoint");
    this.destinationPoint = requireNonNull(destinationPoint, "destinationPoint");
    this.length = 1;
    this.maxVelocity = 1000;
    this.maxReverseVelocity = 1000;
    this.peripheralOperations = new ArrayList<>();
    this.locked = false;
    this.layout = new Layout();
  }

  private Path(String name,
               Map<String, String> properties,
               ObjectHistory history,
               TCSObjectReference<Point> sourcePoint,
               TCSObjectReference<Point> destinationPoint,
               long length,
               int maxVelocity,
               int maxReverseVelocity,
               List<PeripheralOperation> peripheralOperations,
               boolean locked,
               Layout layout) {
    super(name, properties, history);
    this.sourcePoint = requireNonNull(sourcePoint, "sourcePoint");
    this.destinationPoint = requireNonNull(destinationPoint, "destinationPoint");
    this.length = checkInRange(length, 1, Long.MAX_VALUE, "length");
    this.maxVelocity = checkInRange(maxVelocity, 0, Integer.MAX_VALUE, "maxVelocity");
    this.maxReverseVelocity = checkInRange(maxReverseVelocity,
                                           0,
                                           Integer.MAX_VALUE,
                                           "maxReverseVelocity");
    this.peripheralOperations = new ArrayList<>(requireNonNull(peripheralOperations,
                                                               "peripheralOperations"));
    this.locked = locked;
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Path withProperty(String key, String value) {
    return new Path(getName(),
                    propertiesWith(key, value),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
  }

  @Override
  public Path withProperties(Map<String, String> properties) {
    return new Path(getName(),
                    properties,
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
  }

  @Override
  public TCSObject<Path> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Path(getName(),
                    getProperties(),
                    getHistory().withEntryAppended(entry),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
  }

  @Override
  public TCSObject<Path> withHistory(ObjectHistory history) {
    return new Path(getName(),
                    getProperties(),
                    history,
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
  }

  /**
   * Return the length of this path (in mm).
   *
   * @return The length of this path (in mm).
   */
  public long getLength() {
    return length;
  }

  /**
   * Creates a copy of this object, with the given length.
   *
   * @param length The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Path withLength(long length) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
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
   * Creates a copy of this object, with the given maximum velocity.
   *
   * @param maxVelocity The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Path withMaxVelocity(int maxVelocity) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
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
   * Creates a copy of this object, with the given maximum reverse velocity.
   *
   * @param maxReverseVelocity The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Path withMaxReverseVelocity(int maxReverseVelocity) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
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
  public List<PeripheralOperation> getPeripheralOperations() {
    return Collections.unmodifiableList(peripheralOperations);
  }

  /**
   * Creates a copy of this object, with the given peripheral operations.
   *
   * @param peripheralOperations The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Path withPeripheralOperations(@Nonnull List<PeripheralOperation> peripheralOperations) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
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
   * Creates a copy of this object, with the given locked flag.
   *
   * @param locked The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Path withLocked(boolean locked) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
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
  public Path withLayout(Layout layout) {
    return new Path(getName(),
                    getProperties(),
                    getHistory(),
                    sourcePoint,
                    destinationPoint,
                    length,
                    maxVelocity,
                    maxReverseVelocity,
                    peripheralOperations,
                    locked,
                    layout);
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
      throw new IllegalArgumentException(navPoint + " is not an end point of " + this);
    }
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

    /**
     * The connection type a path is represented as.
     */
    public static enum ConnectionType {

      /**
       * A direct connection.
       */
      DIRECT,
      /**
       * An elbow connection.
       */
      ELBOW,
      /**
       * A slanted connection.
       */
      SLANTED,
      /**
       * A polygon path with any number of vertecies.
       */
      POLYPATH,
      /**
       * A bezier curve with 2 control points.
       */
      BEZIER,
      /**
       * A bezier curve with 3 control points.
       */
      BEZIER_3;
    }
  }
}
