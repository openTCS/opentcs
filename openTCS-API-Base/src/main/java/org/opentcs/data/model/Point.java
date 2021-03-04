/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A Point is a node/position in the model graph.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Point
    extends TCSResource<Point>
    implements Serializable,
               Cloneable {

  /**
   * This point's coordinates in mm.
   */
  private Triple position = new Triple(0, 0, 0);
  /**
   * The vehicle's (assumed) orientation angle (-360..360) when it is at this
   * position.
   * May be Double.NaN if an orientation angle is not defined for this point.
   */
  private double vehicleOrientationAngle = Double.NaN;
  /**
   * This point's type.
   */
  private Type type = Type.HALT_POSITION;
  /**
   * A reference to the vehicle occupying this point.
   */
  private TCSObjectReference<Vehicle> occupyingVehicle;
  /**
   * A set of references to paths ending in this point.
   */
  private Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>();
  /**
   * A set of references to paths originating in this point.
   */
  private Set<TCSObjectReference<Path>> outgoingPaths = new HashSet<>();
  /**
   * A set of links attached to this point.
   */
  private Set<Location.Link> attachedLinks = new HashSet<>();

  /**
   * Creates a new point with the given name.
   *
   * @param objectID This point's object ID.
   * @param name This point's name.
   */
  public Point(int objectID, String name) {
    super(objectID, name);
  }

  // Methods not declared in any interface start here
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
   */
  public void setPosition(@Nonnull Triple position) {
    this.position = requireNonNull(position, "position");
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
   */
  public void setVehicleOrientationAngle(double angle) {
    if (!Double.isNaN(angle) && (angle > 360.0 || angle < -360.0)) {
      throw new IllegalArgumentException("angle not in [-360..360]: " + angle);
    }
    vehicleOrientationAngle = angle;
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
   */
  public void setType(@Nonnull Type type) {
    this.type = requireNonNull(type, "type");
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
   */
  public void setOccupyingVehicle(TCSObjectReference<Vehicle> newVehicle) {
    occupyingVehicle = newVehicle;
  }

  /**
   * Returns a set of references to paths ending in this point.
   *
   * @return A set of references to paths ending in this point.
   */
  public Set<TCSObjectReference<Path>> getIncomingPaths() {
    return incomingPaths;
  }

  /**
   * Adds a reference to a path ending in this point.
   * If the referenced path is already among the incoming paths of this point,
   * nothing happens.
   *
   * @param path A reference to the path to be added.
   */
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
   */
  public void removeIncomingPath(TCSObjectReference<Path> rmPath) {
    incomingPaths.remove(rmPath);
  }

  /**
   * Returns a set of references to paths originating in this point.
   *
   * @return A set of references to paths originating in this point.
   */
  public Set<TCSObjectReference<Path>> getOutgoingPaths() {
    return outgoingPaths;
  }

  /**
   * Adds a reference to a path originating in this point.
   * If the referenced path is already among the outgoing paths of this point,
   * nothing happens.
   *
   * @param path A reference to the path to be added.
   */
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
   */
  public void removeOutgoingPath(TCSObjectReference<Path> rmPath) {
    outgoingPaths.remove(rmPath);
  }

  /**
   * Returns a set of links attached to this point.
   *
   * @return A set of links attached to this point.
   */
  public Set<Location.Link> getAttachedLinks() {
    return new HashSet<>(attachedLinks);
  }

  /**
   * Attaches a link to this point.
   *
   * @param link The link to be attached to this point.
   * @return <code>true</code> if, and only if, the given link was not already
   * attached to this point.
   * @throws IllegalArgumentException If the point end of the given link is not
   * this point.
   */
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
   */
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

  @Override
  public Point clone() {
    Point clone = (Point) super.clone();
    clone.position = (position == null) ? null : position.clone();
    clone.occupyingVehicle
        = (occupyingVehicle == null) ? null : occupyingVehicle.clone();
    clone.incomingPaths = new HashSet<>();
    for (TCSObjectReference<Path> curRef : incomingPaths) {
      clone.incomingPaths.add(curRef.clone());
    }
    clone.outgoingPaths = new HashSet<>();
    for (TCSObjectReference<Path> curRef : outgoingPaths) {
      clone.outgoingPaths.add(curRef.clone());
    }
    clone.attachedLinks = new HashSet<>(attachedLinks);
    return clone;
  }

  /**
   * The elements of this enumeration describe the various types of positions
   * known in openTCS models.
   */
  public enum Type {

    /**
     * Indicates a position at which a vehicle is expected to report in.
     * Halting or even parking at such a position is not allowed.
     */
    REPORT_POSITION,
    /**
     * Indicates a position at which a vehicle may halt temporarily, e.g. for
     * executing an operation.
     * The vehicle is also expected to report in when it arrives at such a
     * position. It may not park here for longer than necessary, though.
     */
    HALT_POSITION,
    /**
     * Indicates a position at which a vehicle may halt for longer periods of
     * time when it is not processing orders.
     * The vehicle is also expected to report in when it arrives at such a
     * position.
     */
    PARK_POSITION;
  }
}
