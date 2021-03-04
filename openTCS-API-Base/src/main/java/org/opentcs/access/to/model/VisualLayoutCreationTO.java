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
  private List<ModelLayoutElementCreationTO> modelElements = new LinkedList<>();
  /**
   * This layout's shape layout elements.
   */
  private List<ShapeLayoutElementCreationTO> shapeElements = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this visual layout.
   */
  public VisualLayoutCreationTO(@Nonnull String name) {
    super(name);
  }

  private VisualLayoutCreationTO(@Nonnull String name,
                                @Nonnull Map<String, String> properties,
                                double scaleX,
                                double scaleY,
                                @Nonnull List<ModelLayoutElementCreationTO> modelElements,
                                @Nonnull List<ShapeLayoutElementCreationTO> shapeElements) {
    super(name, properties);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.modelElements = requireNonNull(modelElements, "modelElements");
    this.shapeElements = requireNonNull(shapeElements, "shapeElements");
  }

  /**
   * Sets the name of this visual layout.
   *
   * @param name The new name.
   * @return The modified visual layout.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public VisualLayoutCreationTO setName(@Nonnull String name) {
    return (VisualLayoutCreationTO) super.setName(name);
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
                                      shapeElements);
  }

  /**
   * Sets the properties of this visual layout.
   *
   * @param properties The new properties.
   * @return The modified visual layout.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public VisualLayoutCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (VisualLayoutCreationTO) super.setProperties(properties);
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
                                      shapeElements);
  }

  /**
   * Sets a single property of this visual layout.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified visual layout.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public VisualLayoutCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (VisualLayoutCreationTO) super.setProperty(key, value);
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
                                      shapeElements);
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
   * Sets this layout's scale on the X axis (in mm/pixel).
   *
   * @param scaleX The new scale.
   * @return The modified visual layout
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public VisualLayoutCreationTO setScaleX(double scaleX) {
    this.scaleX = scaleX;
    return this;
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
                                      shapeElements);
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
   * Sets this layout's scale on the Y axis (in mm/pixel).
   *
   * @param scaleY The new scale.
   * @return The modified visual layout
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public VisualLayoutCreationTO setScaleY(double scaleY) {
    this.scaleY = scaleY;
    return this;
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
                                      shapeElements);
  }

  /**
   * Returns the model layout elements of this visual layout.
   *
   * @return The model layout elements of this visual layout.
   */
  @Nonnull
  public List<ModelLayoutElementCreationTO> getModelElements() {
    return Collections.unmodifiableList(modelElements);
  }

  /**
   * Sets the model layout elements of this visual layout.
   *
   * @param modelElements The new model layout elements.
   * @return The modified visual layout.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public VisualLayoutCreationTO setModelElements(
      @Nonnull List<ModelLayoutElementCreationTO> modelElements) {
    this.modelElements = requireNonNull(modelElements, "modelElements");
    return this;
  }

  /**
   * Creates a copy of this object with the given model elements
   *
   * @param modelElements The new model layout elements.
   * @return A copy of this object, differing in the layout elements of this visual layout.
   */
  public VisualLayoutCreationTO withModelElements(
      @Nonnull List<ModelLayoutElementCreationTO> modelElements) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements);
  }

  /**
   * Creates a copy of this object with the given model element.
   *
   * @param modelElement The new model layout elements.
   * @return A copy of this object, differing in the layout elements of this visual layout.
   */
  public VisualLayoutCreationTO withModelElement(
      @Nonnull ModelLayoutElementCreationTO modelElement) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      listWithAppendix(modelElements, modelElement),
                                      shapeElements);
  }

  /**
   * Returns the shape layout elements of this visual layout.
   *
   * @return The shape layout elements of this visual layout.
   */
  @Nonnull
  public List<ShapeLayoutElementCreationTO> getShapeElements() {
    return Collections.unmodifiableList(shapeElements);
  }

  /**
   * Sets the shape layout elements of this visual layout.
   *
   * @param shapeElements The new shape layout elements.
   * @return The modified visual layout.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public VisualLayoutCreationTO setShapeElements(
      @Nonnull List<ShapeLayoutElementCreationTO> shapeElements) {
    this.shapeElements = requireNonNull(shapeElements, "shapeElements");
    return this;
  }

  /**
   * Creates a copy of this object with the given shape elements.
   *
   * @param shapeElements the new shape layout elements.
   * @return A copy of this object, differing in the shape layout elements of this visual layout.
   */
  public VisualLayoutCreationTO withShapeElements(
      @Nonnull List<ShapeLayoutElementCreationTO> shapeElements) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      shapeElements);
  }

  /**
   * Creates a copy of this object with the given shape element.
   *
   * @param shapeElement the new shape layout element.
   * @return A copy of this object, differing in the shape layout elements of this visual layout.
   */
  public VisualLayoutCreationTO withShapeElement(
      @Nonnull ShapeLayoutElementCreationTO shapeElement) {
    return new VisualLayoutCreationTO(getName(),
                                      getModifiableProperties(),
                                      scaleX,
                                      scaleY,
                                      modelElements,
                                      listWithAppendix(shapeElements, shapeElement));
  }
}
