/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PlantModelCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The plant model's points.
   */
  private List<PointCreationTO> points = new LinkedList<>();
  /**
   * The plant model's paths.
   */
  private List<PathCreationTO> paths = new LinkedList<>();
  /**
   * The plant model's location types.
   */
  private List<LocationTypeCreationTO> locationTypes = new LinkedList<>();
  /**
   * The plant model's locations.
   */
  private List<LocationCreationTO> locations = new LinkedList<>();
  /**
   * The plant model's blocks.
   */
  private List<BlockCreationTO> blocks = new LinkedList<>();
  /**
   * The plant model's groups.
   */
  private List<GroupCreationTO> groups = new LinkedList<>();
  /**
   * The plant model's static routes.
   */
  @SuppressWarnings("deprecation")
  private List<StaticRouteCreationTO> staticRoutes = new LinkedList<>();
  /**
   * The plant model's vehicles.
   */
  private List<VehicleCreationTO> vehicles = new LinkedList<>();
  /**
   * The plant model's visual layouts.
   */
  private List<VisualLayoutCreationTO> visualLayouts = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this plant model.
   */
  public PlantModelCreationTO(String name) {
    super(name);
  }

  /**
   * Returns this plant model's points.
   *
   * @return This plant model's points.
   */
  public List<PointCreationTO> getPoints() {
    return points;
  }

  /**
   * Sets this plant model's points.
   *
   * @param points The new points.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setPoints(@Nonnull List<PointCreationTO> points) {
    this.points = requireNonNull(points, "points");
    return this;
  }

  /**
   * Returns this plant model's paths.
   *
   * @return This plant model's paths.
   */
  public List<PathCreationTO> getPaths() {
    return paths;
  }

  /**
   * Sets this plant model's paths.
   *
   * @param paths The new paths.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setPaths(@Nonnull List<PathCreationTO> paths) {
    this.paths = requireNonNull(paths, "paths");
    return this;
  }

  /**
   * Returns this plant model's location types.
   *
   * @return This plant model's location types.
   */
  public List<LocationTypeCreationTO> getLocationTypes() {
    return locationTypes;
  }

  /**
   * Sets this plant model's location types.
   *
   * @param locationTypes The new location types.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setLocationTypes(@Nonnull List<LocationTypeCreationTO> locationTypes) {
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    return this;
  }

  /**
   * Returns this plant model's locations.
   *
   * @return This plant model's locations.
   */
  public List<LocationCreationTO> getLocations() {
    return locations;
  }

  /**
   * Sets this plant model's locations.
   *
   * @param locations The new locations.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setLocations(@Nonnull List<LocationCreationTO> locations) {
    this.locations = requireNonNull(locations, "locations");
    return this;
  }

  /**
   * Returns this plant model's blocks.
   *
   * @return This plant model's blocks.
   */
  public List<BlockCreationTO> getBlocks() {
    return blocks;
  }

  /**
   * Sets this plant model's blocks.
   *
   * @param blocks The new blocks.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setBlocks(@Nonnull List<BlockCreationTO> blocks) {
    this.blocks = requireNonNull(blocks, "blocks");
    return this;
  }

  /**
   * Returns this plant model's groups.
   *
   * @return This plant model's groups.
   */
  public List<GroupCreationTO> getGroups() {
    return groups;
  }

  /**
   * Sets this plant model's groups.
   *
   * @param groups The new groups.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setGroups(@Nonnull List<GroupCreationTO> groups) {
    this.groups = requireNonNull(groups, "groups");
    return this;
  }

  /**
   * Returns this plant model's static routes.
   *
   * @return This plant model's static routes.
   */
  @Deprecated
  public List<StaticRouteCreationTO> getStaticRoutes() {
    return staticRoutes;
  }

  /**
   * Sets this plant model's static routes.
   *
   * @param staticRoutes The new static routes.
   * @return The modified plant model.
   */
  @Deprecated
  @Nonnull
  public PlantModelCreationTO setStaticRoutes(@Nonnull List<StaticRouteCreationTO> staticRoutes) {
    this.staticRoutes = requireNonNull(staticRoutes, "staticRoutes");
    return this;
  }

  /**
   * Returns this plant model's vehicles.
   *
   * @return This plant model's vehicles.
   */
  public List<VehicleCreationTO> getVehicles() {
    return vehicles;
  }

  /**
   * Sets this plant model's vehicles.
   *
   * @param vehicles The new vehicles.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setVehicles(@Nonnull List<VehicleCreationTO> vehicles) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    return this;
  }

  /**
   * Returns this plant model's visual layouts.
   *
   * @return This plant model's visual layouts.
   */
  public List<VisualLayoutCreationTO> getVisualLayouts() {
    return visualLayouts;
  }

  /**
   * Sets this plant model's visual layouts.
   *
   * @param visualLayouts The new visual layouts.
   * @return The modified plant model.
   */
  @Nonnull
  public PlantModelCreationTO setVisualLayouts(@Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    this.visualLayouts = requireNonNull(visualLayouts, "visualLayouts");
    return this;
  }
}
