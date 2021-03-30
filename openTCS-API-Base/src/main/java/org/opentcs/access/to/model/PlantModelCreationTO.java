/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.ModelConstants;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
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
  @Deprecated
  private List<GroupCreationTO> groups = new LinkedList<>();
  /**
   * The plant model's vehicles.
   */
  private List<VehicleCreationTO> vehicles = new LinkedList<>();
  /**
   * The plant model's visual layouts.
   */
  private List<VisualLayoutCreationTO> visualLayouts
      = new LinkedList<>(Arrays.asList(defaultVisualLayout()));

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
                               @Nonnull List<VehicleCreationTO> vehicles,
                               @Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    super(name, properties);
    this.points = requireNonNull(points, "points");
    this.paths = requireNonNull(paths, "paths");
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
    this.locations = requireNonNull(locations, "locations");
    this.blocks = requireNonNull(blocks, "blocks");
    this.groups = requireNonNull(groups, "groups");
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
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Returns this plant model's groups.
   *
   * @return This plant model's groups.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public List<GroupCreationTO> getGroups() {
    return Collections.unmodifiableList(groups);
  }

  /**
   * Creates a copy of this object with the given groups.
   *
   * @param groups The new groups.
   * @return A copy of this model, differing in the given groups.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public PlantModelCreationTO withGroups(@Nonnull List<GroupCreationTO> groups) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
                                    vehicles,
                                    visualLayouts);
  }

  /**
   * Creates a copy of this object that includes the given group in the list of groups.
   *
   * @param group the new group.
   * @return A copy of this model that also includes the given group.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public PlantModelCreationTO withGroup(@Nonnull GroupCreationTO group) {
    requireNonNull(group, "group");
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations, blocks,
                                    listWithAppendix(groups, group),
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
                                    listWithAppendix(vehicles, vehicle),
                                    visualLayouts);
  }

  /**
   * Returns this plant model's visual layout.
   *
   * @return This plant model's visual layout.
   */
  public VisualLayoutCreationTO getVisualLayout() {
    return visualLayouts.get(visualLayouts.size() - 1);
  }

  /**
   * Returns this plant model's visual layouts.
   *
   * @return This plant model's visual layouts.
   * @deprecated Use{@link #getVisualLayout()} instead.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public List<VisualLayoutCreationTO> getVisualLayouts() {
    return Collections.unmodifiableList(visualLayouts);
  }

  /**
   * Creates a copy of this object with the given visual layouts.
   *
   * @param visualLayouts The new visual layouts.
   * @return A copy of this model, differing in the given visual layouts.
   * @deprecated Use {@link #withVisualLayout(org.opentcs.access.to.model.VisualLayoutCreationTO)}
   * instead
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public PlantModelCreationTO withVisualLayouts(@Nonnull List<VisualLayoutCreationTO> visualLayouts) {
    return new PlantModelCreationTO(getName(),
                                    getModifiableProperties(),
                                    points,
                                    paths,
                                    locationTypes,
                                    locations,
                                    blocks,
                                    groups,
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
  @ScheduledApiChange(details = "A plant model will contain only a single visual layout.",
                      when = "6.0")
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
                                    vehicles,
                                    visualLayouts);
  }

  @Override
  public String toString() {
    return "PlantModelCreationTO{"
        + "name=" + getName()
        + ", points=" + points
        + ", paths=" + paths
        + ", locationTypes=" + locationTypes
        + ", locations=" + locations
        + ", blocks=" + blocks
        + ", groups=" + groups
        + ", vehicles=" + vehicles
        + ", visualLayouts=" + visualLayouts
        + ", properties=" + getProperties()
        + '}';
  }

  private VisualLayoutCreationTO defaultVisualLayout() {
    return new VisualLayoutCreationTO(ModelConstants.DEFAULT_VISUAL_LAYOUT_NAME)
        .withLayer(new Layer(ModelConstants.DEFAULT_LAYER_ID,
                             ModelConstants.DEFAULT_LAYER_ORDINAL,
                             true,
                             ModelConstants.DEFAULT_LAYER_NAME,
                             ModelConstants.DEFAULT_LAYER_GROUP_ID))
        .withLayerGroup(new LayerGroup(ModelConstants.DEFAULT_LAYER_GROUP_ID,
                                       ModelConstants.DEFAULT_LAYER_GROUP_NAME,
                                       true));
  }
}
