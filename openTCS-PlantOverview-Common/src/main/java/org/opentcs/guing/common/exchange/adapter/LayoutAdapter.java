/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for VisualLayout instances.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LayoutAdapter.class);
  
  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
    VisualLayout layout = requireNonNull((VisualLayout) tcsObject, "tcsObject");
    LayoutModel model = (LayoutModel) modelComponent;
    
    try {
      model.getPropertyName().setText(layout.getName());
      model.getPropertyName().markChanged();
      
      model.getPropertyScaleX().setValueAndUnit(layout.getScaleX(), LengthProperty.Unit.MM);
      model.getPropertyScaleX().markChanged();
      model.getPropertyScaleY().setValueAndUnit(layout.getScaleY(), LengthProperty.Unit.MM);
      model.getPropertyScaleY().markChanged();
      
      initLayerGroups(model, layout.getLayerGroups());
      initLayers(model, layout.getLayers());
      model.getPropertyLayerWrappers().markChanged();
      
      updateMiscModelProperties(model, layout);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }
  
  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel.withVisualLayout(
        new VisualLayoutCreationTO(modelComponent.getName())
            .withScaleX(getScaleX((LayoutModel) modelComponent))
            .withScaleY(getScaleY((LayoutModel) modelComponent))
            .withProperties(getKernelProperties(modelComponent))
            .withLayers(getLayers((LayoutModel) modelComponent))
            .withLayerGroups(getLayerGroups((LayoutModel) modelComponent))
    );
  }
  
  private void initLayerGroups(LayoutModel model, Collection<LayerGroup> groups) {
    Map<Integer, LayerGroup> layerGroups = model.getPropertyLayerGroups().getValue();
    layerGroups.clear();
    for (LayerGroup group : groups) {
      layerGroups.put(group.getId(), group);
    }
  }
  
  private void initLayers(LayoutModel model, Collection<Layer> layers) {
    Map<Integer, LayerWrapper> layerWrappers = model.getPropertyLayerWrappers().getValue();
    layerWrappers.clear();
    
    Map<Integer, LayerGroup> layerGroups = model.getPropertyLayerGroups().getValue();
    for (Layer layer : layers) {
      layerWrappers.put(layer.getId(),
                        new LayerWrapper(layer, layerGroups.get(layer.getGroupId())));
    }
  }
  
  private double getScaleX(LayoutModel model) {
    return model.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM);
  }
  
  private double getScaleY(LayoutModel model) {
    return model.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM);
  }
  
  private List<Layer> getLayers(LayoutModel model) {
    return model.getPropertyLayerWrappers().getValue().values().stream()
        .map(wrapper -> wrapper.getLayer())
        .sorted(Comparator.comparing(layer -> layer.getId()))
        .collect(Collectors.toList());
  }
  
  private List<LayerGroup> getLayerGroups(LayoutModel model) {
    return model.getPropertyLayerGroups().getValue().values().stream()
        .sorted(Comparator.comparing(group -> group.getId()))
        .collect(Collectors.toList());
  }
}
