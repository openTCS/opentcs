/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import org.opentcs.guing.base.components.layer.LayerWrapper;

/**
 * Provides a method to get the currently active layer.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ActiveLayerProvider {

  /**
   * Returns the {@link LayerWrapper} instance that holds the currently active layer.
   *
   * @return The currently active layer.
   */
  LayerWrapper getActiveLayer();
}
