/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.util.List;
import java.util.Set;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterRegistry;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.drivers.VehicleManagerPool;

/**
 * Declares the methods the openTCS kernel must provide which are not accessible
 * to remote peers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocalKernel
    extends Kernel {

  /**
   * Initializes this local kernel.
   */
  void initialize();

  /**
   * Waits for the local kernels shutdown sequence to finish.
   */
  void waitForTermination();

  /**
   * Returns the name of the configured default model, or the empty string, if
   * no default model is configured.
   *
   * @return The name of the configured default model, or the empty string, if
   * no default model is configured.
   */
  String getDefaultModelName();

  /**
   * Returns the kernel's vehicle manager pool.
   *
   * @return The kernel's vehicle manager pool.
   */
  VehicleManagerPool getVehicleManagerPool();
  
  /**
   * Returns the kernel's vehicle controller pool.
   *
   * @return The kernel's vehicle controller pool.
   */
  VehicleControllerPool getVehicleControllerPool();

  /**
   * Returns the kernel's communication adapter registry.
   *
   * @return The kernel's communication adapter registry.
   */
  CommunicationAdapterRegistry getCommAdapterRegistry();

  /**
   * Returns the kernel's scheduler.
   *
   * @return The kernel's scheduler.
   */
  Scheduler getScheduler();

  /**
   * Sets a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref, int energyLevel)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's recharge operation.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param rechargeOperation The vehicle's new recharge action.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                   String rechargeOperation)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's load handling devices.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param devices The vehicle's new load handling devices.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                     List<LoadHandlingDevice> devices)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's maximum velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref, int velocity)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's maximum velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleState(TCSObjectReference<Vehicle> ref, Vehicle.State newState)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's processing state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new processing state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                           Vehicle.ProcState newState)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's communication adapter's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's communication adapter's new state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                              CommunicationAdapter.State newState)
      throws ObjectUnknownException;

  /**
   * Places a vehicle on a point.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point on which the vehicle is to be
   * placed.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException;

  /**
   * Sets the point which a vehicle is expected to occupy next.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point which the vehicle is expected to
   * occupy next.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                              TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException;

  /**
   * Sets the vehicle's current precise position in mm.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param newPosition The vehicle's precise position in mm.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 Triple newPosition)
      throws ObjectUnknownException;

  /**
   * Sets the vehicle's current orientation angle (-360..360°, or Double.NaN, if
   * the vehicle doesn't provide an angle).
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                  double angle)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param seqRef A reference to the order sequence the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                               TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's index of the last route step travelled for the current
   * drive order of its current transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param index The new index.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  void setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                    int index)
      throws ObjectUnknownException;

  /**
   * Adds a rejection to a transport order.
   *
   * @param ref A reference to the transport order to be modified.
   * @param newRejection The rejection to be added.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   */
  void addTransportOrderRejection(TCSObjectReference<TransportOrder> ref,
                                  Rejection newRejection)
      throws ObjectUnknownException;

  /**
   * Sets a transport order's state.
   *
   * @param ref A reference to the transport order to be modified.
   * @param newState The transport order's new state.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   */
  void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                              TransportOrder.State newState)
      throws ObjectUnknownException;

  /**
   * Sets a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   */
  void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException;

  /**
   * Sets a transport order's initial drive order.
   * Makes the first of the future drive orders the current one for the given
   * transport order. Fails if there already is a current drive order or if the
   * list of future drive orders is empty.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws IllegalStateException If there already is a current drive order or
   * if the list of future drive orders is empty.
   */
  void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException;

  /**
   * Updates a transport order's current drive order.
   * Marks the current drive order as finished, adds it to the list of past
   * drive orders and sets the current drive order to the next one of the list
   * of future drive orders (or <code>null</code>, if that list is empty).
   * If the current drive order is <code>null</code> because all drive orders
   * have been finished already or none has been started, yet, nothing happens.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  void setTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException;

  /**
   * Sets the order sequence a transport order belongs to.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param seqRef A reference to the sequence the transport order belongs to.
   * @throws ObjectUnknownException If any of the referenced objects do not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a transport order's <em>dispensable</em> flag.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param dispensable The new dispensable flag.
   * @throws ObjectUnknownException If any of the referenced objects do not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setTransportOrderDispensable(TCSObjectReference<TransportOrder> orderRef,
                                    boolean dispensable)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's finished index.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param index The sequence's new finished index.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> seqRef,
                                     int index)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's finished flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceFinished(TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's processing vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle processing the order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setOrderSequenceProcessingVehicle(TCSObjectReference<OrderSequence> seqRef,
                                         TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Expands a set of resources <em>A</em> to a set of resources <em>B</em>.
   * <em>B</em> contains the resources in <em>A</em> with blocks expanded to
   * their actual members.
   * The given set is not modified.
   *
   * @param resources The set of resources to be expanded.
   * @return The given set with resources expanded.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  Set<TCSResource> expandResources(Set<TCSResourceReference> resources)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a <code>KernelExtension</code> to this kernel.
   *
   * @param newExtension The extension to be added.
   */
  void addKernelExtension(KernelExtension newExtension);

  /**
   * Removes a <code>KernelExtension</code> from this kernel.
   *
   * @param rmExtension The extension to be removed.
   */
  void removeKernelExtension(KernelExtension rmExtension);
}
