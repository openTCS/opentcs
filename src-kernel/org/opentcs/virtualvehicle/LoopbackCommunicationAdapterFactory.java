/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.BasicCommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterFactory;
import org.opentcs.drivers.CommunicationAdapterRegistry;

/**
 * A factory for loopback communication adapters (virtual vehicles).
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class LoopbackCommunicationAdapterFactory
    implements CommunicationAdapterFactory {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(LoopbackCommunicationAdapterFactory.class.getName());
  /**
   * The kernel.
   */
  private LocalKernel kernel;

  /**
   * Creates a new factory.
   */
  public LoopbackCommunicationAdapterFactory() {
    // Do nada.
  }

  @Override
  @Deprecated
  public void setCommAdapterRegistry(CommunicationAdapterRegistry registry) {
    // Do nada.
  }

  @Override
  public void setKernel(LocalKernel kernel) {
    this.kernel = requireNonNull(kernel, "kernel");
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
  public BasicCommunicationAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    LoopbackCommunicationAdapter adapter
        = new LoopbackCommunicationAdapter(vehicle.getName());
    adapter.setModelPoints(kernel.getTCSObjects(Point.class));

    // Create EnergyStorage
    EnergyStorage energyStorage = EnergyStorage.createInstance();
    adapter.setEnergyStorage(energyStorage);

    // Get energy usage of moving vehicle
    String movementEnergyProp;
    movementEnergyProp = vehicle.getProperties().get(
        ObjectPropConstants.VIRTUAL_VEHICLE_MOVEMENT_ENERGY);
    double movementEnergy;
    if (movementEnergyProp != null) {
      try {
        movementEnergy = Double.parseDouble(movementEnergyProp);
        adapter.setMovementPower(movementEnergy);
      }
      catch (NumberFormatException e) {
        log.fine("Invalid idle specified for movement energy usage of vehicle "
            + vehicle.getName() + ". Using default instead.");
      }
    }

    // Get energy usage of operating vehicle
    String operationEnergyProp;
    operationEnergyProp = vehicle.getProperties().get(
        ObjectPropConstants.VIRTUAL_VEHICLE_OPERATION_ENERGY);
    double operationEnergy;
    if (operationEnergyProp != null) {
      try {
        operationEnergy = Double.parseDouble(operationEnergyProp);
        adapter.setOperationPower(operationEnergy);
      }
      catch (NumberFormatException e) {
        log.fine("Invalid value specified for operation energy usage of vehicle "
            + vehicle.getName() + ". Using default instead.");
      }
    }

    // Get energy usage of idle vehicle 
    String idleEnergyProp;
    idleEnergyProp = vehicle.getProperties().get(
        ObjectPropConstants.VIRTUAL_VEHICLE_IDLE_ENERGY);
    double idleEnergy;
    if (idleEnergyProp != null) {
      try {
        idleEnergy = Double.parseDouble(idleEnergyProp);
        adapter.setIdlePower(idleEnergy);
      }
      catch (NumberFormatException e) {
        log.fine("Invalid value specified for idle energy usage of vehicle "
            + vehicle.getName() + ". Using default instead.");
      }
    }
    return adapter;
  }
}
