// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.util;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility methods for searching in lists.
 */
public class ListSearchUtil {

  /**
   * Prevents instantiation.
   */
  private ListSearchUtil() {
  }

  /**
   * Performs a binary search for the given element in the given list, using the given function for
   * comparison.
   * From all elements in the list leading to the same function result, the one that actually equals
   * the given element is returned.
   *
   * @param list The list to search in.
   * @param element The element to search for.
   * @param function The function to use for comparison.
   * @return The index of the element in the list, or -1 if it is not contained in the list.
   * @param <T> The type of the elements in the list.
   * @param <U> The type of the function's result.
   */
  public static <T, U extends Comparable<U>> int binarySearch(
      List<T> list,
      T element,
      Function<T, U> function
  ) {
    requireNonNull(list, "list");
    requireNonNull(element, "element");
    requireNonNull(function, "function");

    int index = Collections.binarySearch(
        list,
        element,
        Comparator.comparing(function)
    );
    if (index < 0) {
      return -1;
    }
    if (Objects.equals(list.get(index), element)) {
      return index;
    }

    U functionResult = function.apply(element);

    for (int i = index - 1; i >= 0; i--) {
      if (!Objects.equals(function.apply(list.get(i)), functionResult)) {
        break;
      }
      if (Objects.equals(list.get(i), element)) {
        return i;
      }
    }

    for (int i = index + 1; i < list.size(); i++) {
      if (!Objects.equals(function.apply(list.get(i)), functionResult)) {
        break;
      }
      if (Objects.equals(list.get(i), element)) {
        return i;
      }
    }

    return -1;
  }
}
