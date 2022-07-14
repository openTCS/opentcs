/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

/**
 * Provides methods to edit layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerGroupEditor {

  /**
   * Sets a layer group's visible state.
   *
   * @param groupId The ID of the layer group.
   * @param visible The layer group's new visible state.
   */
  void setGroupVisible(int groupId, boolean visible);

  /**
   * Sets a layer group's name.
   *
   * @param groupId The ID of the layer group.
   * @param name The layer group's new name.
   */
  void setGroupName(int groupId, String name);
}
