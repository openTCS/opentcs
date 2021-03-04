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
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import org.opentcs.data.TCSObjectReference;

/**
 * A location at which a vehicle may perform an action.
 * <ul>
 * <li>A <code>Location</code> must be linked to at least one <code>Point</code>
 * to be reachable for a vehicle.</li>
 * <li>It may be linked to multiple <code>Point</code>s.</li>
 * <li>As long as a link's specific set of allowed operations is empty (which is
 * the default), all operations defined by the <code>Location</code>'s
 * referenced <code>LocationType</code> are allowed at the linked
 * <code>Point</code>. If the link's set of allowed operations is not empty,
 * only the operations contained in it are allowed at the linked
 * <code>Point</code>.</li>
 * </ul>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Location
    extends TCSResource<Location>
    implements Serializable,
               Cloneable {

  /**
   * This location's position in mm.
   */
  private Triple position = new Triple(0, 0, 0);
  /**
   * A reference to this location's type.
   */
  private TCSObjectReference<LocationType> type;
  /**
   * A set of links attached to this location.
   */
  private Set<Link> attachedLinks = new HashSet<>();

  /**
   * Creates a new Location.
   *
   * @param objectID The new location's object ID.
   * @param name The new location's name.
   * @param locationType The new location's type.
   */
  public Location(int objectID,
                  String name,
                  TCSObjectReference<LocationType> locationType) {
    super(objectID, name);
    type = Objects.requireNonNull(locationType, "locationType is null");
  }

  /**
   * Returns the physical coordinates of this location in mm.
   *
   * @return The physical coordinates of this location in mm.
   */
  public Triple getPosition() {
    return position;
  }

  /**
   * Sets the physical coordinates of this location in mm.
   *
   * @param newPosition The new physical coordinates of this location. May not
   * be <code>null</code>.
   */
  public void setPosition(Triple newPosition) {
    position = requireNonNull(newPosition, "newPosition is null");
  }

  /**
   * Returns a reference to the type of this location.
   *
   * @return A reference to the type of this location.
   */
  public TCSObjectReference<LocationType> getType() {
    return type;
  }

  /**
   * Sets this location's type.
   *
   * @param newType This location's new type.
   */
  public void setType(TCSObjectReference<LocationType> newType) {
    type = Objects.requireNonNull(newType, "newType is null");
  }

  /**
   * Returns a set of links attached to this location.
   *
   * @return A set of links attached to this location.
   */
  public Set<Link> getAttachedLinks() {
    return new HashSet<>(attachedLinks);
  }

  /**
   * Attaches a link to this location.
   *
   * @param newLink The link to be attached to this location.
   * @return <code>true</code> if, and only if, the given link was not already
   * attached to this location.
   * @throws IllegalArgumentException If the location end of the given link is
   * not this location.
   */
  public boolean attachLink(Link newLink) {
    Objects.requireNonNull(newLink, "newLink is null");
    if (!newLink.getLocation().equals(this.getReference())) {
      throw new IllegalArgumentException(
          "location end of link is not this location");
    }
    return attachedLinks.add(newLink);
  }

  /**
   * Detaches a link from this location.
   *
   * @param pointRef The point end of the link to be detached from this
   * location.
   * @return <code>true</code> if, and only if, there was a link to the given
   * point attached to this location.
   */
  public boolean detachLink(TCSObjectReference<Point> pointRef) {
    Objects.requireNonNull(pointRef, "pointRef is null");
    Iterator<Link> linkIter = attachedLinks.iterator();
    while (linkIter.hasNext()) {
      Link curLink = linkIter.next();
      if (pointRef.equals(curLink.getPoint())) {
        linkIter.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public Location clone() {
    Location clone = (Location) super.clone();
    clone.position = (position == null) ? null : position.clone();
    clone.type = type.clone();
    clone.attachedLinks = new HashSet<>();
    for (Link curLink : attachedLinks) {
      clone.attachedLinks.add(curLink.clone());
    }
    return clone;
  }

  /**
   * A link connecting a point and a location, expressing that the location is
   * reachable from the point.
   */
  public static class Link
      implements Serializable,
                 Cloneable {

    /**
     * A reference to the location end of this link.
     */
    private TCSResourceReference<Location> location;
    /**
     * A reference to the point end of this link.
     */
    private TCSResourceReference<Point> point;
    /**
     * The operations allowed at this link.
     */
    private Set<String> allowedOperations = new TreeSet<>();

    /**
     * Creates a new Link.
     *
     * @param linkLocation A reference to the location end of this link.
     * @param linkPoint A reference to the point end of this link.
     */
    public Link(TCSResourceReference<Location> linkLocation,
                TCSResourceReference<Point> linkPoint) {
      location = Objects.requireNonNull(linkLocation, "linkLocation is null");
      point = Objects.requireNonNull(linkPoint, "linkPoint is null");
    }

    /**
     * Returns a reference to the location end of this link.
     *
     * @return A reference to the location end of this link.
     */
    public TCSResourceReference<Location> getLocation() {
      return location;
    }

    /**
     * Returns a reference to the point end of this link.
     *
     * @return A reference to the point end of this link.
     */
    public TCSResourceReference<Point> getPoint() {
      return point;
    }

    /**
     * Returns the operations allowed at this link.
     *
     * @return The operations allowed at this link.
     */
    public Set<String> getAllowedOperations() {
      return new TreeSet<>(allowedOperations);
    }

    /**
     * Checks if a vehicle is allowed to execute a given operation at this link.
     *
     * @param operation The operation to be checked.
     * @return <code>true</code> if, and only if, vehicles are allowed to
     * execute the given operation at his link.
     */
    public boolean hasAllowedOperation(String operation) {
      Objects.requireNonNull(operation, "operation is null");
      return allowedOperations.contains(operation);
    }

    /**
     * Removes all allowed operations from this link.
     */
    public void clearAllowedOperations() {
      allowedOperations.clear();
    }

    /**
     * Adds an allowed operation.
     *
     * @param operation The operation to be allowed.
     * @return <code>true</code> if, and only if, the given operation wasn't
     * already allowed before.
     */
    public boolean addAllowedOperation(String operation) {
      Objects.requireNonNull(operation, "operation is null");
      return allowedOperations.add(operation);
    }

    /**
     * Removes an allowed operation.
     *
     * @param operation The operation to be disallowed.
     * @return <code>true</code> if, and only if, the given operation was
     * allowed before.
     */
    public boolean removeAllowedOperation(String operation) {
      Objects.requireNonNull(operation, "operation is null");
      return allowedOperations.remove(operation);
    }

    /**
     * Checks if this object is equal to another one.
     * Two <code>Link</code>s are equal if they both reference the same location
     * and point ends.
     *
     * @param obj The object to compare this one to.
     * @return <code>true</code> if, and only if, <code>obj</code> is also a
     * <code>Link</code> and reference the same location and point ends as this
     * one.
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Link) {
        Link other = (Link) obj;
        return point.equals(other.getPoint())
            && location.equals(other.getLocation());
      }
      else {
        return false;
      }
    }

    /**
     * Returns a hash code for this link.
     * The hash code of a <code>Location.Link</code> is computed as the
     * exclusive OR (XOR) of the hash codes of the associated location and point
     * references.
     *
     * @return A hash code for this link.
     */
    @Override
    public int hashCode() {
      return location.hashCode() ^ point.hashCode();
    }

    @Override
    public Link clone() {
      Link clone;
      try {
        clone = (Link) super.clone();
      }
      catch (CloneNotSupportedException exc) {
        throw new IllegalStateException("Unexpected exception", exc);
      }
      clone.location = location.clone();
      clone.point = point.clone();
      clone.allowedOperations = getAllowedOperations();
      return clone;
    }
  }
}
