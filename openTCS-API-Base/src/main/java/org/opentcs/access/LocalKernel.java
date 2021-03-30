/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.KernelExtension;

/**
 * Declares the methods the openTCS kernel must provide which are not accessible
 * to remote peers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocalKernel
    extends Kernel,
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
