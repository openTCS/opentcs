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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.order.TransportOrder;

/**
 * A task that periodically removes old orders if there are too many of them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderCleanerTaskByAmount
    extends OrderCleanerTask {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(OrderCleanerTaskByAmount.class.getName());
  /**
   * The number of orders we keep if orderSweepType is BY_AMOUNT.
   */
  private final int orderSweepThreshold;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
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
    // Get all transport order, sorted by their age.
    Set<TransportOrder> orders = new TreeSet<>(TransportOrder.ageComparator);
    orders.addAll(kernel().getTCSObjects(TransportOrder.class));
    int removeCount = orders.size() - orderSweepThreshold;
    Iterator<TransportOrder> orderIter = orders.iterator();
    while (removeCount > 0 && orderIter.hasNext()) {
      TransportOrder curOrder = orderIter.next();
      if (curOrder.getState().isFinalState()) {
        log.info("Removing old order: " + curOrder);
        try {
          kernel().removeTCSObject(curOrder.getReference());
        }
        catch (ObjectUnknownException exc) {
          log.log(Level.WARNING, "Order vanished", exc);
        }
        removeCount--;
      }
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
