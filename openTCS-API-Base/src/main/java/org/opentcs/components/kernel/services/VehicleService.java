/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * Provides methods concerning {@link Vehicle}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface VehicleService
    extends TCSObjectService {

  /**
   * Attaches the described comm adapter to the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @param description The description for the comm adapter to be attached.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void attachCommAdapter(TCSObjectReference<Vehicle> ref,
                         VehicleCommAdapterDescription description)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Disables the comm adapter attached to the referenced vehicle.
   *
   * @param ref A reference to the vehicle the comm adapter is attached to.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void disableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Enables the comm adapter attached to the referenced vehicle.
   *
   * @param ref A reference to the vehicle the comm adapter is attached to.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void enableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Returns attachment information for the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @return The attachment information.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  AttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Returns the process model for the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @return The process model.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  VehicleProcessModelTO fetchProcessModel(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Sends an {@link AdapterCommand} to the comm adapter attached to the referenced vehicle.
   *
   * @see VehicleCommAdapter#execute(AdapterCommand)
   * @param ref A reference to the vehicle.
   * @param command The adapter command to send.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void sendCommAdapterCommand(TCSObjectReference<Vehicle> ref, AdapterCommand command)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Sends a message to the communication adapter associated with the referenced vehicle.
   * This method provides a generic one-way communication channel to the communication adapter of a
   * vehicle. Note that there is no return value and no guarantee that the communication adapter
   * will understand the message; clients cannot even know which communication adapter is attached
   * to a vehicle, so it's entirely possible that the communication adapter receiving the message
   * does not understand it.
   *
   * @see VehicleCommAdapter#processMessage(java.lang.Object)
   * @param ref The vehicle whose communication adapter shall receive the message.
   * @param message The message to be delivered.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException If the calling client is not allowed to execute this method.
   */
  void sendCommAdapterMessage(TCSObjectReference<Vehicle> ref, Object message)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Updates the vehicle's integration level.
   *
   * @param ref A reference to the vehicle.
   * @param integrationLevel The vehicle's new integration level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @throws IllegalArgumentException If changing the vehicle's integration level to 
   * {@code integrationLevel} is not allowed from its current integration level.
   */
  void updateVehicleIntegrationLevel(TCSObjectReference<Vehicle> ref,
                                     Vehicle.IntegrationLevel integrationLevel)
      throws ObjectUnknownException, KernelRuntimeException, IllegalArgumentException;

  /**
   * Updates the categories of transport orders a vehicle can process.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param processableCategories A set of transport order categories.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                          Set<String> processableCategories)
      throws ObjectUnknownException, KernelRuntimeException;
}
