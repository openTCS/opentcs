/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.layer;

/**
 * Provides methods to edit layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerGroupEditorModeling
    extends LayerGroupEditor {

  /**
   * Creates a new layer group.
   */
  void createLayerGroup();

  /**
   * Deletes the layer group with the given layer group ID.
   *
   * @param groupId The ID of the layer group to delete.
   * @throws IllegalArgumentException If a layer group with the given layer group ID doesn't exist.
   */
  void deleteLayerGroup(int groupId)
      throws IllegalArgumentException;
}
