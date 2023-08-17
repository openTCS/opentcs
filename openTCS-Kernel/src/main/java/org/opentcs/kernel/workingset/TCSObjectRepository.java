/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A container for <code>TCSObject</code>s belonging together.
 * <p>
 * Provides access to a set of data objects and ensures they have unique names.
 * </p>
 */
public class TCSObjectRepository {

  /**
   * The objects contained in this pool, mapped by their names, grouped by their classes.
   */
  private final Map<Class<?>, Map<String, TCSObject<?>>> objects = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public TCSObjectRepository() {
  }

  /**
   * Adds a new object to the pool.
   *
   * @param newObject The object to be added to the pool.
   * @throws ObjectExistsException If an object with the same ID or the same
   * name as the new one already exists in this pool.
   */
  public void addObject(@Nonnull TCSObject<?> newObject)
      throws ObjectExistsException {
    requireNonNull(newObject, "newObject");

    if (containsName(newObject.getName())) {
      throw new ObjectExistsException("Object name already exists: " + newObject.getName());
    }

    Map<String, TCSObject<?>> objectsByName = objects.get(newObject.getClass());
    if (objectsByName == null) {
      objectsByName = new HashMap<>();
      objects.put(newObject.getClass(), objectsByName);
    }
    objectsByName.put(newObject.getName(), newObject);
  }

  /**
   * Uses the given object to replace an object in the pool with same name.
   *
   * @param object The replacing object.
   * @throws IllegalArgumentException If an object with the same name as the given object does not
   * exist in this repository, yet, or if an object with the same name does exist but is an instance
   * of a different class.
   */
  @Nonnull
  public void replaceObject(@Nonnull TCSObject<?> object)
      throws IllegalArgumentException {
    requireNonNull(object, "object");
    TCSObject<?> oldObject = getObjectOrNull(object.getName());
    checkArgument(oldObject != null,
                  "Object named '%s' does not exist",
                  object.getName());
    checkArgument(object.getClass() == oldObject.getClass(),
                  "Object named '%s' not an instance of the same class: '%s' != '%s'",
                  object.getName(),
                  object.getClass().getName(),
                  oldObject.getClass().getName());

    objects.get(object.getClass()).put(object.getName(), object);
  }

  /**
   * Returns an object from the pool.
   *
   * @param ref A reference to the object to return.
   * @return The referenced object, or <code>null</code>, if no such object exists in this pool.
   */
  @Nullable
  public TCSObject<?> getObjectOrNull(@Nonnull TCSObjectReference<?> ref) {
    requireNonNull(ref);

    return objects.getOrDefault(ref.getReferentClass(), Map.of()).get(ref.getName());
  }

