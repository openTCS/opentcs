// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VisualLayoutTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

/**
 */
public class PutPlantModelRequestTO {

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
  public PutPlantModelRequestTO(
      @Nonnull
      @JsonProperty(value = "name", required = true)
      String name
  ) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PutPlantModelRequestTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PutPlantModelRequestTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public List<PointTO> getPoints() {
    return points;
  }

  public PutPlantModelRequestTO setPoints(
      @Nonnull
      List<PointTO> points
  ) {
    this.points = requireNonNull(points, "points");
    return this;
  }

  @Nonnull
  public List<PathTO> getPaths() {
    return paths;
  }

  public PutPlantModelRequestTO setPaths(
      @Nonnull
      List<PathTO> paths
  ) {
    this.paths = requireNonNull(paths, "paths");
    return this;
  }

  @Nonnull
  public List<LocationTO> getLocations() {
    return locations;
  }

  public PutPlantModelRequestTO setLocations(
      @Nonnull
      List<LocationTO> locations
  ) {
    this.locations = requireNonNull(locations, "locations");
    return this;
  }

  @Nonnull
  public List<LocationTypeTO> getLocationTypes() {
    return locationTypes;
  }

  public PutPlantModelRequestTO setLocationTypes(
      @Nonnull
      List<LocationTypeTO> locationTypes
  ) {
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    return this;
  }

  @Nonnull
  public List<BlockTO> getBlocks() {
    return blocks;
  }

  public PutPlantModelRequestTO setBlocks(
      @Nonnull
      List<BlockTO> blocks
  ) {
    this.blocks = requireNonNull(blocks, "blocks");
    return this;
  }

  @Nonnull
  public List<VehicleTO> getVehicles() {
    return vehicles;
  }

  public PutPlantModelRequestTO setVehicles(
      @Nonnull
      List<VehicleTO> vehicles
  ) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    return this;
  }

  @Nonnull
  public VisualLayoutTO getVisualLayout() {
    return visualLayout;
  }

  public PutPlantModelRequestTO setVisualLayout(
      @Nonnull
      VisualLayoutTO visualLayout
  ) {
    this.visualLayout = requireNonNull(visualLayout, "visualLayout");
    return this;
  }

}
