/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.Triple;

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
   * The link names attached to this location.
   */
  @Nonnull
  private Map<String, Set<String>> links = new HashMap<>();

  /**
   * Creates a new isntance.
   *
   * @param name The name of this location.
   * @param typeName The name of this location's type.
   */
  public LocationCreationTO(@Nonnull String name, @Nonnull String typeName) {
    super(name);
    this.typeName = requireNonNull(typeName, "typeName");
  }

  /**
   * Sets the name of this location.
   *
   * @param name The new name.
   * @return The mocdified location.
   */
  @Nonnull
  @Override
  public LocationCreationTO setName(@Nonnull String name) {
    return (LocationCreationTO) super.setName(name);
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
  @Nonnull
  public LocationCreationTO setTypeName(@Nonnull String typeName) {
    this.typeName = requireNonNull(typeName, "typeName");
    return this;
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
  @Nonnull
  public LocationCreationTO setPosition(@Nonnull Triple position) {
    this.position = requireNonNull(position, "position");
    return this;
  }

  /**
   * Returns the link names attached to this location.
   *
   * @return The link names attached to this location.
   */
  @Nonnull
  public Map<String, Set<String>> getLinks() {
    return links;
  }

  /**
   * Sets the link names attached to this location.
   *
   * @param links The new links.
   * @return The modified location.
   */
  @Nonnull
  public LocationCreationTO setLinks(@Nonnull Map<String, Set<String>> links) {
    this.links = requireNonNull(links, "links");
    return this;
  }

  /**
   * Sets the properties of this location.
   *
   * @param properties The new properties.
   * @return The modified location.
   */
  @Nonnull
  @Override
  public LocationCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (LocationCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this location.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified location.
   */
  @Nonnull
  @Override
  public LocationCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (LocationCreationTO) super.setProperty(key, value);
  }
}
