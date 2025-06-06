// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning {@link Vehicle}s.
 */
public interface VehicleService
    extends
      TCSObjectService {

  /**
   * Attaches the described comm adapter to the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @param description The description for the comm adapter to be attached.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void attachCommAdapter(
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterDescription description
  )
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Disables the comm adapter attached to the referenced vehicle.
   *
   * @param ref A reference to the vehicle the comm adapter is attached to.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void disableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Enables the comm adapter attached to the referenced vehicle.
   *
   * @param ref A reference to the vehicle the comm adapter is attached to.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void enableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Returns attachment information for the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @return The attachment information.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  VehicleAttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Returns the process model for the referenced vehicle.
   *
   * @param ref A reference to the vehicle.
   * @return The process model.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  VehicleProcessModelTO fetchProcessModel(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Sends an {@link AdapterCommand} to the comm adapter attached to the referenced vehicle.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the vehicle.
   * @param command The adapter command to send.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @see VehicleCommAdapter#execute(AdapterCommand)
   * @deprecated Use {@link #sendCommAdapterMessage(TCSObjectReference, VehicleCommAdapterMessage)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void sendCommAdapterCommand(TCSObjectReference<Vehicle> ref, AdapterCommand command)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Sends a message to the communication adapter associated with the referenced vehicle.
   * This method provides a generic one-way communication channel to the communication adapter of a
   * vehicle. Note that there is no return value and no guarantee that the communication adapter
   * will understand the message; clients cannot even know which communication adapter is attached
   * to a vehicle, so it's entirely possible that the communication adapter receiving the message
   * does not understand it.
   *
   * @param ref The vehicle whose communication adapter shall receive the message.
   * @param message The message to be delivered.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException If the calling client is not allowed to execute this method.
   * @see VehicleCommAdapter#processMessage(java.lang.Object)
   * @deprecated Use {@link #sendCommAdapterMessage(TCSObjectReference, VehicleCommAdapterMessage)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void sendCommAdapterMessage(TCSObjectReference<Vehicle> ref, Object message)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Sends a message to the communication adapter associated with the referenced vehicle.
   * <p>
   * This method provides a generic one-way communication channel to the communication adapter of a
   * vehicle. Note that there is no return value and no guarantee that the communication adapter
   * will understand the message.
   * </p>
   *
   * @param ref The vehicle whose communication adapter shall receive the message.
   * @param message The message to be delivered.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @see VehicleCommAdapter#processMessage(VehicleCommAdapterMessage)
   */
  default void sendCommAdapterMessage(
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterMessage message
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
  }

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
  void updateVehicleIntegrationLevel(
      TCSObjectReference<Vehicle> ref,
      Vehicle.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException,
        KernelRuntimeException,
        IllegalArgumentException;

  /**
   * Updates the vehicle's paused state.
   *
   * @param ref A reference to the vehicle.
   * @param paused The vehicle's new paused state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateVehiclePaused(
      TCSObjectReference<Vehicle> ref,
      boolean paused
  )
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Updates the vehicle's energy level threshold set.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevelThresholdSet The vehicle's new energy level threshold set.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  @ScheduledApiChange(when = "7.0", details = "Default implementation will be removed.")
  default void updateVehicleEnergyLevelThresholdSet(
      TCSObjectReference<Vehicle> ref,
      EnergyLevelThresholdSet energyLevelThresholdSet
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  /**
   * Updates the types of transport orders a vehicle is allowed to process.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param allowedOrderTypes A set of transport order types.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #updateVehicleAcceptableOrderTypes(TCSObjectReference, Set)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void updateVehicleAllowedOrderTypes(
      TCSObjectReference<Vehicle> ref,
      Set<String> allowedOrderTypes
  )
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Updates the types of transport orders a vehicle is allowed to process.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param acceptableOrderTypes A set of transport order types and their priorities.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  default void updateVehicleAcceptableOrderTypes(
      TCSObjectReference<Vehicle> ref,
      Set<AcceptableOrderType> acceptableOrderTypes
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  /**
   * Updates the vehicle's envelope key.
   *
   * @param ref A reference to the vehicle.
   * @param envelopeKey The vehicle's new envelope key.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws IllegalArgumentException If the referenced vehicle is processing a transport order or
   * is currently claiming/allocating resources.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateVehicleEnvelopeKey(TCSObjectReference<Vehicle> ref, String envelopeKey)
      throws ObjectUnknownException,
        IllegalArgumentException,
        KernelRuntimeException;
}
