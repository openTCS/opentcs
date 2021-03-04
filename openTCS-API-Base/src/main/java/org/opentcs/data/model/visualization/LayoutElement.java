/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.TreeMap;

/**
 * A generic layout element that is to be displayed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class LayoutElement
    implements Serializable {

  /**
   * A set of generic key-value pairs associated with this layout element.
   */
  private Map<String, String> properties = new TreeMap<>();
  /**
   * The layer on which this layout element is to be displayed.
   */
  private int layer;

  /**
   * Creates a new LayoutElement.
   */
  protected LayoutElement() {
    // Do nada.
  }

  /**
   * Returns the layer on which this layout element is to be displayed.
   *
   * @return The layer on which this layout element is to be displayed.
   */
  public int getLayer() {
    return layer;
  }

  /**
   * Sets the layer on which this layout element is to be displayed.
   *
   * @param layer The new layer.
   */
  public void setLayer(int layer) {
    this.layer = layer;
  }

  /**
   * Returns this layout element's properties, a generic set of key-value pairs
   * that can contain basically any information describing this element.
   *
   * @return This layout element's properties.
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets this layout element's properties.
   *
   * @param properties The new properties.
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
  }
}
