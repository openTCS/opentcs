/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

/**
 * Implementations of this interface pool access to a kernel for multiple
 * clients.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface SharedKernelProvider {

  /**
   * Registers the given client object with this access pool.
   *
   * @param client The client to be registered.
   * @return <code>true</code> if, and only if, the client was successfully
   * registered and wasn't registered before.
   */
  boolean register(Object client);

  /**
   * Unregisters the given client object with this access pool.
   *
   * @param client The client to be unregistered.
   * @return <code>true</code> if, and only if, the client was successfully
   * unregistered and was registered before.
   */
  boolean unregister(Object client);

  /**
   * Returns a reference to the pooled kernel.
   *
   * @return A reference to the pooled kernel.
   */
  Kernel getKernel();

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
