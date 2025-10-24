// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.LayerCreationTO;
import org.opentcs.access.to.model.LayerGroupCreationTO;
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
    this.pConverter = requireNonNull(pConverter, "pConverter");
  }

  public VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO vLayout) {
    return new VisualLayoutCreationTO(vLayout.getName())
        .withProperties(pConverter.toPropertyMap(vLayout.getProperties()))
        .withScaleX(vLayout.getScaleX())
        .withScaleY(vLayout.getScaleY())
        .withLayers(convertLayers(vLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(vLayout.getLayerGroups()));
  }

  public VisualLayoutTO toVisualLayoutTO(VisualLayout visualLayout) {
    return new VisualLayoutTO(visualLayout.getName())
        .setProperties(pConverter.toPropertyTOs(visualLayout.getProperties()))
        .setScaleX(visualLayout.getScaleX())
        .setScaleY(visualLayout.getScaleY())
        .setLayers(toLayerTOs(visualLayout.getLayers()))
        .setLayerGroups(toLayerGroupTOs(visualLayout.getLayerGroups()));
  }

  private List<LayerGroupCreationTO> convertLayerGroups(List<LayerGroupTO> layerGroups) {
    return layerGroups.stream()
        .map(
            layerGroup -> new LayerGroupCreationTO(
                layerGroup.getId(),
                layerGroup.getName(),
                layerGroup.isVisible()
            )
        )
        .collect(Collectors.toList());
  }

  private List<LayerGroupTO> toLayerGroupTOs(List<LayerGroup> layerGroups) {
    return layerGroups.stream()
        .map(
            layerGroup -> new LayerGroupTO(
                layerGroup.getId(),
                layerGroup.getName(),
                layerGroup.isVisible()
            )
        )
        .collect(Collectors.toList());
  }

  private List<LayerCreationTO> convertLayers(List<LayerTO> layers) {
    return layers.stream()
        .map(
            layer -> new LayerCreationTO(
                layer.getId(),
                layer.getOrdinal(),
                layer.isVisible(),
                layer.getName(),
                layer.getGroupId()
            )
        )
        .collect(Collectors.toList());
  }

  private List<LayerTO> toLayerTOs(List<Layer> layers) {
    return layers.stream()
        .map(
            layer -> new LayerTO(
                layer.getId(),
                layer.getOrdinal(),
                layer.isVisible(),
                layer.getName(),
                layer.getGroupId()
            )
        )
        .collect(Collectors.toList());
  }
}
