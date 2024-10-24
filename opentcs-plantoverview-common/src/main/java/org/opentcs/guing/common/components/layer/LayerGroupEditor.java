// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

/**
 * Provides methods to edit layer groups.
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
