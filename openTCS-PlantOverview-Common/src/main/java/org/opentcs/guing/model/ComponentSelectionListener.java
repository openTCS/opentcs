/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ComponentSelectionListener {

  /**
   * Informs this listener that the given component has been selected.
   *
   * @param model The selected component.
   */
  void componentSelected(ModelComponent model);
}
