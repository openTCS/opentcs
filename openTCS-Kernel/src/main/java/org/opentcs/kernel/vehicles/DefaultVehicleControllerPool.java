/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains associations of {@link Vehicle}, {@link VehicleController} and
 * {@link VehicleCommAdapter}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class DefaultVehicleControllerPool
    implements LocalVehicleControllerPool {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultVehicleControllerPool.class);
  /**
   * The vehicle service.
   */
  private final InternalVehicleService vehicleService;
  /**
   * A factory for vehicle managers.
   */
  private final VehicleControllerFactory vehicleManagerFactory;
  /**
   * The currently existing/assigned managers, mapped by the names of the
   * corresponding vehicles.
   */
  private final Map<String, PoolEntry> poolEntries = new HashMap<>();
  /**
   * Indicates whether this components is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new StandardVehicleManagerPool.
   *
   * @param vehicleService The vehicle service.
   * @param vehicleManagerFactory A factory for vehicle managers.
   */
  @Inject
  public DefaultVehicleControllerPool(InternalVehicleService vehicleService,
                                      VehicleControllerFactory vehicleManagerFactory) {
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.vehicleManagerFactory = requireNonNull(vehicleManagerFactory, "vehicleManagerFactory");
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized, doing nothing.");
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized, doing nothing.");
      return;
    }
    // Detach all vehicles and reset their positions.
    for (PoolEntry curEntry : poolEntries.values()) {
      curEntry.vehicleController.terminate();
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, curEntry.vehicleName);
      vehicleService.updateVehiclePosition(vehicle.getReference(), null);
    }
    poolEntries.clear();
    initialized = false;
  }

  @Override
  public synchronized void attachVehicleController(String vehicleName,
                                                   VehicleCommAdapter commAdapter) {
    requireNonNull(vehicleName, "vehicleName");
    requireNonNull(commAdapter, "commAdapter");

    if (poolEntries.containsKey(vehicleName)) {
      LOG.warn("manager already attached, doing nothing");
      return;
    }

    Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, vehicleName);
    checkArgument(vehicle != null, "No such vehicle: %s", vehicleName);

    VehicleController controller = vehicleManagerFactory.createVehicleController(vehicle,
                                                                                 commAdapter);
    PoolEntry poolEntry = new PoolEntry(vehicleName, controller, commAdapter);
    poolEntries.put(vehicleName, poolEntry);
    controller.initialize();
  }

  @Override
  public synchronized void detachVehicleController(String vehicleName) {
    requireNonNull(vehicleName, "vehicleName");

    LOG.debug("Detaching controller for vehicle {}...", vehicleName);
    PoolEntry poolEntry = poolEntries.remove(vehicleName);
    if (poolEntry == null) {
      LOG.debug("A vehicle named '{}' is not attached to a controller.", vehicleName);
      return;
    }
    // Clean up - mark vehicle state and adapter state as unknown.
    poolEntry.vehicleController.terminate();
  }

  @Override
  public VehicleController getVehicleController(String vehicleName) {
    requireNonNull(vehicleName, "vehicleName");

    PoolEntry poolEntry = poolEntries.get(vehicleName);
    return poolEntry == null ? new NullVehicleController(vehicleName) : poolEntry.vehicleController;
  }

  /**
   * An entry in this vehicle manager pool.
   */
  private static final class PoolEntry {

    /**
     * The name of the vehicle managed.
     */
    private final String vehicleName;
    /**
     * The vehicle controller associated with the vehicle.
     */
    private final VehicleController vehicleController;
    /**
     * The communication adapter associated with the vehicle.
     */
    private final VehicleCommAdapter commAdapter;

    /**
     * Creates a new pool entry.
     *
     * @param name The name of the vehicle managed.
     * @param manager The vehicle manager associated with the vehicle.
     * @param controller The VehicleController
     * @param adapter The communication adapter associated with the vehicle.
     */
    private PoolEntry(String name, VehicleController controller, VehicleCommAdapter adapter) {
      vehicleName = requireNonNull(name, "name");
      vehicleController = requireNonNull(controller, "controller");
      commAdapter = requireNonNull(adapter, "adapter");
    }
  }
}
