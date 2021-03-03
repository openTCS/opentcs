/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import static java.util.Objects.requireNonNull;

/**
 * Delivers objects from a given list infinitely, wrapping around at the end.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The type of the elements.
 */
public class ObjectListCycler<E>
    implements Iterator<E> {

  /**
   * The list of available objects.
   */
  private final List<E> objects = new ArrayList<>();
  /**
   * The current index in the list.
   */
  private int index;

  /**
   * Creates a new instance.
   *
   * @param objects The objects to be returned by this cycler. May not be empty.
   */
  @SafeVarargs
  public ObjectListCycler(E... objects) {
    requireNonNull(objects, "objects");
    if (objects.length < 1) {
      throw new IllegalArgumentException("objects is empty");
    }
    this.objects.addAll(Arrays.asList(objects));
  }

  /**
   * Creates a new instance.
   *
   * @param objects The objects to be returned by this cycler. May not be empty.
   */
  public ObjectListCycler(Collection<E> objects) {
    requireNonNull(objects, "objects");
    if (objects.isEmpty()) {
      throw new IllegalArgumentException("objects is empty");
    }
    this.objects.addAll(objects);
  }

  /**
   * Always returns <code>true</code>.
   *
   * @return <code>true</code>.
   */
  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public E next() {
    // Increment index, wrapping around at the end of the list.
    index = (index + 1) % objects.size();
    return objects.get(index);
  }

  /**
   * Not supported - always throws an UnsupportedOperationException.
   *
   * @throws UnsupportedOperationException Always thrown, as element removal is
   * not supported.
   */
  @Override
  public void remove()
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Element removal not supported.");
  }

  /**
   * Resets the cycler to the beginning of the list of objects.
   */
  public void reset() {
    index = 0;
  }
}
