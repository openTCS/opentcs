/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

/**
 * Provides access to a shared kernel.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @deprecated Use {@link SharedKernelServicePortal} instead.
 */
@Deprecated
public interface SharedKernelClient
    extends AutoCloseable {

  @Override
  public void close();

  /**
   * Indicates whether this instance is closed/unregistered from the shared kernel pool.
   *
   * @return <code>true</code> if, and only if, this instance is closed.
   */
  boolean isClosed();

  /**
   * Returns the kernel.
   *
   * @return the kernel.
   * @throws IllegalStateException If this instance is closed.
   * @deprecated Use {@link #getPortal()} instead.
   */
  @Deprecated
  Kernel getKernel()
      throws IllegalStateException;

  /**
   * Returns the {@link KernelServicePortal} instance being shared.
   *
   * @return The portal instance being shared.
   * @throws IllegalStateException If this instance is closed.
   */
  KernelServicePortal getPortal()
      throws IllegalStateException;
}
