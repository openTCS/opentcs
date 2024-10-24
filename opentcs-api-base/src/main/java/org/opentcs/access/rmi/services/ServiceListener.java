// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

/**
 * Provides callback methods for instances interested in service updates.
 */
public interface ServiceListener {

  /**
   * Notifies a listener that the service is unavailable, i.e. is not in a usable state.
   */
  void onServiceUnavailable();
}
