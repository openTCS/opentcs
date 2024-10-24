// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model.visualization;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;

/**
 * Describes the visual attributes of a model.
 */
public class VisualLayout
    extends
      TCSObject<VisualLayout>
    implements
      Serializable {

  /**
   * This layout's scale on the X axis (in mm/pixel).
   */
  private final double scaleX;
  /**
   * This layout's scale on the Y axis (in mm/pixel).
   */
  private final double scaleY;
  /**
   * The layers in this model.
   */
  private final List<Layer> layers;
  /**
   * The layer groups in this model.
   */
  private final List<LayerGroup> layerGroups;

  /**
   * Creates a new VisualLayout.
   *
   * @param name This visual layout's name.
   */
  public VisualLayout(String name) {
    super(name);
    this.scaleX = 50.0;
    this.scaleY = 50.0;
    this.layers = List.of();
    this.layerGroups = List.of();
  }

  /**
   * Creates a new VisualLayout.
   *
   * @param name This visual layout's name.
   */
  private VisualLayout(
      String name,
      Map<String, String> properties,
      ObjectHistory history,
      double scaleX,
      double scaleY,
      List<Layer> layers,
      List<LayerGroup> layerGroups
  ) {
    super(name, properties, history);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.layers = new ArrayList<>(requireNonNull(layers, "layers"));
    this.layerGroups = new ArrayList<>(requireNonNull(layerGroups, "layerGroups"));
  }

  @Override
  public VisualLayout withProperty(String key, String value) {
    return new VisualLayout(
        getName(),
        propertiesWith(key, value),
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  @Override
  public VisualLayout withProperties(Map<String, String> properties) {
    return new VisualLayout(
        getName(),
        properties,
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  @Override
  public TCSObject<VisualLayout> withHistoryEntry(ObjectHistory.Entry entry) {
    return new VisualLayout(
        getName(),
        getProperties(),
        getHistory().withEntryAppended(entry),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  @Override
  public TCSObject<VisualLayout> withHistory(ObjectHistory history) {
    return new VisualLayout(
        getName(),
        getProperties(),
        history,
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  /**
   * Returns this layout's scale on the X axis (in mm/pixel).
   *
   * @return This layout's scale on the X axis.
   */
  public double getScaleX() {
    return scaleX;
  }

  /**
   * Creates a copy of this object, with the given scaleX.
   *
   * @param scaleX The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withScaleX(double scaleX) {
    return new VisualLayout(
        getName(),
        getProperties(),
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  /**
   * Returns this layout's scale on the Y axis (in mm/pixel).
   *
   * @return This layout's scale on the Y axis.
   */
  public double getScaleY() {
    return scaleY;
  }

  /**
   * Creates a copy of this object, with the given scaleY.
   *
   * @param scaleY The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withScaleY(double scaleY) {
    return new VisualLayout(
        getName(),
        getProperties(),
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  /**
   * Returns the layers of this layout.
   *
   * @return The layers of this layout.
   */
  public List<Layer> getLayers() {
    return layers;
  }

  /**
   * Creates a copy of this object, with the given layers.
   *
   * @param layers The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withLayers(List<Layer> layers) {
    return new VisualLayout(
        getName(),
        getProperties(),
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }

  /**
   * Returns the layer groups of this layout.
   *
   * @return The layer groups of this layout.
   */
  public List<LayerGroup> getLayerGroups() {
    return layerGroups;
  }

  /**
   * Creates a copy of this object, with the given layer groups.
   *
   * @param layerGroups The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withLayerGroups(List<LayerGroup> layerGroups) {
    return new VisualLayout(
        getName(),
        getProperties(),
        getHistory(),
        scaleX,
        scaleY,
        layers,
        layerGroups
    );
  }
}
