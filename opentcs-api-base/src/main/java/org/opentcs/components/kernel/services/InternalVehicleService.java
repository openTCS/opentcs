// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.util.annotations.ScheduledApiChange;

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
   * Updates the point which a vehicle is expected to occupy next.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point which the vehicle is expected to occupy next.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Will be removed without replacement.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void updateVehicleNextPosition(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> pointRef
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
   * Updates the vehicle's current orientation angle (-360..360 degrees, or {@link Double#NaN}, if
   * the vehicle doesn't provide an angle).
   *
   * @param ref A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #updateVehiclePose(TCSObjectReference,Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void updateVehicleOrientationAngle(TCSObjectReference<Vehicle> ref, double angle)
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
   * Updates the vehicle's current precise position in mm.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param position The vehicle's precise position in mm.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #updateVehiclePose(TCSObjectReference,Pose)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void updateVehiclePrecisePosition(TCSObjectReference<Vehicle> ref, Triple position)
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's pose.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param pose The vehicle's new pose.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @ScheduledApiChange(when = "7.0", details = "Default implementation will be removed.")
  default void updateVehiclePose(TCSObjectReference<Vehicle> ref, Pose pose)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(pose, "pose");

    updateVehiclePrecisePosition(ref, pose.getPosition());
    updateVehicleOrientationAngle(ref, pose.getOrientationAngle());
  }

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
   * Updates a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param length The vehicle's new length.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #updateVehicleBoundingBox(TCSObjectReference, BoundingBox)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void updateVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's bounding box.
   *
   * @param ref A reference to the vehicle.
   * @param boundingBox The vehicle's new bounding box (in mm).
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  @ScheduledApiChange(when = "7.0", details = "Default implementation will be removed.")
  default void updateVehicleBoundingBox(TCSObjectReference<Vehicle> ref, BoundingBox boundingBox)
      throws ObjectUnknownException,
        KernelRuntimeException {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

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
