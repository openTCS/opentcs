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
 * A transfer object describing a model layout element in the visual layout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Will be removed in favor of dedicated layout classes in corresponding TCS data
 * objects.
 */
@Deprecated
@ScheduledApiChange(details = "Will be removed.", when = "6.0")
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

  private ModelLayoutElementCreationTO(@Nonnull String name,
                                       @Nonnull Map<String, String> properties,
                                       int layer) {
    super(name, properties);
    this.layer = layer;
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public ModelLayoutElementCreationTO withName(@Nonnull String name) {
    return new ModelLayoutElementCreationTO(name, getModifiableProperties(), layer);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public ModelLayoutElementCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new ModelLayoutElementCreationTO(getName(), properties, layer);
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
  public ModelLayoutElementCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new ModelLayoutElementCreationTO(getName(), propertiesWith(key, value), layer);
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
   * Creates a copy of this object with the given layer on which this model layout element is to be displayed.
   *
   * @param layer The new layer
   * @return A copy of this object, differing in the given layer.
   */
  public ModelLayoutElementCreationTO withLayer(int layer) {
    return new ModelLayoutElementCreationTO(getName(), getModifiableProperties(), layer);
  }
}
