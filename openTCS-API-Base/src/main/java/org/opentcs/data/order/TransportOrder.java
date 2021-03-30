/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_CREATED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DRIVE_ORDER_FINISHED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_PROCESSING_VEHICLE_CHANGED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_REACHED_FINAL_STATE;

/**
 * Represents a sequence of movements and operations that are to be executed by a {@link Vehicle}.
 * <p>
 * A TransportOrder basically encapsulates a list of {@link DriveOrder} instances.
 * </p>
 * <p>
 * Transport orders may depend on other transport orders in the systems, which means they may not be
 * processed before the orders they depend on have been processed.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrder
    extends TCSObject<TransportOrder>
    implements Serializable {

  /**
   * The type of this transport order.
   */
  @Nonnull
  private final String type;
  /**
   * A set of TransportOrders that must have been finished before this one may
   * be processed.
   */
  @Nonnull
  private final Set<TCSObjectReference<TransportOrder>> dependencies;
  /**
   * The drive orders this transport order consists of.
   */
  @Nonnull
  private final List<DriveOrder> driveOrders;
  /**
   * An optional token for reserving peripheral devices while processing this transport order.
   */
  @Nullable
  private final String peripheralReservationToken;
  /**
   * The index of the currently processed drive order.
   */
  private final int currentDriveOrderIndex;
  /**
   * This transport order's current state.
   */
  @Nonnull
  private final State state;
  /**
   * The point of time at which this transport order was created.
   */
  private final Instant creationTime;
  /**
   * The point of time at which processing of this transport order must be finished.
   */
  private final Instant deadline;
  /**
   * The point of time at which processing of this transport order was finished.
   */
  private final Instant finishedTime;
  /**
   * A reference to the vehicle that is intended to process this transport
   * order. If this order is free to be processed by any vehicle, this is
   * <code>null</code>.
   */
  @Nullable
  private final TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * A reference to the vehicle currently processing this transport order. If
   * this transport order is not being processed at the moment, this is
   * <code>null</code>.
   */
  @Nullable
  private final TCSObjectReference<Vehicle> processingVehicle;
  /**
   * The order sequence this transport order belongs to. May be
   * <code>null</code> in case this order isn't part of any sequence.
   */
  @Nullable
  private final TCSObjectReference<OrderSequence> wrappingSequence;
  /**
   * Whether this order is dispensable (may be withdrawn automatically).
   */
  private final boolean dispensable;

  /**
   * Creates a new TransportOrder.
   *
   * @param name This transport order's name.
   * @param driveOrders A list of drive orders to be processed when processing this transport
   * order.
   */
  public TransportOrder(String name, List<DriveOrder> driveOrders) {
    super(name,
          new HashMap<>(),
          new ObjectHistory().withEntryAppended(new ObjectHistory.Entry(ORDER_CREATED)));
    this.type = OrderConstants.TYPE_NONE;
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
    this.peripheralReservationToken = null;
    this.currentDriveOrderIndex = -1;
    this.state = State.RAW;
    this.creationTime = Instant.EPOCH;
    this.intendedVehicle = null;
    this.processingVehicle = null;
    this.deadline = Instant.MAX;
    this.finishedTime = Instant.MAX;
    this.dispensable = false;
    this.wrappingSequence = null;
    this.dependencies = new LinkedHashSet<>();
  }

  /**
   * Creates a new TransportOrder.
   *
   * @param objectID This transport order's ID.
   * @param name This transport order's name.
   * @param destinations A list of destinations that are to be travelled to
   * when processing this transport order.
   * @param creationTime The creation time stamp to be set.
   */
  private TransportOrder(String name,
                         Map<String, String> properties,
                         ObjectHistory history,
                         String type,
                         List<DriveOrder> driveOrders,
                         String peripheralReservationToken,
                         int currentDriveOrderIndex,
                         Instant creationTime,
                         TCSObjectReference<Vehicle> intendedVehicle,
                         Instant deadline,
                         boolean dispensable,
                         TCSObjectReference<OrderSequence> wrappingSequence,
                         Set<TCSObjectReference<TransportOrder>> dependencies,
                         TCSObjectReference<Vehicle> processingVehicle,
                         State state,
                         Instant finishedTime) {
    super(name, properties, history);
    this.type = requireNonNull(type, "type");
    requireNonNull(driveOrders, "driveOrders");
    this.driveOrders = new LinkedList<>();
    for (DriveOrder driveOrder : driveOrders) {
      this.driveOrders.add(driveOrder.withTransportOrder(this.getReference()));
    }

    this.peripheralReservationToken = peripheralReservationToken;
    this.currentDriveOrderIndex = currentDriveOrderIndex;
    this.creationTime = requireNonNull(creationTime, "creationTime");
    this.intendedVehicle = intendedVehicle;
    this.deadline = requireNonNull(deadline, "deadline");
    this.dispensable = dispensable;
    this.wrappingSequence = wrappingSequence;
    this.dependencies = requireNonNull(dependencies, "dependencies");
    this.processingVehicle = processingVehicle;
    this.state = requireNonNull(state, "state");
    this.finishedTime = requireNonNull(finishedTime, "finishedTime");
  }

  @Override
  public TransportOrder withProperty(String key, String value) {
    return new TransportOrder(getName(),
                              propertiesWith(key, value),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public TransportOrder withProperties(Map<String, String> properties) {
    return new TransportOrder(getName(),
                              properties,
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public TransportOrder withHistoryEntry(ObjectHistory.Entry entry) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory().withEntryAppended(entry),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public TransportOrder withHistory(ObjectHistory history) {
    return new TransportOrder(getName(),
                              getProperties(),
                              history,
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Retruns this transport order's type.
   *
   * @return This transport order's type.
   */
  public String getType() {
    return type;
  }

  /**
   * Creates a copy of this obejct, with the given type.
   *
   * @param type The tpye to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withType(String type) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns this transport order's current state.
   *
   * @return This transport order's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Checks if this transport order's current state is equal to the given one.
   *
   * @param otherState The state to compare to this transport order's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * transport order's one.
   */
  public boolean hasState(State otherState) {
    requireNonNull(otherState, "otherState");
    return this.state.equals(otherState);
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withState(@Nonnull State state) {
    // XXX Finished time should probably not be set implicitly.
    return new TransportOrder(getName(),
                              getProperties(),
                              historyForNewState(state),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              state == State.FINISHED ? Instant.now() : finishedTime);
  }

  /**
   * Returns this transport order's creation time.
   *
   * @return This transport order's creation time.
   */
  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param creationTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCreationTime(Instant creationTime) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns this transport order's deadline. If the value of transport order's
   * deadline was not changed, the initial value {@link Instant#MAX} is returned.
   *
   * @return This transport order's deadline or the initial deadline value.{@link Instant#MAX}, if
   * the deadline was not changed.
   */
  public Instant getDeadline() {
    return deadline;
  }

  /**
   * Creates a copy of this object, with the given deadline.
   *
   * @param deadline The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDeadline(Instant deadline) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the point of time at which this transport order was finished.
   * If the transport order has not been finished, yet, {@link Instant#MAX} is returned.
   *
   * @return The point of time at which this transport order was finished, or {@link Instant#MAX},
   * if the transport order has not been finished, yet.
   */
  public Instant getFinishedTime() {
    return finishedTime;
  }

  /**
   * Creates a copy of this object, with the given finished time.
   *
   * @param finishedTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withFinishedTime(Instant finishedTime) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a reference to the vehicle that is intended to process this
   * transport order.
   *
   * @return A reference to the vehicle that is intended to process this
   * transport order. If this order is free to be processed by any vehicle,
   * <code>null</code> is returned.
   */
  @Nullable
  public TCSObjectReference<Vehicle> getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Creates a copy of this object, with the given intended vehicle.
   *
   * @param intendedVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withIntendedVehicle(@Nullable TCSObjectReference<Vehicle> intendedVehicle) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a reference to the vehicle currently processing this transport
   * order.
   *
   * @return A reference to the vehicle currently processing this transport
   * order. If this transport order is not currently being processed,
   * <code>null</code> is returned.
   */
  @Nullable
  public TCSObjectReference<Vehicle> getProcessingVehicle() {
    return processingVehicle;
  }

  /**
   * Creates a copy of this object, with the given processing vehicle.
   *
   * @param processingVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withProcessingVehicle(
      @Nullable TCSObjectReference<Vehicle> processingVehicle) {
    return new TransportOrder(getName(),
                              getProperties(),
                              historyForNewProcessingVehicle(processingVehicle),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the set of transport orders this order depends on.
   *
   * @return The set of transport orders this order depends on.
   */
  public Set<TCSObjectReference<TransportOrder>> getDependencies() {
    return dependencies;
  }

  /**
   * Creates a copy of this object, with the given dependencies.
   *
   * @param dependencies The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDependencies(
      @Nonnull Set<TCSObjectReference<TransportOrder>> dependencies) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a list of DriveOrders that have been processed already.
   *
   * @return A list of DriveOrders that have been processed already.
   */
  @Nonnull
  public List<DriveOrder> getPastDriveOrders() {
    List<DriveOrder> result = new ArrayList<>();
    for (int i = 0; i < currentDriveOrderIndex; i++) {
      result.add(driveOrders.get(i));
    }
    return result;
  }

  /**
   * Returns a list of DriveOrders that still need to be processed.
   *
   * @return A list of DriveOrders that still need to be processed.
   */
  @Nonnull
  public List<DriveOrder> getFutureDriveOrders() {
    List<DriveOrder> result = new ArrayList<>();
    for (int i = currentDriveOrderIndex + 1; i < driveOrders.size(); i++) {
      result.add(driveOrders.get(i));
    }
    return result;
  }

  /**
   * Creates a copy of this object, with the given drive orders.
   *
   * @param driveOrders The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDriveOrders(@Nonnull List<DriveOrder> driveOrders) {
    requireNonNull(driveOrders, "driveOrders");
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   *
   * @return the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   */
  @Nullable
  public DriveOrder getCurrentDriveOrder() {
    return (currentDriveOrderIndex >= 0 && currentDriveOrderIndex < driveOrders.size())
        ? driveOrders.get(currentDriveOrderIndex)
        : null;
  }

  /**
   * Returns a list of all drive orders, i.e. the past, current and future drive
   * orders.
   *
   * @return A list of all drive orders, i.e. the past, current and future drive
   * orders. If no drive orders exist, the returned list is empty.
   */
  @Nonnull
  public List<DriveOrder> getAllDriveOrders() {
    return new ArrayList<>(driveOrders);
  }

  /**
   * Returns an optional token for reserving peripheral devices while processing this transport
   * order.
   *
   * @return An optional token for reserving peripheral devices while processing this transport
   * order.
   */
  @Nullable
  public String getPeripheralReservationToken() {
    return peripheralReservationToken;
  }

  /**
   * Creates a copy of this object, with the given reservation token.
   *
   * @param peripheralReservationToken The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withPeripheralReservationToken(
      @Nullable String peripheralReservationToken) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the index of the currently processed drive order.
   *
   * @return The index of the currently processed drive order.
   */
  public int getCurrentDriveOrderIndex() {
    return currentDriveOrderIndex;
  }

  /**
   * Creates a copy of this object, with the given drive order index.
   *
   * @param currentDriveOrderIndex The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCurrentDriveOrderIndex(int currentDriveOrderIndex) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Creates a copy of this object, with the given current drive order state.
   *
   * @param driveOrderState The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCurrentDriveOrderState(@Nonnull DriveOrder.State driveOrderState) {
    requireNonNull(driveOrderState, "driveOrderState");

    List<DriveOrder> newDriveOrders = new ArrayList<>(this.driveOrders);
    newDriveOrders.set(currentDriveOrderIndex,
                       newDriveOrders.get(currentDriveOrderIndex).withState(driveOrderState));

    return new TransportOrder(getName(),
                              getProperties(),
                              historyForNewDriveOrderState(driveOrderState),
                              type,
                              newDriveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   *
   * @return The order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   */
  @Nullable
  public TCSObjectReference<OrderSequence> getWrappingSequence() {
    return wrappingSequence;
  }

  /**
   * Creates a copy of this object, with the given wrapping sequence.
   *
   * @param wrappingSequence The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withWrappingSequence(
      @Nullable TCSObjectReference<OrderSequence> wrappingSequence) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Checks if this order is dispensable.
   *
   * @return <code>true</code> if, and only if, this order is dispensable.
   */
  public boolean isDispensable() {
    return dispensable;
  }

  /**
   * Creates a copy of this object, with the given dispensable flag.
   *
   * @param dispensable The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDispensable(boolean dispensable) {
    return new TransportOrder(getName(),
                              getProperties(),
                              getHistory(),
                              type,
                              driveOrders,
                              peripheralReservationToken,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public String toString() {
    return "TransportOrder{"
        + "name=" + getName()
        + ", state=" + state
        + ", intendedVehicle=" + intendedVehicle
        + ", processingVehicle=" + processingVehicle
        + ", creationTime=" + creationTime
        + ", deadline=" + deadline
        + ", finishedTime=" + finishedTime
        + ", wrappingSequence=" + wrappingSequence
        + ", dispensable=" + dispensable
        + ", type=" + type
        + ", peripheralReservationToken=" + peripheralReservationToken
        + ", dependencies=" + dependencies
        + ", driveOrders=" + driveOrders
        + ", currentDriveOrderIndex=" + currentDriveOrderIndex
        + '}';
  }

  private ObjectHistory historyForNewState(State state) {
    return state.isFinalState()
        ? getHistory().withEntryAppended(new ObjectHistory.Entry(ORDER_REACHED_FINAL_STATE))
        : getHistory();
  }

  private ObjectHistory historyForNewDriveOrderState(DriveOrder.State state) {
    return state == DriveOrder.State.FINISHED
        ? getHistory().withEntryAppended(new ObjectHistory.Entry(ORDER_DRIVE_ORDER_FINISHED))
        : getHistory();
  }

  private ObjectHistory historyForNewProcessingVehicle(TCSObjectReference<Vehicle> ref) {
    return getHistory().withEntryAppended(
        new ObjectHistory.Entry(ORDER_PROCESSING_VEHICLE_CHANGED,
                                ref == null ? "" : ref.getName())
    );
  }

  /**
   * This enumeration defines the various states a transport order may be in.
   */
  public static enum State {

    /**
     * A transport order's initial state.
     * A transport order remains in this state until its parameters have been
     * set up completely.
     */
    RAW,
    /**
     * Set (by a user/client) when a transport order's parameters have been set
     * up completely and the kernel should dispatch it when possible.
     */
    ACTIVE,
    /**
     * Marks a transport order as ready to be dispatched to a vehicle (i.e. all
     * its dependencies have been finished).
     */
    DISPATCHABLE,
    /**
     * Marks a transport order as being processed by a vehicle.
     */
    BEING_PROCESSED,
    /**
     * Indicates the transport order is withdrawn from a processing vehicle but
     * not yet in its final state (which will be FAILED), as the vehicle has not
     * yet finished/cleaned up.
     */
    WITHDRAWN,
    /**
     * Marks a transport order as successfully completed.
     */
    FINISHED,
    /**
     * General failure state that marks a transport order as failed.
     */
    FAILED,
    /**
     * Failure state that marks a transport order as unroutable, i.e. it is
     * impossible to find a route that would allow a vehicle to process the
     * transport order completely.
     */
    UNROUTABLE;

    /**
     * Checks if this state is a final state for a transport order.
     *
     * @return <code>true</code> if, and only if, this state is a final state
     * for a transport order - i.e. FINISHED, FAILED or UNROUTABLE.
     */
    public boolean isFinalState() {
      return this.equals(FINISHED)
          || this.equals(FAILED)
          || this.equals(UNROUTABLE);
    }
  }
}
