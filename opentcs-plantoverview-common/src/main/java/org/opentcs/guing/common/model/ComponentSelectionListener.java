// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.model;

import org.opentcs.guing.base.model.ModelComponent;

/**
 */
public interface ComponentSelectionListener {

  /**
   * Informs this listener that the given component has been selected.
   *
   * @param model The selected component.
   */
  void componentSelected(ModelComponent model);
}
