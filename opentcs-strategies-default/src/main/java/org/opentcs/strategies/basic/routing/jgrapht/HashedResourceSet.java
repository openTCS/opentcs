// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * A wrapper for a set of that also provides a hash value for that set according to a given hash
 * function.
 *
 * @param <T> The type of the set.
 */
public class HashedResourceSet<T> {

  private final Function<Set<T>, Integer> hashFunction;
  private final Set<T> resources = new HashSet<>();
  private int hash;

  /**
   * Creates a new instance.
   *
   * @param hashFunction The function to use for calculating the set's hash value.
   */
  public HashedResourceSet(
      @Nonnull
      Function<Set<T>, Integer> hashFunction
  ) {
    this.hashFunction = requireNonNull(hashFunction, "hashFunction");
    updateHash();
  }

  /**
   * Indicates whether the underlying set is empty.
   *
   * @return {@code true}, if the set is empty, otherwise {@code false}.
   */
  public boolean isEmpty() {
    return resources.isEmpty();
  }

  /**
   * Clears the underlying set.
   */
  public void clear() {
    resources.clear();
    updateHash();
  }

  /**
   * Updates the underlying set so that it only contains the elements contained in the given
   * collection.
   *
   * @param collection The collection.
   */
  public void overrideResources(
      @Nonnull
      Collection<T> collection
  ) {
    requireNonNull(collection, "collection");

    this.resources.clear();
    this.resources.addAll(collection);
    updateHash();
  }

  /**
   * Updates the underlying set by adding the elements contained in the given collection or
   * replacing them if they already exist.
   *
   * @param collection The collection.
   */
  public void updateResources(
      @Nonnull
      Collection<T> collection
  ) {
    requireNonNull(collection, "collection");

    this.resources.removeAll(collection);
    this.resources.addAll(collection);
    updateHash();
  }

  /**
   * Returns the underlying set.
   *
   * @return The underlying set.
   */
  @Nonnull
  public Set<T> getResources() {
    return resources;
  }

  /**
   * Returns the hash value for the underlying set.
   *
   * @return The hash value.
   */
  public int getHash() {
    return hash;
  }

  private void updateHash() {
    hash = hashFunction.apply(resources);
  }
}
