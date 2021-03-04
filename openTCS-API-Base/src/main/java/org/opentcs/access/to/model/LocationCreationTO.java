/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.Triple;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a location in a plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The name of this location's type.
   */
  @Nonnull
  private String typeName;
  /**
   * This location's position (in mm).
   */
  @Nonnull
  private Triple position;
  /**
   * The links attaching points to this location.
   * This is a map of point names to allowed operations.
   */
  @Nonnull
  private Map<String, Set<String>> links = new HashMap<>();

  /**
   * Creates a new isntance.
   *
   * @param name The name of this location.
   * @param typeName The name of this location's type.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public LocationCreationTO(@Nonnull String name, @Nonnull String typeName) {
    this(name, typeName, new Triple(0, 0, 0));
  }

  /**
   * Creates a new instance
   * @param name The name of this location.
   * @param typeName The name of this location's type.
   * @param position The position of this location.
   */
  public LocationCreationTO(@Nonnull String name,
                            @Nonnull String typeName,
                            @Nonnull Triple position) {
    super(name);
    this.typeName = requireNonNull(typeName, "typeName");
    this.position = position;
  }

  private LocationCreationTO(@Nonnull String name,
                             @Nonnull Map<String, String> properties,
                             @Nonnull String typeName,
                             @Nonnull Triple position,
                             @Nonnull Map<String, Set<String>> links) {
    super(name, properties);
    this.typeName = requireNonNull(typeName, "typeName");
    this.position = requireNonNull(position, "position");
    this.links = requireNonNull(links, "links");
  }

  /**
   * Sets the name of this location.
   *
   * @param name The new name.
   * @return The mocdified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationCreationTO setName(@Nonnull String name) {
    return (LocationCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public LocationCreationTO withName(@Nonnull String name) {
    return new LocationCreationTO(name, getModifiableProperties(), typeName, position, links);
  }

  /**
   * Returns the name of this location's type.
   *
   * @return The name of this location's type.
   */
  @Nonnull
  public String getTypeName() {
    return typeName;
  }

  /**
   * Sets the name of this location's type.
   *
   * @param typeName The new location type.
   * @return The modified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public LocationCreationTO setTypeName(@Nonnull String typeName) {
    this.typeName = requireNonNull(typeName, "typeName");
    return this;
  }

  /**
   * Creates a copy of this object with the location's type.
   *
   * @param typeName The location type.
   * @return A copy of this object, differing in the given type.
   */
  public LocationCreationTO withTypeName(@Nonnull String typeName) {
    return new LocationCreationTO(getName(), getModifiableProperties(), typeName, position, links);
  }

  /**
   * Returns the position of this location (in mm).
   *
   * @return The position of this location (in mm).
   */
  @Nonnull
  public Triple getPosition() {
    return position;
  }

  /**
   * Sets the position of this location (in mm).
   *
   * @param position The position of this location (in mm).
   * @return The modified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public LocationCreationTO setPosition(@Nonnull Triple position) {
    this.position = requireNonNull(position, "position");
    return this;
  }

  /**
   * Creates a copy of this object with the given position (in mm).
   *
   * @param position the new position of this location (in mm).
   * @return A copy of this object, differing in the given position.
   */
  public LocationCreationTO withPosition(@Nonnull Triple position) {
    return new LocationCreationTO(getName(), getModifiableProperties(), typeName, position, links);
  }

  /**
   * Returns the links attaching points to this location.
   * This is a map of point names to allowed operations.
   *
   * @return The links attaching points to this location.
   */
  @Nonnull
  public Map<String, Set<String>> getLinks() {
    return Collections.unmodifiableMap(links);
  }

  /**
   * Sets the links attaching points to this location.
   *
   * @param links The new links. This is supposed to be a map of point names to allowed operations.
   * @return The modified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public LocationCreationTO setLinks(@Nonnull Map<String, Set<String>> links) {
    this.links = requireNonNull(links, "links");
    return this;
  }

  /**
   * Creates a copy of this object with the given links that attach points to this location.
   *
   * @param links the new links. This is supposed to be a map of point names to allowed operations.
   * @return A copy of this object, differing in the given links.
   */
  public LocationCreationTO withLinks(@Nonnull Map<String, Set<String>> links) {
    return new LocationCreationTO(getName(), getModifiableProperties(), typeName, position, links);
  }

  /**
   * Creates a copy of this object with the given links that attach points to this location.
   *
   * @param pointName The name of the point linked to.
   * @param allowedOperations The operations allowed at the point.
   * @return A copy of this object, differing in the given link.
   */
  public LocationCreationTO withLink(@Nonnull String pointName,
                                     @Nonnull Set<String> allowedOperations) {
    return new LocationCreationTO(getName(),
                                  getModifiableProperties(),
                                  typeName,
                                  position,
                                  mapWithMapping(links, pointName, allowedOperations));
  }

  /**
   * Sets the properties of this location.
   *
   * @param properties The new properties.
   * @return The modified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (LocationCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public LocationCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new LocationCreationTO(getName(), properties, typeName, position, links);
  }

  /**
   * Sets a single property of this location.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified location.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (LocationCreationTO) super.setProperty(key, value);
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
  public LocationCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new LocationCreationTO(getName(), propertiesWith(key, value), typeName, position, links);
  }
}
