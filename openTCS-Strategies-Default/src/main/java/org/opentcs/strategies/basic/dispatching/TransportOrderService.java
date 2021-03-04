/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static com.google.common.base.Preconditions.checkState;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Provides service functions for working with transport orders and their states.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderService {

  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;

  @Inject
  public TransportOrderService(@Nonnull LocalKernel kernel) {
    this.kernel = requireNonNull(kernel, "kernel");
  }

  /**
   * Checks if a transport order's dependencies are completely satisfied or not.
   *
   * @param order A reference to the transport order to be checked.
   * @return <code>false</code> if all the order's dependencies are finished (or
   * don't exist any more), else <code>true</code>.
   */
  public boolean hasUnfinishedDependencies(TransportOrder order) {
    requireNonNull(order, "order");

    // Assume that FINISHED orders do not have unfinished dependencies.
    if (order.hasState(TransportOrder.State.FINISHED)) {
      return false;
    }
    // Check if any transport order referenced as a an explicit dependency
    // (really still exists and) is not finished.
    if (order.getDependencies().stream()
        .map(depRef -> kernel.getTCSObject(TransportOrder.class, depRef))
        .anyMatch(dep -> dep != null && !dep.hasState(TransportOrder.State.FINISHED))) {
      return true;
    }

    // Check if the transport order is part of an order sequence and if yes,
    // if it's the next unfinished order in the sequence.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class, order.getWrappingSequence());
      if (!order.getReference().equals(seq.getNextUnfinishedOrder())) {
        return true;
      }
    }
    // All referenced transport orders either don't exist (any more) or have
    // been finished already.
    return false;
  }

  /**
   * Finds transport orders that are ACTIVE and do not have any unfinished dependencies (any more),
   * implicitly marking them as DISPATCHABLE.
   *
   * @return A set of transport orders that are now dispatchable.
   */
  public Set<TransportOrder> findNewDispatchableOrders() {
    Set<TransportOrder> result = new HashSet<>();
    kernel.getTCSObjects(TransportOrder.class).stream()
        .filter(order -> order.hasState(TransportOrder.State.ACTIVE))
        .filter(order -> !hasUnfinishedDependencies(order))
        .forEach(order -> {
          updateTransportOrderState(order.getReference(), TransportOrder.State.DISPATCHABLE);
          result.add(order);
        });
    return result;
  }

  public void updateTransportOrderState(@Nonnull TCSObjectReference<TransportOrder> ref,
                                        @Nonnull TransportOrder.State newState) {
    requireNonNull(ref, "ref");
    requireNonNull(newState, "newState");

    switch (newState) {
      case FINISHED:
        setTOStateFinished(ref);
        break;
      case FAILED:
        setTOStateFailed(ref);
        break;
      default:
        // Set the transport order's state.
        kernel.setTransportOrderState(ref, newState);
    }
  }

  /**
   * Properly sets a transport order to a finished state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFinished(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    // Set the transport order's state.
    kernel.setTransportOrderState(ref, TransportOrder.State.FINISHED);
    TransportOrder order = kernel.getTCSObject(TransportOrder.class, ref);
    // If it is part of an order sequence, we should proceed to its next order.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class,
                                              order.getWrappingSequence());
      // Sanity check: The finished order must be the next one in the sequence;
      // if it is not, something has already gone wrong.
      checkState(ref.equals(seq.getNextUnfinishedOrder()),
                 "Finished TO %s != next unfinished TO %s in sequence %s",
                 ref,
                 seq.getNextUnfinishedOrder(),
                 seq);
      kernel.setOrderSequenceFinishedIndex(seq.getReference(),
                                           seq.getFinishedIndex() + 1);
      // Get an up-to-date copy of the order sequence
      seq = kernel.getTCSObject(OrderSequence.class, seq.getReference());
      // If the sequence is complete and this was its last order, the sequence
      // is also finished.
      if (seq.isComplete() && seq.getNextUnfinishedOrder() == null) {
        kernel.setOrderSequenceFinished(seq.getReference());
        // Reset the processing vehicle's back reference on the sequence.
        kernel.setVehicleOrderSequence(seq.getProcessingVehicle(), null);
      }
    }
  }

  /**
   * Properly sets a transport order to a failed state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFailed(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    TransportOrder failedOrder = kernel.getTCSObject(TransportOrder.class, ref);
    kernel.setTransportOrderState(ref, TransportOrder.State.FAILED);
    // A transport order has failed - check if it's part of an order
    // sequence that we need to take care of.
    if (failedOrder.getWrappingSequence() == null) {
      return;
    }
    OrderSequence sequence = kernel.getTCSObject(OrderSequence.class,
                                                 failedOrder.getWrappingSequence());

    if (sequence.isFailureFatal()) {
      // Mark the sequence as complete to make sure no further orders are
      // added.
      kernel.setOrderSequenceComplete(sequence.getReference());
      // Mark all orders of the sequence that are not in a final state as
      // FAILED.
      sequence.getOrders().stream()
          .map(curRef -> kernel.getTCSObject(TransportOrder.class, curRef))
          .filter(o -> !o.getState().isFinalState())
          .forEach(o -> updateTransportOrderState(o.getReference(), TransportOrder.State.FAILED));
      // Move the finished index of the sequence to its end.
      kernel.setOrderSequenceFinishedIndex(sequence.getReference(),
                                           sequence.getOrders().size() - 1);
    }
    else {
      // Since failure of an order in the sequence is not fatal, increment the
      // finished index of the sequence by one to move to the next order.
      kernel.setOrderSequenceFinishedIndex(sequence.getReference(),
                                           sequence.getFinishedIndex() + 1);
    }
    // The sequence may have changed. Get an up-to-date copy.
    sequence = kernel.getTCSObject(OrderSequence.class, failedOrder.getWrappingSequence());
    // Mark the sequence as finished if there's nothing more to do in it.
    if (sequence.isComplete() && sequence.getNextUnfinishedOrder() == null) {
      kernel.setOrderSequenceFinished(sequence.getReference());
      // If the sequence was assigned to a vehicle, reset its back reference
      // on the sequence to make it available for orders again.
      if (sequence.getProcessingVehicle() != null) {
        kernel.setVehicleOrderSequence(sequence.getProcessingVehicle(), null);
      }
    }
  }

}
