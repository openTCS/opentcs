/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Indicates which permissions a client needs to have to be allowed to call an
 * annotated method.
 *
 * @see RemoteKernel
 * @see org.opentcs.data.user.UserPermission
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Call permissions are implementation-specific.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CallPermissions {

  /**
   * Returns the permissions required to call the annotated method.
   *
   * @return the permissions required to call the annotated method.
   */
  org.opentcs.data.user.UserPermission[] value();
}
