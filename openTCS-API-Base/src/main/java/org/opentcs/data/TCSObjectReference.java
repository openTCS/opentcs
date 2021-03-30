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

/**
 * A transient reference to a {@link TCSObject}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual object class.
 */
public class TCSObjectReference<E extends TCSObject<E>>
    implements Serializable {

  /**
   * The referenced object's class.
   */
  private final Class<?> referentClass;
  /**
   * The referenced object's name.
   */
  private final String name;

  /**
   * Creates a new TCSObjectReference.
   *
   * @param referent The object this reference references.
   */
  protected TCSObjectReference(@Nonnull TCSObject<E> referent) {
    requireNonNull(referent, "newReferent");

    referentClass = referent.getClass();
    name = referent.getName();
  }

  private TCSObjectReference(@Nonnull Class<?> clazz, @Nonnull String newName) {
    name = requireNonNull(newName, "newName");
    referentClass = requireNonNull(clazz, "clazz");
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
   * Returns the referenced object's name.
   *
   * @return The referenced object's name.
   */
  public final String getName() {
    return name;
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
        + ", name=" + name
        + '}';
  }
}
