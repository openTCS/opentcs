/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;

/**
 * Describes a layer group in a plant model.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerGroup
    implements Serializable {

  /**
   * The unique ID of this layer group.
   */
  private final int id;
  /**
   * The name of this layer group.
   */
  private final String name;
  /**
   * Whether this layer group is visible or not.
   */
  private final boolean visible;

  /**
   * Creates a new instance.
   *
   * @param id The unique ID of the layer group.
   * @param name The name of the layer group.
   * @param visible Whether the layer group is visible or not.
   */
  public LayerGroup(int id, String name, boolean visible) {
    this.id = id;
    this.name = requireNonNull(name, "name");
    this.visible = visible;
  }

  /**
   * Returns the unique ID of this layer group.
   *
   * @return The unique Id of this layer group.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns whether this layer group is visible or not.
   *
   * @return Whether this layer group is visible or not.
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Creates a copy of this object, with the given visible state.
   *
   * @param visible The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LayerGroup withVisible(boolean visible) {
    return new LayerGroup(id, name, visible);
  }

  /**
   * Returns the name of this layer group.
   *
   * @return The name of this layer group.
   */
  public String getName() {
    return name;
  }

  /**
   * Creates a copy of this object, with the given name.
   *
   * @param name The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LayerGroup withName(String name) {
    return new LayerGroup(id, name, visible);
  }

  @Override
  public String toString() {
    return "LayerGroup{" + ""
        + "id=" + id + ", "
        + "name=" + name + ", "
        + "visible=" + visible + '}';
  }
}
