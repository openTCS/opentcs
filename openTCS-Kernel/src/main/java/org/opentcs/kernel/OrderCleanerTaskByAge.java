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
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * A task that periodically removes orders that are older than a certain age.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderCleanerTaskByAge
    extends OrderCleanerTask {

  /**
   * The minimum age of orders to remove if orderSweepType is BY_AGE.
   */
  private final int orderSweepAge;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
   * @param orderSweepInterval The interval between sweeps, in milliseconds.
   * @param orderSweepAge The maximum age of orders to be kept, in milliseconds.
   */
  @Inject
  public OrderCleanerTaskByAge(LocalKernel kernel,
                               @SweepInterval long orderSweepInterval,
                               @SweepAge int orderSweepAge) {
    super(kernel, orderSweepInterval);
    this.orderSweepAge = orderSweepAge;
  }

  @Override
  protected void runActualTask() {
    Set<Candidate> candidates = new TreeSet<>();
    // Get all transport orders in a final state that do NOT belong to a
    // sequence.
    kernel().getTCSObjects(TransportOrder.class).stream()
        .filter(order -> order.getState().isFinalState())
        .filter(order -> order.getWrappingSequence() == null)
        .forEach(order -> candidates.add(createEntry(order)));
    // Get all order sequences that have been finished.
    kernel().getTCSObjects(OrderSequence.class).stream()
        .filter(seq -> seq.isFinished())
        .forEach(seq -> candidates.add(createEntry(seq)));

    // Candidates that are created before this point of time should be removed.
    long creationTimeThreshold = System.currentTimeMillis() - orderSweepAge;

    candidates.stream()
        .filter(cand -> cand.getCreationTime() < creationTimeThreshold)
        .forEach(cand -> cand.removeOrders());
  }

  /**
   * Annotation type for injecting the sweep age.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface SweepAge {
    // Nothing here.
  }
}
