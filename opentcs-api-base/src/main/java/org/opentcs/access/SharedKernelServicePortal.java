// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access;

/**
 * Provides access to a shared {@link KernelServicePortal} instance.
 */
public interface SharedKernelServicePortal
    extends
      AutoCloseable {

  @Override
  void close();

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
