/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

import org.opentcs.components.Lifecycle;
import org.opentcs.util.event.EventHandler;

/**
 * Organizes the layers in a plant model.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerManager
    extends Lifecycle,
            LayerEditor,
            EventHandler {

  /**
   * Sets the listener for layer data changes.
   *
   * @param listener The listener for layer data changes.
   */
  void setLayerChangeListener(LayerChangeListener listener);

  /**
   * Returns whether the layer with the given layer ID contains any components.
   *
   * @param layerId The ID of the layer.
   * @return Whether the layer contains any components.
   */
  boolean containsComponents(int layerId);
}
