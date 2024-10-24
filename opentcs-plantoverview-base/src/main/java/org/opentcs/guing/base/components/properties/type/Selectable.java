// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import java.util.List;

/**
 * Interface for a property indicating this property has different
 * possible values to choose from.
 *
 * @param <E> The type of elements that can be selected from.
 */
public interface Selectable<E> {

  /**
   * Sets the possible values.
   *
   * @param possibleValues An array with the possible values.
   */
  void setPossibleValues(List<E> possibleValues);

  /**
   * Returns the possible values.
   *
   * @return The possible values.
   */
  List<E> getPossibleValues();
}
