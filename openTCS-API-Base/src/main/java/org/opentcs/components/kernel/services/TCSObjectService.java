/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * Provides methods concerning {@link TCSObject}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface TCSObjectService {

  /**
   * Returns a single {@link TCSObject} of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return A copy of the referenced object, or {@code null} if no such object exists or if an
   * object exists but is not an instance of the given class.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  <T extends TCSObject<T>> T fetchObject(Class<T> clazz, TCSObjectReference<T> ref)
      throws KernelRuntimeException;

  /**
   * Returns a single {@link TCSObject} of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return A copy of the named object, or {@code null} if no such object exists or if an object
   * exists but is not an instance of the given class.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  <T extends TCSObject<T>> T fetchObject(Class<T> clazz, String name)
      throws KernelRuntimeException;

  /**
   * Returns all existing {@link TCSObject}s of the given class.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @return Copies of all existing objects of the given class.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  <T extends TCSObject<T>> Set<T> fetchObjects(Class<T> clazz)
      throws KernelRuntimeException;

  /**
   * Returns all existing {@link TCSObject}s of the given class for which the given predicate is
   * true.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @param predicate The predicate that must be true for returned objects.
   * @return Copies of all existing objects of the given class for which the given predicate is
   * true. If no such objects exist, the returned set will be empty.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  <T extends TCSObject<T>> Set<T> fetchObjects(@Nonnull Class<T> clazz,
                                               @Nonnull Predicate<? super T> predicate)
      throws KernelRuntimeException;
  
  /**
   * Updates an {@link TCSObject}'s property.
   *
   * @param ref A reference to the TCSObject to be modified.
   * @param key The property's key.
   * @param value The property's (new) value. If {@code null}, removes the property from the object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateObjectProperty(TCSObjectReference<?> ref, String key, @Nullable String value)
      throws ObjectUnknownException, KernelRuntimeException;
}
