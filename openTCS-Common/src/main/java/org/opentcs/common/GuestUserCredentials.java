/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

/**
 * Defines the credentials for a guest user account.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface GuestUserCredentials {

  /**
   * The default/guest user name.
   */
  String USER = "Alice";
  /**
   * The default/guest password.
   */
  String PASSWORD = "xyz";
}
