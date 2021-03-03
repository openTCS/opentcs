/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * A task that periodically removes orders if there are too many of them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderCleanerTaskByAmount
    extends OrderCleanerTask {

  /**
   * The number of orders we keep.
   */
  private final int orderSweepThreshold;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
   * @param orderSweepInterval The interval betweeen sweeps, in milliseconds.
   * @param orderSweepThreshold The number of removable orders to be kept.
   */
  @Inject
  public OrderCleanerTaskByAmount(LocalKernel kernel,
                                  @SweepInterval long orderSweepInterval,
                                  @SweepThreshold int orderSweepThreshold) {
    super(kernel, orderSweepInterval);
    this.orderSweepThreshold = orderSweepThreshold;
  }

  @Override
  protected void runActualTask() {
    Set<Candidate> candidates = new TreeSet<>();
    Set<TransportOrder> orders = kernel().getTCSObjects(TransportOrder.class);
    int removeCount = orders.size() - orderSweepThreshold;
    // Get all transport orders in a final state that do NOT belong to a
    // sequence.
    orders.stream()
        .filter(order -> order.getState().isFinalState())
        .filter(order -> order.getWrappingSequence() == null)
        .forEach(order -> candidates.add(createEntry(order)));
    // Get all order sequences that have been finished.
    kernel().getTCSObjects(OrderSequence.class).stream()
        .filter(seq -> seq.isFinished())
        .forEach(seq -> candidates.add(createEntry(seq)));

    Iterator<Candidate> candIter = candidates.iterator();
    while (removeCount > 0 && candIter.hasNext()) {
      removeCount -= candIter.next().removeOrders();
    }
  }

  /**
   * Annotation type for injecting the sweep threshold.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface SweepThreshold {
    // Nothing here.
  }
}
