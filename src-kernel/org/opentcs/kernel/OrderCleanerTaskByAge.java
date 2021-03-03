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
class OrderCleanerTaskByAge
    extends OrderCleanerTask {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(OrderCleanerTaskByAge.class.getName());
  /**
   * The minimum age of orders to remove if orderSweepType is BY_AGE.
   */
  private final int orderSweepAge;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
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
    // Get all transport order, sorted by their age.
    Set<TransportOrder> orders = new TreeSet<>(TransportOrder.ageComparator);
    orders.addAll(kernel().getTCSObjects(TransportOrder.class));
    Iterator<TransportOrder> orderIter = orders.iterator();
    boolean finished = false;
    while (orderIter.hasNext() && !finished) {
      TransportOrder curOrder = orderIter.next();
      long ageOfCurrentOrder
          = System.currentTimeMillis() - curOrder.getCreationTime();
      if (curOrder.getState().isFinalState()
          && ageOfCurrentOrder > orderSweepAge) {
        log.info("Removing old order: " + curOrder);
        try {
          kernel().removeTCSObject(curOrder.getReference());
        }
        catch (ObjectUnknownException exc) {
          log.log(Level.WARNING, "Order vanished", exc);
        }
      }
      if (ageOfCurrentOrder < orderSweepAge) {
        finished = true;
      }
    }
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
