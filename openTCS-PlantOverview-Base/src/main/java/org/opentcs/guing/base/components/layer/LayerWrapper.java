/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.layer;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * Wraps a {@link Layer} instance and the {@link LayerGroup} instance that the layer is assigned to.
 * Instances of this class are referenced by {@link ModelComponent}s. This allows multiple model
 * components to be updated simultaneously with the update of only one layer wrapper.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerWrapper {

  /**
   * The layer.
   */
  private Layer layer;
  /**
   * The layer group the layer is assigned to.
   */
  private LayerGroup layerGroup;

  /**
   * Creates a new instance.
   *
   * @param layer The layer.
   * @param layerGroup The layer group the layer is assigned to.
   */
  public LayerWrapper(@Nonnull Layer layer, @Nonnull LayerGroup layerGroup) {
    this.layer = requireNonNull(layer, "layer");
    this.layerGroup = requireNonNull(layerGroup, "layerGroup");
  }

  /**
   * Returns the layer.
   *
   * @return The layer.
   */
  @Nonnull
  public Layer getLayer() {
    return layer;
  }

  /**
   * Sets the layer.
   *
   * @param layer The layer.
   */
  public void setLayer(@Nonnull Layer layer) {
    this.layer = requireNonNull(layer, "layer");
  }

  /**
   * Returns the layer group the layer is assigned to.
   *
   * @return The layer group.
   */
  public LayerGroup getLayerGroup() {
    return layerGroup;
  }

  /**
   * Sets the layer group the layer is assigned to.
   *
   * @param layerGroup The layer group.
   */
  public void setLayerGroup(LayerGroup layerGroup) {
    this.layerGroup = layerGroup;
  }
}
