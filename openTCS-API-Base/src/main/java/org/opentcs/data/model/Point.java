/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Describes a position in the driving course at which a {@link Vehicle} may be located.
 *
 * @see Path
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Point
    extends TCSResource<Point>
    implements Serializable {

  /**
   * This point's coordinates in mm.
   */
  private final Triple position;
  /**
   * This point's type.
   */
  private final Type type;
  /**
   * The vehicle's (assumed) orientation angle (-360..360) when it is at this
   * position.
   * May be Double.NaN if an orientation angle is not defined for this point.
   */
  private final double vehicleOrientationAngle;
  /**
   * A set of references to paths ending in this point.
   */
  private final Set<TCSObjectReference<Path>> incomingPaths;
  /**
   * A set of references to paths originating in this point.
   */
  private final Set<TCSObjectReference<Path>> outgoingPaths;
  /**
   * A set of links attached to this point.
   */
  private final Set<Location.Link> attachedLinks;
  /**
   * A reference to the vehicle occupying this point.
   */
  private final TCSObjectReference<Vehicle> occupyingVehicle;
  /**
   * The information regarding the grahical representation of this point.
   */
  private final Layout layout;

  /**
   * Creates a new point with the given name.
   *
   * @param name This point's name.
   */
  public Point(String name) {
    super(name);
    this.position = new Triple(0, 0, 0);
    this.type = Type.HALT_POSITION;
    this.vehicleOrientationAngle = Double.NaN;
    this.incomingPaths = new HashSet<>();
    this.outgoingPaths = new HashSet<>();
    this.attachedLinks = new HashSet<>();
    this.occupyingVehicle = null;
    this.layout = new Layout();
  }

  private Point(String name,
                Map<String, String> properties,
                ObjectHistory history,
                Triple position,
                Type type,
                double vehicleOrientationAngle,
                Set<TCSObjectReference<Path>> incomingPaths,
                Set<TCSObjectReference<Path>> outgoingPaths,
                Set<Location.Link> attachedLinks,
                TCSObjectReference<Vehicle> occupyingVehicle,
                Layout layout) {
    super(name, properties, history);
    this.position = requireNonNull(position, "position");
    this.type = requireNonNull(type, "type");
    checkArgument(Double.isNaN(vehicleOrientationAngle)
        || (vehicleOrientationAngle >= -360.0 && vehicleOrientationAngle <= 360.0),
                  "angle not in [-360..360]: %s",
                  vehicleOrientationAngle);
    this.vehicleOrientationAngle = vehicleOrientationAngle;
    this.incomingPaths = setWithoutNullValues(requireNonNull(incomingPaths, "incomingPaths"));
    this.outgoingPaths = setWithoutNullValues(requireNonNull(outgoingPaths, "outgoingPaths"));
    this.attachedLinks = setWithoutNullValues(requireNonNull(attachedLinks, "attachedLinks"));
    this.occupyingVehicle = occupyingVehicle;
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Point withProperty(String key, String value) {
    return new Point(getName(),
                     propertiesWith(key, value),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  @Override
  public Point withProperties(Map<String, String> properties) {
    return new Point(getName(),
                     properties,
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  @Override
  public TCSObject<Point> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Point(getName(),
                     getProperties(),
                     getHistory().withEntryAppended(entry),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  @Override
  public TCSObject<Point> withHistory(ObjectHistory history) {
    return new Point(getName(),
                     getProperties(),
                     history,
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns the physical coordinates of this point in mm.
   *
   * @return The physical coordinates of this point in mm.
   */
  public Triple getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withPosition(Triple position) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns a vehicle's orientation angle at this position.
   * (-360..360, or <code>Double.NaN</code>, if an orientation angle is not
   * specified for this point.)
   *
   * @return The vehicle's orientation angle when it's at this position.
   */
  public double getVehicleOrientationAngle() {
    return vehicleOrientationAngle;
  }

  /**
   * Creates a copy of this object, with the given vehicle orientation angle.
   *
   * @param vehicleOrientationAngle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withVehicleOrientationAngle(double vehicleOrientationAngle) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns this point's type.
   *
   * @return This point's type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object, with the given type.
   *
   * @param type The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withType(Type type) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Checks whether parking a vehicle on this point is allowed.
   * <p>
   * This method is a convenience method; its return value is equal to
   * <code>getType().equals(Point.Type.PARK_POSITION)</code>.
   * </p>
   *
   * @return <code>true</code> if, and only if, parking is allowed on this
   * point.
   */
  public boolean isParkingPosition() {
    return type.equals(Type.PARK_POSITION);
  }

  /**
   * Checks whether halting on this point is allowed.
   * <p>
   * This method is a convenience method; its return value is equal to
   * <code>getType().equals(Point.Type.PARK_POSITION) ||
   * getType().equals(Point.Type.HALT_POSITION)</code>.
   * </p>
   *
   * @return <code>true</code> if, and only if, halting is allowed on this
   * point.
   */
  public boolean isHaltingPosition() {
    return type.equals(Type.PARK_POSITION) || type.equals(Type.HALT_POSITION);
  }

  /**
   * Returns a reference to the vehicle occupying this point.
   *
   * @return A reference to the vehicle occupying this point, or
   * <code>null</code>, if this point isn't currently occupied by any vehicle.
   */
  public TCSObjectReference<Vehicle> getOccupyingVehicle() {
    return occupyingVehicle;
  }

  /**
   * Creates a copy of this object, with the given occupying vehicle.
   *
   * @param occupyingVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withOccupyingVehicle(TCSObjectReference<Vehicle> occupyingVehicle) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns a set of references to paths ending in this point.
   *
   * @return A set of references to paths ending in this point.
   */
  public Set<TCSObjectReference<Path>> getIncomingPaths() {
    return Collections.unmodifiableSet(incomingPaths);
  }

  /**
   * Creates a copy of this object, with the given incoming paths.
   *
   * @param incomingPaths The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withIncomingPaths(Set<TCSObjectReference<Path>> incomingPaths) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns a set of references to paths originating in this point.
   *
   * @return A set of references to paths originating in this point.
   */
  public Set<TCSObjectReference<Path>> getOutgoingPaths() {
    return Collections.unmodifiableSet(outgoingPaths);
  }

  /**
   * Creates a copy of this object, with the given outgoing paths.
   *
   * @param outgoingPaths The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withOutgoingPaths(Set<TCSObjectReference<Path>> outgoingPaths) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns a set of links attached to this point.
   *
   * @return A set of links attached to this point.
   */
  public Set<Location.Link> getAttachedLinks() {
    return Collections.unmodifiableSet(attachedLinks);
  }

  /**
   * Creates a copy of this object, with the given attached links.
   *
   * @param attachedLinks The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withAttachedLinks(Set<Location.Link> attachedLinks) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Returns the information regarding the grahical representation of this point.
   *
   * @return The information regarding the grahical representation of this point.
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
  public Point withLayout(Layout layout) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     layout);
  }

  /**
   * Describes the types of positions in a driving course.
   */
  public enum Type {

    /**
     * Indicates a position at which a vehicle is expected to report in.
     * Halting or even parking at such a position is not allowed.
     */
    REPORT_POSITION,
    /**
     * Indicates a position at which a vehicle may halt temporarily, e.g. for executing an
     * operation.
     * The vehicle is also expected to report in when it arrives at such a position.
     * It may not park here for longer than necessary, though.
     */
    HALT_POSITION,
    /**
     * Indicates a position at which a vehicle may halt for longer periods of time when it is not
     * processing orders.
     * The vehicle is also expected to report in when it arrives at such a position.
     */
    PARK_POSITION;
  }

  /**
   * Contains information regarding the grahical representation of a point.
   */
  public static class Layout
      implements Serializable {

    /**
     * The coordinates at which the point is to be drawn (in mm).
     */
    private final Couple position;
    /**
     * The offset of the label's position to the point's position (in lu).
     */
    private final Couple labelOffset;
    /**
     * The ID of the layer on which the point is to be drawn.
     */
    private final int layerId;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(new Couple(0, 0), new Couple(0, 0), 0);
    }

    /**
     * Creates a new instance.
     *
     * @param position The coordinates at which the point is to be drawn (in mm).
     * @param labelOffset The offset of the label's position to the point's position (in lu).
     * @param layerId The ID of the layer on which the point is to be drawn.
     */
    public Layout(Couple position,
                  Couple labelOffset,
                  int layerId) {
      this.position = requireNonNull(position, "position");
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      this.layerId = layerId;
    }

    /**
     * Returns the coordinates at which the point is to be drawn (in mm).
     *
     * @return The coordinates at which the point is to be drawn (in mm).
     */
    public Couple getPosition() {
      return position;
    }

    /**
     * Creates a copy of this object, with the given position.
     *
     * @param position The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withPosition(Couple position) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }

    /**
     * Returns the offset of the label's position to the point's position (in lu).
     *
     * @return The offset of the label's position to the point's position (in lu).
     */
    public Couple getLabelOffset() {
      return labelOffset;
    }

    /**
     * Creates a copy of this object, with the given X label offset.
     *
     * @param labelOffset The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withLabelOffset(Couple labelOffset) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }

    /**
     * Returns the ID of the layer on which the point is to be drawn.
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
    public Layout withLayerId(int layerId) {
      return new Layout(position,
                        labelOffset,
                        layerId);
    }
  }
}
