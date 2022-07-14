/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.Drawing;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.common.application.ViewManager;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.guing.common.model.SystemModel;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventBus;

/**
 * The default implementation of {@link LayerManager}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultLayerManager
    implements LayerManager,
               LayerGroupManager {

  /**
   * The sets of model components mapped to the IDs of the layers the model components are drawn on.
   */
  private final Map<Integer, Set<DrawnModelComponent>> components = new HashMap<>();
  /**
   * The view manager.
   */
  private final ViewManager viewManager;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * The listeners for layer group data changes.
   */
  private final Set<LayerGroupChangeListener> layerGroupChangeListeners = new HashSet<>();
  /**
   * The listener for layer data changes.
   */
  private LayerChangeListener layerChangeListener;
  /**
   * The system model we're working with.
   */
  private SystemModel systemModel;
  /**
   * Whether this instance is initialized or not.
   */
  private boolean initialized;

  @Inject
  public DefaultLayerManager(ViewManager viewManager, @ApplicationEventBus EventBus eventBus) {
    this.viewManager = requireNonNull(viewManager, "viewManager");
    this.eventBus = requireNonNull(eventBus, "eventBus");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void setLayerChangeListener(LayerChangeListener listener) {
    layerChangeListener = listener;
  }

  @Override
  public void setLayerVisible(int layerId, boolean visible) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    LayerWrapper wrapper = getLayerWrapper(layerId);
    // We want to  manipulate the drawing (by adding or removing figures) only if the visible state
    // of the layer has actually changed. This prevents figures from being added multiple times and
    // figures from being removed although they are no longer present in the drawing.
    if (wrapper.getLayer().isVisible() == visible) {
      return;
    }

    boolean visibleBefore = wrapper.getLayer().isVisible() && wrapper.getLayerGroup().isVisible();
    Layer oldLayer = wrapper.getLayer();
    Layer newLayer = oldLayer.withVisible(visible);
    wrapper.setLayer(newLayer);
    boolean visibleAfter = wrapper.getLayer().isVisible() && wrapper.getLayerGroup().isVisible();

    if (visibleBefore != visibleAfter) {
      layerVisibilityChanged(newLayer, visibleAfter);
    }

    layerChangeListener.layersChanged();
  }

  @Override
  public void setLayerName(int layerId, String name) {
    checkArgument(getLayerWrapper(layerId) != null,
                  "A layer with layer ID '%d' doesn't exist.",
                  layerId);

    LayerWrapper wrapper = getLayerWrapper(layerId);
    Layer oldLayer = wrapper.getLayer();
    Layer newLayer = oldLayer.withName(name);
    wrapper.setLayer(newLayer);

    layerChangeListener.layersChanged();
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof SystemModelTransitionEvent)) {
      return;
    }

    SystemModelTransitionEvent evt = (SystemModelTransitionEvent) event;
    switch (evt.getStage()) {
      case UNLOADED:
        reset();
        break;
      case LOADED:
        systemModel = evt.getModel();
        restoreLayers();
        break;
      default: // Do nothing.
    }
  }

  @Override
  public void addLayerGroupChangeListener(LayerGroupChangeListener listener) {
    layerGroupChangeListeners.add(listener);
  }

  @Override
  public boolean containsComponents(int layerId) {
    return !components.getOrDefault(layerId, new HashSet<>()).isEmpty();
  }

  @Override
  public void setGroupVisible(int groupId, boolean visible) {
    checkArgument(getLayerGroup(groupId) != null,
                  "A layer group with layer group ID '%d' doesn't exist.",
                  groupId);

    LayerGroup oldGroup = getLayerGroup(groupId);
    if (oldGroup.isVisible() == visible) {
      return;
    }

    Map<Boolean, List<LayerWrapper>> wrappersByLayerVisibility
        = getLayerWrappers().values().stream()
            .filter(wrapper -> wrapper.getLayer().getGroupId() == groupId)
            .collect(Collectors.partitioningBy(wrapper -> wrapper.getLayer().isVisible()));

    // We only need to take care of "visible" layers. Non-visible layers are not affected by 
    // any changes to a group's visibility.
    for (LayerWrapper wrapper : wrappersByLayerVisibility.get(Boolean.TRUE)) {
      if (wrapper.getLayerGroup().isVisible() != visible) {
        layerVisibilityChanged(wrapper.getLayer(), visible);
      }
    }

    // Update the group for all layer wrappers that are assigned to it.
    LayerGroup newGroup = oldGroup.withVisible(visible);
    getLayerWrappers().values().stream()
        .filter(wrapper -> wrapper.getLayer().getGroupId() == groupId)
        .forEach(wrapper -> wrapper.setLayerGroup(newGroup));

    // Update the group in the system model's layout.
    getLayerGroups().put(groupId, newGroup);

    notifyGroupsChanged();
  }

  @Override
  public void setGroupName(int groupId, String name) {
    checkArgument(getLayerGroup(groupId) != null,
                  "A layer group with layer group ID '%d' doesn't exist.",
                  groupId);

    LayerGroup oldGroup = getLayerGroup(groupId);
    LayerGroup newGroup = oldGroup.withName(name);

    // Update the group for all layer wrappers that are assigned to it.
    getLayerWrappers().values().stream()
        .filter(wrapper -> wrapper.getLayer().getGroupId() == groupId)
        .forEach(wrapper -> wrapper.setLayerGroup(newGroup));

    // Update the group in the system model's layout.
    getLayerGroups().put(groupId, newGroup);

    notifyGroupsChanged();
    layerChangeListener.layersChanged();
  }

  protected void reset() {
    components.clear();
  }

  protected void restoreLayers() {
    restoreComponentsMap();

    notifyGroupsInitialized();
    layerChangeListener.layersInitialized();
  }

  protected LayerChangeListener getLayerChangeListener() {
    return layerChangeListener;
  }

  /**
   * Returns the system model the layer manager is working with.
   *
   * @return The system model.
   */
  protected SystemModel getSystemModel() {
    return systemModel;
  }

  /**
   * Returns the model components mapped to the IDs of the layers they are drawn on.
   *
   * @return The model components.
   */
  protected Map<Integer, Set<DrawnModelComponent>> getComponents() {
    return components;
  }

  /**
   * Returns the view manager the layer manager is working with.
   *
   * @return The view manager.
   */
  protected ViewManager getViewManager() {
    return viewManager;
  }

  /**
   * Returns the set of drawings the layer manager is working with.
   *
   * @return The set of drawings the layer manager is working with.
   */
  protected Set<Drawing> getDrawings() {
    return viewManager.getDrawingViewMap().values().stream()
        .map(scrollPane -> scrollPane.getDrawingView().getDrawing())
        .collect(Collectors.toSet());
  }

  /**
   * Maps the given model component to the given layer ID.
   *
   * @param modelComponent The model component to add.
   * @param layerId The ID of the layer to map the model component to.
   * @see #getComponents()
   */
  protected void addComponent(DrawnModelComponent modelComponent, int layerId) {
    components.get(layerId).add(modelComponent);
  }

  /**
   * Removes the mapping for the given model component.
   *
   * @param modelComponent The model component to remove.
   * @see #getComponents()
   */
  protected void removeComponent(DrawnModelComponent modelComponent) {
    LayerWrapper layerWrapper = modelComponent.getPropertyLayerWrapper().getValue();
    components.get(layerWrapper.getLayer().getId()).remove(modelComponent);
  }

  /**
   * Returns the layer wrappers in the system model's layout.
   *
   * @return The layer wrappers in the system model's layout.
   */
  protected Map<Integer, LayerWrapper> getLayerWrappers() {
    return systemModel.getLayoutModel().getPropertyLayerWrappers().getValue();
  }

  /**
   * Returns the layer wrapper for the given layer ID.
   *
   * @param layerId The layer ID.
   * @return The layer wrapper.
   */
  protected LayerWrapper getLayerWrapper(int layerId) {
    return getLayerWrappers().get(layerId);
  }

  /**
   * Returns the layer groups in the system model's layout.
   *
   * @return The layer groups in the system model's layout.
   */
  protected Map<Integer, LayerGroup> getLayerGroups() {
    return systemModel.getLayoutModel().getPropertyLayerGroups().getValue();
  }

  /**
   * Returns the layer group for the given layer ID.
   *
   * @param groupId The layer group ID.
   * @return The layer group.
   */
  protected LayerGroup getLayerGroup(int groupId) {
    return getLayerGroups().get(groupId);
  }

  protected void notifyGroupsInitialized() {
    for (LayerGroupChangeListener listener : layerGroupChangeListeners) {
      listener.groupsInitialized();
    }
  }

  protected void notifyGroupsChanged() {
    for (LayerGroupChangeListener listener : layerGroupChangeListeners) {
      listener.groupsChanged();
    }
  }

  protected void notifyGroupAdded() {
    for (LayerGroupChangeListener listener : layerGroupChangeListeners) {
      listener.groupAdded();
    }
  }

  protected void notifyGroupRemoved() {
    for (LayerGroupChangeListener listener : layerGroupChangeListeners) {
      listener.groupRemoved();
    }
  }

  private void restoreComponentsMap() {
    // Prepare an entry in the components map for every registered layer.
    for (LayerWrapper wrapper : getLayerWrappers().values()) {
      components.put(wrapper.getLayer().getId(), new HashSet<>());
    }

    List<DrawnModelComponent> drawnModelComponents = new ArrayList<>();
    drawnModelComponents.addAll(systemModel.getPointModels());
    drawnModelComponents.addAll(systemModel.getPathModels());
    drawnModelComponents.addAll(systemModel.getLocationModels());
    drawnModelComponents.addAll(systemModel.getLinkModels());

    // Add all model components to their respective layer.
    for (DrawnModelComponent modelComponent : drawnModelComponents) {
      addComponent(modelComponent,
                   modelComponent.getPropertyLayerWrapper().getValue().getLayer().getId());
    }

  }

  protected void layerVisibilityChanged(Layer layer, boolean visible) {
    Set<Drawing> drawings = getDrawings();
    SwingUtilities.invokeLater(() -> drawings.forEach(drawing -> drawing.willChange()));
    SwingUtilities.invokeLater(() -> drawings.forEach(drawing -> drawing.changed()));
  }
}
