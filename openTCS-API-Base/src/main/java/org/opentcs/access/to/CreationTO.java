/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * The base class for all creation transfer objects.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CreationTO
    implements Serializable {

  /**
   * The name of this transfer object.
   */
  @Nonnull
  private String name;

  /**
   * The properties of this transfer object.
   */
  @Nonnull
  private Map<String, String> properties = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this transfer object.
   */
  public CreationTO(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
  }

  /**
   * Returns the name this transfer object.
   *
   * @return The name of this transfer object.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this transfer object.
   *
   * @param name The new name.
   * @return The modified transfer object.
   */
  @Nonnull
  public CreationTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  /**
   * Returns the properties of this transfer object.
   *
   * @return The properties of this transfer object.
   */
  @Nonnull
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets the properties of this transfer object.
   *
   * @param properties The new properties.
   * @return The modified transfer object.
   */
  @Nonnull
  public CreationTO setProperties(@Nonnull Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  /**
   * Sets a single property of this transfer object.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified transfer object.
   */
  @Nonnull
  public CreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    requireNonNull(key, "key");
    requireNonNull(value, "value");
    properties.put(key, value);
    return this;
  }
}
