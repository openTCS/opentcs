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
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A point in the driving course at which a {@link Vehicle} may be located.
 *
 * @see Path
 */
public class Point
    extends TCSResource<Point>
    implements Serializable {

  /**
   * The pose of the vehicle at this point.
   */
  private final Pose pose;
  /**
   * This point's type.
   */
  private final Type type;
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
   * A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  private final Map<String, Envelope> vehicleEnvelopes;
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
    this.pose = new Pose(new Triple(0, 0, 0), Double.NaN);
    this.type = Type.HALT_POSITION;
    this.incomingPaths = new HashSet<>();
    this.outgoingPaths = new HashSet<>();
    this.attachedLinks = new HashSet<>();
    this.occupyingVehicle = null;
    this.vehicleEnvelopes = Map.of();
    this.layout = new Layout();
  }

  private Point(String name,
                Map<String, String> properties,
                ObjectHistory history,
                Pose pose,
                Type type,
                Set<TCSObjectReference<Path>> incomingPaths,
                Set<TCSObjectReference<Path>> outgoingPaths,
                Set<Location.Link> attachedLinks,
                TCSObjectReference<Vehicle> occupyingVehicle,
                Map<String, Envelope> vehicleEnvelopes,
                Layout layout) {
    super(name, properties, history);
    this.pose = requireNonNull(pose, "pose");
    this.type = requireNonNull(type, "type");
    this.incomingPaths = setWithoutNullValues(requireNonNull(incomingPaths, "incomingPaths"));
    this.outgoingPaths = setWithoutNullValues(requireNonNull(outgoingPaths, "outgoingPaths"));
    this.attachedLinks = setWithoutNullValues(requireNonNull(attachedLinks, "attachedLinks"));
    this.occupyingVehicle = occupyingVehicle;
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Point withProperty(String key, String value) {
    return new Point(getName(),
                     propertiesWith(key, value),
                     getHistory(),
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  @Override
  public Point withProperties(Map<String, String> properties) {
    return new Point(getName(),
                     properties,
                     getHistory(),
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  @Override
  public TCSObject<Point> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Point(getName(),
                     getProperties(),
                     getHistory().withEntryAppended(entry),
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  @Override
  public TCSObject<Point> withHistory(ObjectHistory history) {
    return new Point(getName(),
                     getProperties(),
                     history,
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  /**
   * Returns the pose of the vehicle at this point.
   *
   * @return The pose of the vehicle at this point.
   */
  public Pose getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object, with the given pose.
   *
   * @param pose The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withPose(Pose pose) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  /**
   * Returns the physical coordinates of this point in mm.
   *
   * @return The physical coordinates of this point in mm.
   * @deprecated Use {@link #getPose()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public Triple getPosition() {
    return pose.getPosition();
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withPose(org.opentcs.data.model.Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public Point withPosition(Triple position) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     pose.withPosition(position),
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  /**
   * Returns a vehicle's orientation angle at this position.
   * (-360..360, or <code>Double.NaN</code>, if an orientation angle is not
   * specified for this point.)
   *
   * @return The vehicle's orientation angle when it's at this position.
   * @deprecated Use {@link #getPose()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public double getVehicleOrientationAngle() {
    return pose.getOrientationAngle();
  }

  /**
   * Creates a copy of this object, with the given vehicle orientation angle.
   *
   * @param vehicleOrientationAngle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withPose(org.opentcs.data.model.Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public Point withVehicleOrientationAngle(double vehicleOrientationAngle) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     pose.withOrientationAngle(vehicleOrientationAngle),
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  /**
   * Returns a map of envelope keys to envelopes that vehicles located at this point may occupy.
   *
   * @return A map of envelope keys to envelopes that vehicles located at this point may occupy.
   */
  public Map<String, Envelope> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  /**
   * Creates a copy of this object, with the given vehicle envelopes.
   *
   * @param vehicleEnvelopes The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withVehicleEnvelopes(Map<String, Envelope> vehicleEnvelopes) {
    return new Point(getName(),
                     getProperties(),
                     getHistory(),
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
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
                     pose,
                     type,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle,
                     vehicleEnvelopes,
                     layout);
  }

  /**
   * Describes the types of positions in a driving course.
   */
  public enum Type {

    /**
     * Indicates a position at which a vehicle is expected to report in.
     * Halting or even parking at such a position is not allowed.
     *
     * @deprecated Support for report points will be removed.
     */
    @Deprecated
    @ScheduledApiChange(when = "6.0", details = "Will be removed")
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
