/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transient reference to a {@link TCSObject}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class TCSObjectReference<E extends TCSObject<E>>
    implements Serializable,
               Cloneable {

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
   * @param referent The object this reference references.
   */
  @SuppressWarnings("deprecation")
  protected TCSObjectReference(@Nonnull TCSObject<E> referent) {
    requireNonNull(referent, "newReferent");

    referentClass = referent.getClass();
    id = referent.getId();
    name = referent.getName();
    dummy = false;
  }

  private TCSObjectReference(@Nonnull Class<?> clazz, @Nonnull String newName) {
    name = requireNonNull(newName, "newName");
    referentClass = requireNonNull(clazz, "clazz");
    id = Integer.MAX_VALUE;
    dummy = true;
  }

  private TCSObjectReference(@Nonnull Class<?> clazz,
                             @Nonnull String newName,
                             int id,
                             boolean dummy) {
    referentClass = requireNonNull(clazz, "clazz");
    name = requireNonNull(newName, "newName");
    this.id = id;
    this.dummy = dummy;
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
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public final void setName(@Nonnull String newName) {
    name = requireNonNull(newName, "newName");
  }

  /**
   * Returns <code>false</code> if this reference really references an object,
   * or <code>true</code> if it's a dummy reference without a real object.
   *
   * @return <code>false</code> if this reference really references an object,
   * or <code>true</code> if it's a dummy reference without a real object.
   * @deprecated Dummy references are deprecated. In case of drive order destinations, use
   * {@link DriveOrder.Destination#Destination(org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean isDummy() {
    return dummy;
  }

  @Override
  public boolean equals(Object otherObj) {
    if (otherObj instanceof TCSObjectReference) {
      TCSObjectReference<?> otherRef = (TCSObjectReference<?>) otherObj;
      return referentClass.equals(otherRef.referentClass) && name.equals(otherRef.name);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "TCSObjectReference{"
        + "referentClass=" + referentClass
        + ", id=" + id
        + ", name=" + name
        + '}';
  }

  /**
   * Returns a distinct copy of this reference.
   * The clone's <code>referent</code> attribute is set to <code>null</code> to
   * prevent the actual referenced object to be leaked outside the kernel.
   *
   * @return A distinct copy of this reference, with its <code>referent</code>
   * attribute set to <code>null</code>.
   */
  @Override
  public TCSObjectReference<E> clone() {
    return new TCSObjectReference<>(referentClass, name, id, dummy);
  }

  /**
   * Returns a dummy reference, referencing nothing.
   *
   * @param <T> The type of the dummy reference to be returned.
   * @param clazz The class of the dummy reference to be returned.
   * @param name The name of the dummy reference to be returned.
   * @return A dummy reference, referencing nothing.
   * @deprecated Dummy references are deprecated. In case of drive order destinations, use
   * {@link DriveOrder.Destination#Destination(org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public static <T extends TCSObject<T>> TCSObjectReference<T>
      getDummyReference(Class<T> clazz, String name) {
    return new TCSObjectReference<>(clazz, name);
  }
}
