/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a location type in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationTypeCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The allowed operations for this location type.
   */
  private List<String> allowedOperations = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this location type.
   */
  public LocationTypeCreationTO(@Nonnull String name) {
    super(name);
  }

  /**
   * Returns the allowed operations for this location type.
   *
   * @return The allowed operations for this location type.
   */
  @Nonnull
  public List<String> getAllowedOperations() {
    return allowedOperations;
  }

  /**
   * Sets the allowed operations for this location type.
   *
   * @param allowedOperations The new allowed operations.
   * @return The modified location type.
   */
  @Nonnull
  public LocationTypeCreationTO setAllowedOperations(@Nonnull List<String> allowedOperations) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

  /**
   * Sets the name of this location type.
   *
   * @param name The new name.
   * @return The modified location type.
   */
  @Nonnull
  @Override
  public LocationTypeCreationTO setName(@Nonnull String name) {
    return (LocationTypeCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this location type.
   *
   * @param properties The new properties.
   * @return The modified location type.
   */
  @Nonnull
  @Override
  public LocationTypeCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (LocationTypeCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this location type.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified location type.
   */
  @Nonnull
  @Override
  public LocationTypeCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (LocationTypeCreationTO) super.setProperty(key, value);
  }
}
