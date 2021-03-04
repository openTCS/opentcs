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
 * A transfer object describing a model layout element in the visual layout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelLayoutElementCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The layer on which this model layout element is to be displayed.
   */
  private int layer;

  /**
   * Creates a new instance.
   *
   * @param name The name of this model layout element.
   */
  public ModelLayoutElementCreationTO(String name) {
    super(name);
  }

  /**
   * Sets the name of this model layout element.
   *
   * @param name The new name.
   * @return The modified model layout element.
   */
  @Nonnull
  @Override
  public ModelLayoutElementCreationTO setName(@Nonnull String name) {
    return (ModelLayoutElementCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this model layout element.
   *
   * @param properties The new properties.
   * @return The modified model layout element.
   */
  @Nonnull
  @Override
  public ModelLayoutElementCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (ModelLayoutElementCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this model layout element.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified model layout element.
   */
  @Nonnull
  @Override
  public ModelLayoutElementCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (ModelLayoutElementCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the layer on which this model layout element is to be displayed.
   *
   * @return The layer on which this model layout element is to be displayed.
   */
  public int getLayer() {
    return layer;
  }

  /**
   * Sets the layer on which this model layout element is to be displayed.
   *
   * @param layer The new layer.
   * @return The modified model layout element.
   */
  @Nonnull
  public ModelLayoutElementCreationTO setLayer(int layer) {
    this.layer = layer;
    return this;
  }
}
