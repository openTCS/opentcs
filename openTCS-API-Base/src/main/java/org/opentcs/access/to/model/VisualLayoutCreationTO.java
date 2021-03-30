/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a visual layout in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VisualLayoutCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * This layout's scale on the X axis (in mm/pixel).
   */
  private double scaleX = 50.0;
  /**
   * This layout's scale on the Y axis (in mm/pixel).
   */
  private double scaleY = 50.0;
  /**
   * This layout's model layout elements.
   */
  @Deprecated
  private List<ModelLayoutElementCreationTO> modelElements = new LinkedList<>();
  /**
   * This layout's shape layout elements.
   */
  @Deprecated
  private List<ShapeLayoutElementCreationTO> shapeElements = new LinkedList<>();
  /**
   * This layout's layers.
   */
  private List<Layer> layers = new LinkedList<>();
  /**
   * The layout's layer groups.
   */
  private List<LayerGroup> layerGroups = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this visual layout.
   */
  public VisualLayoutCreationTO(@Nonnull String name) {
    super(name);
  }

  @SuppressWarnings("deprecation")
  private VisualLayoutCreationTO(@Nonnull String name,
                                 @Nonnull Map<String, String> properties,
                                 double scaleX,
                                 double scaleY,
                                 @Nonnull List<ModelLayoutElementCreationTO> modelElements,
                                 @Nonnull List<ShapeLayoutElementCreationTO> shapeElements,
                                 @Nonnull List<Layer> layers,
                                 @Nonnull List<LayerGroup> layerGroups) {
    super(name, properties);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.modelElements = requireNonNull(modelElements, "modelElements");
    this.shapeElements = requireNonNull(shapeElements, "shapeElements");
    this.layers = requireNonNull(layers, "layers");
    this.layerGroups = requireNonNull(layerGroups, "layerGroups");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name the new name of the instance.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public VisualLayoutCreationTO withName(@Nonnull String name) {
    return new VisualLayoutCreationTO(name,
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public VisualLayoutCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new VisualLayoutCreationTO(getName(),
                                      properties,
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public VisualLayoutCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new VisualLayoutCreationTO(getName(),
                                      propertiesWith(key, value),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
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
   * Creates a copy of this object with the layout's scale on the X axis (in mm/pixel).
   *
   * @param scaleX The new scale.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withScaleX(double scaleX) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
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
   * Creates a copy of this object with the given layout's scale on the Y axis (in mm/pixel)
   *
   * @param scaleY The new scale.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withScaleY(double scaleY) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Returns the model layout elements of this visual layout.
   *
   * @return The model layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  @Nonnull
  public List<ModelLayoutElementCreationTO> getModelElements() {
    return Collections.unmodifiableList(modelElements);
  }

  /**
   * Creates a copy of this object with the given model elements
   *
   * @param modelElements The new model layout elements.
   * @return A copy of this object, differing in the layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public VisualLayoutCreationTO withModelElements(
      @Nonnull List<ModelLayoutElementCreationTO> modelElements) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object with the given model element.
   *
   * @param modelElement The new model layout elements.
   * @return A copy of this object, differing in the layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public VisualLayoutCreationTO withModelElement(
      @Nonnull ModelLayoutElementCreationTO modelElement) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      listWithAppendix(modelElements, modelElement),
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Returns the shape layout elements of this visual layout.
   *
   * @return The shape layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  @Nonnull
  public List<ShapeLayoutElementCreationTO> getShapeElements() {
    return Collections.unmodifiableList(shapeElements);
  }

  /**
   * Creates a copy of this object with the given shape elements.
   *
   * @param shapeElements the new shape layout elements.
   * @return A copy of this object, differing in the shape layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public VisualLayoutCreationTO withShapeElements(
      @Nonnull List<ShapeLayoutElementCreationTO> shapeElements) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object with the given shape element.
   *
   * @param shapeElement the new shape layout element.
   * @return A copy of this object, differing in the shape layout elements of this visual layout.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public VisualLayoutCreationTO withShapeElement(
      @Nonnull ShapeLayoutElementCreationTO shapeElement) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      listWithAppendix(shapeElements, shapeElement),
                                      layers,
                                      layerGroups);
  }

  /**
   * Returns the layers of this visual layout.
   *
   * @return The layers of this visual layout.
   */
  @Nonnull
  public List<Layer> getLayers() {
    return Collections.unmodifiableList(layers);
  }

  /**
   * Creates a copy of this object, with the given layers.
   *
   * @param layers The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withLayers(@Nonnull List<Layer> layers) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object, with the given layer.
   *
   * @param layer The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withLayer(@Nonnull Layer layer) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      listWithAppendix(layers, layer),
                                      layerGroups);
  }

  /**
   * Returns the layer groups of this visual layout.
   *
   * @return The layer groups of this visual layout.
   */
  @Nonnull
  public List<LayerGroup> getLayerGroups() {
    return Collections.unmodifiableList(layerGroups);
  }

  /**
   * Creates a copy of this object, with the given layer groups.
   *
   * @param layerGroups The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withLayerGroups(@Nonnull List<LayerGroup> layerGroups) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      layerGroups);
  }

  /**
   * Creates a copy of this object, with the given layer group.
   *
   * @param layerGroup The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayoutCreationTO withLayerGroup(@Nonnull LayerGroup layerGroup) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements,
                                      layers,
                                      listWithAppendix(layerGroups, layerGroup));
  }

  @Override
  public String toString() {
    return "VisualLayoutCreationTO{"
        + "name=" + getName()
        + ", scaleX=" + scaleX
        + ", scaleY=" + scaleY
        + ", modelElements=" + modelElements
        + ", shapeElements=" + shapeElements
        + ", layers=" + layers
        + ", layerGroups=" + layerGroups
        + ", properties=" + getProperties()
        + '}';
  }

}
