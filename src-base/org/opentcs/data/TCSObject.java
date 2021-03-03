/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This class declares and defines methods common to all data objects in the
 * openTCS system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
public abstract class TCSObject<E extends TCSObject<E>>
    implements Serializable, Cloneable {

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   */
  public static final Comparator<TCSObject<?>> idComparator =
      new IDComparator();
  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  public static final Comparator<TCSObject<?>> nameComparator =
      new NameComparator();
  /**
   * This class's Logger.
   */
  private static final Logger log = Logger.getLogger(TCSObject.class.getName());
  /**
   * A transient reference to this business object.
   */
  protected TCSObjectReference<E> reference;
  /**
   * A set of properties (key-value pairs) associated with this object.
   */
  private Map<String, String> properties = new HashMap<>();
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
  protected TCSObject(int objectID, String objectName) {
    log.finer("method entry");
    if (objectID < 0) {
      throw new IllegalArgumentException("objectID is negative");
    }
    if (objectName == null) {
      throw new NullPointerException("objectName is null");
    }
    if (objectName.isEmpty()) {
      throw new IllegalArgumentException("objectName is empty String");
    }
    id = objectID;
    name = objectName;
    reference = new TCSObjectReference<>(this);
  }

  // Methods not declared in any interface start here
  /**
   * Returns this object's ID.
   *
   * @return This object's ID.
   */
  public final int getId() {
    log.finer("method entry");
    return id;
  }

  /**
   * Returns this object's name.
   *
   * @return This object's name.
   */
  public final String getName() {
    log.finer("method entry");
    return name;
  }

  /**
   * Sets this object's name.
   *
   * @param newName This object's new name.
   */
  public final void setName(String newName) {
    log.finer("method entry");
    Objects.requireNonNull(newName, "newName is null. Current name: " + name);
    if (newName.isEmpty()) {
      throw new IllegalArgumentException(
          "newName is empty String. Current name: " + name);
    }
    name = newName;
    reference.setName(newName);
  }

  /**
   * Returns a transient/soft reference to this object.
   *
   * @return A transient/soft reference to this object.
   */
  public TCSObjectReference<E> getReference() {
    log.finer("method entry");
    return reference;
  }

  /**
   * Returns this object's properties.
   *
   * @return This object's properties.
   */
  public Map<String, String> getProperties() {
    return new HashMap<>(properties);
  }

  /**
   * Sets a property for this object.
   *
   * @param key The new property's key.
   * @param value The new property's value. If <code>null</code>, removes the
   * property from this object.
   */
  public void setProperty(String key, String value) {
    if (key == null) {
      throw new NullPointerException("key is null");
    }
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

  // Methods inherited from Object start here.
  /**
   * Returns this object's name.
   *
   * @return This object's name.
   */
  @Override
  public String toString() {
    return name;
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
    log.finer("method entry");
    if (obj instanceof TCSObject) {
      TCSObject other = (TCSObject) obj;
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
    log.finer("method entry");
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
    log.finer("method entry");
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
    return clone;
  }

  // Private classes start here.
  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their IDs.
   */
  private static final class IDComparator
      implements Comparator<TCSObject<?>> {

    /**
     * Creates a new IDComparator.
     */
    private IDComparator() {
    }

    @Override
    public int compare(TCSObject<?> o1, TCSObject<?> o2) {
      return o1.getId() - o2.getId();
    }
  }

  /**
   * A <code>Comparator</code> for ordering <code>TCSObject</code>s ascendingly
   * by their names.
   */
  private static final class NameComparator
      implements Comparator<TCSObject<?>> {

    /**
     * Creates a new NameComparator.
     */
    private NameComparator() {
    }

    @Override
    public int compare(TCSObject<?> o1, TCSObject<?> o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
