/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.SimCommunicationAdapter;
import org.opentcs.drivers.VehicleController;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.drivers.VehicleManager;
import org.opentcs.drivers.VehicleManagerPool;

/**
 * Maintains associations of <code>Vehicle</code>s,
 * <code>VehicleManager</code>s, <code>VehicleController</code>s and
 * <code>CommunicationAdapter</code>s.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class StandardVehicleManagerPool
    implements VehicleManagerPool, VehicleControllerPool {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(StandardVehicleManagerPool.class.getName());
  /**
   * The local kernel maintaining the Vehicle instances.
   */
  private final LocalKernel localKernel;
  /**
   * The currently existing/assigned managers, mapped by the names of the
   * corresponding vehicles.
   */
  private final Map<String, PoolEntry> managers = new HashMap<>();
  /**
   * The current time factor for simulation mode.
   */
  private double simulationTimeFactor = 1.0;

  /**
   * Creates a new StandardVehicleManagerPool.
   *
   * @param kernel The local kernel that maintains the
   * {@link org.opentcs.data.model.Vehicle Vehicle} instances.
   */
  @Inject
  public StandardVehicleManagerPool(LocalKernel kernel) {
    log.finer("method entry");
    localKernel = Objects.requireNonNull(kernel, "kernel is null");
  }

  // Methods declared in interface VehicleManagerPool start here.
  @Override
  public synchronized VehicleManager getVehicleManager(String vehicleName,
                                                       CommunicationAdapter commAdapter)
      throws IllegalArgumentException {
    log.finer("method entry");
    Objects.requireNonNull(vehicleName, "vehicleName is null");
    Objects.requireNonNull(commAdapter, "commAdapter is null");

    VehicleManager manager;
    PoolEntry poolEntry = managers.get(vehicleName);
    if (poolEntry == null) {
      log.fine("manager not in pool, creating new one");
      Vehicle vehicle = localKernel.getTCSObject(Vehicle.class, vehicleName);
      if (vehicle == null) {
        throw new IllegalArgumentException("No such vehicle: " + vehicleName);
      }
      StandardVehicleController controller =
          new StandardVehicleController(vehicle, commAdapter, localKernel);
      poolEntry = new PoolEntry(vehicleName, controller, controller, commAdapter);
      managers.put(vehicleName, poolEntry);
      controller.enable();
      manager = controller;
      // If the communication adapter is intended to simulate a vehicle, set
      // our current simulation time factor.
      if (commAdapter instanceof SimCommunicationAdapter) {
        ((SimCommunicationAdapter) commAdapter).setSimTimeFactor(simulationTimeFactor);
      }
    }
    else {
      log.fine("manager in pool, returning it");
      // XXX Actually, with the entry being removed from the pool on detach,
      // this should never happen. Maybe make it an error instead?
      manager = poolEntry.vehicleManager;
    }
    return manager;
  }

  @Override
  public synchronized void detachVehicleManager(String vehicleName)
      throws IllegalArgumentException {
    log.finer("method entry");
    Objects.requireNonNull(vehicleName, "vehicleName is null");
    log.info("Detaching vehicle manager for vehicle " + vehicleName);
    PoolEntry poolEntry = managers.remove(vehicleName);
    if (poolEntry == null) {
      throw new IllegalArgumentException(
          "A vehicle with the given name (" + vehicleName
          + ") is not associated with a manager");
    }
    // Clean up - mark vehicle state and adapter state as unknown.
    poolEntry.vehicleManager.setAdapterState(CommunicationAdapter.State.UNKNOWN);
    poolEntry.vehicleManager.setVehicleState(Vehicle.State.UNKNOWN);
    poolEntry.vehicleController.disable();
  }

  // Methods declared in interface VehicleControllerPool start here.
  @Override
  public VehicleController getVehicleController(String vehicleName) {
    log.finer("method entry");
    Objects.requireNonNull(vehicleName, "vehicleName is null");
    PoolEntry poolEntry = managers.get(vehicleName);
    return poolEntry == null ? null : poolEntry.vehicleController;
  }

  @Override
  public double getSimulationTimeFactor() {
    return simulationTimeFactor;
  }

  @Override
  public void setSimulationTimeFactor(double factor)
      throws IllegalArgumentException {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("Illegal factor value: " + factor);
    }
    simulationTimeFactor = factor;
    // Update time factor with all vehicle drivers intended for simulation.
    for (PoolEntry poolEntry : managers.values()) {
      if (poolEntry.commAdapter instanceof SimCommunicationAdapter) {
        ((SimCommunicationAdapter) poolEntry.commAdapter).setSimTimeFactor(factor);
      }
    }
  }

  @Override
  public void terminate() {
    log.finer("method entry");
    // Detach all vehicles and reset their positions and states.
    for (PoolEntry curEntry : managers.values()) {
      curEntry.vehicleManager.setAdapterState(
          CommunicationAdapter.State.UNKNOWN);
      curEntry.vehicleManager.setVehicleState(Vehicle.State.UNKNOWN);
      curEntry.vehicleManager.setVehiclePosition(null);
      curEntry.vehicleController.disable();
    }
  }

  // Inner classes start here.
  /**
   * An entry in this vehicle manager pool.
   */
  private static final class PoolEntry {

    /**
     * The name of the vehicle managed.
     */
    private final String vehicleName;
    /**
     * The vehicle manager associated with the vehicle.
     */
    private final VehicleManager vehicleManager;
    /**
     * The vehicle controller associated with the vehicle.
     */
    private final VehicleController vehicleController;
    /**
     * The communication adapter associated with the vehicle.
     */
    private final CommunicationAdapter commAdapter;

    /**
     * Creates a new pool entry.
     *
     * @param name The name of the vehicle managed.
     * @param manager The vehicle manager associated with the vehicle.
     * @param controller The VehicleController
     * @param adapter The communication adapter associated with the vehicle.
     */
    private PoolEntry(String name,
                      VehicleManager manager,
                      VehicleController controller,
                      CommunicationAdapter adapter) {
      vehicleName = Objects.requireNonNull(name, "name is null");
      vehicleManager = Objects.requireNonNull(manager, "manager is null");
      vehicleController = Objects.requireNonNull(controller,
                                                 "controller is null");
      commAdapter = Objects.requireNonNull(adapter, "adapter is null");
    }
  }
}
