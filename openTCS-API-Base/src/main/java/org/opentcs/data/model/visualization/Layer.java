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
 * Describes a layer in a plant model which is used to group model elements.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class Layer
    implements Serializable {

  /**
   * The unique ID of this layer.
   */
  private final int id;
  /**
   * The ordinal of this layer.
   * Layers with a higher ordinal are positioned above layers with a lower ordinal.
   */
  private final int ordinal;
  /**
   * Whether this layer is visible or not.
   */
  private final boolean visible;
  /**
   * The name of this layer.
   */
  private final String name;
  /**
   * The ID of the layer group this layer is assigned to.
   */
  private final int groupId;

  /**
   * Creates a new instance.
   *
   * @param id The unique ID of the layer.
   * @param ordinal The ordinal of the layer.
   * @param visible Whether the layer is visible or not.
   * @param name The name of the layer.
   * @param groupId The ID of the layer group the layer is assigned to.
   */
  public Layer(int id, int ordinal, boolean visible, String name, int groupId) {
    this.id = id;
    this.ordinal = ordinal;
    this.visible = visible;
    this.name = requireNonNull(name, "name");
    this.groupId = groupId;
  }

  /**
   * Returns the unique ID of this layer.
   *
   * @return The unique Id of this layer.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the ordinal of this layer.
   * Layers with a higher ordinal are positioned above layers with a lower ordinal.
   *
   * @return The ordinal of this layer.
   */
  public int getOrdinal() {
    return ordinal;
  }

  /**
   * Creates a copy of this object, with the given ordinal.
   *
   * @param ordinal The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Layer withOrdinal(int ordinal) {
    return new Layer(id, ordinal, visible, name, groupId);
  }

  /**
   * Returns whether this layer is visible or not.
   *
   * @return Whether this layer is visible or not.
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
  public Layer withVisible(boolean visible) {
    return new Layer(id, ordinal, visible, name, groupId);
  }

  /**
   * Returns the name of this layer.
   *
   * @return The name of this layer.
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
  public Layer withName(String name) {
    return new Layer(id, ordinal, visible, name, groupId);
  }

  /**
   * Returns the ID of the layer group this layer is assigned to.
   *
   * @return The ID of the layer group this layer is assigned to.
   */
  public int getGroupId() {
    return groupId;
  }

  /**
   * Creates a copy of this object, with the given group ID.
   *
   * @param groupId The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Layer withGroupId(int groupId) {
    return new Layer(id, ordinal, visible, name, groupId);
  }

  @Override
  public String toString() {
    return "Layer{" + ""
        + "id=" + id + ", "
        + "ordinal=" + ordinal + ", "
        + "visible=" + visible + ", "
        + "name=" + name +  ", "
        + "groupId=" + groupId + '}';
  }
}
