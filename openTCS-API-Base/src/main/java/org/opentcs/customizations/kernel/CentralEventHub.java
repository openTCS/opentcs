/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.kernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Annotation type to mark a central injectable <code>EventHub</code>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link ApplicationEventBus} instead.
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@ScheduledApiChange(when = "5.0")
public @interface CentralEventHub {
  // Nothing here.
}
