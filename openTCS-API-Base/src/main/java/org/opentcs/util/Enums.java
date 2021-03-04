/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides helper methods for working with enums.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Enums {

  /**
   * Prevents undesired instantiation.
   */
  private Enums() {
    // Do nada.
  }

  /**
   * Returns the String representations of all elements of the given enum class
   * as a set.
   *
   * @param enumClass The enum class for which to return the String
   * representations.
   * @return The String representations of all elements of the given enum class
   * as a set.
   */
  public static Set<String> asStringSet(Class<? extends Enum<?>> enumClass) {
    Objects.requireNonNull(enumClass, "enumClass is null");

    Set<String> result = new HashSet<>();
    for (Enum<?> constant : enumClass.getEnumConstants()) {
      result.add(constant.name());
    }
    return result;
  }

  /**
   * Returns the String representations of all elements of the given enum class
   * as an array.
   *
   * @param enumClass The enum class for which to return the String
   * representations.
   * @param <E> The enum class's type.
   * @return The String representations of all elements of the given enum class
   * as an array.
   */
  public static <E extends Enum<?>> String[] asStringArray(Class<E> enumClass) {
    Objects.requireNonNull(enumClass, "enumClass is null");

    E[] constants = enumClass.getEnumConstants();
    String[] result = new String[constants.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = constants[i].name();
    }
    return result;
  }
}
