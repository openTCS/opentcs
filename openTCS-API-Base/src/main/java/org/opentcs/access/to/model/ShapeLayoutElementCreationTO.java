/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a shape layout element in the visual layout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ShapeLayoutElementCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The layer on which this shape layout element is to be displayed.
   */
  private int layer;

  /**
   * Creates a new instance.
   *
   * @param name The name of this shape layout element.
   */
  public ShapeLayoutElementCreationTO(String name) {
    super(name);
  }

  /**
   * Sets the name of this shape layout element.
   *
   * @param name The new name.
   * @return The modified shape layout element.
   */
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setName(@Nonnull String name) {
    return (ShapeLayoutElementCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this shape layout element.
   *
   * @param properties The new properties.
   * @return The modified shape layout element.
   */
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (ShapeLayoutElementCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this shape layout element.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified shape layout element.
   */
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (ShapeLayoutElementCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the layer on which this shape layout element is to be displayed.
   *
   * @return The layer on which this shape layout element is to be displayed.
   */
  public int getLayer() {
    return layer;
  }

  /**
   * Sets the layer on which this shape layout element is to be displayed.
   *
   * @param layer The new layer.
   * @return The modified shape layout element.
   */
  @Nonnull
  public ShapeLayoutElementCreationTO setLayer(int layer) {
    this.layer = layer;
    return this;
  }
}
