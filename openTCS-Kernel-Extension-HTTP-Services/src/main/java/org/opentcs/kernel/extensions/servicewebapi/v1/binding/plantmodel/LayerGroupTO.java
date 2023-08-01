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
public class LayerGroupTO {

  private int id;
  private String name;
  private boolean visible;

  @JsonCreator
  public LayerGroupTO(@JsonProperty(value = "id", required = true) int id,
                      @Nonnull @JsonProperty(value = "name", required = true) String name,
                      @JsonProperty(value = "visible", required = true) boolean visible) {
    this.id = id;
    this.name = requireNonNull(name, "name");
    this.visible = visible;
  }

  public int getId() {
    return id;
  }

  public LayerGroupTO setId(int id) {
    this.id = id;
    return this;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public LayerGroupTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  public boolean isVisible() {
    return visible;
  }

  public LayerGroupTO setVisible(boolean visible) {
    this.visible = visible;
    return this;
  }

}
