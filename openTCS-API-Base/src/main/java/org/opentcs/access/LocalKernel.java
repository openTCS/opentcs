/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods the openTCS kernel must provide which are not accessible
 * to remote peers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocalKernel
    extends Kernel,
            Lifecycle {

  /**
   * Loads the saved model into the kernel.
   * If there is no saved model, a new empty model will be loaded.
   *
   * @throws IllegalStateException If the model cannot be loaded.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link InternalPlantModelService#loadPlantModel()} instead.
   */
  @Deprecated
  void loadPlantModel()
      throws CredentialsException, IllegalStateException;

  /**
   * Saves the current model under the given name.
   * If there is a saved model, it will be overwritten.
   *
   * @throws IllegalStateException If the model could not be persisted for some reason.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link InternalPlantModelService#savePlantModel()} instead.
   */
  @Deprecated
  void savePlantModel()
      throws CredentialsException, IllegalStateException;

  /**
   * Returns a single TCSObject of the given class.
   * <p>
   * <em>Note:
   * This method returns the original object(s) as existing within the kernel,
   * not a copy. When working with the result, be aware its contents may change
   * over time as the kernel modifies it, and that modifying its contents
   * directly instead of using the appropriate kernel methods may lead to
   * unpredictable results. This method should only be used when the performance
   * impact of copying objects is inacceptable and the above implications are
   * handled appropriately.
   * </em>
   * </p>
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return The referenced object, or <code>null</code> if no such object
   * exists or if an object exists but is not an instance of the given class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObject(
   * java.lang.Class, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> T getTCSObjectOriginal(Class<T> clazz,
                                                  TCSObjectReference<T> ref)
      throws CredentialsException;

  /**
   * Returns a single TCSObject of the given class.
   * <p>
   * <em>Note:
   * This method returns the original object(s) as existing within the kernel,
   * not a copy. When working with the result, be aware its contents may change
   * over time as the kernel modifies it, and that modifying its contents
   * directly instead of using the appropriate kernel methods may lead to
   * unpredictable results. This method should only be used when the performance
   * impact of copying objects is inacceptable and the above implications are
   * handled appropriately.
   * </em>
   * </p>
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return The named object, or <code>null</code> if no such object exists or
   * if an object exists but is not an instance of the given class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObject(
   * java.lang.Class, java.lang.String)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> T getTCSObjectOriginal(Class<T> clazz,
                                                  String name)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class.
   * <p>
   * <em>Note:
   * This method returns the original object(s) as existing within the kernel,
   * not a copy. When working with the result, be aware its contents may change
   * over time as the kernel modifies it, and that modifying its contents
   * directly instead of using the appropriate kernel methods may lead to
   * unpredictable results. This method should only be used when the performance
   * impact of copying objects is inacceptable and the above implications are
   * handled appropriately.
   * </em>
   * </p>
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @return All existing objects of the given class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObjects(java.lang.Class)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(Class<T> clazz)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class whose names match the
   * given pattern.
   * <p>
   * <em>Note:
   * This method returns the original object(s) as existing within the kernel,
   * not a copy. When working with the result, be aware its contents may change
   * over time as the kernel modifies it, and that modifying its contents
   * directly instead of using the appropriate kernel methods may lead to
   * unpredictable results. This method should only be used when the performance
   * impact of copying objects is inacceptable and the above implications are
   * handled appropriately.
   * </em>
   * </p>
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @param regexp A regular expression describing the names of the objects to
   * be returned; if <code>null</code>, all objects of the given class are
   * returned.
   * @return All existing objects of the given class whose names match the given
   * pattern. If no such objects exist, the returned set will be empty.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObjects(
   * java.lang.Class, java.util.function.Predicate)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(@Nullable Class<T> clazz,
                                                        Pattern regexp)
      throws CredentialsException;

  /**
   * Sets a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleEnergyLevel(
   * org.opentcs.data.TCSObjectReference, int)} instead.
   */
  @Deprecated
  void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref, int energyLevel)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's recharge operation.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param rechargeOperation The vehicle's new recharge action.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleRechargeOperation(
   * org.opentcs.data.TCSObjectReference, java.lang.String)} instead.
   */
  @Deprecated
  void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                   String rechargeOperation)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's load handling devices.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param devices The vehicle's new load handling devices.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleLoadHandlingDevices(
   * org.opentcs.data.TCSObjectReference, java.util.List)} instead.
   */
  @Deprecated
  void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                     List<LoadHandlingDevice> devices)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's maximum velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref, int velocity)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's maximum velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleState(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.model.Vehicle.State)} instead.
   */
  @Deprecated
  void setVehicleState(TCSObjectReference<Vehicle> ref, Vehicle.State newState)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's processing state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new processing state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleProcState(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.model.Vehicle.ProcState)} instead.
   */
  @Deprecated
  void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                           Vehicle.ProcState newState)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's communication adapter's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's communication adapter's new state.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated VehicleCommAdapter.State is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void setVehicleAdapterState(TCSObjectReference<Vehicle> ref, VehicleCommAdapter.State newState)
      throws ObjectUnknownException;

  /**
   * Places a vehicle on a point.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param pointRef A reference to the point on which the vehicle is to be
   * placed.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehiclePosition(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
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
   * @deprecated Use{@link InternalVehicleService#updateVehicleNextPosition(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                              TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException;

  /**
   * Sets the vehicle's current precise position in mm.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param newPosition The vehicle's precise position in mm.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehiclePrecisePosition(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.model.Triple)} instead.
   */
  @Deprecated
  void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 Triple newPosition)
      throws ObjectUnknownException;

  /**
   * Sets the vehicle's current orientation angle (-360..360 degrees, or
   * Double.NaN, if the vehicle doesn't provide an angle).
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleOrientationAngle(
   * org.opentcs.data.TCSObjectReference, double)} instead.
   */
  @Deprecated
  void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                  double angle)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleTransportOrder(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference) } instead.
   */
  @Deprecated
  void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException;

  /**
   * Sets a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param seqRef A reference to the order sequence the vehicle processes.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use{@link InternalVehicleService#updateVehicleOrderSequence(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
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
   * @deprecated Use{@link InternalVehicleService#updateVehicleRouteProgressIndex(
   * org.opentcs.data.TCSObjectReference, int)} instead.
   */
  @Deprecated
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
   * @deprecated Use {@link InternalTransportOrderService#registerTransportOrderRejection(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.order.Rejection)} instead.
   */
  @Deprecated
  void addTransportOrderRejection(TCSObjectReference<TransportOrder> ref,
                                  Rejection newRejection)
      throws ObjectUnknownException;

  /**
   * Sets a transport order's state.
   * Note that transport order states are intended to be manipulated by the
   * dispatcher only. Calling this method from any other parts of the kernel may
   * result in undefined behaviour.
   *
   * @param ref A reference to the transport order to be modified.
   * @param newState The transport order's new state.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @deprecated Use {@link InternalTransportOrderService#updateTransportOrderState(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.order.TransportOrder.State)} instead.
   */
  @Deprecated
  void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                              TransportOrder.State newState)
      throws ObjectUnknownException;

  /**
   * Sets a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @param driveOrders The drive orders containing the data to be copied into this transport
   * order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   * @deprecated Use {@link InternalTransportOrderService#updateTransportOrderProcessingVehicle(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  void setTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                          TCSObjectReference<Vehicle> vehicleRef,
                                          List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException;

  /**
   * Sets a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @deprecated Use {@link #setTransportOrderProcessingVehicle(org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException;

  /**
   * Sets drive order data from for the given transport order.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   * @deprecated Use {@link #setTransportOrderProcessingVehicle(org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                    List<DriveOrder> newOrders)
      throws ObjectUnknownException, IllegalArgumentException;

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
   * @deprecated Use {@link #setTransportOrderProcessingVehicle(org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
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
   * @deprecated Use {@link InternalTransportOrderService#updateTransportOrderNextDriveOrder(
   * org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
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
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
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
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
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
   * @deprecated Use {@link InternalTransportOrderService#updateOrderSequenceFinishedIndex(
   * org.opentcs.data.TCSObjectReference, int)} instead.
   */
  @Deprecated
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
   * @deprecated Use {@link InternalTransportOrderService#markOrderSequenceFinished(
   * org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
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
   * @deprecated Use {@link InternalTransportOrderService#updateOrderSequenceProcessingVehicle(
   * org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
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
   * @deprecated Use {@link InternalPlantModelService#expandResources(java.util.Set)} instead.
   */
  @Deprecated
  Set<TCSResource<?>> expandResources(Set<TCSResourceReference<?>> resources)
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

  /**
   * Returns all configuration items existing in the kernel.
   *
   * @return All configuration items existing in the kernel.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Configuration management is out of scope for the kernel.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  Set<ConfigurationItemTO> getConfigurationItems()
      throws CredentialsException;

  /**
   * Sets a single configuration item in the kernel.
   *
   * @param itemTO The configuration item to be set.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Configuration management is out of scope for the kernel.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void setConfigurationItem(ConfigurationItemTO itemTO)
      throws CredentialsException;

}
