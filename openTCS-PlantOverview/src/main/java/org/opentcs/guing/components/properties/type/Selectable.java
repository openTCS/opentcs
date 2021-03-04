/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import java.util.List;

/**
 * Interface for a property indicating this property has different
 * possible values to choose from.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public interface Selectable<E> {

  /**
   * Sets the possible values.
   *
   * @param possibleValues An array with the possible values.
   */
  public void setPossibleValues(List<E> possibleValues);

  /**
   * Returns the possible values.
   *
   * @return The possible values.
   */
  public List<E> getPossibleValues();
}
