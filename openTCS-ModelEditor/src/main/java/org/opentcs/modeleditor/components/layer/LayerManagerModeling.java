/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jhotdraw.draw.AbstractFigure;
import org.jhotdraw.draw.Drawing;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.application.ViewManager;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.common.components.layer.DefaultLayerManager;
import org.opentcs.guing.common.components.layer.LayerManager;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventBus;

/**
 * The {@link LayerManager} implementation for the model editor application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerManagerModeling
    extends DefaultLayerManager
    implements LayerEditorModeling,
               LayerGroupEditorModeling,
               ActiveLayerProvider {

  /**
   * The currently active layer.
   */
  private LayerWrapper activeLayerWrapper;

  @Inject
  public LayerManagerModeling(ViewManager viewManager, @ApplicationEventBus EventBus eventBus) {
    super(viewManager, eventBus);
  }

  @Override
  public void createLayer() {
    Layer layer = createLayerWrapper().getLayer();
    getLayerChangeListener().layerAdded();

    setLayerActive(layer.getId());
  }

  @Override
  public void deleteLayer(int layerId)
      throws IllegalArgumentException {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    deleteLayerWrapper(layerId);
    getLayerChangeListener().layerRemoved();

    if (layerId == activeLayerWrapper.getLayer().getId()) {
      handleActiveLayerRemoved();
    }
  }

  @Override
  public void add(DrawnModelComponent modelComponent) {
    int layerId = modelComponent.getPropertyLayerWrapper().getValue().getLayer().getId();
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    addComponent(modelComponent, layerId);
  }

  @Override
  public void remove(DrawnModelComponent modelComponent) {
    removeComponent(modelComponent);
  }

  @Override
  public void moveLayerDown(int layerId) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    List<LayerWrapper> layerWrappersByOrdinal = getLayerWrappers().values().stream()
        .sorted(Comparator.comparing(wrapper -> wrapper.getLayer().getOrdinal()))
        .collect(Collectors.toList());

    if (layerId == layerWrappersByOrdinal.get(0).getLayer().getId()) {
      // The layer with the given layer ID is already the lowest layer.
      return;
    }

    shiftLayerByOne(layerId, layerWrappersByOrdinal);

    getLayerChangeListener().layersChanged();
  }

  @Override
  public void moveLayerUp(int layerId) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    List<LayerWrapper> layerWrappersByReverseOrdinal = getLayerWrappers().values().stream()
        .sorted(Comparator.comparing(wrapper -> wrapper.getLayer().getOrdinal(),
                                     Comparator.reverseOrder()))
        .collect(Collectors.toList());

    if (layerId == layerWrappersByReverseOrdinal.get(0).getLayer().getId()) {
      // The layer with the given layer ID is already the heighest layer.
      return;
    }

    shiftLayerByOne(layerId, layerWrappersByReverseOrdinal);

    getLayerChangeListener().layersChanged();
  }

  @Override
  public void setLayerActive(int layerId) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    activeLayerWrapper = getLayerWrapper(layerId);

    getLayerChangeListener().layersChanged();
  }

  @Override
  public void createLayerGroup() {
    int groupId = getNextAvailableLayerGroupId();

    // Add the created layer group to the system model's layout.
    addLayerGroup(new LayerGroup(groupId, "Group " + groupId, true));

    notifyGroupAdded();
  }

  @Override
  public void deleteLayerGroup(int groupId)
      throws IllegalArgumentException {
    checkArgument(getLayerGroup(groupId) != null,
                  "A layer group with layer group ID '%d' doesn't exist.",
                  groupId);

    // Delete the layers that are assigned to the group.
    Set<Integer> layerAssignedToGroup = getLayerWrappers().values().stream()
        .map(wrapper -> wrapper.getLayer())
        .filter(layer -> layer.getGroupId() == groupId)
        .map(layer -> layer.getId())
        .collect(Collectors.toSet());
    layerAssignedToGroup.forEach(layerId -> deleteLayer(layerId));

    removeLayerGroup(groupId);
    notifyGroupRemoved();
  }

  @Override
  public void setLayerGroupId(int layerId, int groupId) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);
    checkArgument(getLayerGroup(groupId) != null,
                  "A layer group with layer group ID '%d' doesn't exist.",
                  groupId);

    LayerWrapper wrapper = getLayerWrapper(layerId);

    boolean visibleBefore = wrapper.getLayer().isVisible() && wrapper.getLayerGroup().isVisible();
    wrapper.setLayer(wrapper.getLayer().withGroupId(groupId));
    wrapper.setLayerGroup(getLayerGroup(groupId));
    boolean visibleAfter = wrapper.getLayer().isVisible() && wrapper.getLayerGroup().isVisible();

    if (visibleBefore != visibleAfter) {
      layerVisibilityChanged(wrapper.getLayer(), visibleAfter);
    }

    getLayerChangeListener().layersChanged();
  }

  @Override
  public LayerWrapper getActiveLayer() {
    return activeLayerWrapper;
  }

  @Override
  protected void reset() {
    super.reset();
  }

  @Override
  protected void restoreLayers() {
    // Make sure there will be one layer set as the active layer. Do it for the highest layer.
    activeLayerWrapper = getLayerWrappers().values().stream()
        .sorted(Comparator.comparing(wrapper -> wrapper.getLayer().getOrdinal(),
                                     Comparator.reverseOrder()))
        .findFirst()
        .get();

    super.restoreLayers();
  }

  private LayerWrapper createLayerWrapper() {
    int layerId = getNextAvailableLayerId();
    int layerOrdinal = getNextAvailableLayerOrdinal();
    LayerGroup group = getLayerGroups().values().iterator().next();
    Layer layer = new Layer(layerId, layerOrdinal, true, "Layer " + layerId, group.getId());
    LayerWrapper wrapper = new LayerWrapper(layer, group);
    getComponents().put(layerId, new HashSet<>());

    // Add the created layer wrapper to the system model's layout.
    addLayerWrapper(wrapper);

    return wrapper;
  }

  private int getNextAvailableLayerId() {
    return getLayerWrappers().values().stream()
        .mapToInt(wrapper -> wrapper.getLayer().getId())
        .max()
        .getAsInt() + 1;
  }

  private int getNextAvailableLayerOrdinal() {
    return getLayerWrappers().values().stream()
        .mapToInt(wrapper -> wrapper.getLayer().getOrdinal())
        .max()
        .getAsInt() + 1;
  }

  private int getNextAvailableLayerGroupId() {
    return getLayerGroups().values().stream()
        .mapToInt(group -> group.getId())
        .max()
        .getAsInt() + 1;
  }

  private void deleteLayerWrapper(int layerId) {
    Set<OpenTCSDrawingView> drawingViews = getDrawingViews();
    drawingViews.forEach(drawingView -> drawingView.getDrawing().willChange());

    Set<ModelComponent> componentsToDelete = getComponents().get(layerId).stream()
        .map(component -> (ModelComponent) component)
        .collect(Collectors.toSet());
    drawingViews.forEach(drawingView -> drawingView.delete(componentsToDelete));

    drawingViews.forEach(drawingView -> drawingView.getDrawing().changed());

    getComponents().remove(layerId);

    // Remove the deleted layer wrapper from the system model's layout.
    removeLayerWrapper(layerId);
  }

  /**
   * Returns the set of drawing views the layer manager is working with.
   *
   * @return The set of drawing views the layer manager is working with.
   */
  private Set<OpenTCSDrawingView> getDrawingViews() {
    return getViewManager().getDrawingViewMap().values().stream()
        .map(scrollPane -> scrollPane.getDrawingView())
        .collect(Collectors.toSet());
  }

  private void handleActiveLayerRemoved() {
    List<Layer> layersByOrdinal = getLayerWrappers().values().stream()
        .map(wrapper -> wrapper.getLayer())
        .sorted(Comparator.comparing(layer -> layer.getOrdinal()))
        .collect(Collectors.toList());

    Optional<Layer> layerUnderRemovedActiveLayer = layersByOrdinal.stream()
        .takeWhile(layer -> layer.getOrdinal() < activeLayerWrapper.getLayer().getOrdinal())
        .reduce((layer1, layer2) -> layer2);

    // If there's a layer right under the active layer that just has been removed, select that layer
    // as the new active layer. Otherwise, just select the lowest layer.
    if (layerUnderRemovedActiveLayer.isPresent()) {
      setLayerActive(layerUnderRemovedActiveLayer.get().getId());
    }
    else {
      setLayerActive(layersByOrdinal.get(0).getId());
    }
  }

  private void shiftLayerByOne(int layerId, List<LayerWrapper> layers) {
    LayerWrapper layerWrapper = getLayerWrapper(layerId);
    int layerWrapperBeforeIndex = layers.indexOf(layerWrapper) - 1;
    LayerWrapper layerWrapperBefore = layers.get(layerWrapperBeforeIndex);
    swapLayerOrdinals(layerWrapper, layerWrapperBefore);
    updateLayerComponentsInDrawing(layerWrapper.getLayer(), layerWrapperBefore.getLayer());
  }

  private void swapLayerOrdinals(LayerWrapper layerWrapper1, LayerWrapper layerWrapper2) {
    int ordinal1 = layerWrapper1.getLayer().getOrdinal();
    int ordinal2 = layerWrapper2.getLayer().getOrdinal();

    Layer oldLayer = layerWrapper1.getLayer();
    Layer newLayer = oldLayer.withOrdinal(ordinal2);
    layerWrapper1.setLayer(newLayer);

    oldLayer = layerWrapper2.getLayer();
    newLayer = oldLayer.withOrdinal(ordinal1);
    layerWrapper2.setLayer(newLayer);
  }

  private void updateLayerComponentsInDrawing(Layer... layers) {
    Set<DrawnModelComponent> componentsToUpdate = new HashSet<>();
    for (Layer layer : layers) {
      if (layer.isVisible()) {
        componentsToUpdate.addAll(getComponents().get(layer.getId()));
      }
    }

    Set<Drawing> drawings = getDrawings();

    drawings.forEach(drawing -> drawing.willChange());
    for (DrawnModelComponent modelComponent : componentsToUpdate) {
      AbstractFigure figure = (AbstractFigure) getSystemModel().getFigure(modelComponent);
      drawings.forEach(drawing -> {
        drawing.basicRemove(figure);
        drawing.basicAdd(figure);
      });
    }
    drawings.forEach(drawing -> drawing.changed());
  }

  /**
   * Adds the given layer group to the system model's layout.
   *
   * @param layerWrapper The layer group to add.
   * @throws IllegalArgumentException If a layer group with the same layer group ID already exists
   * in the system model's layout.
   */
  private void addLayerGroup(LayerGroup layerGroup)
      throws IllegalArgumentException {
    int groupId = layerGroup.getId();
    checkArgument(getLayerGroup(groupId) == null,
                  "A layer group for group ID '%d' already exists in the model.",
                  groupId);

    getLayerGroups().put(groupId, layerGroup);
  }

  /**
   * Removes the layer group with the given group ID from the system model's layout.
   *
   * @param layerId The layer group ID.
   */
  private void removeLayerGroup(int groupId) {
    getLayerGroups().remove(groupId);
  }

  /**
   * Adds the given layer wrapper to the system model's layout.
   *
   * @param layerWrapper The layer wrapper to add.
   * @throws IllegalArgumentException If a layer wrapper with the same layer ID already exists
   * in the system model's layout.
   */
  private void addLayerWrapper(LayerWrapper layerWrapper)
      throws IllegalArgumentException {
    int layerId = layerWrapper.getLayer().getId();
    checkArgument(getLayerWrapper(layerId) == null,
                  "A layer wrapper for layer ID '%d' already exists in the model.",
                  layerId);

    getLayerWrappers().put(layerId, layerWrapper);
  }

  /**
   * Removes the layer wrapper with the given layer ID from the system model's layout.
   *
   * @param layerId The layer ID.
   */
  private void removeLayerWrapper(int layerId) {
    getLayerWrappers().remove(layerId);
  }
}
