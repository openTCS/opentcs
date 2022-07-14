/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

/**
 * Organizes the layer groups in a plant model.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerGroupManager
    extends LayerGroupEditor {

  /**
   * Add a listener to the set that's notified each time a change to group data occurs.
   *
   * @param listener The listener to add.
   */
  void addLayerGroupChangeListener(LayerGroupChangeListener listener);
}