  /**
   * Returns an object from the pool.
   *
   * @param ref A reference to the object to return.
   * @return The referenced object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  @Nonnull
  public TCSObject<?> getObject(@Nonnull TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    TCSObject<?> result = getObjectOrNull(ref);
    if (result == null) {
      throw new ObjectUnknownException(ref);
    }
    return result;
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return The referenced object, or <code>null</code>, if no such object
   * exists in this pool or if an object exists but is not an instance of the
   * given class.
   */
  @Nullable
  public <T extends TCSObject<T>> T getObjectOrNull(@Nonnull Class<T> clazz,
                                                    @Nonnull TCSObjectReference<T> ref) {
    requireNonNull(clazz, "clazz");
    requireNonNull(ref, "ref");

    TCSObject<?> result = objects.getOrDefault(clazz, Map.of()).get(ref.getName());
    if (clazz.isInstance(result)) {
      return clazz.cast(result);
    }
    else {
      return null;
    }
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return The referenced object.
   * @throws ObjectUnknownException If the referenced object does not exist, or if an object exists
   * but is not an instance of the given class.
   */
  @Nonnull
  public <T extends TCSObject<T>> T getObject(@Nonnull Class<T> clazz,
                                              @Nonnull TCSObjectReference<T> ref)
      throws ObjectUnknownException {
    T result = getObjectOrNull(clazz, ref);
    if (result == null) {
      throw new ObjectUnknownException(ref);
    }
    return result;
  }

  /**
   * Returns an object from the pool.
   *
   * @param name The name of the object to return.
   * @return The object with the given name, or <code>null</code>, if no such
   * object exists in this pool.
   */
  @Nullable
  public TCSObject<?> getObjectOrNull(@Nonnull String name) {
    requireNonNull(name, "name");

    return objects.values().stream()
        .map(objectsByName -> objectsByName.get(name))
        .filter(object -> object != null)
        .findAny()
        .orElse(null);
  }

  /**
   * Returns an object from the pool.
   *
   * @param name The name of the object to return.
   * @return The object with the given name.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  @Nonnull
  public TCSObject<?> getObject(@Nonnull String name)
      throws ObjectUnknownException {
    TCSObject<?> result = getObjectOrNull(name);
    if (result == null) {
      throw new ObjectUnknownException(name);
    }
    return result;
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return The named object, or <code>null</code>, if no such object
   * exists in this pool or if an object exists but is not an instance of the
   * given class.
   */
  @Nullable
  public <T extends TCSObject<T>> T getObjectOrNull(@Nonnull Class<T> clazz, @Nonnull String name) {
    requireNonNull(clazz, "clazz");
    requireNonNull(name, "name");

    TCSObject<?> result = objects.getOrDefault(clazz, Map.of()).get(name);
    if (clazz.isInstance(result)) {
      return clazz.cast(result);
    }
    else {
      return null;
    }
  }

  /**
   * Returns an object from the pool.
   *
   * @param <T> The object's type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return The named object.
   * @throws ObjectUnknownException If no object with the given name exists in this pool or if an
   * object exists but is not an instance of the given class.
   */
  @Nonnull
  public <T extends TCSObject<T>> T getObject(@Nonnull Class<T> clazz, @Nonnull String name)
      throws ObjectUnknownException {
    T result = getObjectOrNull(clazz, name);
    if (result == null) {
      throw new ObjectUnknownException(name);
    }
    return result;
  }

  /**
   * Returns a set of objects belonging to the given class.
   *
   * @param <T> The objects' type.
   * @param clazz The class of the objects to be returned.
   * @return A set of objects belonging to the given class.
   */
  @Nonnull
  public <T extends TCSObject<T>> Set<T> getObjects(@Nonnull Class<T> clazz) {
    return objects.getOrDefault(clazz, Map.of()).values().stream()
        .map(object -> clazz.cast(object))
        .collect(Collectors.toSet());
  }

  /**
   * Returns a set of objects of the given class for which the given predicate is true.
   *
   * @param <T> The objects' type.
   * @param clazz The class of the objects to be returned.
   * @param predicate The predicate that must be true for returned objects.
   * @return A set of objects of the given class for which the given predicate is true. If no such
   * objects exist, the returned set is empty.
   */
  @Nonnull
  public <T extends TCSObject<T>> Set<T> getObjects(@Nonnull Class<T> clazz,
                                                    @Nonnull Predicate<? super T> predicate) {
    requireNonNull(clazz, "clazz");
    requireNonNull(predicate, "predicate");

    return objects.getOrDefault(clazz, Map.of()).values().stream()
        .map(object -> clazz.cast(object))
        .filter(predicate)
        .collect(Collectors.toSet());
  }

  /**
   * Removes a referenced object from this pool.
   *
   * @param ref A reference to the object to be removed.
   * @return The object that was removed from the pool.
   * @throws ObjectUnknownException If the referenced object does not exist.
   */
  @Nonnull
  public TCSObject<?> removeObject(@Nonnull TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    Map<String, TCSObject<?>> map = objects.get(ref.getReferentClass());
    TCSObject<?> obj = (map == null) ? null : map.remove(ref.getName());
    if (obj == null) {
      throw new ObjectUnknownException(ref);
    }
    return obj;
  }

  private boolean containsName(String name) {
    return objects.values().stream().anyMatch(objectsByName -> objectsByName.containsKey(name));
  }
}
