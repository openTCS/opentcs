/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.opentcs.access.Kernel;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Indicates in which kernel states the annotated query is available.
 *
 * @see org.opentcs.access.Kernel.State
 * @see org.opentcs.access.Kernel#getState()
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Instead of queries, explicit service calls should be used.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Deprecated
@ScheduledApiChange(when = "5.0")
public @interface Availability {

  /**
   * Returns the kernel states in which the annotated query is available.
   *
   * @return the kernel states in which the annotated query is available.
   */
  Kernel.State[] value();
}
