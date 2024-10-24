// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access;

import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.KernelExtension;

/**
 * Declares the methods the openTCS kernel must provide which are not accessible
 * to remote peers.
 */
public interface LocalKernel
    extends
      Kernel,
      Lifecycle {

  /**
   * Adds a <code>KernelExtension</code> to this kernel.
   *
   * @param newExtension The extension to be added.
   */
  void addKernelExtension(KernelExtension newExtension);

  /**
   * Removes a <code>KernelExtension</code> from this kernel.
   *
   * @param rmExtension The extension to be removed.
   */
  void removeKernelExtension(KernelExtension rmExtension);
}
