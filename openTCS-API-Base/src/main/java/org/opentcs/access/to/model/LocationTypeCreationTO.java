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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.util.annotations.ScheduledApiChange;

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

  private LocationTypeCreationTO(@Nonnull String name,
                                 @Nonnull Map<String, String> properties,
                                 @Nonnull List<String> allowedOperations) {
    super(name, properties);
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");

  }

  /**
   * Returns the allowed operations for this location type.
   *
   * @return The allowed operations for this location type.
   */
  @Nonnull
  public List<String> getAllowedOperations() {
    return Collections.unmodifiableList(allowedOperations);
  }

  /**
   * Sets the allowed operations for this location type.
   *
   * @param allowedOperations The new allowed operations.
   * @return The modified location type.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public LocationTypeCreationTO setAllowedOperations(@Nonnull List<String> allowedOperations) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

  /**
   * Creates a copy of this object with the given allowed operations.
   *
   * @param allowedOperations the new allowed operations.
   * @return A copy of this object, differing in the given value.
   */
  public LocationTypeCreationTO withAllowedOperations(@Nonnull List<String> allowedOperations) {
    return new LocationTypeCreationTO(getName(), getModifiableProperties(), allowedOperations);
  }

  /**
   * Sets the name of this location type.
   *
   * @param name The new name.
   * @return The modified location type.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationTypeCreationTO setName(@Nonnull String name) {
    return (LocationTypeCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public LocationTypeCreationTO withName(@Nonnull String name) {
    return new LocationTypeCreationTO(name, getProperties(), allowedOperations);
  }

  /**
   * Sets the properties of this location type.
   *
   * @param properties The new properties.
   * @return The modified location type.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationTypeCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (LocationTypeCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public LocationTypeCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new LocationTypeCreationTO(getName(), properties, allowedOperations);
  }

  /**
   * Sets a single property of this location type.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified location type.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public LocationTypeCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (LocationTypeCreationTO) super.setProperty(key, value);
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
  public LocationTypeCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new LocationTypeCreationTO(getName(), propertiesWith(key, value), allowedOperations);
  }
}
