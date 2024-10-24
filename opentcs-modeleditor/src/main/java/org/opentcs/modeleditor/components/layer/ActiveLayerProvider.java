// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.components.layer;

import org.opentcs.guing.base.components.layer.LayerWrapper;

/**
 * Provides a method to get the currently active layer.
 */
public interface ActiveLayerProvider {

  /**
   * Returns the {@link LayerWrapper} instance that holds the currently active layer.
   *
   * @return The currently active layer.
   */
  LayerWrapper getActiveLayer();
}
