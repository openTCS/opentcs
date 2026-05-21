// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.VisualLayout;

/**
 * An immutable representation of a complete plant model's state.
 */
public class PlantModel
    implements
      Serializable {

  private final String name;
  private final Map<String, String> properties;
  private final Map<String, Point> points;
  private final Map<String, Path> paths;
  private final Map<String, LocationType> locationTypes;
  private final Map<String, Location> locations;
  private final Map<String, Block> blocks;
  private final Map<String, Vehicle> vehicles;
  private final VisualLayout visualLayout;

  /**
   * Creates a new instance.
   *
   * @param name The model's name.
   */
  public PlantModel(
      @Nonnull
      String name
  ) {
    this(
        name, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
        defaultVisualLayout()
    );
  }

  private PlantModel(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      Map<String, Point> points,
      @Nonnull
      Map<String, Path> paths,
      @Nonnull
      Map<String, LocationType> locationTypes,
      @Nonnull
      Map<String, Location> locations,
      @Nonnull
      Map<String, Block> blocks,
      @Nonnull
      Map<String, Vehicle> vehicles,
      @Nonnull
      VisualLayout visualLayout
  ) {
    this.name = requireNonNull(name, "name");
    this.properties = Map.copyOf(properties);
    this.points = Map.copyOf(points);
    this.paths = Map.copyOf(paths);
    this.locationTypes = Map.copyOf(locationTypes);
    this.locations = Map.copyOf(locations);
    this.blocks = Map.copyOf(blocks);
    this.vehicles = Map.copyOf(vehicles);
    this.visualLayout = requireNonNull(visualLayout, "visualLayout");
  }

  /**
   * Returns the name of the plant model.
   *
   * @return The name of the plant model.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Returns the plant model's properties.
   *
   * @return The plant model's properties.
   */
  @Nonnull
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Returns a copy of this plant model, with its properties replaced by the given ones.
   *
   * @param properties The properties.
   * @return A copy of this plant model, with its properties replaced by the given ones.
   */
  public PlantModel withProperties(Map<String, String> properties) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the points in this plant model.
   *
   * @return The points in this plant model.
   */
  @Nonnull
  public Set<Point> getPoints() {
    return Set.copyOf(points.values());
  }

  /**
   * Returns the points in this plant model.
   *
   * @return The points in this plant model.
   */
  @Nonnull
  public Map<String, Point> getPointsByName() {
    return points;
  }

  /**
   * Returns the point with the given name.
   *
   * @param name The name of the point.
   * @return An {@link Optional} containing the named point, or an empty {@link Optional} if no
   * such point exists.
   */
  public Optional<Point> getPoint(String name) {
    return Optional.ofNullable(points.get(name));
  }

  /**
   * Returns a copy of this plant model, with its points replaced by the given ones.
   *
   * @param points The points.
   * @return A copy of this plant model, with its points replaced by the given ones.
   */
  public PlantModel withPoints(
      @Nonnull
      Set<Point> points
  ) {
    return new PlantModel(
        name,
        properties,
        points.stream().collect(Collectors.toUnmodifiableMap(Point::getName, Function.identity())),
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its points replaced by the given ones.
   *
   * @param points The points.
   * @return A copy of this plant model, with its points replaced by the given ones.
   */
  public PlantModel withPoints(
      @Nonnull
      Map<String, Point> points
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the paths in this plant model.
   *
   * @return The paths in this plant model.
   */
  @Nonnull
  public Set<Path> getPaths() {
    return Set.copyOf(paths.values());
  }

  /**
   * Returns the paths in this plant model.
   *
   * @return The paths in this plant model.
   */
  @Nonnull
  public Map<String, Path> getPathsByName() {
    return paths;
  }

  /**
   * Returns the path with the given name.
   *
   * @param name The name of the path.
   * @return An {@link Optional} containing the named path, or an empty {@link Optional} if no
   * such path exists.
   */
  public Optional<Path> getPath(String name) {
    return Optional.ofNullable(paths.get(name));
  }

  /**
   * Returns a copy of this plant model, with its paths replaced by the given ones.
   *
   * @param paths The paths.
   * @return A copy of this plant model, with its paths replaced by the given ones.
   */
  public PlantModel withPaths(
      @Nonnull
      Set<Path> paths
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths.stream().collect(Collectors.toUnmodifiableMap(Path::getName, Function.identity())),
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its paths replaced by the given ones.
   *
   * @param paths The paths.
   * @return A copy of this plant model, with its paths replaced by the given ones.
   */
  public PlantModel withPaths(
      @Nonnull
      Map<String, Path> paths
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the location types in this plant model.
   *
   * @return The location types in this plant model.
   */
  @Nonnull
  public Set<LocationType> getLocationTypes() {
    return Set.copyOf(locationTypes.values());
  }

  /**
   * Returns the location types in this plant model.
   *
   * @return The location types in this plant model.
   */
  @Nonnull
  public Map<String, LocationType> getLocationTypesByName() {
    return locationTypes;
  }

  /**
   * Returns the location type with the given name.
   *
   * @param name The name of the location type.
   * @return An {@link Optional} containing the named location type, or an empty {@link Optional}
   * if no such location type exists.
   */
  public Optional<LocationType> getLocationType(String name) {
    return Optional.ofNullable(locationTypes.get(name));
  }

  /**
   * Returns a copy of this plant model, with its location types replaced by the given ones.
   *
   * @param locationTypes The location types.
   * @return A copy of this plant model, with its location types replaced by the given ones.
   */
  public PlantModel withLocationTypes(
      @Nonnull
      Set<LocationType> locationTypes
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes.stream()
            .collect(Collectors.toUnmodifiableMap(LocationType::getName, Function.identity())),
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its location types replaced by the given ones.
   *
   * @param locationTypes The location types.
   * @return A copy of this plant model, with its location types replaced by the given ones.
   */
  public PlantModel withLocationTypes(
      @Nonnull
      Map<String, LocationType> locationTypes
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the locations in this plant model.
   *
   * @return The locations in this plant model.
   */
  @Nonnull
  public Set<Location> getLocations() {
    return Set.copyOf(locations.values());
  }

  /**
   * Returns the locations in this plant model.
   *
   * @return The locations in this plant model.
   */
  @Nonnull
  public Map<String, Location> getLocationsByName() {
    return locations;
  }

  /**
   * Returns the location with the given name.
   *
   * @param name The name of the location.
   * @return An {@link Optional} containing the named location, or an empty {@link Optional} if no
   * such location exists.
   */
  public Optional<Location> getLocation(String name) {
    return Optional.ofNullable(locations.get(name));
  }

  /**
   * Returns a copy of this plant model, with its locations replaced by the given ones.
   *
   * @param locations The locations.
   * @return A copy of this plant model, with its locations replaced by the given ones.
   */
  public PlantModel withLocations(
      @Nonnull
      Set<Location> locations
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations.stream()
            .collect(Collectors.toUnmodifiableMap(Location::getName, Function.identity())),
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its locations replaced by the given ones.
   *
   * @param locations The locations.
   * @return A copy of this plant model, with its locations replaced by the given ones.
   */
  public PlantModel withLocations(
      @Nonnull
      Map<String, Location> locations
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the blocks in this plant model.
   *
   * @return The blocks in this plant model.
   */
  @Nonnull
  public Set<Block> getBlocks() {
    return Set.copyOf(blocks.values());
  }

  /**
   * Returns the blocks in this plant model.
   *
   * @return The blocks in this plant model.
   */
  @Nonnull
  public Map<String, Block> getBlocksByName() {
    return blocks;
  }

  /**
   * Returns the block with the given name.
   *
   * @param name The name of the block.
   * @return An {@link Optional} containing the named block, or an empty {@link Optional} if no
   * such block exists.
   */
  public Optional<Block> getBlock(String name) {
    return Optional.ofNullable(blocks.get(name));
  }

  /**
   * Returns a copy of this plant model, with its blocks replaced by the given ones.
   *
   * @param blocks The blocks.
   * @return A copy of this plant model, with its blocks replaced by the given ones.
   */
  public PlantModel withBlocks(
      @Nonnull
      Set<Block> blocks
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks.stream().collect(Collectors.toUnmodifiableMap(Block::getName, Function.identity())),
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its blocks replaced by the given ones.
   *
   * @param blocks The blocks.
   * @return A copy of this plant model, with its blocks replaced by the given ones.
   */
  public PlantModel withBlocks(
      @Nonnull
      Map<String, Block> blocks
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the vehicles in this plant model.
   *
   * @return The vehicles in this plant model.
   */
  @Nonnull
  public Set<Vehicle> getVehicles() {
    return Set.copyOf(vehicles.values());
  }

  /**
   * Returns the vehicles in this plant model.
   *
   * @return The vehicles in this plant model.
   */
  @Nonnull
  public Map<String, Vehicle> getVehiclesByName() {
    return vehicles;
  }

  /**
   * Returns the vehicle with the given name.
   *
   * @param name The name of the vehicle.
   * @return An {@link Optional} containing the named vehicle, or an empty {@link Optional} if no
   * such vehicle exists.
   */
  public Optional<Vehicle> getVehicle(String name) {
    return Optional.ofNullable(vehicles.get(name));
  }

  /**
   * Returns a copy of this plant model, with its vehicles replaced by the given ones.
   *
   * @param vehicles The vehicles.
   * @return A copy of this plant model, with its vehicles replaced by the given ones.
   */
  public PlantModel withVehicles(
      @Nonnull
      Set<Vehicle> vehicles
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles.stream()
            .collect(Collectors.toUnmodifiableMap(Vehicle::getName, Function.identity())),
        visualLayout
    );
  }

  /**
   * Returns a copy of this plant model, with its vehicles replaced by the given ones.
   *
   * @param vehicles The vehicles.
   * @return A copy of this plant model, with its vehicles replaced by the given ones.
   */
  public PlantModel withVehicles(
      @Nonnull
      Map<String, Vehicle> vehicles
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  /**
   * Returns the visual layout in this plant model.
   *
   * @return The visual layout in this plant model.
   */
  @Nonnull
  public VisualLayout getVisualLayout() {
    return visualLayout;
  }

  /**
   * Returns a copy of this plant model, with its visual layout replaced by the given one.
   *
   * @param visualLayout The visual layout to be set.
   * @return A copy of this plant model, with its visual layout replaced by the given one.
   */
  public PlantModel withVisualLayout(
      @Nonnull
      VisualLayout visualLayout
  ) {
    return new PlantModel(
        name,
        properties,
        points,
        paths,
        locationTypes,
        locations,
        blocks,
        vehicles,
        visualLayout
    );
  }

  @Override
  public String toString() {
    return "PlantModel{"
        + "name=" + name
        + ", properties=" + properties
        + ", points=" + points
        + ", paths=" + paths
        + ", locationTypes=" + locationTypes
        + ", locations=" + locations
        + ", blocks=" + blocks
        + ", vehicles=" + vehicles
        + ", visualLayout=" + visualLayout
        + '}';
  }

  private static VisualLayout defaultVisualLayout() {
    return new VisualLayout(ModelConstants.DEFAULT_VISUAL_LAYOUT_NAME)
        .withLayers(
            List.of(
                new Layer(
                    ModelConstants.DEFAULT_LAYER_ID,
                    ModelConstants.DEFAULT_LAYER_ORDINAL,
                    true,
                    ModelConstants.DEFAULT_LAYER_NAME,
                    ModelConstants.DEFAULT_LAYER_GROUP_ID
                )
            )
        )
        .withLayerGroups(
            List.of(
                new LayerGroup(
                    ModelConstants.DEFAULT_LAYER_GROUP_ID,
                    ModelConstants.DEFAULT_LAYER_GROUP_NAME,
                    true
                )
            )
        );
  }

}
