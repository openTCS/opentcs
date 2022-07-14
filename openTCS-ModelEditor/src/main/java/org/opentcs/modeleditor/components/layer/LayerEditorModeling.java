/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.common.components.layer.LayerEditor;

/**
 * Provides methods to edit layers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerEditorModeling
    extends LayerEditor {

  /**
   * Creates a new layer.
   */
  void createLayer();

  /**
   * Deletes the layer with the given layer ID.
   *
   * @param layerId The ID of the layer to delete.
   * @throws IllegalArgumentException If a layer with the given layer ID doesn't exist.
   */
  void deleteLayer(int layerId)
      throws IllegalArgumentException;

  /**
   * Adds the given model component to the layer that is set in the component's layer wrapper 
   * property.
   *
   * @param modelComponent The model component to add.
   */
  void add(DrawnModelComponent modelComponent);

  /**
   * Removes the given model component from its layer.
   *
   * @param modelComponent The model component to remove.
   */
  void remove(DrawnModelComponent modelComponent);

  /**
   * Moves the layer with the given ID one level down.
   *
   * @param layerId The ID of the layer.
   */
  void moveLayerDown(int layerId);

  /**
   * Moves the layer with the given ID one level up.
   *
   * @param layerId The ID of the layer.
   */
  void moveLayerUp(int layerId);

  /**
   * Sets the layer with the given ID as the active layer.
   *
   * @param layerId The ID of the layer.
   */
  void setLayerActive(int layerId);

  /**
   * Sets the group ID for the layer with the given ID.
   *
   * @param layerId The ID of the layer.
   * @param groupId The ID of the layer group.
   */
  void setLayerGroupId(int layerId, int groupId);
}
