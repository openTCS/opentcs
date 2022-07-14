/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.model;

import org.opentcs.guing.base.model.ModelComponent;

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
