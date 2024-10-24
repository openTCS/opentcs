// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkState;

import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;

/**
 * Implementation of the {@link SharedKernelServicePortal} interface to give access to a shared
 * portal object.
 */
public class ApplicationPortal
    implements
      SharedKernelServicePortal {

  /**
   * The portal.
   */
  private final KernelServicePortal portal;
  /**
   * The portal provider instance that this client is registered at.
   */
  private final ApplicationPortalProvider sharedPortalProvider;
  /**
   * The object registered with the provider.
   */
  private final Object registeredToken;
  /**
   * Indicates whether this instance is closed or not.
   */
  private boolean closed;

  /**
   * Creates a new instance.
   *
   * @param portal The shared portal instance.
   * @param portalProvider The provider this client is registered with.
   * @param registeredToken The token that is actually registered with the provider.
   */
  public ApplicationPortal(
      KernelServicePortal portal,
      ApplicationPortalProvider portalProvider,
      Object registeredToken
  ) {
    this.portal = requireNonNull(portal, "portal");
    this.sharedPortalProvider = requireNonNull(portalProvider, "portalProvider");
    this.registeredToken = requireNonNull(registeredToken, "registeredToken");
  }

  @Override
  public void close() {
    if (isClosed()) {
      return;
    }

    sharedPortalProvider.unregister(registeredToken);
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public KernelServicePortal getPortal()
      throws IllegalStateException {
    checkState(!isClosed(), "Closed already.");

    return portal;
  }
}
