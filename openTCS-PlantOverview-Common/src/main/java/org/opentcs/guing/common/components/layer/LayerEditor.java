/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

/**
 * Provides methods to edit layers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerEditor {

  /**
   * Sets a layer's visible state.
   *
   * @param layerId The ID of the layer.
   * @param visible The layer's new visible state.
   */
  void setLayerVisible(int layerId, boolean visible);

  /**
   * Sets a layer's name.
   *
   * @param layerId The ID of the layer.
   * @param name The layer's new name.
   */
  void setLayerName(int layerId, String name);
}
