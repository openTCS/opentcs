/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.VisualLayout;

/**
 * Caches some elements a plant model contains of.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PlantModelCache {

  /**
   * The plant model's visual layouts.
   */
  private Set<VisualLayout> visualLayouts = new HashSet<>();
  /**
   * The plant model's points.
   */
  private Map<String, Point> points = new HashMap<>();
  /**
   * The plant model's paths.
   */
  private Map<String, Path> paths = new HashMap<>();
  /**
   * The plant model's location types.
   */
  private Map<String, LocationType> locationTypes = new HashMap<>();
  /**
   * The plant model's locations.
   */
  private Map<String, Location> locations = new HashMap<>();

  public Set<VisualLayout> getVisualLayouts() {
    return visualLayouts;
  }

  public void setVisualLayouts(@Nonnull Set<VisualLayout> visualLayouts) {
    this.visualLayouts = requireNonNull(visualLayouts, "visualLayouts");
  }

  public Map<String, Point> getPoints() {
    return points;
  }

  public void setPoints(@Nonnull Map<String, Point> points) {
    this.points = requireNonNull(points, "points");
  }

  public Map<String, Path> getPaths() {
    return paths;
  }

  public void setPaths(@Nonnull Map<String, Path> paths) {
    this.paths = requireNonNull(paths, "pahts");
  }

  public Map<String, LocationType> getLocationTypes() {
    return locationTypes;
  }

  public void setLocationTypes(@Nonnull Map<String, LocationType> locationTypes) {
    this.locationTypes = requireNonNull(locationTypes, "locationTypes");
  }

  public Map<String, Location> getLocations() {
    return locations;
  }

  public void setLocations(@Nonnull Map<String, Location> locations) {
    this.locations = requireNonNull(locations, "locations");
  }
}
