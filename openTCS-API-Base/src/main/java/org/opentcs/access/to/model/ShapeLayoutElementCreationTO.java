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
import org.opentcs.util.annotations.ScheduledApiChange;

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
   * Creates a new instance.
   *
   * @param name the name.
   * @param properties the properties.
   * @param layer the layer.
   */
  private ShapeLayoutElementCreationTO(String name, Map<String, String> properties, int layer) {
    super(name, properties);
    this.layer = layer;
  }

  /**
   * Sets the name of this shape layout element.
   *
   * @param name The new name.
   * @return The modified shape layout element.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setName(@Nonnull String name) {
    return (ShapeLayoutElementCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public ShapeLayoutElementCreationTO withName(@Nonnull String name) {
    return new ShapeLayoutElementCreationTO(name, getModifiableProperties(), layer);
  }

  /**
   * Sets the properties of this shape layout element.
   *
   * @param properties The new properties.
   * @return The modified shape layout element.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (ShapeLayoutElementCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public ShapeLayoutElementCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new ShapeLayoutElementCreationTO(getName(), properties, layer);
  }

  /**
   * Sets a single property of this shape layout element.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified shape layout element.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public ShapeLayoutElementCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (ShapeLayoutElementCreationTO) super.setProperty(key, value);
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
  public ShapeLayoutElementCreationTO withPropery(@Nonnull String key, @Nonnull String value) {
    return new ShapeLayoutElementCreationTO(getName(), propertiesWith(key, value), layer);
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
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public ShapeLayoutElementCreationTO setLayer(int layer) {
    this.layer = layer;
    return this;
  }

  /**
   * Creates a copy of this object with the given layer 
   * on which this shape layout element is to be displayed.
   *
   * @param layer the new layer.
   * @return a copy of this object, differing in the given layer.
   */
  public ShapeLayoutElementCreationTO withLayer(int layer) {
    return new ShapeLayoutElementCreationTO(getName(), getModifiableProperties(), layer);
  }
}
