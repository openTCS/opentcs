// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static java.util.Objects.requireNonNull;
import static org.opentcs.data.order.OrderSequenceHistoryCodes.SEQUENCE_COMPLETED;
import static org.opentcs.data.order.OrderSequenceHistoryCodes.SEQUENCE_CREATED;
import static org.opentcs.data.order.OrderSequenceHistoryCodes.SEQUENCE_FINISHED;
import static org.opentcs.data.order.OrderSequenceHistoryCodes.SEQUENCE_ORDER_APPENDED;
import static org.opentcs.data.order.OrderSequenceHistoryCodes.SEQUENCE_PROCESSING_VEHICLE_CHANGED;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * Describes a process spanning multiple {@link TransportOrder}s which are to be executed
 * subsequently by the same {@link Vehicle}.
 * <p>
 * The most important rules for order sequence processing are:
 * </p>
 * <ul>
 * <li>Only transport orders that have not yet been activated may be added to an order sequence.
 * Allowing them to be added at a later point of time would imply that, due to concurrency in the
 * kernel, a transport order might happen to be dispatched at the same time or shortly after it is
 * added to a sequence, regardless of if its predecessors in the sequence have already been finished
 * or not.</li>
 * <li>The <em>intendedVehicle</em> of a transport order being added to an order sequence must be
 * the same as that of the sequence itself.
 * If it is <code>null</code> in the sequence, a vehicle that will process all orders in the
 * sequence will be chosen automatically once the first order in the sequence is dispatched.</li>
 * <li>If an order sequence is marked as <em>complete</em> and all transport orders belonging to it
 * have arrived in state <code>FINISHED</code> or <code>FAILED</code>, it will be marked as
 * <em>finished</em> implicitly.</li>
 * <li>If a transport order belonging to an order sequence fails and the sequence's
 * <em>failureFatal</em> flag is set, all subsequent orders in the sequence will automatically be
 * considered (and marked as) failed, too, and the order sequence will implicitly be marked as
 * <em>complete</em> (and <em>finished</em>).</li>
 * </ul>
 */
