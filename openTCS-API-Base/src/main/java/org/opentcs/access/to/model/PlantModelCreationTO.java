/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.util.annotations.ScheduledApiChange;

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

  @SuppressWarnings("deprecation")
  private PlantModelCreationTO(@Nonnull String name,
                               @Nonnull Map<String, String> properties,
                               @Nonnull List<PointCreationTO> points,
                               @Nonnull List<PathCreationTO> paths,
                               @Nonnull List<LocationTypeCreationTO> locationTypes,
                               @Nonnull List<LocationCreationTO> locations,
                               @Nonnull List<BlockCreationTO> blocks,
                               @Nonnull List<GroupCreationTO> groups,
                               @Nonnull List<StaticRouteCreationTO> staticRoutes,
                               @Nonnull List<VehicleCreationTO> vehicles,
                               @Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    super(name, properties);
    this.points = requireNonNull(points, "points");
    this.paths = requireNonNull(paths, "paths");
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    this.locations = requireNonNull(locations, "locations");
    this.blocks = requireNonNull(blocks, "blocks");
    this.groups = requireNonNull(groups, "groups");
    this.staticRoutes = requireNonNull(staticRoutes, "staticRoutes");
    this.vehicles = requireNonNull(vehicles, "vehicles");
    this.visualLayouts = requireNonNull(visualLayouts, "visualLayouts");
  }

  /**
   * Returns this plant model's points.
   *
   * @return This plant model's points.
   */
  public List<PointCreationTO> getPoints() {
    return Collections.unmodifiableList(points);
  }

  /**
   * Sets this plant model's points.
   *
   * @param points The new points.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setPoints(@Nonnull List<PointCreationTO> points) {
    this.points = requireNonNull(points, "points");
    return this;
  }

  /**
   * Creates a copy of this object with the given points.
   *
   * @param points The new points.
   * @return A copy of this model, differing in the given points.
   */
  public PlantModelCreationTO withPoints(@Nonnull List<PointCreationTO> points) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given point in the list of points.
   *
   * @param point the new point.
   * @return A copy of this model that also includes the given point.
   */
  public PlantModelCreationTO withPoint(@Nonnull PointCreationTO point) {
    requireNonNull(point, "point");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    listWithAppendix(points, point),
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's paths.
   *
   * @return This plant model's paths.
   */
  public List<PathCreationTO> getPaths() {
    return Collections.unmodifiableList(paths);
  }

  /**
   * Sets this plant model's paths.
   *
   * @param paths The new paths.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setPaths(@Nonnull List<PathCreationTO> paths) {
    this.paths = requireNonNull(paths, "paths");
    return this;
  }

  /**
   * Creates a copy of this object with the given paths.
   *
   * @param paths The new paths.
   * @return A copy of this model, differing in the given paths.
   */
  public PlantModelCreationTO withPaths(@Nonnull List<PathCreationTO> paths) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given path in the list of paths.
   *
   * @param path the new path.
   * @return A copy of this model that also includes the given path.
   */
  public PlantModelCreationTO withPath(@Nonnull PathCreationTO path) {
    requireNonNull(path, "path");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    listWithAppendix(paths, path),
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's location types.
   *
   * @return This plant model's location types.
   */
  public List<LocationTypeCreationTO> getLocationTypes() {
    return Collections.unmodifiableList(locationTypes);
  }

  /**
   * Sets this plant model's location types.
   *
   * @param locationTypes The new location types.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setLocationTypes(@Nonnull List<LocationTypeCreationTO> locationTypes) {
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    return this;
  }

  /**
   * Creates a copy of this object with the given location type.
   *
   * @param locationTypes The new location types.
   * @return A copy of this model, differing in the given location types.
   */
  public PlantModelCreationTO withLocationTypes(@Nonnull List<LocationTypeCreationTO> locationTypes) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given path in the list of location types.
   *
   * @param locationType the new location type.
   * @return A copy of this model that also includes the given location type.
   */
  public PlantModelCreationTO withLocationType(@Nonnull LocationTypeCreationTO locationType) {
    requireNonNull(locationType, "locationType");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    listWithAppendix(locationTypes, locationType),
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's locations.
   *
   * @return This plant model's locations.
   */
  public List<LocationCreationTO> getLocations() {
    return Collections.unmodifiableList(locations);
  }

  /**
   * Sets this plant model's locations.
   *
   * @param locations The new locations.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setLocations(@Nonnull List<LocationCreationTO> locations) {
    this.locations = requireNonNull(locations, "locations");
    return this;
  }

  /**
   * Creates a copy of this object with the given locations.
   *
   * @param locations The new locations.
   * @return A copy of this model, differing in the given locations.
   */
  public PlantModelCreationTO withLocations(@Nonnull List<LocationCreationTO> locations) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given block in the list of locations.
   *
   * @param location the new location.
   * @return A copy of this model that also includes the given location.
   */
  public PlantModelCreationTO withLocation(@Nonnull LocationCreationTO location) {
    requireNonNull(location, "location");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    listWithAppendix(locations, location),
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's blocks.
   *
   * @return This plant model's blocks.
   */
  public List<BlockCreationTO> getBlocks() {
    return Collections.unmodifiableList(blocks);
  }

  /**
   * Sets this plant model's blocks.
   *
   * @param blocks The new blocks.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setBlocks(@Nonnull List<BlockCreationTO> blocks) {
    this.blocks = requireNonNull(blocks, "blocks");
    return this;
  }

  /**
   * Creates a copy of this object with the given blocks.
   *
   * @param blocks The new blocks.
   * @return A copy of this model, differing in the given blocks.
   */
  public PlantModelCreationTO withBlocks(@Nonnull List<BlockCreationTO> blocks) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given block in the list of blocks.
   *
   * @param block the new block.
   * @return A copy of this model that also includes the given block.
   */
  public PlantModelCreationTO withBlock(@Nonnull BlockCreationTO block) {
    requireNonNull(block, "block");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    CreationTO.listWithAppendix(blocks, block),
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's groups.
   *
   * @return This plant model's groups.
   */
  public List<GroupCreationTO> getGroups() {
    return Collections.unmodifiableList(groups);
  }

  /**
   * Sets this plant model's groups.
   *
   * @param groups The new groups.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setGroups(@Nonnull List<GroupCreationTO> groups) {
    this.groups = requireNonNull(groups, "groups");
    return this;
  }

  /**
   * Creates a copy of this object with the given groups.
   *
   * @param groups The new groups.
   * @return A copy of this model, differing in the given groups.
   */
  public PlantModelCreationTO withGroups(@Nonnull List<GroupCreationTO> groups) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given group in the list of groups.
   *
   * @param group the new group.
   * @return A copy of this model that also includes the given group.
   */
  public PlantModelCreationTO withGroup(@Nonnull GroupCreationTO group) {
    requireNonNull(group, "group");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations, blocks,
                                    listWithAppendix(groups, group),
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's static routes.
   *
   * @return This plant model's static routes.
   */
  @Deprecated
  public List<StaticRouteCreationTO> getStaticRoutes() {
    return Collections.unmodifiableList(staticRoutes);
  }

  /**
   * Sets this plant model's static routes.
   *
   * @param staticRoutes The new static routes.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setStaticRoutes(@Nonnull List<StaticRouteCreationTO> staticRoutes) {
    this.staticRoutes = requireNonNull(staticRoutes, "staticRoutes");
    return this;
  }

  /**
   * Creates a copy of this object with the given static routes.
   *
   * @param staticRoutes The new static routes.
   * @return A copy of this model, differing in the given static routes.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public PlantModelCreationTO withStaticRoutes(@Nonnull List<StaticRouteCreationTO> staticRoutes) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given route in the list of routes.
   *
   * @param staticRoute the new route.
   * @return A copy of this model that also includes the given route.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public PlantModelCreationTO withStaticRoute(@Nonnull StaticRouteCreationTO staticRoute) {
    requireNonNull(staticRoute, "staticRoute");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    listWithAppendix(staticRoutes, staticRoute),
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's vehicles.
   *
   * @return This plant model's vehicles.
   */
  public List<VehicleCreationTO> getVehicles() {
    return Collections.unmodifiableList(vehicles);
  }

  /**
   * Sets this plant model's vehicles.
   *
   * @param vehicles The new vehicles.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setVehicles(@Nonnull List<VehicleCreationTO> vehicles) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    return this;
  }

  /**
   * Creates a copy of this object with the given vehicles.
   *
   * @param vehicles The new vehicles.
   * @return A copy of this model, differing in the given vehicles.
   */
  public PlantModelCreationTO withVehicles(@Nonnull List<VehicleCreationTO> vehicles) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given vehicle in the list of vehicles.
   *
   * @param vehicle the new vehicle.
   * @return A copy of this model that also includes the given vehicle.
   */
  public PlantModelCreationTO withVehicle(@Nonnull VehicleCreationTO vehicle) {
    requireNonNull(vehicle, "vehicle");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    listWithAppendix(vehicles, vehicle),
                                    visualLayouts);
  }

  /**
   * Returns this plant model's visual layouts.
   *
   * @return This plant model's visual layouts.
   */
  public List<VisualLayoutCreationTO> getVisualLayouts() {
    return Collections.unmodifiableList(visualLayouts);
  }

  /**
   * Sets this plant model's visual layouts.
   *
   * @param visualLayouts The new visual layouts.
   * @return The modified plant model.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public PlantModelCreationTO setVisualLayouts(@Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    this.visualLayouts = requireNonNull(visualLayouts, "visualLayouts");
    return this;
  }

  /**
   * Creates a copy of this object with the given visual layouts.
   *
   * @param visualLayouts The new visual layouts.
   * @return A copy of this model, differing in the given visual layouts.
   */
  public PlantModelCreationTO withVisualLayouts(@Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given visual layout in the list of
   * visual layout elements.
   *
   * @param visualLayout the new visual layout.
   * @return A copy of this model that also includes the given visual layout.
   */
  public PlantModelCreationTO withVisualLayout(@Nonnull VisualLayoutCreationTO visualLayout) {
    requireNonNull(visualLayout, "visualLayout");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    listWithAppendix(visualLayouts, visualLayout));
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public PlantModelCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new PlantModelCreationTO(getName(),
                                    properties,
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
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
  public PlantModelCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new PlantModelCreationTO(getName(),
                                    propertiesWith(key, value),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    staticRoutes,
                                    vehicles,
                                    visualLayouts);
  }
}
