// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access;

import org.opentcs.components.kernel.services.ServiceUnavailableException;

/**
 * Pools access to a {@link KernelServicePortal} instance for multiple clients.
 */
public interface SharedKernelServicePortalProvider {

  /**
   * Creates and registers a new client with this access pool.
   * This is a convenience method that supports try-with-ressources and does not require a
   * preexisting client.
   *
   * @return The {@link SharedKernelServicePortal}.
   * @throws ServiceUnavailableException in case of connection falure with the portal.
   */
  SharedKernelServicePortal register()
      throws ServiceUnavailableException;

  /**
   * Checks whether a kernel reference is currently being shared.
   *
   * @return {@code true} if, and only if, a portal reference is currently being shared, meaning
   * that at least one client is registered and a usable portal reference exists.
   */
  boolean portalShared();

  /**
   * Returns a description for the portal currently being shared.
   *
   * @return A description for the portal currently being shared, or the empty string, if none is
   * currently being shared.
   */
  String getPortalDescription();
}
