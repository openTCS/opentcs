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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API detail (class, method, field) that is scheduled for an incompatible change.
 * <p>
 * <em>This annotation should not be used outside the source code of the openTCS project itself. It
 * should not be considered part of the public API.</em>
 * </p>
 * <p>
 * This annotation is intended to be used as a supplement for @Deprecated for two purposes:
 * </p>
 * <ol>
 * <li>For users of the openTCS API to gain knowledge about upcoming changes.</li>
 * <li>For openTCS developers to easily find pending changes during pre-release cleanups.</li>
 * </ol>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Target({CONSTRUCTOR, FIELD, METHOD, TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
@Repeatable(ScheduledApiChanges.class)
public @interface ScheduledApiChange {

  /**
   * Returns the date or version at which the API is scheduled to be changed.
   *
   * @return The date or version at which the API is scheduled to be changed.
   */
  String when();

  /**
   * Returns optional details about the scheduled change.
   *
   * @return Optional details about the scheduled change.
   */
  String details() default "";
}
