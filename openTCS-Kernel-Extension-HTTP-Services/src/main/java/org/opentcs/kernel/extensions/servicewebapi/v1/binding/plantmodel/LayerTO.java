/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 */
public class LayerTO {

  private int id;
  private int ordinal;
  private boolean visible;
  private String name;
  private int groupId;

  @JsonCreator
  public LayerTO(@JsonProperty(value = "id", required = true) int id,
                 @JsonProperty(value = "ordinal", required = true) int ordinal,
                 @JsonProperty(value = "visible", required = true) boolean visible,
                 @Nonnull @JsonProperty(value = "name", required = true) String name,
                 @JsonProperty(value = "groupId", required = true) int groupId) {
    this.id = id;
    this.ordinal = ordinal;
    this.visible = visible;
    this.name = requireNonNull(name, "name");
    this.groupId = groupId;
  }

  public int getId() {
    return id;
  }

  public LayerTO setId(int id) {
    this.id = id;
    return this;
  }

  public int getOrdinal() {
    return ordinal;
  }

  public LayerTO setOrdinal(int ordinal) {
    this.ordinal = ordinal;
    return this;
  }

  public boolean isVisible() {
    return visible;
  }

  public LayerTO setVisible(boolean visible) {
    this.visible = visible;
    return this;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public LayerTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  public int getGroupId() {
    return groupId;
  }

  public LayerTO setGroupId(int groupId) {
    this.groupId = groupId;
    return this;
  }

}
