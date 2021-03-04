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
import java.util.Iterator;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes a position in the driving course at which a {@link Vehicle} may be located.
 *
 * @see Path
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class Point
    extends TCSResource<Point>
    implements Serializable,
               Cloneable {

  /**
   * This point's coordinates in mm.
   */
  private Triple position = new Triple(0, 0, 0);
  /**
   * This point's type.
   */
  private Type type = Type.HALT_POSITION;
  /**
   * The vehicle's (assumed) orientation angle (-360..360) when it is at this
   * position.
   * May be Double.NaN if an orientation angle is not defined for this point.
   */
  private double vehicleOrientationAngle;
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
  private TCSObjectReference<Vehicle> occupyingVehicle;

  /**
   * Creates a new point with the given name.
   *
   * @param objectID This point's object ID.
   * @param name This point's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Point(int objectID, String name) {
    super(objectID, name);
    this.position = new Triple(0, 0, 0);
    this.type = Type.HALT_POSITION;
    this.vehicleOrientationAngle = Double.NaN;
    this.incomingPaths = new HashSet<>();
    this.outgoingPaths = new HashSet<>();
    this.attachedLinks = new HashSet<>();
    this.occupyingVehicle = null;
  }

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
  }

  @SuppressWarnings("deprecation")
  private Point(int objectID,
                String name,
                Map<String, String> properties,
                Triple position,
                Type type,
                double vehicleOrientationAngle,
                Set<TCSObjectReference<Path>> incomingPaths,
                Set<TCSObjectReference<Path>> outgoingPaths,
                Set<Location.Link> attachedLinks,
                TCSObjectReference<Vehicle> occupyingVehicle) {
    super(objectID, name, properties);
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
  }

  @Override
  public Point withProperty(String key, String value) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     propertiesWith(key, value),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
  }

  @Override
  public Point withProperties(Map<String, String> properties) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     properties,
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Sets the physical coordinates of this point in mm.
   *
   * @param position The new physical coordinates of this point.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setPosition(@Nonnull Triple position) {
    this.position = requireNonNull(position, "position");
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withPosition(Triple position) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Sets the vehicle's (assumed) orientation angle when it's at this position.
   * Allowed value range: [-360..360], or <code>Double.NaN</code> to indicate
   * that there is no specific orientation angle for this point.
   *
   * @param angle The new angle.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setVehicleOrientationAngle(double angle) {
    if (!Double.isNaN(angle) && (angle > 360.0 || angle < -360.0)) {
      throw new IllegalArgumentException("angle not in [-360..360]: " + angle);
    }
    vehicleOrientationAngle = angle;
  }

  /**
   * Creates a copy of this object, with the given vehicle orientation angle.
   *
   * @param vehicleOrientationAngle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withVehicleOrientationAngle(double vehicleOrientationAngle) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Sets this point's type.
   *
   * @param type This point's new type.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setType(@Nonnull Type type) {
    this.type = requireNonNull(type, "type");
  }

  /**
   * Creates a copy of this object, with the given type.
   *
   * @param type The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withType(Type type) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Sets this point's occupying vehicle.
   *
   * @param newVehicle A reference to the vehicle occupying this point.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setOccupyingVehicle(TCSObjectReference<Vehicle> newVehicle) {
    occupyingVehicle = newVehicle;
  }

  /**
   * Creates a copy of this object, with the given occupying vehicle.
   *
   * @param occupyingVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withOccupyingVehicle(TCSObjectReference<Vehicle> occupyingVehicle) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Adds a reference to a path ending in this point.
   * If the referenced path is already among the incoming paths of this point,
   * nothing happens.
   *
   * @param path A reference to the path to be added.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addIncomingPath(@Nonnull TCSObjectReference<Path> path) {
    requireNonNull(path, "path");
    incomingPaths.add(path);
  }

  /**
   * Removes a reference to a path ending in this point.
   * If the referenced path is not among the incoming paths of this point,
   * nothing happens.
   *
   * @param rmPath A reference to the path to be removed.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void removeIncomingPath(TCSObjectReference<Path> rmPath) {
    incomingPaths.remove(rmPath);
  }

  /**
   * Creates a copy of this object, with the given incoming paths.
   *
   * @param incomingPaths The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withIncomingPaths(Set<TCSObjectReference<Path>> incomingPaths) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Adds a reference to a path originating in this point.
   * If the referenced path is already among the outgoing paths of this point,
   * nothing happens.
   *
   * @param path A reference to the path to be added.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addOutgoingPath(@Nonnull TCSObjectReference<Path> path) {
    requireNonNull(path, "path");
    outgoingPaths.add(path);
  }

  /**
   * Removes a reference to a path originating in this point.
   * If the referenced path is not among the outgoing paths of this point,
   * nothing happens.
   *
   * @param rmPath A reference to the path to be removed.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void removeOutgoingPath(TCSObjectReference<Path> rmPath) {
    outgoingPaths.remove(rmPath);
  }

  /**
   * Creates a copy of this object, with the given outgoing paths.
   *
   * @param outgoingPaths The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withOutgoingPaths(Set<TCSObjectReference<Path>> outgoingPaths) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
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
   * Attaches a link to this point.
   *
   * @param link The link to be attached to this point.
   * @return <code>true</code> if, and only if, the given link was not already
   * attached to this point.
   * @throws IllegalArgumentException If the point end of the given link is not
   * this point.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean attachLink(@Nonnull Location.Link link) {
    requireNonNull(link, "link");
    checkArgument(link.getPoint().equals(this.getReference()),
                  "point end of link is not this point");
    return attachedLinks.add(link);
  }

  /**
   * Detaches a link from this point.
   *
   * @param locRef The location end of the link to be detached from this point.
   * @return <code>true</code> if, and only if, there was a link to the given
   * location attached to this point.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean detachLink(@Nonnull TCSObjectReference<Location> locRef) {
    requireNonNull(locRef, "locRef");
    Iterator<Location.Link> linkIter = attachedLinks.iterator();
    while (linkIter.hasNext()) {
      Location.Link curLink = linkIter.next();
      if (locRef.equals(curLink.getLocation())) {
        linkIter.remove();
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a copy of this object, with the given attached links.
   *
   * @param attachedLinks The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Point withAttachedLinks(Set<Location.Link> attachedLinks) {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Point clone() {
    return new Point(getIdWithoutDeprecationWarning(),
                     getName(),
                     getProperties(),
                     position,
                     type,
                     vehicleOrientationAngle,
                     incomingPaths,
                     outgoingPaths,
                     attachedLinks,
                     occupyingVehicle);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
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
}