public class OrderSequence
    extends
      TCSObject<OrderSequence>
    implements
      Serializable {

  /**
   * The type of this order sequence.
   * An order sequence and all transport orders it contains share the same type.
   */
  @Nonnull
  private final String type;
  /**
   * Transport orders belonging to this sequence that still need to be processed.
   */
  private final List<TCSObjectReference<TransportOrder>> orders;
  /**
   * The index of the order that was last finished in the sequence.
   * -1 if none was finished, yet.
   */
  private final int finishedIndex;
  /**
   * Indicates whether this order sequence is complete and will not be extended by more orders.
   */
  private final boolean complete;
  /**
   * Indicates whether this order sequence has been processed completely.
   */
  private final boolean finished;
  /**
   * Indicates whether the failure of one order in this sequence is fatal to all subsequent orders.
   */
  private final boolean failureFatal;
  /**
   * The vehicle that is intended to process this order sequence.
   * If this sequence is free to be processed by any vehicle, this is <code>null</code>.
   */
  private final TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * The vehicle processing this order sequence, or <code>null</code>, if no vehicle has been
   * assigned to it, yet.
   */
  private final TCSObjectReference<Vehicle> processingVehicle;
  /**
   * The point of time at which this order sequence was created.
   */
  private final Instant creationTime;
  /**
   * The point of time at which this order sequence has been processed completely.
   */
  private final Instant finishedTime;

  /**
   * Creates a new OrderSequence.
   *
   * @param name This sequence's name.
   */
  public OrderSequence(String name) {
    super(
        name,
        new HashMap<>(),
        new ObjectHistory().withEntryAppended(new ObjectHistory.Entry(SEQUENCE_CREATED))
    );
    this.type = OrderConstants.TYPE_NONE;
    this.orders = List.of();
    this.creationTime = Instant.EPOCH;
    this.finishedIndex = -1;
    this.complete = false;
    this.finished = false;
    this.failureFatal = false;
    this.finishedTime = Instant.MAX;
    this.intendedVehicle = null;
    this.processingVehicle = null;
  }

  private OrderSequence(
      String name,
      Map<String, String> properties,
      ObjectHistory history,
      String type,
      TCSObjectReference<Vehicle> intendedVehicle,
      List<TCSObjectReference<TransportOrder>> orders,
      Instant creationTime,
      int finishedIndex,
      boolean complete,
      boolean failureFatal,
      boolean finished,
      Instant finishedTime,
      TCSObjectReference<Vehicle> processingVehicle
  ) {
    super(name, properties, history);
    this.type = requireNonNull(type, "type");
    this.intendedVehicle = intendedVehicle;
    this.orders = new ArrayList<>(requireNonNull(orders, "orders"));
    this.creationTime = requireNonNull(creationTime, "creationTime");
    this.finishedIndex = finishedIndex;
    this.complete = complete;
    this.failureFatal = failureFatal;
    this.finished = finished;
    this.finishedTime = requireNonNull(finishedTime, "finishedTime");
    this.processingVehicle = processingVehicle;
  }

  @Override
  public OrderSequence withProperty(String key, String value) {
    return new OrderSequence(
        getName(),
        propertiesWith(key, value),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  @Override
  public OrderSequence withProperties(Map<String, String> properties) {
    return new OrderSequence(
        getName(),
        properties,
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  @Override
  public TCSObject<OrderSequence> withHistoryEntry(ObjectHistory.Entry entry) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory().withEntryAppended(entry),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  @Override
  public TCSObject<OrderSequence> withHistory(ObjectHistory history) {
    return new OrderSequence(
        getName(),
        getProperties(),
        history,
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns this order sequence's type.
   *
   * @return This order sequence's type.
   */
  @Nonnull
  public String getType() {
    return type;
  }

  /**
   * Creates a copy of this object, with the given type.
   *
   * @param type The type to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withType(String type) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns the list of orders making up this sequence.
   *
   * @return The list of orders making up this sequence.
   */
  public List<TCSObjectReference<TransportOrder>> getOrders() {
    return Collections.unmodifiableList(orders);
  }

  /**
   * Creates a copy of this object, with the given order.
   *
   * @param order The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withOrder(TCSObjectReference<TransportOrder> order) {
    checkArgument(!complete, "Sequence complete, cannot add order");
    checkArgument(!orders.contains(order), "Sequence already contains order %s", order);

    return new OrderSequence(
        getName(),
        getProperties(),
        historyForAppendedOrder(order),
        type,
        intendedVehicle,
        ordersWithAppended(order),
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns this order sequence's creation time.
   *
   * @return This order sequence's creation time.
   */
  @Nonnull
  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param creationTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withCreationTime(
      @Nonnull
      Instant creationTime
  ) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns the next order in the sequence that hasn't been finished, yet.
   *
   * @return <code>null</code> if this sequence has been finished already or
   * currently doesn't have any unfinished orders, else the order after the one
   * that was last finished.
   */
  public TCSObjectReference<TransportOrder> getNextUnfinishedOrder() {
    // If the whole sequence has been finished already, return null.
    if (finished) {
      return null;
    }
    // If the sequence has not been marked as finished but the last order in the
    // list has been, return null, too.
    else if (finishedIndex + 1 >= orders.size()) {
      return null;
    }
    // Otherwise just get the order after the one that was last finished.
    else {
      return orders.get(finishedIndex + 1);
    }
  }

  /**
   * Returns the index of the order that was last finished in the sequence, or
   * -1, if none was finished, yet.
   *
   * @return the index of the order that was last finished in the sequence.
   */
  public int getFinishedIndex() {
    return finishedIndex;
  }

  /**
   * Creates a copy of this object, with the given finished index.
   *
   * @param finishedIndex The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withFinishedIndex(int finishedIndex) {
    checkInRange(finishedIndex, 0, orders.size() - 1, "finishedIndex");

    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Indicates whether this order sequence is complete and will not be extended
   * by more orders.
   *
   * @return <code>true</code> if, and only if, this order sequence is complete
   * and will not be extended by more orders.
   */
  public boolean isComplete() {
    return complete;
  }

  /**
   * Creates a copy of this object, with the given complete flag.
   *
   * @param complete The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withComplete(boolean complete) {
    return new OrderSequence(
        getName(),
        getProperties(),
        historyForComplete(complete),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Indicates whether this order sequence has been processed completely.
   * (Note that <em>processed completely</em> does not necessarily mean
   * <em>finished successfully</em>; it is possible that one or more transport
   * orders belonging to this sequence have failed.)
   *
   * @return <code>true</code> if, and only if, this order sequence has been
   * processed completely.
   */
  public boolean isFinished() {
    return finished;
  }

  /**
   * Creates a copy of this object, with the given finished flag.
   *
   * @param finished The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withFinished(boolean finished) {
    return new OrderSequence(
        getName(),
        getProperties(),
        historyForFinished(finished),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Indicates whether the failure of a single order in this sequence implies
   * that all subsequent orders in this sequence are to be considered failed,
   * too.
   *
   * @return <code>true</code> if, and only if, the failure of an order in this
   * sequence implies the failure of all subsequent orders.
   */
  public boolean isFailureFatal() {
    return failureFatal;
  }

  /**
   * Creates a copy of this object, with the given failure-fatal flag.
   *
   * @param failureFatal The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withFailureFatal(boolean failureFatal) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns the point of time at which this order sequence has been processed completely.
   * If the order sequence has not been finished, yet, {@link Instant#MAX} is returned.
   *
   * @return The point of time at which this order sequence has been processed competely,
   * or {@link Instant#MAX}, if the order sequence has not been processed completely, yet.
   */
  @Nonnull
  public Instant getFinishedTime() {
    return finishedTime;
  }

  /**
   * Creates a copy of this object, with the given finished time.
   *
   * @param finishedTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withFinishedTime(
      @Nonnull
      Instant finishedTime
  ) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns a reference to the vehicle that is intended to process this
   * order sequence.
   *
   * @return A reference to the vehicle that is intended to process this
   * order sequence. If this sequence is free to be processed by any vehicle,
   * <code>null</code> is returned.
   */
  public TCSObjectReference<Vehicle> getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Creates a copy of this object, with the given intended vehicle.
   *
   * @param intendedVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withIntendedVehicle(TCSObjectReference<Vehicle> intendedVehicle) {
    return new OrderSequence(
        getName(),
        getProperties(),
        getHistory(),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  /**
   * Returns a reference to the vehicle currently processing this sequence.
   *
   * @return A reference to the vehicle currently processing this sequence. If
   * this sequence has not been processed, yet, <code>null</code> is
   * returned.
   */
  public TCSObjectReference<Vehicle> getProcessingVehicle() {
    return processingVehicle;
  }

  /**
   * Creates a copy of this object, with the given processing vehicle.
   *
   * @param processingVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequence withProcessingVehicle(TCSObjectReference<Vehicle> processingVehicle) {
    return new OrderSequence(
        getName(),
        getProperties(),
        historyForNewProcessingVehicle(processingVehicle),
        type,
        intendedVehicle,
        orders,
        creationTime,
        finishedIndex,
        complete,
        failureFatal,
        finished,
        finishedTime,
        processingVehicle
    );
  }

  @Override
  public String toString() {
    return "OrderSequence{"
        + "name=" + getName()
        + ", type=" + type
        + ", failureFatal=" + failureFatal
        + ", complete=" + complete
        + ", finished=" + finished
        + ", finishedIndex=" + finishedIndex
        + ", intendedVehicle=" + intendedVehicle
        + ", processingVehicle=" + processingVehicle
        + ", orders=" + orders
        + ", properties=" + getProperties()
        + ", history=" + getHistory()
        + '}';
  }

  private List<TCSObjectReference<TransportOrder>> ordersWithAppended(
      @Nonnull
      TCSObjectReference<TransportOrder> order
  ) {
    List<TCSObjectReference<TransportOrder>> result = new ArrayList<>(orders.size() + 1);
    result.addAll(orders);
    result.add(order);
    return result;
  }

  private ObjectHistory historyForNewProcessingVehicle(TCSObjectReference<Vehicle> ref) {
    return Objects.equals(ref, processingVehicle)
        ? getHistory()
        : getHistory().withEntryAppended(
            new ObjectHistory.Entry(
                SEQUENCE_PROCESSING_VEHICLE_CHANGED,
                ref == null ? List.of("") : List.of(ref.getName())
            )
        );
  }

  private ObjectHistory historyForAppendedOrder(TCSObjectReference<TransportOrder> ref) {
    return getHistory().withEntryAppended(
        new ObjectHistory.Entry(
            SEQUENCE_ORDER_APPENDED,
            ref == null ? List.of("") : List.of(ref.getName())
        )
    );
  }

  private ObjectHistory historyForFinished(boolean finished) {
    return finished
        ? getHistory().withEntryAppended(
            new ObjectHistory.Entry(SEQUENCE_FINISHED)
        )
        : getHistory();
  }

  private ObjectHistory historyForComplete(boolean complete) {
    return complete
        ? getHistory().withEntryAppended(
            new ObjectHistory.Entry(SEQUENCE_COMPLETED)
        )
        : getHistory();
  }
}
