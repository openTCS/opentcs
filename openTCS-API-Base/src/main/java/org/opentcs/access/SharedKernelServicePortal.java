/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

/**
 * Provides access to a shared {@link KernelServicePortal} instance.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface SharedKernelServicePortal
    extends AutoCloseable {

  @Override
  public void close();

  /**
   * Indicates whether this instance is closed/unregistered from the shared portal pool.
   *
   * @return {@code true} if, and only if, this instance is closed.
   */
  boolean isClosed();

  /**
   * Returns the {@link KernelServicePortal} instance being shared.
   *
   * @return The portal instance being shared.
   * @throws IllegalStateException If this instance is closed.
   */
  KernelServicePortal getPortal()
      throws IllegalStateException;
}
