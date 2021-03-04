/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import java.util.List;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Declares the methods the vehicle service must provide which are not accessible to remote peers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface InternalVehicleService
    extends VehicleService {

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
  void updateVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                        List<LoadHandlingDevice> devices)
      throws ObjectUnknownException;

  /**
   * Updates the point which a vehicle is expected to occupy next.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point which the vehicle is expected to occupy next.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException;

  /**
   * Updates a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param sequenceRef A reference to the order sequence the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                  TCSObjectReference<OrderSequence> sequenceRef)
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's current orientation angle (-360..360 degrees, or {@link Double#NaN}, if
   * the vehicle doesn't provide an angle).
   *
   * @param ref A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleOrientationAngle(TCSObjectReference<Vehicle> ref, double angle)
      throws ObjectUnknownException;

  /**
   * Places a vehicle on a point.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point on which the vehicle is to be placed.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                             TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException;

  /**
   * Updates the vehicle's current precise position in mm.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param position The vehicle's precise position in mm.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehiclePrecisePosition(TCSObjectReference<Vehicle> ref, Triple position)
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
   * Updates a vehicle's index of the last route step travelled for the current drive order of its
   * current transport order.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param index The new index.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleRouteProgressIndex(TCSObjectReference<Vehicle> ref, int index)
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
   * Updates a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void updateVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                   TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException;
}
