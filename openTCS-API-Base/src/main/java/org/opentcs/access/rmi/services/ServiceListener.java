/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

/**
 * Provides callback methods for instances interested in service updates.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ServiceListener {

  /**
   * Notifies a listener that the service is unavailable, i.e. is not in a usable state.
   */
  void onServiceUnavailable();
}
