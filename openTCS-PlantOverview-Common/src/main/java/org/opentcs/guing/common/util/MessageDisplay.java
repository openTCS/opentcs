/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import org.opentcs.data.notification.UserNotification;

/**
 * Implementations of this interface provide a way to display messages to the
 * user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface MessageDisplay {

  /**
   * Displays the given message.
   *
   * @param message The message.
   */
  void display(UserNotification message);
}
