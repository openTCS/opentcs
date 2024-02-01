/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.ObjectNameProvider;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps all {@code TransportOrder}s and provides methods to create and manipulate them.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of instances of this
 * class must be synchronized externally.
 * </p>
 */
public class TransportOrderPoolManager
    extends TCSObjectManager {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderPoolManager.class);
  /**
   * Provides names for transport orders and order sequences.
   */
  private final ObjectNameProvider objectNameProvider;

  /**
   * Creates a new instance.
   *
   * @param objectRepo The object repo.
   * @param eventHandler The event handler to publish events to.
   * @param orderNameProvider Provides names for transport orders.
   */
  @Inject
  public TransportOrderPoolManager(@Nonnull TCSObjectRepository objectRepo,
                                   @Nonnull @ApplicationEventBus EventHandler eventHandler,
                                   @Nonnull ObjectNameProvider orderNameProvider) {
    super(objectRepo, eventHandler);
    this.objectNameProvider = requireNonNull(orderNameProvider, "orderNameProvider");
  }

  /**
   * Removes all transport orders from this pool.
   */
  public void clear() {
    List<TCSObject<?>> objects = new ArrayList<>();
    objects.addAll(getObjectRepo().getObjects(OrderSequence.class));
    objects.addAll(getObjectRepo().getObjects(TransportOrder.class));

    for (TCSObject<?> curObject : objects) {
      getObjectRepo().removeObject(curObject.getReference());
      emitObjectEvent(null,
                      curObject,
                      TCSObjectEvent.Type.OBJECT_REMOVED);
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
   * @throws IllegalArgumentException One of:
   * <ol>
   * <li>The order is supposed to be part of an order sequence, but
   * the sequence is already complete.</li>
   * <li>The type of the transport order and the order sequence differ.</li>
   * <li>The intended vehicle of the transport order and the order sequence differ.</li>
   * <li>A destination operation is not a valid operation on the destination object.</li>
   * </ol>
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

    LOG.info("Transport order is being created: {} -- {}",
             newOrder.getName(),
             newOrder.getAllDriveOrders());

    getObjectRepo().addObject(newOrder);
    emitObjectEvent(newOrder, null, TCSObjectEvent.Type.OBJECT_CREATED);

    if (newOrder.getWrappingSequence() != null) {
      OrderSequence sequence = getObjectRepo().getObject(OrderSequence.class,
                                                         newOrder.getWrappingSequence());
      OrderSequence prevSeq = sequence;
      sequence = sequence.withOrder(newOrder.getReference());
      getObjectRepo().replaceObject(sequence);
      emitObjectEvent(sequence, prevSeq, TCSObjectEvent.Type.OBJECT_MODIFIED);
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
    TransportOrder previousState = getObjectRepo().getObject(TransportOrder.class, ref);

    checkArgument(!previousState.getState().isFinalState(),
                  "Transport order %s already in a final state, not changing %s -> %s.",
                  ref.getName(),
                  previousState.getState(),
                  newState);

    LOG.info("Transport order's state changes: {} -- {} -> {}",
             previousState.getName(),
             previousState.getState(),
             newState);

    TransportOrder order = previousState.withState(newState);
    getObjectRepo().replaceObject(order);
    emitObjectEvent(order,
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
    TransportOrder order = getObjectRepo().getObject(TransportOrder.class, orderRef);

    LOG.info("Transport order's processing vehicle changes: {} -- {} -> {}",
             order.getName(),
             toObjectName(order.getProcessingVehicle()),
             toObjectName(vehicleRef));

    TransportOrder previousState = order;
    if (vehicleRef == null) {
      order = order.withProcessingVehicle(null);
      getObjectRepo().replaceObject(order);
    }
    else {
      Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleRef);
      order = order.withProcessingVehicle(vehicle.getReference())
          .withDriveOrders(driveOrders)
          .withCurrentDriveOrderIndex(0);
      getObjectRepo().replaceObject(order);
      if (order.getCurrentDriveOrder() != null) {
        order = order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING);
        getObjectRepo().replaceObject(order);
      }
    }
    emitObjectEvent(order,
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
    TransportOrder previousState = getObjectRepo().getObject(TransportOrder.class, orderRef);
    TransportOrder order = previousState.withDriveOrders(newOrders);
    getObjectRepo().replaceObject(order);
    emitObjectEvent(order,
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
    TransportOrder previousState = getObjectRepo().getObject(TransportOrder.class, ref);
    TransportOrder order = previousState;
    // First, mark the current drive order as FINISHED and send an event.
    // Then, shift drive orders and send a second event.
    // Then, mark the current drive order as TRAVELLING and send another event.
    if (order.getCurrentDriveOrder() != null) {
      LOG.info("Transport order's drive order finished: {} -- {}",
               order.getName(),
               order.getCurrentDriveOrder().getDestination());

      order = order.withCurrentDriveOrderState(DriveOrder.State.FINISHED);
      getObjectRepo().replaceObject(order);
      TransportOrder newState = order;
      emitObjectEvent(newState,
                      previousState,
                      TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      order = order.withCurrentDriveOrderIndex(order.getCurrentDriveOrderIndex() + 1)
          .withCurrentRouteStepIndex(TransportOrder.ROUTE_STEP_INDEX_DEFAULT);
      getObjectRepo().replaceObject(order);
      newState = order;
      emitObjectEvent(newState,
                      previousState,
                      TCSObjectEvent.Type.OBJECT_MODIFIED);
      previousState = newState;
      if (order.getCurrentDriveOrder() != null) {
        order = order.withCurrentDriveOrderState(DriveOrder.State.TRAVELLING);
        getObjectRepo().replaceObject(order);
        newState = order;
        emitObjectEvent(newState,
                        previousState,
                        TCSObjectEvent.Type.OBJECT_MODIFIED);
        previousState = newState;
      }
    }
    emitObjectEvent(order,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Sets a transport order's index of the last route step travelled for the currently processed
   * drive order.
   *
   * @param ref A reference to the transport order to be modified.
   * @param index The new index.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   */
  public TransportOrder setTransportOrderCurrentRouteStepIndex(
      TCSObjectReference<TransportOrder> ref,
      int index)
      throws ObjectUnknownException {
    TransportOrder previousState = getObjectRepo().getObject(TransportOrder.class, ref);
    TransportOrder order = previousState.withCurrentRouteStepIndex(index);
    getObjectRepo().replaceObject(order);
    emitObjectEvent(order,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return order;
  }

  /**
   * Set a transport order's intended vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle intended for the transport order.
   * @return The modified transport order.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool or if the intended vehicle is not null and not in this this pool.
   * @throws IllegalArgumentException If the transport order is not in the dispatchable state.
   */
  public TransportOrder setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, IllegalArgumentException {
    TransportOrder order = getObjectRepo().getObject(TransportOrder.class, orderRef);

    if (!canSetIntendedVehicle(order)) {
      throw new IllegalArgumentException(String.format(
          "Cannot set intended vehicle '%s' for transport order '%s' in state '%s'",
          toObjectName(vehicleRef),
          order.getName(),
          order.getState()
      ));
    }

    if (vehicleRef != null && getObjectRepo().getObjectOrNull(Vehicle.class, vehicleRef) == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + vehicleRef.getName());
    }

    LOG.info("Transport order's intended vehicle changes: {} -- {} -> {}",
             order.getName(),
             toObjectName(order.getIntendedVehicle()),
             toObjectName(vehicleRef));

    TransportOrder previousState = order;
    order = order.withIntendedVehicle(vehicleRef);
    getObjectRepo().replaceObject(order);
    emitObjectEvent(order,
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
    TransportOrder order = getObjectRepo().getObject(TransportOrder.class, ref);
    // Make sure only orders in a final state are removed.
    checkArgument(order.getState().isFinalState(),
                  "Transport order %s is not in a final state.",
                  order.getName());
    getObjectRepo().removeObject(ref);
    emitObjectEvent(null,
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

    LOG.info("Order sequence is being created: {}", newSequence.getName());

    getObjectRepo().addObject(newSequence);
    emitObjectEvent(newSequence,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, seqRef);
    OrderSequence sequence = previousState.withFinishedIndex(index);
    getObjectRepo().replaceObject(sequence);
    emitObjectEvent(sequence,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, seqRef);
    OrderSequence sequence = previousState.withComplete(true);
    getObjectRepo().replaceObject(sequence);
    emitObjectEvent(sequence,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, seqRef);
    OrderSequence sequence = previousState.withFinished(true);
    getObjectRepo().replaceObject(sequence);
    emitObjectEvent(sequence,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, seqRef);

    LOG.info("Order sequence's processing vehicle changes: {} -- {} -> {}",
             previousState.getName(),
             toObjectName(previousState.getProcessingVehicle()),
             toObjectName(vehicleRef));

    OrderSequence sequence = previousState;
    if (vehicleRef == null) {
      sequence = sequence.withProcessingVehicle(null);
      getObjectRepo().replaceObject(sequence);
    }
    else {
      Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleRef);
      sequence = sequence.withProcessingVehicle(vehicle.getReference());
      getObjectRepo().replaceObject(sequence);
    }
    emitObjectEvent(sequence,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, ref);
    OrderSequence sequence = previousState;
    // XXX Any sanity checks here?
    getObjectRepo().removeObject(ref);
    emitObjectEvent(null,
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
    OrderSequence previousState = getObjectRepo().getObject(OrderSequence.class, ref);
    checkArgument(previousState.isFinished(),
                  "Order sequence %s is not finished",
                  previousState.getName());
    OrderSequence sequence = previousState;
    getObjectRepo().removeObject(ref);
    emitObjectEvent(null, previousState, TCSObjectEvent.Type.OBJECT_REMOVED);
    // Also remove all orders in the sequence.
    for (TCSObjectReference<TransportOrder> orderRef : sequence.getOrders()) {
      removeTransportOrder(orderRef);
    }
  }

  private Set<TCSObjectReference<TransportOrder>> getDependencies(TransportOrderCreationTO to)
      throws ObjectUnknownException {
    Set<TCSObjectReference<TransportOrder>> result = new HashSet<>();
    for (String dependencyName : to.getDependencyNames()) {
      result.add(getObjectRepo().getObject(TransportOrder.class, dependencyName).getReference());
    }
    return result;
  }

  @SuppressWarnings("deprecation")
  private TCSObjectReference<OrderSequence> getWrappingSequence(TransportOrderCreationTO to)
      throws ObjectUnknownException, IllegalArgumentException {
    if (to.getWrappingSequence() == null) {
      return null;
    }
    OrderSequence sequence = getObjectRepo().getObject(OrderSequence.class,
                                                       to.getWrappingSequence());
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
    Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleName);
    return vehicle.getReference();
  }

  private List<DriveOrder> toDriveOrders(List<DestinationCreationTO> dests)
      throws ObjectUnknownException, IllegalArgumentException {
    List<DriveOrder> result = new ArrayList<>(dests.size());
    for (DestinationCreationTO destTo : dests) {
      TCSObject<?> destObject = getObjectRepo().getObjectOrNull(destTo.getDestLocationName());

      if (destObject instanceof Point) {
        if (!isValidOperationOnPoint(destTo.getDestOperation())) {
          throw new IllegalArgumentException(destTo.getDestOperation()
              + " is not a valid operation for point destination " + destObject.getName());
        }
      }
      else if (destObject instanceof Location) {
        if (!isValidLocationDestination(destTo, (Location) destObject)) {
          throw new IllegalArgumentException(destTo.getDestOperation()
              + " is not a valid operation for location destination " + destObject.getName());
        }
      }
      else {
        throw new ObjectUnknownException(destTo.getDestLocationName());
      }
      result.add(new DriveOrder(new DriveOrder.Destination(destObject.getReference())
          .withOperation(destTo.getDestOperation())
          .withProperties(destTo.getProperties())));
    }
    return result;
  }

  private boolean isValidOperationOnPoint(String operation) {
    return operation.equals(Destination.OP_MOVE)
        || operation.equals(Destination.OP_PARK);
  }

  private boolean isValidLocationDestination(DestinationCreationTO dest, Location location) {
    LocationType type = getObjectRepo()
        .getObjectOrNull(LocationType.class, location.getType().getName());

    return type != null
        && isValidOperationOnLocationType(dest.getDestOperation(), type)
        && location.getAttachedLinks().stream()
            .anyMatch(link -> isValidOperationOnLink(dest.getDestOperation(), link));
  }

  private boolean isValidOperationOnLink(String operation, Link link) {
    return link.getAllowedOperations().isEmpty()
        || link.getAllowedOperations().contains(operation)
        || operation.equals(Destination.OP_NOP);
  }

  private boolean isValidOperationOnLocationType(String operation, LocationType type) {
    return type.isAllowedOperation(operation)
        || operation.equals(Destination.OP_NOP);
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

  @Nonnull
  private String toObjectName(TCSObjectReference<?> ref) {
    return ref == null ? "null" : ref.getName();
  }

  private boolean canSetIntendedVehicle(TransportOrder order) {
    return order.hasState(TransportOrder.State.RAW)
        || order.hasState(TransportOrder.State.ACTIVE)
        || order.hasState(TransportOrder.State.DISPATCHABLE);
  }
}
