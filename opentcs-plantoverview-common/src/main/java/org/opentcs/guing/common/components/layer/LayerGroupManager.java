// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

/**
 * Organizes the layer groups in a plant model.
 */
public interface LayerGroupManager
    extends
      LayerGroupEditor {

  /**
   * Add a listener to the set that's notified each time a change to group data occurs.
   *
   * @param listener The listener to add.
   */
  void addLayerGroupChangeListener(LayerGroupChangeListener listener);
}
