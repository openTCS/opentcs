/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.Comparators;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that periodically removes orders in a final state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class OrderCleanerTask
    extends CyclicTask {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(OrderCleanerTaskByAmount.class);
  /**
   * The kernel we scan regularly.
   */
  private final LocalKernel kernel;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
   * @param orderSweepInterval The interval between sweeps (in milliseconds).
   */
  @Inject
  public OrderCleanerTask(LocalKernel kernel,
                          @SweepInterval long orderSweepInterval) {
    super(orderSweepInterval);
    this.kernel = requireNonNull(kernel, "kernel");
  }

  /**
   * Returns the kernel.
   *
   * @return The kernel.
   */
  protected LocalKernel kernel() {
    return kernel;
  }

  /**
   * Creates a candidate for the given order sequence.
   *
   * @param seq The sequence.
   * @return A candidate for the given order sequence.
   */
  protected Candidate createEntry(OrderSequence seq) {
    List<TransportOrder> seqOrders = new LinkedList<>();
    seq.getOrders().stream()
        .map(orderRef -> kernel().getTCSObject(TransportOrder.class,
                                               orderRef))
        .forEachOrdered(seqOrders::add);

    return new SequenceCandidate(seq, seqOrders);
  }

  /**
   * Creates a candidate for the given transport order.
   *
   * @param order The order.
   * @return A candidate for the given transport order.
   */
  protected Candidate createEntry(TransportOrder order) {
    return new OrderCandidate(order);
  }

  /**
   * Specifies how the cleanup task should decide which orders to remove from
   * the pool in each run.
   */
  public static enum OrderSweepType {

    /**
     * Remove finalized orders if the whole number of orders exceeds a certain
     * amount.
     */
    BY_AMOUNT,
    /**
     * Remove finalized orders that have exceeded a certain age.
     */
    BY_AGE
  }

  /**
   * Annotation type for injecting the sweep interval.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface SweepInterval {
    // Nothing here.
  }

  /**
   * A candidate entry for removing one or more orders.
   */
  protected abstract class Candidate
      implements Comparable<Candidate> {

    @Override
    public int compareTo(Candidate o) {
      return Long.signum(getCreationTime() - o.getCreationTime());
    }

    /**
     * Returns the creation time stamp(s) of the order(s) of this candidate.
     * If multiple orders belong to this candidate, the latest creation time
     * stamp is returned.
     *
     * @return The creation time of the order(s) of this candidate.
     */
    protected abstract long getCreationTime();

    /**
     * Removes the order(s) of this candidate and returns the number of removed
     * orders.
     *
     * @return The number of removed orders.
     */
    protected abstract int removeOrders();

    /**
     * Removes the referenced transport order.
     *
     * @param orderRef The order.
     * @return 1 if the order was removed, 0 if it wasn't.
     */
    protected int removeSingleOrder(TCSObjectReference<TransportOrder> orderRef) {
      try {
        log.info("Removing order: " + orderRef);
        kernel().removeTCSObject(orderRef);
        return 1;
      }
      catch (ObjectUnknownException exc) {
        log.warn("Order vanished", exc);
        return 0;
      }
    }
  }

  /**
   * A candidate for a single order.
   */
  private class OrderCandidate
      extends Candidate {

    /**
     * The transport order.
     */
    private final TransportOrder order;

    /**
     * Creates a new instance.
     *
     * @param order The transport order.
     */
    private OrderCandidate(TransportOrder order) {
      this.order = requireNonNull(order, "order");
    }

    @Override
    protected long getCreationTime() {
      return order.getCreationTime();
    }

    @Override
    protected int removeOrders() {
      return removeSingleOrder(order.getReference());
    }
  }

  /**
   * A candidate for an order sequence.
   */
  private class SequenceCandidate
      extends Candidate {

    /**
     * The order sequence.
     */
    private final OrderSequence sequence;
    /**
     * The list of orders in the sequence.
     */
    private final List<TransportOrder> orders;
    /**
     * The creation time of the youngest order in the sequence (not necessarily
     * the last order in the sequence).
     */
    private final long youngestCreationTime;

    /**
     * Creates a new instance.
     *
     * @param sequence The sequence.
     * @param orders  The orders in the sequence.
     */
    private SequenceCandidate(OrderSequence sequence,
                              List<TransportOrder> orders) {
      this.sequence = requireNonNull(sequence, "sequence");
      this.orders = requireNonNull(orders, "orders");
      checkArgument(!orders.isEmpty(), "orders is empty");
      youngestCreationTime = orders.stream()
          .max(Comparators.ordersByAge()).get().getCreationTime();
    }

    @Override
    protected long getCreationTime() {
      return youngestCreationTime;
    }

    @Override
    protected int removeOrders() {
      int result = 0;
      // Remove all enclosed orders.
      for (TransportOrder curOrder : orders) {
        result += removeSingleOrder(curOrder.getReference());
      }
      // Remove the sequence itself.
      try {
        kernel().removeTCSObject(sequence.getReference());
      }
      catch (ObjectUnknownException exc) {
        log.warn("Sequence vanished", exc);
      }
      return result;
    }
  }
}
