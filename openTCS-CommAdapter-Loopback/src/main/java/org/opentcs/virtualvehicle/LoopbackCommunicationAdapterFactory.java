/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for loopback communication adapters (virtual vehicles).
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommunicationAdapterFactory
    implements VehicleCommAdapterFactory {

  /**
   * A property key for {@link Vehicle} instances used to provide the amount of energy (in W) the
   * vehicle consumes during movement to the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  public static final String PROP_MOVEMENT_ENERGY = "tcs:virtualVehicleMovementEnergy";
  /**
   * A property key for {@link Vehicle} instances used to provide the amount of energy (in W) the
   * vehicle consumes during operations to the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  public static final String PROP_OPERATION_ENERGY = "tcs:virtualVehicleOperationEnergy";
  /**
   * A property key for {@link Vehicle} instances used to provide the amount of energy (in W) the
   * vehicle consumes when idle to the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  public static final String PROP_IDLE_ENERGY = "tcs:virtualVehicleIdleEnergy";
  /**
   * This class's Logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(LoopbackCommunicationAdapterFactory.class);
  /**
   * The adapter components factory.
   */
  private final LoopbackAdapterComponentsFactory adapterFactory;
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new factory.
   *
   * @param componentsFactory The adapter components factory.
   */
  @Inject
  public LoopbackCommunicationAdapterFactory(LoopbackAdapterComponentsFactory componentsFactory) {
    this.adapterFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public void initialize() {
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    initialized = false;
  }

  @Override
  public String getAdapterDescription() {
    return ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle")
        .getString("AdapterFactoryDescription");
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return true;
  }

  @Override
  public LoopbackCommunicationAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    LoopbackCommunicationAdapter adapter = adapterFactory.createLoopbackCommAdapter(vehicle);

    adapter.setEnergyStorage(EnergyStorage.createInstance());

    // Get energy usage of moving vehicle
    String movementEnergyProp = vehicle.getProperty(PROP_MOVEMENT_ENERGY);
    if (movementEnergyProp != null) {
      try {
        adapter.getProcessModel().setMovementPower(Double.parseDouble(movementEnergyProp));
      }
      catch (NumberFormatException e) {
        LOG.debug("Invalid movement energy usage value for vehicle {}. Using default.",
                  vehicle.getName(),
                  e);
      }
    }

    // Get energy usage of operating vehicle
    String operationEnergyProp = vehicle.getProperties().get(PROP_OPERATION_ENERGY);
    if (operationEnergyProp != null) {
      try {
        adapter.getProcessModel().setOperationPower(Double.parseDouble(operationEnergyProp));
      }
      catch (NumberFormatException e) {
        LOG.debug("Invalid operation energy usage value for vehicle {}. Using default.",
                  vehicle.getName(),
                  e);
      }
    }

    // Get energy usage of idle vehicle 
    String idleEnergyProp = vehicle.getProperties().get(PROP_IDLE_ENERGY);
    if (idleEnergyProp != null) {
      try {
        adapter.getProcessModel().setIdlePower(Double.parseDouble(idleEnergyProp));
      }
      catch (NumberFormatException e) {
        LOG.debug("Invalid idle energy usage value for vehicle {}. Using default.",
                  vehicle.getName(),
                  e);
      }
    }
    return adapter;
  }
}
