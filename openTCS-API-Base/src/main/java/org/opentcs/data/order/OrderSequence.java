/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * Describes a process spanning multiple transport orders which are to be
 * executed one after the other by the same vehicle.
 * <p>
 * The most important rules for <code>OrderSequence</code> processing are:
 * </p>
 * <ul>
 * <li>Only <code>TransportOrder</code>s that have not yet been activated may be
 * added to an <code>OrderSequence</code>. Allowing them to be added at a later
 * point of time would imply that, due to concurrency in the kernel, a transport
 * order might happen to be dispatched at the same time or shortly after it is
 * added to a sequence, regardless of if its predecessors in the sequence have
 * already been finished or not.</li>
 * <li>The <em>intendedVehicle</em> of a <code>TransportOrder</code> being added
 * to an <code>OrderSequence</code> must be the same as that of the sequence
 * itself. If it is <code>null</code> in the sequence (and the orders belonging
 * to it), a vehicle that will process all orders in the sequence will be chosen
 * automatically once the first order in the sequence is dispatched.</li>
 * <li>If an <code>OrderSequence</code> is marked as <em>complete</em> and all
 * <code>TransportOrder</code>s belonging to it have arrived in state
 * <code>FINISHED</code> or <code>FAILED</code>, it will be marked as
 * <em>finished</em> implicitly.
 * <li>If a <code>TransportOrder</code> belonging to an
 * <code>OrderSequence</code> fails and the sequence's <em>failureFatal</em>
 * flag is set, all subsequent orders in the sequence will automatically be
 * considered (and marked as) failed, too, and the order sequence will
 * implicitly be marked as <em>complete</em> (and <em>finished</em>).</li>
 * <li>If an <code>OrderSequence</code> is removed from the kernel, the
 * contained <code>TransportOrder</code>s will remain in the kernel (with their
 * references on the wrapping sequence cleared) and may be dispatched to
 * vehicles.</li>
 * <li>If a <code>TransportOrder</code> belonging to an
 * <code>OrderSequence</code> is removed from the kernel, the reference on it in
 * the sequence will be removed as well without any further action. (This
 * behaviour is likely to change in future versions.)</li>
 * </ul>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequence
    extends TCSObject<OrderSequence>
    implements Serializable, Cloneable {

  /**
   * Transport orders belonging to this sequence that still need to be
   * processed.
   */
  private List<TCSObjectReference<TransportOrder>> orders = new LinkedList<>();
  /**
   * The index of the order that was last finished in the sequence. -1 if none
   * was finished, yet.
   */
  private int finishedIndex = -1;
  /**
   * A flag indicating whether this order sequence is complete and will not be
   * extended by more orders.
   */
  private boolean complete;
  /**
   * A flag indicating whether this order sequence has been processed
   * completely.
   */
  private boolean finished;
  /**
   * Whether the failure of one order in this sequence is fatal to all
   * subsequent orders.
   */
  private boolean failureFatal;
  /**
   * The vehicle that is intended to process this order sequence. If this
   * sequence is free to be processed by any vehicle, this is <code>null</code>.
   */
  private TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * The vehicle processing this order sequence, or <code>null</code>, if no
   * vehicle has been assigned to it, yet.
   */
  private TCSObjectReference<Vehicle> processingVehicle;

  /**
   * Creates a new OrderSequence.
   *
   * @param objectID This sequence's ID.
   * @param name This sequence's name.
   */
  public OrderSequence(int objectID, String name) {
    super(objectID, name);
  }

  /**
   * Returns the list of orders making up this sequence.
   *
   * @return The list of orders making up this sequence.
   */
  public List<TCSObjectReference<TransportOrder>> getOrders() {
    return new LinkedList<>(orders);
  }

  /**
   * Adds an order to this sequence.
   *
   * @param newOrder The new order.
   * @throws IllegalArgumentException If this sequence is already marked as
   * <em>complete</em> or if this sequence already contains the given order.
   */
  public void addOrder(TCSObjectReference<TransportOrder> newOrder)
      throws IllegalArgumentException {
    requireNonNull(newOrder, "newOrder");
    checkArgument(!complete, "Sequence complete, cannot add order");
    checkArgument(!orders.contains(newOrder),
                  "Sequence already contains order %s",
                  newOrder);
    orders.add(newOrder);
  }

  /**
   * Removes an order from this sequence.
   *
   * @param order The order to be removed.
   */
  public void removeOrder(TCSObjectReference<TransportOrder> order) {
    requireNonNull(order, "order is null");
    orders.remove(order);
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
   * Sets the index of the order that was last finished in the sequence.
   *
   * @param finishedIndex The new index.
   */
  public void setFinishedIndex(int finishedIndex) {
    this.finishedIndex = checkInRange(finishedIndex, 0, orders.size() - 1, "finishedIndex");
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
   * Sets this sequence's <em>complete</em> flag.
   */
  public void setComplete() {
    this.complete = true;
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
   * Sets this sequence's <em>finished</em> flag.
   */
  public void setFinished() {
    this.finished = true;
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
   * Sets this sequence's <em>failureFatal</em> flag.
   *
   * @param failureFatal The new value.
   */
  public void setFailureFatal(boolean failureFatal) {
    this.failureFatal = failureFatal;
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
   * Sets a reference to the vehicle that is intended to process this sequence.
   *
   * @param vehicle The reference to the vehicle intended to process this
   * sequence.
   */
  public void setIntendedVehicle(TCSObjectReference<Vehicle> vehicle) {
    intendedVehicle = vehicle;
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
   * Sets a reference to the vehicle currently processing this sequence.
   *
   * @param vehicle The reference to the vehicle currently processing this
   * sequence.
   */
  public void setProcessingVehicle(TCSObjectReference<Vehicle> vehicle) {
    processingVehicle = vehicle;
  }

  @Override
  public OrderSequence clone() {
    OrderSequence clone = (OrderSequence) super.clone();
    clone.orders = new LinkedList<>();
    clone.orders.addAll(this.orders);
    return clone;
  }
}
