/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static java.util.Objects.requireNonNull;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import static org.opentcs.util.Assertions.checkState;

/**
 * Implementation of the {@link SharedKernelServicePortal} interface to give access to a shared portal object.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ApplicationPortal
    implements SharedKernelServicePortal {

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
  public ApplicationPortal(KernelServicePortal portal,
                           ApplicationPortalProvider portalProvider,
                           Object registeredToken) {
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
