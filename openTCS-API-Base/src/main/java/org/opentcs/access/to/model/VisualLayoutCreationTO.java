/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

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

  /**
   * Sets the name of this visual layout.
   *
   * @param name The new name.
   * @return The modified visual layout.
   */
  @Nonnull
  @Override
  public VisualLayoutCreationTO setName(@Nonnull String name) {
    return (VisualLayoutCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this visual layout.
   *
   * @param properties The new properties.
   * @return The modified visual layout.
   */
  @Nonnull
  @Override
  public VisualLayoutCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (VisualLayoutCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this visual layout.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified visual layout.
   */
  @Nonnull
  @Override
  public VisualLayoutCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (VisualLayoutCreationTO) super.setProperty(key, value);
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
  @Nonnull
  public VisualLayoutCreationTO setScaleX(double scaleX) {
    this.scaleX = scaleX;
    return this;
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
  @Nonnull
  public VisualLayoutCreationTO setScaleY(double scaleY) {
    this.scaleY = scaleY;
    return this;
  }

  /**
   * Returns the model layout elements of this visual layout.
   *
   * @return The model layout elements of this visual layout.
   */
  @Nonnull
  public List<ModelLayoutElementCreationTO> getModelElements() {
    return modelElements;
  }

  /**
   * Sets the model layout elements of this visual layout.
   *
   * @param modelElements The new model layout elements.
   * @return The modified visual layout.
   */
  @Nonnull
  public VisualLayoutCreationTO setModelElements(
      @Nonnull List<ModelLayoutElementCreationTO> modelElements) {
    this.modelElements = requireNonNull(modelElements, "modelElements");
    return this;
  }

  /**
   * Returns the shape layout elements of this visual layout.
   *
   * @return The shape layout elements of this visual layout.
   */
  @Nonnull
  public List<ShapeLayoutElementCreationTO> getShapeElements() {
    return shapeElements;
  }

  /**
   * Sets the shape layout elements of this visual layout.
   *
   * @param shapeElements The new shape layout elements.
   * @return The modified visual layout.
   */
  @Nonnull
  public VisualLayoutCreationTO setShapeElements(
      @Nonnull List<ShapeLayoutElementCreationTO> shapeElements) {
    this.shapeElements = requireNonNull(shapeElements, "shapeElements");
    return this;
  }
}
