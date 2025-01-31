// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to find out stranded vehicles.
 */
public class StrandedVehicles
    implements
      Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StrandedVehicles.class);
  /**
   * Object service to access the model.
   */
  private final TCSObjectService objectService;
  /**
   * Provider to get the current time.
   */
  private final TimeProvider timeProvider;
  /**
   * Map to store the current snapshot for each vehicle.
   */
  private final Map<String, VehicleSnapshot> currentSnapshots = new HashMap<>();
  /**
   * Map to store the previous snapshot for each vehicle.
   */
  private final Map<String, VehicleSnapshot> previousSnapshots = new HashMap<>();
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param timeProvider Provider to get the current time.
   */
  @Inject
  public StrandedVehicles(
      TCSObjectService objectService,
      TimeProvider timeProvider
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.timeProvider = requireNonNull(timeProvider, "timeProvider");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    long currentTime = timeProvider.getCurrentTimeEpochMillis();
    objectService.fetchObjects(Vehicle.class).forEach(vehicle -> {
      VehicleSnapshot vehicleSnapshot = new VehicleSnapshot(vehicle);
      vehicleSnapshot.setLastRelevantStateChange(currentTime);
      currentSnapshots.put(vehicle.getName(), vehicleSnapshot);
    });

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    currentSnapshots.clear();
    previousSnapshots.clear();

    initialized = false;
  }

  /**
   * Identifies stranded vehicles.
   *
   * @param currentTime The current time.
   * @param strandedDurationThreshold The duration that a vehicle must be in a stranded state to
   * actually be considered stranded.
   */
  public void identifyStrandedVehicles(
      long currentTime,
      long strandedDurationThreshold
  ) {
    LOG.debug("Identifying stranded vehicles...");
    previousSnapshots.clear();
    previousSnapshots.putAll(currentSnapshots);
    currentSnapshots.clear();

    objectService.fetchObjects(Vehicle.class)
        .stream()
        .forEach(vehicle -> {
          VehicleSnapshot previousSnapshot = previousSnapshots.get(vehicle.getName());
          VehicleSnapshot currentSnapshot = new VehicleSnapshot(vehicle);

          if (vehicle.getState() != Vehicle.State.IDLE
              || relevantPropertiesChanged(previousSnapshot, currentSnapshot)) {
            currentSnapshot.setLastRelevantStateChange(currentTime);
            currentSnapshot.setStranded(false);
          }
          else if (isInStrandedState(vehicle)) {
            LOG.debug("Checking if vehicle '{}' is stranded long enough...", vehicle);
            long lastRelevantStateChange = previousSnapshot.getLastRelevantStateChange();
            currentSnapshot.setLastRelevantStateChange(lastRelevantStateChange);

            long strandedDuration = currentTime - lastRelevantStateChange;
            currentSnapshot.setStranded(strandedDuration >= strandedDurationThreshold);
          }
          else {
            // The state of the vehicle has not effectively changed. Therefore, apply values from
            // the previous snapshot.
            currentSnapshot.setLastRelevantStateChange(
                previousSnapshot.getLastRelevantStateChange()
            );
            currentSnapshot.setStranded(previousSnapshot.isStranded());
          }

          LOG.debug("Snapshot of vehicle '{}': {}", vehicle.getName(), currentSnapshot);
          currentSnapshots.put(vehicle.getName(), currentSnapshot);
        });
  }

  /**
   * Returns vehicles that are currently considered newly stranded (i.e., vehicles that were
   * previously not considered stranded, but are now considered stranded).
   *
   * @return The set of vehicles that are considered newly stranded.
   */
  public Set<VehicleSnapshot> newlyStrandedVehicles() {
    return currentSnapshots.values().stream()
        .filter(
            vehicleSnapshot -> !previousSnapshots.get(vehicleSnapshot.getVehicle().getName())
                .isStranded()
                && vehicleSnapshot.isStranded()
        )
        .collect(Collectors.toSet());
  }

  /**
   * Returns vehicles that are currently considered no longer stranded (i.e., vehicles that were
   * previously considered stranded, but are now not considered stranded).
   *
   * @return The set of vehicles that are no longer considered stranded.
   */
  public Set<VehicleSnapshot> noLongerStrandedVehicles() {
    return currentSnapshots.values().stream()
        .filter(
            vehicleSnapshot -> previousSnapshots.get(vehicleSnapshot.getVehicle().getName())
                .isStranded()
                && !vehicleSnapshot.isStranded()
        )
        .collect(Collectors.toSet());
  }

  private boolean isInStrandedState(Vehicle vehicle) {
    return isIdleAtNoParkingPosition(vehicle) || isIdleWithTransportOrder(vehicle);
  }

  private boolean isIdleAtNoParkingPosition(Vehicle vehicle) {
    return vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && objectService.fetchObject(Point.class, vehicle.getCurrentPosition())
            .getType() != Point.Type.PARK_POSITION;
  }

  private boolean isIdleWithTransportOrder(Vehicle vehicle) {
    return vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getTransportOrder() != null;
  }

  private boolean relevantPropertiesChanged(
      VehicleSnapshot previousSnapshot,
      VehicleSnapshot currentSnapshot
  ) {
    return previousSnapshot.getLastState() != currentSnapshot.getLastState()
        || !Objects.equals(previousSnapshot.getLastPosition(), currentSnapshot.getLastPosition())
        || !Objects.equals(
            previousSnapshot.getVehicle().getTransportOrder(),
            currentSnapshot.getVehicle().getTransportOrder()
        );
  }

  /**
   * A snapshot of a vehicle's state with additional information on whether the given vehicle is
   * considered stranded and when the last state change (with regard to the "stranded" state)
   * occurred.
   */
  public static class VehicleSnapshot {

    private final Vehicle vehicle;
    private long lastRelevantStateChange;
    private boolean stranded;

    /**
     * Creates a new instance.
     *
     * @param vehicle Vehicle to be stored.
     */
    public VehicleSnapshot(Vehicle vehicle) {
      this.vehicle = requireNonNull(vehicle, "vehicle");
    }

    public boolean isStranded() {
      return stranded;
    }

    public void setStranded(boolean stranded) {
      this.stranded = stranded;
    }

    public Vehicle getVehicle() {
      return vehicle;
    }

    public long getLastRelevantStateChange() {
      return lastRelevantStateChange;
    }

    public void setLastRelevantStateChange(long lastRelevantStateChange) {
      this.lastRelevantStateChange = lastRelevantStateChange;
    }

    public TCSObjectReference<Point> getLastPosition() {
      return vehicle.getCurrentPosition();
    }

    public Vehicle.State getLastState() {
      return vehicle.getState();
    }

    @Override
    public String toString() {
      return "VehicleSnapshot{"
          + "stranded=" + stranded
          + ", lastRelevantStateChange=" + lastRelevantStateChange
          + ", vehicle=" + vehicle
          + '}';
    }
  }
}
