// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

/**
 * Provides methods to edit layers.
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
