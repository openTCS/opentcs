// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container annotation for {@link ScheduledApiChange}.
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
