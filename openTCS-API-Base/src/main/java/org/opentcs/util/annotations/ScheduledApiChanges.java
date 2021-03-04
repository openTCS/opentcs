/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container annotation for {@link ScheduledApiChange}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Target({CONSTRUCTOR, FIELD, METHOD, TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ScheduledApiChanges {

  /**
   * Returns the contained schedules.
   *
   * @return The contained schedules.
   */
  ScheduledApiChange[] value();
}
