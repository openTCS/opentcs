/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes the base behaviour of TCS data objects.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public abstract class TCSObject<E extends TCSObject<E>>
    implements Serializable,
               Cloneable {

  /**
   * Holds the next ID.
   */
  private static AtomicInteger nextId = new AtomicInteger();
  /**
   * A transient reference to this business object.
   */
  protected TCSObjectReference<E> reference;
  /**
   * A set of properties (key-value pairs) associated with this object.
   */
  private Map<String, String> properties = new HashMap<>();
  /**
   * An unmodifiable view on this object's properties.
   * This mainly exists for {@link #getProperties()}, as the alternative of
   * creating ad-hoc copies or unmodifiable views can lead to performance issues
   * related to garbage collection in situations where {@link #getProperties()}
   * is called often.
   */
  private Map<String, String> propertiesReadOnly = Collections.unmodifiableMap(properties);
  /**
   * This object's ID.
   */
  private final int id;
  /**
   * The name of the business object.
   */
  private String name;

  /**
   * Creates a new TCSObject.
   *
   * @param objectID The new object's ID.
   * @param objectName The new object's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected TCSObject(int objectID, @Nonnull String objectName) {
    requireNonNull(objectName, "objectName");

    id = objectID;
    name = objectName;
    reference = new TCSObjectReference<>(this);
  }

  /**
   * Creates a new TCSObject.
   *
   * @param objectName The new object's name.
   */
  protected TCSObject(@Nonnull String objectName) {
    this(objectName, new HashMap<>());
  }

  /**
   * Creates a new TCSObject.
   *
   * @param objectID The new object's ID.
   * @param objectName The new object's name.
   * @param properties A set of properties (key-value pairs) associated with this object.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected TCSObject(int objectID,
                      @Nonnull String objectName,
                      @Nonnull Map<String, String> properties) {
    this.id = objectID;
    this.name = requireNonNull(objectName, "objectName");
    this.properties = mapWithoutNullValues(properties);
    this.propertiesReadOnly = Collections.unmodifiableMap(this.properties);
    this.reference = new TCSObjectReference<>(this);
  }

  /**
   * Creates a new TCSObject.
   *
   * @param objectName The new object's name.
   * @param properties A set of properties (key-value pairs) associated with this object.
   */
  protected TCSObject(@Nonnull String objectName, @Nonnull Map<String, String> properties) {
    this.name = requireNonNull(objectName, "objectName");
    this.properties = mapWithoutNullValues(properties);
    this.propertiesReadOnly = Collections.unmodifiableMap(this.properties);
    this.id = nextId.getAndIncrement();
    this.reference = new TCSObjectReference<>(this);
  }

  /**
   * Returns this object's ID.
   *
   * @return This object's ID.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public int getId() {
    return id;
  }

  /**
   * Returns this object's name.
   *
   * @return This object's name.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Sets this object's name.
   *
   * @param newName This object's new name.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setName(@Nonnull String newName) {
    requireNonNull(newName, "newName");
    checkArgument(!newName.isEmpty(), "newName is empty string");
    name = newName;
    reference.setName(newName);
  }

  /**
   * Returns a transient/soft reference to this object.
   *
   * @return A transient/soft reference to this object.
   */
  public TCSObjectReference<E> getReference() {
    return reference;
  }

  /**
   * Returns an unmodifiable view on this object's properties.
   *
   * @return This object's properties.
   */
  @Nonnull
  public Map<String, String> getProperties() {
    return propertiesReadOnly;
  }

  /**
   * Returns the property value for the given key.
   * This is basically a shortcut for <code>getProperties().get(key)</code>.
   *
   * @param key The property's key.
   * @return The property value for the given key, or <code>null</code>, if there is none.
   */
  @Nullable
  public String getProperty(String key) {
    return properties.get(key);
  }

  /**
   * Sets a property for this object.
   *
   * @param key The new property's key.
   * @param value The new property's value. If <code>null</code>, removes the
   * property from this object.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setProperty(String key, String value) {
    if (value == null) {
      properties.remove(key);
    }
    else {
      properties.put(key, value);
    }
  }

  /**
   * Clears all of this object's properties.
   *
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void clearProperties() {
    properties.clear();
  }

  /**
   * Creates a copy of this object, with the given property integrated.
   *
   * @param key The key of the property to be changed.
   * @param value The new value of the property, or <code>null</code>, if the property is to be
   * removed.
   * @return A copy of this object, with the given property integrated.
   */
  public abstract TCSObject<E> withProperty(String key, String value);

  /**
   * Creates a copy of this object, with the given properties.
   *
   * @param properties The properties.
   * @return A copy of this object, with the given properties.
   */
  public abstract TCSObject<E> withProperties(Map<String, String> properties);

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{id=" + id + ", name=" + name + '}';
  }

  /**
   * Checks if this object is equal to another one.
   * Two <code>TCSObject</code>s are equal if both their IDs and their runtime
   * classes are equal.
   *
   * @param obj The object to compare this one to.
   * @return <code>true</code> if, and only if, <code>obj</code> is also a
   * <code>TCSObject</code> and both its ID and runtime class equal those of
   * this object.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TCSObject) {
      TCSObject<?> other = (TCSObject<?>) obj;
      return this.getName().equals(other.getName()) && this.getClass().equals(other.getClass());
    }
    else {
      return false;
    }
  }

  /**
   * Returns this object's hashcode.
   * A <code>TCSObject</code>'s hashcode is calculated by XORing its ID's
   * hashcode and the hashcode of its runtime class's name.
   *
   * @return This object's hashcode.
   */
  @Override
  public int hashCode() {
    return getName().hashCode()
        ^ this.getClass().getName().hashCode();
  }

  /**
   * Returns a distinct copy of this object.
   *
   * @return A distinct copy of this object.
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @SuppressWarnings("unchecked")
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TCSObject<E> clone() {
    TCSObject<E> clone;
    try {
      clone = (TCSObject<E>) super.clone();
    }
    catch (CloneNotSupportedException exc) {
      throw new RuntimeException("Unexpected exception", exc);
    }
    // Clone the reference, too, to prevent leakage of this object outside the
    // kernel.
    clone.reference = reference.clone();
    clone.properties = new HashMap<>(properties);
    clone.propertiesReadOnly = Collections.unmodifiableMap(clone.properties);
    return clone;
  }

  /**
   * Returns a new map of this object's properties, with the given property integrated.
   *
   * @param key The key of the property to be changed.
   * @param value The new value of the property, or <code>null</code>, if the property is to be
   * removed.
   * @return A new map of this object's properties, with the given property integrated.
   */
  protected final Map<String, String> propertiesWith(String key, String value) {
    requireNonNull(key, "key");

    Map<String, String> result = new HashMap<>(properties);
    if (value == null) {
      result.remove(key);
    }
    else {
      result.put(key, value);
    }
    return result;
  }

  /**
   * Returns a new map with the entries from the given map but all entries with <code>null</code>
   * values removed.
   *
   * @param <K> The type of the map's keys.
   * @param <V> The type of the map's values.
   * @param original The original map.
   * @return A new map with the entries from the given map but all entries with <code>null</code>
   * values removed.
   */
  protected static final <K, V> Map<K, V> mapWithoutNullValues(Map<K, V> original) {
    requireNonNull(original, "original");

    Map<K, V> result = new HashMap<>();
    for (Map.Entry<K, V> entry : original.entrySet()) {
      if (entry.getValue() != null) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  /**
   * Returns a new list with the values from the given list but all <code>null</code> values
   * removed.
   *
   * @param <V> The type of the list's values.
   * @param original The original list.
   * @return A new list with the values from the given list but all <code>null</code> values
   * removed.
   */
  protected static final <V> List<V> listWithoutNullValues(List<V> original) {
    requireNonNull(original, "original");

    return original.stream()
        .filter(value -> value != null)
        .collect(Collectors.toList());
  }

  /**
   * Returns a new set with the values from the given set but all <code>null</code> values removed.
   *
   * @param <V> The type of the set's values.
   * @param original The original set.
   * @return A new set with the values from the given set but all <code>null</code> values removed.
   */
  protected static final <V> Set<V> setWithoutNullValues(Set<V> original) {
    requireNonNull(original, "original");

    return original.stream()
        .filter(value -> value != null)
        .collect(Collectors.toSet());
  }
}
