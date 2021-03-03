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
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * Instances of this class provide transient references to business objects.
 * They can be used to prevent serialization of whole object graphs but still
 * keep a reference to the actual object (i.e. its ID and name, which both are
 * unique in a model).
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
public class TCSObjectReference<E extends TCSObject<E>>
    implements Serializable, Cloneable {

  /**
   * The referenced object.
   * (Transient to prevent serialization of whole object graphs.)
   */
  private transient TCSObject<E> referent;
  /**
   * The referenced object's class.
   */
  private final Class<?> referentClass;
  /**
   * The referenced object's ID.
   */
  private final int id;
  /**
   * Indicates whether this reference is really a reference to an object (false)
   * or a dummy reference without a real object (true).
   */
  private final boolean dummy;
  /**
   * The referenced object's name.
   */
  private String name;

  /**
   * Creates a new TCSObjectReference.
   *
   * @param newReferent The object this reference references.
   */
  protected TCSObjectReference(@Nonnull TCSObject<E> newReferent) {
    referent = requireNonNull(newReferent, "newReferent");
    referentClass = referent.getClass();
    id = referent.getId();
    name = referent.getName();
    dummy = false;
  }

  /**
   * Creates a dummy reference, referencing nothing.
   *
   * @param clazz The class of the object being referenced.
   * @param newName The new reference's name.
   */
  private TCSObjectReference(@Nonnull Class<?> clazz, @Nonnull String newName) {
    name = requireNonNull(newName, "newName");
    referentClass = requireNonNull(clazz, "clazz");
    referent = null;
    id = Integer.MAX_VALUE;
    dummy = true;
  }

  /**
   * Returns the referenced object's class.
   *
   * @return The referenced object's class.
   */
  public Class<?> getReferentClass() {
    return referentClass;
  }

  /**
   * Returns the referenced object's ID.
   *
   * @return The referenced object's ID.
   */
  public final int getId() {
    return id;
  }

  /**
   * Returns the referenced object's name.
   *
   * @return The referenced object's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Sets the referenced object's name.
   * Note that this method can be used to change the name stored inside this
   * reference to the referenced object's actual name - it cannot be used to
   * change the name of the referenced object.
   *
   * @param newName The referenced object's new name.
   */
  public final void setName(@Nonnull String newName) {
    name = requireNonNull(newName, "newName");
  }

  /**
   * Returns <code>false</code> if this reference really references an object,
   * or <code>true</code> if it's a dummy reference without a real object.
   *
   * @return <code>false</code> if this reference really references an object,
   * or <code>true</code> if it's a dummy reference without a real object.
   */
  public boolean isDummy() {
    return dummy;
  }

  /**
   * Indicates whether a TCSObjectReference is equal to another one.
   * Two TCSObjectReferences are equal if
   * <ul>
   * <li>the IDs of the TCSObjects they refer to are equal and
   * <li>the classes of the TCSObjects they refer to are equal.
   * </ul>
   *
   * @param otherObj The object to check for equality.
   * @return <code>true</code> if <code>otherObj</code> is not
   * <code>null</code>, is a TCSObjectReference, too, and both its ID and
   * the implementing class of TCSObject refers to are equal, else
   * <code>false</code>.
   */
  @Override
  public boolean equals(Object otherObj) {
    if (otherObj instanceof TCSObjectReference) {
      TCSObjectReference<?> otherRef = (TCSObjectReference<?>) otherObj;
      return id == otherRef.id;
    }
    else {
      return false;
    }
  }

  /**
   * Returns a hash code for this TCSObjectReference.
   * The hash code for a TCSObjectReference is computed as the exclusive
   * OR (XOR) of the hash codes of the ID and the class name of the TCSObject
   * the reference refers to.
   *
   * @return A hash code for this TCSObjectReference.
   */
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "[" + id + "]:" + name;
  }

  /**
   * Returns a distinct copy of this reference.
   * The clone's <code>referent</code> attribute is set to <code>null</code> to
   * prevent the actual referenced object to be leaked outside the kernel.
   *
   * @return A distinct copy of this reference, with its <code>referent</code>
   * attribute set to <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  @Override
  public TCSObjectReference<E> clone() {
    TCSObjectReference<E> clone = null;
    try {
      clone = (TCSObjectReference<E>) super.clone();
    }
    catch (CloneNotSupportedException exc) {
      throw new RuntimeException("Unexpected exception", exc);
    }
    clone.referent = null;
    return clone;
  }

  /**
   * Returns a dummy reference, referencing nothing.
   *
   * @param <T> The type of the dummy reference to be returned.
   * @param clazz The class of the dummy reference to be returned.
   * @param name The name of the dummy reference to be returned.
   * @return A dummy reference, referencing nothing.
   */
  public static <T extends TCSObject<T>> TCSObjectReference<T>
      getDummyReference(Class<T> clazz, String name) {
    return new TCSObjectReference<>(clazz, name);
  }
}
