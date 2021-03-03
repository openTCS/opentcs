/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.UniqueTimestampGenerator;
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
  private static final Logger log
      = LoggerFactory.getLogger(TransportOrderPool.class);
  /**
   * The system's global object pool.
   */
  private final TCSObjectPool objectPool;
  /**
   * The timestamp generator for order creation times.
   */
  private final UniqueTimestampGenerator timestampGenerator
      = new UniqueTimestampGenerator();

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
    log.debug("method entry");
    return objectPool;
  }

  /**
   * Removes all transport orders from this pool.
   */
  public void clear() {
    log.debug("method entry");
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
   */
  public TransportOrder createTransportOrder(
      List<DriveOrder.Destination> destinations) {
    log.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int orderID = objectPool.getUniqueObjectId();
    String orderName = objectPool.getUniqueObjectName("TOrder-", "0000");
    TransportOrder newOrder
        = new TransportOrder(orderID,
                             orderName,
                             destinations,
                             timestampGenerator.getNextTimestamp());
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
   * Returns the referenced transport order.
   *
   * @param ref A reference to the transport order to return.
   * @return The referenced transport order, or <code>null</code>, if no such
   * order exists.
   */
  public TransportOrder getTransportOrder(
      TCSObjectReference<TransportOrder> ref) {
    log.debug("method entry");
    return objectPool.getObject(TransportOrder.class, ref);
  }

  /**
   * Returns the transport order with the given name.
   *
   * @param orderName The name of the TransportOrder to return.
   * @return The transport order with the given name, or <code>null</code>, if
   * no such order exists.
   */
  public TransportOrder getTransportOrder(String orderName) {
    log.debug("method entry");
    return objectPool.getObject(TransportOrder.class, orderName);
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
   */
  public Set<TransportOrder> getTransportOrders(Pattern regexp) {
    log.debug("method entry");
    return objectPool.getObjects(TransportOrder.class, regexp);
  }

  /**
   * Returns a set of transport orders current in the given state.
   *
   * @param state The state of the transport orders to be returned.
   * @return A set of transport orders current in the given state. If no such
   * transport orders exist, the returned set is empty.
   */
  public Set<TransportOrder> getTransportOrders(TransportOrder.State state) {
    log.debug("method entry");
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
   * Sets a transport order's deadline.
   *
   * @param ref A reference to the transport order to be modified.
   * @param deadline The transport order's new deadline.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  public TransportOrder setTransportOrderDeadline(
      TCSObjectReference<TransportOrder> ref, long deadline)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
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
  public TransportOrder setTransportOrderState(
      TCSObjectReference<TransportOrder> ref, TransportOrder.State newState)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    if (order == null) {
      throw new ObjectUnknownException(ref);
    }
    TransportOrder previousState = order.clone();
    order.setState(newState);
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
   */
  public TransportOrder setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    if (vehicleRef == null) {
      order.setIntendedVehicle(null);
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
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
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  public TransportOrder setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    if (vehicleRef == null) {
      order.setProcessingVehicle(null);
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      if (vehicle == null) {
        throw new ObjectUnknownException(vehicleRef);
      }
      order.setProcessingVehicle(vehicle.getReference());
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
  public TransportOrder setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    order.setFutureDriveOrders(newOrders);
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
   */
  public TransportOrder setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    if (order == null) {
      throw new ObjectUnknownException(ref);
    }
    TransportOrder previousState = order.clone();
    order.setInitialDriveOrder();
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
  public TransportOrder setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    if (order == null) {
      throw new ObjectUnknownException(ref);
    }
    TransportOrder previousState = order.clone();
    // First, mark the current drive order as FINISHED and send an event.
    // Then, shift drive orders and send a second event.
    // Then, mark the current drive order as TRAVELLING and send another event.
    if (order.getCurrentDriveOrder() != null) {
      order.setCurrentDriveOrderState(DriveOrder.State.FINISHED);
      TransportOrder newState = order.clone();
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      order.setNextDriveOrder();
      newState = order.clone();
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      if (order.getCurrentDriveOrder() != null) {
        order.setCurrentDriveOrderState(DriveOrder.State.TRAVELLING);
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
   */
  public TransportOrder addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    TransportOrder newDep = objectPool.getObject(TransportOrder.class,
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
   */
  public TransportOrder removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    TransportOrder rmDep = objectPool.getObject(TransportOrder.class, rmDepRef);
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
  public TransportOrder addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    order.addRejection(newRejection);
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
   */
  public TransportOrder setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    if (order == null) {
      throw new ObjectUnknownException(orderRef);
    }
    TransportOrder previousState = order.clone();
    if (seqRef == null) {
      order.setWrappingSequence(null);
    }
    else {
      OrderSequence orderSequence = objectPool.getObject(OrderSequence.class,
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
   */
  public TransportOrder setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
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
  public TransportOrder removeTransportOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    if (order == null) {
      throw new ObjectUnknownException(ref);
    }
    TransportOrder previousState = order.clone();
    // Make sure orders currently being processed are not removed.
    switch (order.getState()) {
      case BEING_PROCESSED:
        throw new IllegalArgumentException("Transport order to be removed is "
            + "in state " + order.getState());
      default:
      // OK, do nothing.
    }
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(order.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return order;
  }

  /**
   * Adds a new order sequence to the pool.
   *
   * @return The newly created order sequence.
   */
  public OrderSequence createOrderSequence() {
    log.debug("method entry");
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
   * Returns the referenced order sequence.
   *
   * @param ref A reference to the order sequence to return.
   * @return The referenced order sequence, or <code>null</code>, if no such
   * sequence exists.
   */
  public OrderSequence getOrderSequence(TCSObjectReference<OrderSequence> ref) {
    log.debug("method entry");
    return objectPool.getObject(OrderSequence.class, ref);
  }

  /**
   * Returns the order sequence with the given name.
   *
   * @param seqName The name of the order sequence to return.
   * @return The order sequence with the given name, or <code>null</code>, if
   * no such sequence exists.
   */
  public OrderSequence getOrderSequence(String seqName) {
    log.debug("method entry");
    return objectPool.getObject(OrderSequence.class, seqName);
  }

  /**
   * Returns a set of order sequences whose names match the given regular
   * expression.
   *
   * @param regexp The regular expression describing the names of the
   * sequences to return. If <code>null</code>, all sequences are returned.
   * @return A set of order sequences whose names match the given regular
   * expression. If no such sequences exist, the returned set is empty.
   */
  public Set<OrderSequence> getOrderSequences(Pattern regexp) {
    log.debug("method entry");
    return objectPool.getObjects(OrderSequence.class, regexp);
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
   */
  public OrderSequence addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, IllegalArgumentException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
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
   */
  public OrderSequence removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
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
  public OrderSequence setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> seqRef,
      int index)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    sequence.setFinishedIndex(index);
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
  public OrderSequence setOrderSequenceComplete(
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    sequence.setComplete();
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
  public OrderSequence setOrderSequenceFinished(
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    sequence.setFinished();
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
   */
  public OrderSequence setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> seqRef,
      boolean fatal)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
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
   */
  public OrderSequence setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    if (vehicleRef == null) {
      sequence.setIntendedVehicle(vehicleRef);
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
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
  public OrderSequence setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    if (sequence == null) {
      throw new ObjectUnknownException(seqRef);
    }
    OrderSequence previousState = sequence.clone();
    if (vehicleRef == null) {
      sequence.setProcessingVehicle(null);
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      if (vehicle == null) {
        throw new ObjectUnknownException(vehicleRef);
      }
      sequence.setProcessingVehicle(vehicle.getReference());
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
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   */
  public OrderSequence removeOrderSequence(
      TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    log.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, ref);
    if (sequence == null) {
      throw new ObjectUnknownException(ref);
    }
    OrderSequence previousState = sequence.clone();
    // XXX Any sanity checks here?
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return sequence;
  }
}
