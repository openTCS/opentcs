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
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.ObjectNameProvider;
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
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.util.Assertions.checkArgument;
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
   * Provides names for transport orders and order sequences.
   */
  private final ObjectNameProvider objectNameProvider;

  /**
   * Creates a new instance.
   *
   * @param objectPool The object pool serving as the container for this order pool's data.
   * @param orderNameProvider Provides names for transport orders.
   */
  @Inject
  public TransportOrderPool(TCSObjectPool objectPool,
                            ObjectNameProvider orderNameProvider) {
    this.objectPool = requireNonNull(objectPool, "objectPool");
    this.objectNameProvider = requireNonNull(orderNameProvider, "orderNameProvider");
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
    for (TCSObject<?> curObject : objectPool.getObjects((Pattern) null)) {
      if (curObject instanceof TransportOrder
          || curObject instanceof OrderSequence) {
        objectPool.removeObject(curObject.getReference());
        objectPool.emitObjectEvent(null,
                                   curObject,
                                   TCSObjectEvent.Type.OBJECT_REMOVED);
      }
    }
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
  public TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, IllegalArgumentException {
    TransportOrder newOrder = new TransportOrder(nameFor(to),
                                                 toDriveOrders(to.getDestinations()))
        .withCreationTime(Instant.now())
        .withPeripheralReservationToken(to.getPeripheralReservationToken())
        .withIntendedVehicle(toVehicleReference(to.getIntendedVehicleName()))
        .withType(to.getType())
        .withDeadline(to.getDeadline())
        .withDispensable(to.isDispensable())
        .withWrappingSequence(getWrappingSequence(to))
        .withDependencies(getDependencies(to))
        .withProperties(to.getProperties());
    objectPool.addObject(newOrder);
    objectPool.emitObjectEvent(newOrder, null, TCSObjectEvent.Type.OBJECT_CREATED);

    if (newOrder.getWrappingSequence() != null) {
      OrderSequence sequence = objectPool.getObject(OrderSequence.class,
                                                    newOrder.getWrappingSequence());
      OrderSequence prevSeq = sequence;
      sequence = objectPool.replaceObject(sequence.withOrder(newOrder.getReference()));
      objectPool.emitObjectEvent(sequence, prevSeq, TCSObjectEvent.Type.OBJECT_MODIFIED);
    }

    // Return the newly created transport order.
    return newOrder;
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
  public TransportOrder setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                               TransportOrder.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    TransportOrder previousState = order;
    order = objectPool.replaceObject(order.withState(newState));
    objectPool.emitObjectEvent(order,
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
  public TransportOrder setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef,
      List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order;
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
    objectPool.emitObjectEvent(order,
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
  public TransportOrder setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                                     List<DriveOrder> newOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
    TransportOrder previousState = order;
    order = objectPool.replaceObject(order.withDriveOrders(newOrders));
    objectPool.emitObjectEvent(order,
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
  public TransportOrder setTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    TransportOrder order = objectPool.getObject(TransportOrder.class, ref);
    TransportOrder previousState = order;
    // First, mark the current drive order as FINISHED and send an event.
    // Then, shift drive orders and send a second event.
    // Then, mark the current drive order as TRAVELLING and send another event.
    if (order.getCurrentDriveOrder() != null) {
      order = objectPool.replaceObject(order.withCurrentDriveOrderState(DriveOrder.State.FINISHED));
      TransportOrder newState = order;
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      order = objectPool.replaceObject(
          order.withCurrentDriveOrderIndex(order.getCurrentDriveOrderIndex() + 1));
      newState = order;
      objectPool.emitObjectEvent(newState,
                                 previousState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      if (order.getCurrentDriveOrder() != null) {
        order = objectPool.replaceObject(
            order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING));
        newState = order;
        objectPool.emitObjectEvent(newState,
                                   previousState,
                                   TCSObjectEvent.Type.OBJECT_MODIFIED);
        previousState = newState;
      }
    }
    objectPool.emitObjectEvent(order,
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
                               order,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return order;
  }

  /**
   * Adds a new order sequence to the pool.
   *
   * @param to The transfer object from which to create the new order sequence.
   * @return The newly created order sequence.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    OrderSequence newSequence = new OrderSequence(nameFor(to))
        .withType(to.getType())
        .withIntendedVehicle(toVehicleReference(to.getIntendedVehicleName()))
        .withFailureFatal(to.isFailureFatal())
        .withProperties(to.getProperties());
    objectPool.addObject(newSequence);
    objectPool.emitObjectEvent(newSequence,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created transport order.
    return newSequence;
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
  public OrderSequence setOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> seqRef,
                                                     int index)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence;
    sequence = objectPool.replaceObject(sequence.withFinishedIndex(index));
    objectPool.emitObjectEvent(sequence,
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
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence;
    sequence = objectPool.replaceObject(sequence.withComplete(true));
    objectPool.emitObjectEvent(sequence,
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
  public OrderSequence setOrderSequenceFinished(TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence;
    sequence = objectPool.replaceObject(sequence.withFinished(true));
    objectPool.emitObjectEvent(sequence,
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
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, seqRef);
    OrderSequence previousState = sequence;
    if (vehicleRef == null) {
      sequence = objectPool.replaceObject(sequence.withProcessingVehicle(null));
    }
    else {
      Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
      sequence = objectPool.replaceObject(sequence.withProcessingVehicle(vehicle.getReference()));
    }
    objectPool.emitObjectEvent(sequence,
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
  public OrderSequence removeOrderSequence(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, ref);
    OrderSequence previousState = sequence;
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
  public void removeFinishedOrderSequenceAndOrders(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException, IllegalArgumentException {
    OrderSequence sequence = objectPool.getObject(OrderSequence.class, ref);
    checkArgument(sequence.isFinished(), "Order sequence %s is not finished", sequence.getName());
    OrderSequence previousState = sequence;
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
      TransportOrder dep = getObjectPool().getObject(TransportOrder.class, dependencyName);
      if (dep == null) {
        throw new ObjectUnknownException(dependencyName);
      }
      result.add(dep.getReference());
    }
    return result;
  }

  @SuppressWarnings("deprecation")
  private TCSObjectReference<OrderSequence> getWrappingSequence(TransportOrderCreationTO to)
      throws ObjectUnknownException, IllegalArgumentException {
    if (to.getWrappingSequence() == null) {
      return null;
    }
    OrderSequence sequence = getObjectPool().getObject(OrderSequence.class,
                                                       to.getWrappingSequence());
    if (sequence == null) {
      throw new ObjectUnknownException(to.getWrappingSequence());
    }
    checkArgument(!sequence.isComplete(), "Order sequence %s is already complete", sequence);
    checkArgument(Objects.equals(to.getType(), sequence.getType()),
                  "Order sequence %s has different type than order %s: %s != %s",
                  sequence,
                  to.getName(),
                  sequence.getType(),
                  to.getType());
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

  @Nonnull
  private String nameFor(@Nonnull TransportOrderCreationTO to) {
    if (to.hasIncompleteName()) {
      return objectNameProvider.apply(to);
    }
    else {
      return to.getName();
    }
  }

  @Nonnull
  private String nameFor(@Nonnull OrderSequenceCreationTO to) {
    if (to.hasIncompleteName()) {
      return objectNameProvider.apply(to);
    }
    else {
      return to.getName();
    }
  }
}
