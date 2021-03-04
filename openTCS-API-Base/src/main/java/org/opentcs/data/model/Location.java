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
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A location at which a {@link Vehicle} may perform an action.
 * <p>
 * A location must be linked to at least one {@link Point} to be reachable for a vehicle.
 * It may be linked to multiple points.
 * As long as a link's specific set of allowed operations is empty (which is the default), all
 * operations defined by the location's referenced {@link LocationType} are allowed at the linked
 * point.
 * If the link's set of allowed operations is not empty, only the operations contained in it are
 * allowed at the linked point.
 * </p>
 *
 * @see LocationType
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
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
  private final Set<Link> attachedLinks;

  /**
   * Creates a new Location.
   *
   * @param objectID The new location's object ID.
   * @param name The new location's name.
   * @param type The new location's type.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Location(int objectID, String name, TCSObjectReference<LocationType> type) {
    super(objectID, name);
    this.type = requireNonNull(type, "type");
    this.position = new Triple(0, 0, 0);
    this.attachedLinks = new HashSet<>();
  }

  /**
   * Creates a new Location.
   *
   * @param name The new location's name.
   * @param type The new location's type.
   */
  public Location(String name, TCSObjectReference<LocationType> type) {
    super(name);
    this.type = requireNonNull(type, "type");
    this.position = new Triple(0, 0, 0);
    this.attachedLinks = new HashSet<>();
  }

  @SuppressWarnings("deprecation")
  private Location(int objectID,
                   String name,
                   Map<String, String> properties,
                   TCSObjectReference<LocationType> locationType,
                   Triple position,
                   Set<Link> attachedLinks) {
    super(objectID, name, properties);
    type = requireNonNull(locationType, "locationType");
    this.position = requireNonNull(position, "position");
    this.attachedLinks = new HashSet<>(requireNonNull(attachedLinks, "attachedLinks"));
  }

  @Override
  public Location withProperty(String key, String value) {
    return new Location(getIdWithoutDeprecationWarning(),
                        getName(),
                        propertiesWith(key, value),
                        type,
                        position,
                        attachedLinks);
  }

  @Override
  public Location withProperties(Map<String, String> properties) {
    return new Location(getIdWithoutDeprecationWarning(),
                        getName(),
                        properties,
                        type,
                        position,
                        attachedLinks);
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setPosition(Triple newPosition) {
    position = requireNonNull(newPosition, "newPosition is null");
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Location withPosition(Triple position) {
    return new Location(getIdWithoutDeprecationWarning(),
                        getName(),
                        getProperties(),
                        type,
                        position,
                        attachedLinks);
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setType(TCSObjectReference<LocationType> newType) {
    type = requireNonNull(newType, "newType");
  }

  /**
   * Returns a set of links attached to this location.
   *
   * @return A set of links attached to this location.
   */
  public Set<Link> getAttachedLinks() {
    return Collections.unmodifiableSet(attachedLinks);
  }

  /**
   * Attaches a link to this location.
   *
   * @param newLink The link to be attached to this location.
   * @return <code>true</code> if, and only if, the given link was not already
   * attached to this location.
   * @throws IllegalArgumentException If the location end of the given link is
   * not this location.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean attachLink(Link newLink) {
    requireNonNull(newLink, "newLink");
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean detachLink(TCSObjectReference<Point> pointRef) {
    requireNonNull(pointRef, "pointRef");
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

  /**
   * Creates a copy of this object, with the given attached links.
   *
   * @param attachedLinks The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Location withAttachedLinks(@Nonnull Set<Link> attachedLinks) {
    return new Location(getIdWithoutDeprecationWarning(),
                        getName(),
                        getProperties(),
                        type,
                        position,
                        attachedLinks);
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Location clone() {
    return new Location(getIdWithoutDeprecationWarning(),
                        getName(),
                        getProperties(),
                        type,
                        position,
                        attachedLinks);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

  /**
   * A link connecting a point and a location, expressing that the location is
   * reachable from the point.
   */
  @ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
  public static class Link
      implements Serializable,
                 Cloneable {

    /**
     * A reference to the location end of this link.
     */
    private final TCSResourceReference<Location> location;
    /**
     * A reference to the point end of this link.
     */
    private final TCSResourceReference<Point> point;
    /**
     * The operations allowed at this link.
     */
    private final Set<String> allowedOperations;

    /**
     * Creates a new Link.
     *
     * @param location A reference to the location end of this link.
     * @param point A reference to the point end of this link.
     */
    public Link(TCSResourceReference<Location> location,
                TCSResourceReference<Point> point) {
      this.location = requireNonNull(location, "location");
      this.point = requireNonNull(point, "point");
      this.allowedOperations = new TreeSet<>();
    }

    private Link(TCSResourceReference<Location> location,
                 TCSResourceReference<Point> point,
                 Set<String> allowedOperations) {
      this.location = requireNonNull(location, "location");
      this.point = requireNonNull(point, "point");
      this.allowedOperations = new TreeSet<>(requireNonNull(allowedOperations,
                                                            "allowedOperations"));
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
      return Collections.unmodifiableSet(allowedOperations);
    }

    /**
     * Checks if a vehicle is allowed to execute a given operation at this link.
     *
     * @param operation The operation to be checked.
     * @return <code>true</code> if, and only if, vehicles are allowed to
     * execute the given operation at his link.
     */
    public boolean hasAllowedOperation(String operation) {
      requireNonNull(operation, "operation");
      return allowedOperations.contains(operation);
    }

    /**
     * Removes all allowed operations from this link.
     *
     * @deprecated Will become immutable.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    public void clearAllowedOperations() {
      allowedOperations.clear();
    }

    /**
     * Adds an allowed operation.
     *
     * @param operation The operation to be allowed.
     * @return <code>true</code> if, and only if, the given operation wasn't
     * already allowed before.
     * @deprecated Will become immutable.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    public boolean addAllowedOperation(String operation) {
      requireNonNull(operation, "operation");
      return allowedOperations.add(operation);
    }

    /**
     * Removes an allowed operation.
     *
     * @param operation The operation to be disallowed.
     * @return <code>true</code> if, and only if, the given operation was
     * allowed before.
     * @deprecated Will become immutable.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    public boolean removeAllowedOperation(String operation) {
      requireNonNull(operation, "operation");
      return allowedOperations.remove(operation);
    }

    /**
     * Creates a copy of this object, with the given allowed operations.
     *
     * @param allowedOperations The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Link withAllowedOperations(Set<String> allowedOperations) {
      return new Link(location, point, allowedOperations);
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

    /**
     * {@inheritDoc}
     *
     * @deprecated Will become immutable and not implement Cloneable any more.
     */
    @Override
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    public Link clone() {
      return new Link(location, point, allowedOperations);
    }
  }
}
