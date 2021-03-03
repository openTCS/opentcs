/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An <code>ArrayList</code> that grows automatically when its
 * <code>set()</code> method is called with an index greater than or equal to
 * its size.
 * This class can be useful if you need to map integers to objects but do not
 * want to use a <code>Map</code> to avoid the costs of autoboxing
 * <code>int</code> to <code>Integer</code> with lookups.
 *
 * @param <E> The type of elements in the list.
 * @author Stefan Walter (Fraunhofer IML)
 */
final class AutoGrowingArrayList<E>
    extends ArrayList<E> {

  /**
   * Delegates to <code>ArrayList()</code>.
   */
  public AutoGrowingArrayList() {
    super();
  }

  /**
   * Delegates to <code>ArrayList(Collection<? extends E>)</code>.
   */
  public AutoGrowingArrayList(Collection<? extends E> c) {
    super(c);
  }

  /**
   * Delegates to <code>ArrayList(int initialCapacity)</code>.
   */
  public AutoGrowingArrayList(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Returns the element at the specified position in this list. If the given
   * index is greater than or equal to this list's size, <code>null</code> is
   * returned.
   *
   * @param index index of element to return.
   * @return the element at the specified position in this list, or
   * <code>null</code>, if the given index is greater than or equal to this
   * list's size.
   * @throws IndexOutOfBoundsException if index is out of range
   * <code>(index < 0)</code>.
   */
  @Override
  public E get(final int index) {
    if (index >= size()) {
      return null;
    }
    else {
      return super.get(index);
    }
  }

  /**
   * Replaces the element at the specified position in this list with the
   * specified element. If the specified position does not exist in this list,
   * yet, the list will be padded with <code>null</code> elements until it does.
   * Note that, strictly speaking, this violates the contract in
   * <code>List</code> which states that this method throws an
   * <code>IndexOutOfBoundsException</code> if <code>(index >= size())</code>.
   *
   * @param index index of element to replace.
   * @param element element to be stored at the specified position.
   * @return The element previously at the specified position.
   * @throws IndexOutOfBoundsException If index out of range
   * <code>(index < 0)</code>.
   */
  @Override
  public E set(final int index, final E element) {
    if (index >= size()) {
      ensureCapacity(index + 1);
      for (int i = index - size(); i > 0; i--) {
        add(null);
      }
      add(element);
      return null;
    }
    else {
      return super.set(index, element);
    }
  }
}
