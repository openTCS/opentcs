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
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * This class declares and defines methods common to all data objects in the
 * openTCS system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
public abstract class TCSObject<E extends TCSObject<E>>
    implements Serializable,
               Cloneable {

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
   */
  protected TCSObject(int objectID, @Nonnull String objectName) {
    requireNonNull(objectName, "objectName");
    checkArgument(!objectName.isEmpty(), "objectName is empty");

    id = objectID;
    name = objectName;
    reference = new TCSObjectReference<>(this);
  }

  /**
   * Returns this object's ID.
   *
   * @return This object's ID.
   */
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
   */
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
   */
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
   */
  public void clearProperties() {
    properties.clear();
  }

  /**
   * Returns this object's name.
   *
   * @return This object's name.
   */
  @Override
  public String toString() {
    return getClass() + "[id=" + id + ", name=" + name + "]";
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
      return id == other.id && this.getClass().equals(other.getClass());
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
    return Long.valueOf(id).hashCode()
        ^ this.getClass().getName().hashCode();
  }

  /**
   * Returns a distinct copy of this object.
   *
   * @return A distinct copy of this object.
   */
  @SuppressWarnings("unchecked")
  @Override
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
}
