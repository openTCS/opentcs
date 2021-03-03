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
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.util.CyclicTask;

/**
 * A task that periodically removes old orders if there are too many of them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class OrderCleanerTask
    extends CyclicTask {

  /**
   * The kernel we scan regularly.
   */
  private final LocalKernel kernel;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
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
}
