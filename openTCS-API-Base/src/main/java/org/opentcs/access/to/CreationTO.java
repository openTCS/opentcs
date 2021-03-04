/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.util.annotations.ScheduledApiChange;

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

  protected CreationTO(@Nonnull String name, @Nonnull Map<String, String> properties) {
    this.name = requireNonNull(name, "name");
    this.properties = requireNonNull(properties, "properties");
  }

  /**
   * Returns the name of this transfer object.
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
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public CreationTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name the new name
   * @return A copy of this object, differing in the given value.
   */
  public CreationTO withName(@Nonnull String name) {
    return new CreationTO(name,
                          properties);
  }

  /**
   * Returns the properties of this transfer object in an unmodifiable map.
   *
   * @return The properties of this transfer object in an unmodifiable map.
   */
  @Nonnull
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(properties);
  }

  /**
   * Returns the properties of this transfer object.
   *
   * @return The properties of this transfer object.
   */
  protected Map<String, String> getModifiableProperties() {
    return properties;
  }

  /**
   * Sets the properties of this transfer object.
   *
   * @param properties The new properties.
   * @return The modified transfer object.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public CreationTO setProperties(@Nonnull Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  public CreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new CreationTO(name, properties);
  }

  /**
   * Sets a single property of this transfer object.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified transfer object.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public CreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    requireNonNull(key, "key");
    requireNonNull(value, "value");
    properties.put(key, value);
    return this;
  }

  /**
   * Creates a copy of this object with the given property.
   * If value == null is true then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that includes the given property or
   * removes the entry, if value == null.
   */
  public CreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new CreationTO(name,
                          propertiesWith(key, value));
  }

  protected final Map<String, String> propertiesWith(String key, String value) {
    return mapWithMapping(properties, key, value);
  }

  /**
   * Returns a new map, with the mappings of the given map and the given mapping added to it.
   *
   * @param <K> The type of the map's keys.
   * @param <V> The type of the map's values.
   * @param map The map to be extended.
   * @param key The key.
   * @param value The value. May be <code>null</code> to remove the mapping from the given map.
   * @return a new map, with the mappings of the given map and the given mapping added to it.
   */
  protected static final <K, V> Map<K, V> mapWithMapping(Map<K, V> map, K key, V value) {
    requireNonNull(map, "map");
    requireNonNull(key, "key");

    Map<K, V> result = new HashMap<>(map);

    if (value == null) {
      result.remove(key);
    }
    else {
      result.put(key, value);
    }

    return result;
  }

  /**
   * Returns a new list, with the elements of the given list and the given element added to it.
   *
   * @param <T> The element type of the list.
   * @param list The list to be extended.
   * @param newElement The element to be added to the list.
   * @return A new list, consisting of the given list and the given element added to it.
   */
  protected static final <T> List<T> listWithAppendix(List<T> list, T newElement) {
    List<T> result = new ArrayList<>(list.size() + 1);
    result.addAll(list);
    result.add(newElement);
    return result;
  }
}
