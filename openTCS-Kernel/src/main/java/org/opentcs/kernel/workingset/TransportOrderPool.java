/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code TransportOrderPool} keeps all {@code TransportOrder}s for an openTCS
 * kernel and provides methods to create and manipulate them.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of
 * instances of this class must be synchronized externally.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderPool {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderPool.class);
  /**
   * The system's global object pool.
   */
  private final TCSObjectPool objectPool;

  /**
   * Creates a new TransportOrderPool.
   *
   * @param globalPool The object pool serving as the container for this order
   * pool's data.
   */
  @Inject
  public TransportOrderPool(TCSObjectPool globalPool) {
    objectPool = Objects.requireNonNull(globalPool);
  }

  /**
   * Returns the <code>TCSObjectPool</code> serving as the container for this
   * order pool's data.
   *
   * @return The <code>TCSObjectPool</code> serving as the container for this
   * order pool's data.
   */
  public TCSObjectPool getObjectPool() {
    LOG.debug("method entry");
    return objectPool;
  }

  /**
   * Removes all transport orders from this pool.
   */
  public void clear() {
    LOG.debug("method entry");
    Set<TCSObject<?>> objects = objectPool.getObjects((Pattern) null);
    Set<String> removableNames = new HashSet<>();
    for (TCSObject<?> curObject : objects) {
      if (curObject instanceof TransportOrder
          || curObject instanceof OrderSequence) {
        removableNames.add(curObject.getName());
      }
    }
    objectPool.removeObjects(removableNames);
  }

  /**
   * Adds a new, pristine transport order to the pool.
   *
   * @param destinations A list of destinations that are to be travelled to
   * when processing this transport order.
   * @return The newly created transport order.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder createTransportOrder(List<DriveOrder.Destination> destinations) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int orderID = objectPool.getUniqueObjectId();
    String orderName = objectPool.getUniqueObjectName("TOrder-", "0000");
    TransportOrder newOrder
        = new TransportOrder(orderID,
                             orderName,
                             destinations,
                             System.currentTimeMillis());
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newOrder);
    }
    catch (ObjectExistsException exc) {
      throw new IllegalStateException(
          "Allegedly unique object name already exists: " + orderName, exc);
    }
    objectPool.emitObjectEvent(newOrder.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created transport order.
    return newOrder;
  }

  /**
   * Adds a new transport order to the pool.
   * This method implicitly adds the transport order to its wrapping sequence, if any.
   *
   * @param to The transfer object from which to create the new transport order.
   * @return The newly created transport order.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   * @throws IllegalArgumentException If the order is supposed to be part of an order sequence, but
   * the sequence is already complete, the categories of the two differ or the intended vehicles of
   * the two differ.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, IllegalArgumentException {
    TransportOrder newOrder = new TransportOrder(to.getName(), toDriveOrders(to.getDestinations()))
        .withCreationTime(Instant.now())
        .withIntendedVehicle(toVehicleReference(to.getIntendedVehicleName()))
        .withCategory(to.getCategory())
        .withDeadline(to.getDeadline().toInstant())
        .withDispensable(to.isDispensable())
        .withWrappingSequence(getWrappingSequence(to))
        .withDependencies(getDependencies(to))
        .withProperties(to.getProperties());
    objectPool.addObject(newOrder);
    objectPool.emitObjectEvent(newOrder.clone(), null, TCSObjectEvent.Type.OBJECT_CREATED);

    if (newOrder.getWrappingSequence() != null) {
      OrderSequence sequence = objectPool.getObject(OrderSequence.class,
                                                    newOrder.getWrappingSequence());
      OrderSequence prevSeq = sequence.clone();
      sequence = objectPool.replaceObject(sequence.withOrder(newOrder.getReference()));
      objectPool.emitObjectEvent(sequence.clone(), prevSeq, TCSObjectEvent.Type.OBJECT_MODIFIED);
    }

    // Return the newly created transport order.
    return newOrder;
  }

  /**
   * Returns the referenced transport order.
   *
   * @param ref A reference to the transport order to return.
   * @return The referenced transport order, or <code>null</code>, if no such
   * order exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder getTransportOrder(TCSObjectReference<TransportOrder> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(TransportOrder.class, ref);
  }

  /**
   * Returns the transport order with the given name.
   *
   * @param orderName The name of the TransportOrder to return.
   * @return The transport order with the given name, or <code>null</code>, if
   * no such order exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder getTransportOrder(String orderName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(TransportOrder.class, orderName);
  }

  /**
   * Returns a set of transport orders whose names match the given regular
   * expression.
   *
   * @param regexp The regular expression describing the names of the
   * transport orders to return. If <code>null</code>, all transport orders are
   * returned.
   * @return A set of transport orders whose names match the given regular
   * expression. If no such transport orders exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<TransportOrder> getTransportOrders(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(TransportOrder.class, regexp);
  }

  /**
   * Returns a set of transport orders current in the given state.
   *
   * @param state The state of the transport orders to be returned.
   * @return A set of transport orders current in the given state. If no such
   * transport orders exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<TransportOrder> getTransportOrders(TransportOrder.State state) {
    LOG.debug("method entry");
    if (state == null) {
      throw new NullPointerException("state is null");
    }
    Set<TransportOrder> result = new HashSet<>();
    for (TransportOrder order : objectPool.getObjects(TransportOrder.class)) {
      if (order.hasState(state)) {
        result.add(order);
      }
    }
    return result;
  }

  /**
   * Returns a set of transport orders for which the given predicate evaluates to <code>true</code>.
   *
   * @param predicate The predicate.
   * @return A set of transport orders for which the given predicate is true.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<TransportOrder> getTransportOrders(Predicate<? super TransportOrder> predicate) {
    return objectPool.getObjects(TransportOrder.class, predicate);
  }

  /**
   * Sets a transport order's deadline.
   *
   * @param ref A reference to the transport order to be modified.
   * @param deadline The transport order's new deadline.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                                  long deadline)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, ref);
    if (order == null) {
      throw new ObjectUnknownException(ref);
    }
    TransportOrder previousState = order.clone();
    order.setDeadline(deadline);
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's state.
   *
   * @param ref A reference to the transport order to be modified.
   * @param newState The transport order's new state.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                               TransportOrder.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    TransportOrder previousState = order.clone();
    order = objectPool.replaceObject(order.withState(newState));
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's intended vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    if (vehicleRef == null) {
      order.setIntendedVehicle(null);
    }
    else {
      Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, vehicleRef);
      if (vehicle == null) {
        throw new ObjectUnknownException(vehicleRef);
      }
      order.setIntendedVehicle(vehicle.getReference());
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @param driveOrders The drive orders containing the data to be copied into this transport
   * order's drive orders.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef,
      List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order.clone();
    if (vehicleRef == null) {
      order = objectPool.replaceObject(order.withProcessingVehicle(null));
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      order = objectPool.replaceObject(
          order.withProcessingVehicle(vehicle.getReference())
              .withDriveOrders(driveOrders)
              .withCurrentDriveOrderIndex(0)
      );
      if (order.getCurrentDriveOrder() != null) {
        order = objectPool.replaceObject(
            order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING));
      }
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's processing vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle processing the order.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use {@link #setTransportOrderProcessingVehicle(org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order.clone();
    if (vehicleRef == null) {
      order = objectPool.replaceObject(order.withProcessingVehicle(null));
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      order = objectPool.replaceObject(order.withProcessingVehicle(vehicle.getReference()));
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Copies drive order data from a list of drive orders to the given transport
   * order's future drive orders.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                                     List<DriveOrder> newOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order.clone();
    order = objectPool.replaceObject(order.withDriveOrders(newOrders));
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's initial drive order.
   * Makes the first of the future drive orders the current one for the given
   * transport order. Fails if there already is a current drive order or if the
   * list of future drive orders is empty.
   *
   * @param ref A reference to the transport order to be modified.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws IllegalStateException If there already is a current drive order or
   * if the list of future drive orders is empty.
   * @deprecated Use {@link #setTransportOrderProcessingVehicle(org.opentcs.data.TCSObjectReference, org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder setTransportOrderInitialDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    checkState(order.getCurrentDriveOrderIndex() < 0, "currentDriveOrder already set");
    checkState(!order.getAllDriveOrders().isEmpty(), "driveOrders is empty");

    TransportOrder previousState = order.clone();
    order = objectPool.replaceObject(order.withCurrentDriveOrderIndex(0));
    if (order.getCurrentDriveOrder() != null) {
      order = objectPool.replaceObject(
          order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING));
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Updates a transport order's current drive order.
   * Marks the current drive order as finished, adds it to the list of past
   * drive orders and sets the current drive order to the next one of the list
   * of future drive orders (or <code>null</code>, if that list is empty).
   * If the current drive order is <code>null</code> because all drive orders
   * have been finished already or none has been started, yet, nothing happens.
   *
   * @param ref A reference to the transport order to be modified.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder setTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    TransportOrder previousState = order.clone();
    // First, mark the current drive order as FINISHED and send an event.
    // Then, shift drive orders and send a second event.
    // Then, mark the current drive order as TRAVELLING and send another event.
    if (order.getCurrentDriveOrder() != null) {
      order = objectPool.replaceObject(order.withCurrentDriveOrderState(DriveOrder.State.FINISHED));
      TransportOrder newState = order.clone();
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      order = objectPool.replaceObject(
          order.withCurrentDriveOrderIndex(order.getCurrentDriveOrderIndex() + 1));
      newState = order.clone();
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      if (order.getCurrentDriveOrder() != null) {
        order = objectPool.replaceObject(
            order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING));
        newState = order.clone();
        objectPool.emitObjectEvent(newState,
                                   previousState,
                                   TCSObjectEvent.Type.OBJECT_MODIFIED);
        previousState = newState;
      }
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Adds a dependency to a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be added
   * to.
   * @param newDepRef A reference to the order that is the new dependency.
   * @return The modified transport order.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder addTransportOrderDependency(TCSObjectReference<TransportOrder> orderRef,
                                                    TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    TransportOrder newDep = objectPool.getObjectOrNull(TransportOrder.class,
                                                       newDepRef);
    if (newDep == null) {
      throw new ObjectUnknownException(newDepRef);
    }
    order.addDependency(newDep.getReference());
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Removes a dependency from a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be
   * removed from.
   * @param rmDepRef A reference to the order that is not to be depended on any
   * more.
   * @return The modified transport order.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder removeTransportOrderDependency(TCSObjectReference<TransportOrder> orderRef,
                                                       TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    TransportOrder rmDep = objectPool.getObjectOrNull(TransportOrder.class, rmDepRef);
    if (rmDep == null) {
      throw new ObjectUnknownException(rmDepRef);
    }
    order.removeDependency(rmDep.getReference());
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Adds a rejection to a transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be added
   * to.
   * @param newRejection The rejection to be added to the transport order.
   * @return The modified transport order.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder addTransportOrderRejection(TCSObjectReference<TransportOrder> orderRef,
                                                   Rejection newRejection)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order.clone();
    order = objectPool.replaceObject(order.withRejection(newRejection));
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets the order sequence a transport order belongs to.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param seqRef A reference to the sequence the order belongs to.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    if (seqRef == null) {
      order.setWrappingSequence(null);
    }
    else {
      OrderSequence orderSequence = objectPool.getObjectOrNull(OrderSequence.class,
                                                               seqRef);
      if (orderSequence == null) {
        throw new ObjectUnknownException(seqRef);
      }
      order.setWrappingSequence(orderSequence.getReference());
    }
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets the <em>dispensable</em> flag of a transport order.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param dispensable The new dispensable flag.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public TransportOrder setTransportOrderDispensable(TCSObjectReference<TransportOrder> orderRef,
                                                     boolean dispensable)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    order.setDispensable(dispensable);
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Removes the referenced transport order from this pool.
   *
   * @param ref A reference to the transport order to be removed.
   * @return The removed transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public TransportOrder removeTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    // Make sure orders currently being processed are not removed.
    checkArgument(!order.hasState(TransportOrder.State.BEING_PROCESSED),
                  "Transport order %s is being processed.",
                  order.getName());
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               order.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return order;
  }

  /**
   * Adds a new order sequence to the pool.
   *
   * @return The newly created order sequence.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  public OrderSequence createOrderSequence() {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int orderID = objectPool.getUniqueObjectId();
    String orderName = objectPool.getUniqueObjectName("OrderSeq-", "0000");
    OrderSequence newSequence = new OrderSequence(orderID, orderName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newSequence);
    }
    catch (ObjectExistsException exc) {
      throw new IllegalStateException(
          "Allegedly unique object name already exists: " + orderName);
    }
    objectPool.emitObjectEvent(newSequence.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created transport order.
    return newSequence;
  }

  /**
   * Adds a new order sequence to the pool.
   *
   * @param to The transfer object from which to create the new order sequence.
   * @return The newly created order sequence.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    OrderSequence newSequence = new OrderSequence(to.getName())
        .withCategory(to.getCategory())
        .withIntendedVehicle(toVehicleReference(to.getIntendedVehicleName()))
        .withFailureFatal(to.isFailureFatal())
        .withProperties(to.getProperties());
    objectPool.addObject(newSequence);
    objectPool.emitObjectEvent(newSequence.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created transport order.
    return newSequence;
  }

  /**
   * Returns the referenced order sequence.
   *
   * @param ref A reference to the order sequence to return.
   * @return The referenced order sequence, or <code>null</code>, if no such
   * sequence exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public OrderSequence getOrderSequence(TCSObjectReference<OrderSequence> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(OrderSequence.class, ref);
  }

  /**
   * Returns the order sequence with the given name.
   *
   * @param seqName The name of the order sequence to return.
   * @return The order sequence with the given name, or <code>null</code>, if
   * no such sequence exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public OrderSequence getOrderSequence(String seqName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(OrderSequence.class, seqName);
  }

  /**
   * Returns a set of order sequences whose names match the given regular
   * expression.
   *
   * @param regexp The regular expression describing the names of the
   * sequences to return. If <code>null</code>, all sequences are returned.
   * @return A set of order sequences whose names match the given regular
   * expression. If no such sequences exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<OrderSequence> getOrderSequences(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(OrderSequence.class, regexp);
  }

  /**
   * Returns a set of order sequences for which the given predicate evaluates to <code>true</code>.
   *
   * @param predicate The predicate.
   * @return A set of order sequences for which the given predicate is true.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<OrderSequence> getOrderSequences(Predicate<? super OrderSequence> predicate) {
    return objectPool.getObjects(OrderSequence.class, predicate);
  }

  /**
   * Adds a transport order to an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be added.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order or order
   * sequence are not in this pool.
   * @throws IllegalArgumentException If the sequence is already marked as
   * <em>complete</em>, if the sequence already contains the given order or
   * if the given transport order has already been activated.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  public OrderSequence addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, IllegalArgumentException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObjectOrNull(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    // Only orders that have not yet been activated are allowed to be added to
    // an order sequence.
    if (!order.hasState(TransportOrder.State.RAW)) {
      throw new IllegalArgumentException(
          "Transport order " + order.getName() + " has already been activated");
    }
    // The sequence and the order must refer to the same intended vehicle.
    if (!Objects.equals(sequence.getIntendedVehicle(),
                        order.getIntendedVehicle())) {
      throw new IllegalArgumentException("Order sequence " + sequence.getName()
          + " and transport order " + order.getName()
          + " have different intended vehicles.");
    }
    OrderSequence previousSeqState = sequence.clone();
    TransportOrder previousOrderState = order.clone();
    // Add the order's reference to the sequence.
    sequence.addOrder(order.getReference());
    objectPool.emitObjectEvent(sequence.clone(),
                               previousSeqState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // Set the back reference to the sequence in the order, too.
    order.setWrappingSequence(sequence.getReference());
    objectPool.emitObjectEvent(order.clone(),
                               previousOrderState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Removes a transport order from an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be removed.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order or order
   * sequence are not in this pool.
   * @deprecated Usage unclear. Handling of subsequent orders in the sequence is fuzzy.
   */
  @Deprecated
  public OrderSequence removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObjectOrNull(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    TransportOrder order = objectPool.getObjectOrNull(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    OrderSequence previousState = sequence.clone();
    sequence.removeOrder(orderRef);
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's finished index.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param index The sequence's new finished index.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence setOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> seqRef,
                                                     int index)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence.clone();
    sequence = objectPool.replaceObject(sequence.withFinishedIndex(index));
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's complete flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence setOrderSequenceComplete(
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence.clone();
    sequence = objectPool.replaceObject(sequence.withComplete(true));
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's finished flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence setOrderSequenceFinished(TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence.clone();
    sequence = objectPool.replaceObject(sequence.withFinished(true));
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's failureFatal flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param fatal The sequence's new fatal flag.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  public OrderSequence setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> seqRef,
      boolean fatal)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObjectOrNull(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    sequence.setFailureFatal(fatal);
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's intended vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order
   * sequence.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  public OrderSequence setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObjectOrNull(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    if (vehicleRef == null) {
      sequence.setIntendedVehicle(vehicleRef);
    }
    else {
      Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, vehicleRef);
      if (vehicle == null) {
        throw new ObjectUnknownException(vehicleRef);
      }
      sequence.setIntendedVehicle(vehicle.getReference());
    }
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Sets an order sequence's processing vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle processing the order sequence.
   * @return The modified order sequence.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence.clone();
    if (vehicleRef == null) {
      sequence = objectPool.replaceObject(sequence.withProcessingVehicle(null));
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      sequence = objectPool.replaceObject(sequence.withProcessingVehicle(vehicle.getReference()));
    }
    objectPool.emitObjectEvent(sequence.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return sequence;
  }

  /**
   * Removes the referenced order sequence from this pool.
   *
   * @param ref A reference to the order sequence to be removed.
   * @return The removed order sequence.
   * @throws ObjectUnknownException If the referenced order sequence is not in this pool.
   */
  @SuppressWarnings("deprecation")
  public OrderSequence removeOrderSequence(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, ref);
    OrderSequence previousState = sequence.clone();
    // XXX Any sanity checks here?
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return sequence;
  }

  /**
   * Removes a completed order sequence including its transport orders.
   *
   * @param ref A reference to the order sequence.
   * @throws ObjectUnknownException If the referenced order sequence is not in this pool.
   * @throws IllegalArgumentException If the order sequence is not finished, yet.
   */
  @SuppressWarnings("deprecation")
  public void removeFinishedOrderSequenceAndOrders(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException, IllegalArgumentException {
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, ref);
    checkArgument(sequence.isFinished(), "Order sequence %s is not finished", sequence.getName());
    OrderSequence previousState = sequence.clone();
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null, previousState, TCSObjectEvent.Type.OBJECT_REMOVED);
    // Also remove all orders in the sequence.
    for (TCSObjectReference<TransportOrder> orderRef : sequence.getOrders()) {
      removeTransportOrder(orderRef);
    }
  }

  private Set<TCSObjectReference<TransportOrder>> getDependencies(TransportOrderCreationTO to)
      throws ObjectUnknownException {
    Set<TCSObjectReference<TransportOrder>> result = new HashSet<>();
    for (String dependencyName : to.getDependencyNames()) {
      TransportOrder dep = getTransportOrder(dependencyName);
      if (dep == null) {
        throw new ObjectUnknownException(dependencyName);
      }
      result.add(dep.getReference());
    }
    return result;
  }

  private TCSObjectReference<OrderSequence> getWrappingSequence(TransportOrderCreationTO to)
      throws ObjectUnknownException, IllegalArgumentException {
    if (to.getWrappingSequence() == null) {
      return null;
    }
    OrderSequence sequence = getOrderSequence(to.getWrappingSequence());
    if (sequence == null) {
      throw new ObjectUnknownException(to.getWrappingSequence());
    }
    checkArgument(!sequence.isComplete(), "Order sequence %s is already complete", sequence);
    checkArgument(Objects.equals(to.getCategory(), sequence.getCategory()),
                  "Order sequence %s has different category than order %s: %s != %s",
                  sequence,
                  to.getName(),
                  sequence.getCategory(),
                  to.getCategory());
    checkArgument(Objects.equals(to.getIntendedVehicleName(), getIntendedVehicleName(sequence)),
                  "Order sequence %s has different intended vehicle than order %s: %s != %s",
                  sequence,
                  to.getName(),
                  sequence.getIntendedVehicle(),
                  to.getIntendedVehicleName());
    return sequence.getReference();
  }

  private TCSObjectReference<Vehicle> toVehicleReference(String vehicleName)
      throws ObjectUnknownException {
    if (vehicleName == null) {
      return null;
    }
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleName);
    return vehicle.getReference();
  }

  private List<DriveOrder> toDriveOrders(List<DestinationCreationTO> dests)
      throws ObjectUnknownException {
    List<DriveOrder> result = new LinkedList<>();
    for (DestinationCreationTO destTo : dests) {
      TCSObject<?> destObject = objectPool.getObjectOrNull(destTo.getDestLocationName());
      if (!(destObject instanceof Location || destObject instanceof Point)) {
        throw new ObjectUnknownException(destTo.getDestLocationName());
      }
      result.add(new DriveOrder(new DriveOrder.Destination(destObject.getReference())
          .withOperation(destTo.getDestOperation())
          .withProperties(destTo.getProperties())));
    }
    return result;
  }

  @Nullable
  private String getIntendedVehicleName(OrderSequence sequence) {
    return sequence.getIntendedVehicle() == null ? null : sequence.getIntendedVehicle().getName();
  }
}
