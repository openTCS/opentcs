// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import java.util.List;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Declares the methods the vehicle service must provide which are not accessible to remote peers.
 */
public interface InternalVehicleService
    extends
      VehicleService,
      InternalTCSObjectService {

  /**
   * Updates a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleEnergyLevel(TCSObjectReference<Vehicle> ref, int energyLevel)
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's load handling devices.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param devices The vehicle's new load handling devices.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleLoadHandlingDevices(
      TCSObjectReference<Vehicle> ref,
      List<LoadHandlingDevice> devices
  )
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param sequenceRef A reference to the order sequence the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleOrderSequence(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<OrderSequence> sequenceRef
  )
      throws ObjectUnknownException;

  /**
   * Places a vehicle on a point.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point on which the vehicle is to be placed.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehiclePosition(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> pointRef
  )
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's pose.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param pose The vehicle's new pose.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehiclePose(TCSObjectReference<Vehicle> ref, Pose pose)
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's processing state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param state The vehicle's new processing state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleProcState(TCSObjectReference<Vehicle> ref, Vehicle.ProcState state)
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's recharge operation.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param rechargeOperation The vehicle's new recharge action.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleRechargeOperation(TCSObjectReference<Vehicle> ref, String rechargeOperation)
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's claimed resources.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param resources The new resources.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleClaimedResources(
      TCSObjectReference<Vehicle> ref,
      List<Set<TCSResourceReference<?>>> resources
  )
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's allocated resources.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param resources The new resources.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleAllocatedResources(
      TCSObjectReference<Vehicle> ref,
      List<Set<TCSResourceReference<?>>> resources
  )
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param state The vehicle's new state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleState(TCSObjectReference<Vehicle> ref, Vehicle.State state)
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's bounding box.
   *
   * @param ref A reference to the vehicle.
   * @param boundingBox The vehicle's new bounding box (in mm).
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateVehicleBoundingBox(TCSObjectReference<Vehicle> ref, BoundingBox boundingBox)
      throws ObjectUnknownException,
        KernelRuntimeException;

  /**
   * Updates a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef
  )
      throws ObjectUnknownException;
}
