/*
 *
 * Created on 20.08.2013 11:56:11
 */
package org.opentcs.guing.plugins.themes;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 * Standard implementation of <code>VehicleTheme</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class StandardVehicleTheme2
    implements VehicleTheme {

  /**
   * Map containing images for a specific vehicle state when it's unloaded.
   */
  private final Map<Vehicle.State, String> stateMapUnloaded
      = new EnumMap<>(Vehicle.State.class);
  /**
   * Map containing images for a specific vehicle state when it's loaded.
   */
  private final Map<Vehicle.State, String> stateMapLoaded
      = new EnumMap<>(Vehicle.State.class);
  /**
   * The path containing the images.
   */
  private static final String path
      = "/org/opentcs/guing/res/symbols/vehicle/";

  public StandardVehicleTheme2() {
    initMaps();
  }

  @Override
  public String getImagePathFor(Vehicle vehicle) {
    Objects.requireNonNull(vehicle, "vehicle is null");
    String relativePath = checkLoaded(vehicle)
        ? stateMapLoaded.get(vehicle.getState())
        : stateMapUnloaded.get(vehicle.getState());
    return relativePath == null ? null : path + relativePath;
  }

  @Override
  public Set<String> getAllImagePaths() {
    Set<String> images = new HashSet<>();
    for (String value : stateMapLoaded.values()) {
      images.add(path + value);
    }
    for (String value : stateMapUnloaded.values()) {
      images.add(path + value);
    }
    return images;
  }

  @Override
  public String getName() {
    return "Standard vehicle theme 2";
  }

  /**
   * Initializes the maps with values.
   */
  private void initMaps() {
    stateMapUnloaded.put(Vehicle.State.CHARGING, "unloaded_charging.png");
    stateMapUnloaded.put(Vehicle.State.ERROR, "unloaded_error.png");
    stateMapUnloaded.put(Vehicle.State.EXECUTING, "unloaded_normal.png");
    stateMapUnloaded.put(Vehicle.State.IDLE, "unloaded_normal.png");
    stateMapUnloaded.put(Vehicle.State.UNAVAILABLE, "unloaded_normal.png");
    stateMapUnloaded.put(Vehicle.State.UNKNOWN, "unloaded_normal.png");

    stateMapLoaded.put(Vehicle.State.CHARGING, "loaded_charging.png");
    stateMapLoaded.put(Vehicle.State.ERROR, "loaded_error.png");
    stateMapLoaded.put(Vehicle.State.EXECUTING, "loaded_normal.png");
    stateMapLoaded.put(Vehicle.State.IDLE, "loaded_normal.png");
    stateMapLoaded.put(Vehicle.State.UNAVAILABLE, "loaded_normal.png");
    stateMapLoaded.put(Vehicle.State.UNKNOWN, "loaded_normal.png");
  }

  /**
   * Checks if a given vehicle is loaded.
   *
   * @param vehicle The vehicle.
   * @return Flag indicating if it is loaded.
   */
  private boolean checkLoaded(Vehicle vehicle) {
    if (vehicle.getLoadHandlingDevices().isEmpty()) {
      return false;
    }
    else {
      for (LoadHandlingDevice device : vehicle.getLoadHandlingDevices()) {
        if (device.isFull()) {
          return true;
        }
      }
    }
    return false;
  }
}
