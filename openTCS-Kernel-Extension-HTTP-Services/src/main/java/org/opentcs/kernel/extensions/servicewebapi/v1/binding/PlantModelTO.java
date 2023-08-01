/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VisualLayoutTO;

/**
 */
public class PlantModelTO {

  private String name;
  private List<PointTO> points = List.of();
  private List<PathTO> paths = List.of();
  private List<LocationTypeTO> locationTypes = List.of();
  private List<LocationTO> locations = List.of();
  private List<BlockTO> blocks = List.of();
  private List<VehicleTO> vehicles = List.of();
  private VisualLayoutTO visualLayout = new VisualLayoutTO("unnamed");
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public PlantModelTO(@Nonnull @JsonProperty(value = "name", required = true) String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PlantModelTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PlantModelTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public List<PointTO> getPoints() {
    return points;
  }

  public PlantModelTO setPoints(@Nonnull List<PointTO> points) {
    this.points = requireNonNull(points, "points");
    return this;
  }

  @Nonnull
  public List<PathTO> getPaths() {
    return paths;
  }

  public PlantModelTO setPaths(@Nonnull List<PathTO> paths) {
    this.paths = requireNonNull(paths, "paths");
    return this;
  }

  @Nonnull
  public List<LocationTO> getLocations() {
    return locations;
  }

  public PlantModelTO setLocations(@Nonnull List<LocationTO> locations) {
    this.locations = requireNonNull(locations, "locations");
    return this;
  }

  @Nonnull
  public List<LocationTypeTO> getLocationTypes() {
    return locationTypes;
  }

  public PlantModelTO setLocationTypes(@Nonnull List<LocationTypeTO> locationTypes) {
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    return this;
  }

  @Nonnull
  public List<BlockTO> getBlocks() {
    return blocks;
  }

  public PlantModelTO setBlocks(@Nonnull List<BlockTO> blocks) {
    this.blocks = requireNonNull(blocks, "blocks");
    return this;
  }

  @Nonnull
  public List<VehicleTO> getVehicles() {
    return vehicles;
  }

  public PlantModelTO setVehicles(@Nonnull List<VehicleTO> vehicles) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    return this;
  }

  @Nonnull
  public VisualLayoutTO getVisualLayout() {
    return visualLayout;
  }

  public PlantModelTO setVisualLayout(@Nonnull VisualLayoutTO visualLayout) {
    this.visualLayout = requireNonNull(visualLayout, "visualLayout");
    return this;
  }

}
