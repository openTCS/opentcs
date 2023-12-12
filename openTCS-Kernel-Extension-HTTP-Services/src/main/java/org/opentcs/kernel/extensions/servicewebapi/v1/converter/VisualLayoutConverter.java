/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VisualLayoutTO;

/**
 * Includes the conversion methods for all VisualLayout classes.
 */
public class VisualLayoutConverter {

  private final PropertyConverter pConverter;
  
  @Inject
  public VisualLayoutConverter(PropertyConverter pConverter) {
    this.pConverter=requireNonNull(pConverter, "pConverter");
  }

  public VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO vLayout) {
    return new VisualLayoutCreationTO(vLayout.getName())
        .withProperties(pConverter.toPropertyMap(vLayout.getProperties()))
        .withScaleX(vLayout.getScaleX())
        .withScaleY(vLayout.getScaleY())
        .withLayers(convertLayers(vLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(vLayout.getLayerGroups()));
  }

  public VisualLayoutTO toVisualLayoutTO(Set<VisualLayout> visualLayouts) {
    return visualLayouts.stream()
        .findFirst()
        .map(
            visualLayout -> new VisualLayoutTO(visualLayout.getName())
                .setProperties(pConverter.toPropertyTOs(visualLayout.getProperties()))
                .setScaleX(visualLayout.getScaleX())
                .setScaleY(visualLayout.getScaleY())
                .setLayers(toLayerTOs(visualLayout.getLayers()))
                .setLayerGroups(toLayerGroupTOs(visualLayout.getLayerGroups())))
        .orElse(new VisualLayoutTO("default visual layout"));
  }

  private List<LayerGroup> convertLayerGroups(List<LayerGroupTO> layerGroups) {
    return layerGroups.stream()
        .map(layerGroup -> new LayerGroup(layerGroup.getId(),
                                          layerGroup.getName(),
                                          layerGroup.isVisible()))
        .collect(Collectors.toList());
  }

  private List<LayerGroupTO> toLayerGroupTOs(List<LayerGroup> layerGroups) {
    return layerGroups.stream()
        .map(layerGroup -> new LayerGroupTO(layerGroup.getId(),
                                            layerGroup.getName(),
                                            layerGroup.isVisible()))
        .collect(Collectors.toList());
  }

  private List<Layer> convertLayers(List<LayerTO> layers) {
    return layers.stream()
        .map(layer -> new Layer(layer.getId(),
                                layer.getOrdinal(),
                                layer.isVisible(),
                                layer.getName(),
                                layer.getGroupId()))
        .collect(Collectors.toList());
  }

  private List<LayerTO> toLayerTOs(List<Layer> layers) {
    return layers.stream()
        .map(layer -> new LayerTO(layer.getId(),
                                  layer.getOrdinal(),
                                  layer.isVisible(),
                                  layer.getName(),
                                  layer.getGroupId()))
        .collect(Collectors.toList());
  }
}
