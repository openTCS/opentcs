/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Pools access to a kernel for multiple clients.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link SharedKernelServicePortalProvider} instead.
 */
@Deprecated
public interface SharedKernelProvider {

  /**
   * Registers the given client object with this access pool.
   *
   * @param client The client to be registered.
   * @return <code>true</code> if, and only if, the client was successfully
   * registered and wasn't registered before.
   * @deprecated Use {@link #register()} to register and returned {@link SharedKernelClient}
   * to provide kernel and unregister instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  boolean register(Object client);

  /**
   * Creates and registers a new client with this access pool.
   * This is a convenience method that supports try-with-ressources and does not require a
   * preexisting client.
   *
   * @return the new client
   * @throws org.opentcs.access.rmi.KernelUnavailableException in case of connection failure with
   * the Kernel.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default SharedKernelClient register()
      throws org.opentcs.access.rmi.KernelUnavailableException {
    throw new org.opentcs.access.rmi.KernelUnavailableException("No default implementation.");
  }

  /**
   * Unregisters the given client object with this access pool.
   *
   * @param client The client to be unregistered.
   * @return <code>true</code> if, and only if, the client was successfully
   * unregistered and was registered before.
   * @deprecated Use {@link #register()} to register and returned {@link SharedKernelClient}
   * to provide kernel and unregister instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  boolean unregister(Object client);

  /**
   * Returns a reference to the pooled kernel.
   *
   * @return A reference to the pooled kernel.
   * @deprecated Use {@link #register()} to register and returned {@link SharedKernelClient}
   * to provide kernel and unregister instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Kernel getKernel();

  /**
   * Returns a reference to the pooled portal.
   *
   * @return A reference to the pooled portal.
   * @deprecated Use {@link #register()} to register and the returned
   * {@link SharedKernelServicePortal} to provide a portal and unregister instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  KernelServicePortal getPortal();
  
  /**
   * Checks whether a kernel reference is currently being shared.
   *
   * @return <code>true</code> if, and only if, a kernel reference is currently
   * being shared, meaning that at least one client is registered and a usable
   * kernel reference exists.
   */
  boolean kernelShared();

  /**
   * Returns a description for the kernel currently being shared.
   *
   * @return A description for the kernel currently being shared, or the empty
   * string, if none is currently being shared.
   */
  String getKernelDescription();
}
