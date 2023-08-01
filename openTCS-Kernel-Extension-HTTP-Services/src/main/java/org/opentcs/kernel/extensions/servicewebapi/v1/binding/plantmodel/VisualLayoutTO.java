/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 */
public class VisualLayoutTO {

  private String name;
  private double scaleX = 50.0;
  private double scaleY = 50.0;
  private List<LayerTO> layers = List.of(new LayerTO(0, 0, true, "layer0", 0));
  private List<LayerGroupTO> layerGroups = List.of(new LayerGroupTO(0, "layerGroup0", true));
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public VisualLayoutTO(@Nonnull @JsonProperty(value = "name", required = true) String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public VisualLayoutTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public VisualLayoutTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  public double getScaleX() {
    return scaleX;
  }

  public VisualLayoutTO setScaleX(double scaleX) {
    this.scaleX = scaleX;
    return this;
  }

  public double getScaleY() {
    return scaleY;
  }

  public VisualLayoutTO setScaleY(double scaleY) {
    this.scaleY = scaleY;
    return this;
  }

  @Nonnull
  public List<LayerTO> getLayers() {
    return layers;
  }

  public VisualLayoutTO setLayers(@Nonnull List<LayerTO> layers) {
    this.layers = requireNonNull(layers, "layers");
    return this;
  }

  @Nonnull
  public List<LayerGroupTO> getLayerGroups() {
    return layerGroups;
  }

  public VisualLayoutTO setLayerGroups(@Nonnull List<LayerGroupTO> layerGroups) {
    this.layerGroups = requireNonNull(layerGroups, "layerGroups");
    return this;
  }

}
