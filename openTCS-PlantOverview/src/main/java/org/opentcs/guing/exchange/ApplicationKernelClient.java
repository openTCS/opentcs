/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelClient;
import static org.opentcs.util.Assertions.checkState;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationKernelClient
    implements SharedKernelClient {

  /**
   * The kernel.
   */
  private final Kernel kernel;
  /**
   * The kernel provider instance that this client is registered at.
   */
  private final ApplicationKernelProvider sharedKernelProvider;
  /**
   * The object registered with the provider.
   */
  private final Object registeredToken;
  /**
   * Indicates whether this instance is closed.
   */
  private boolean closed;

  /**
   * Creates a new instance.
   *
   * @param kernel The shared kernel instance.
   * @param kernelProvider The provider this client is registered with.
   * @param registeredToken The token that is actually registered with the provider.
   */
  public ApplicationKernelClient(Kernel kernel,
                                 ApplicationKernelProvider kernelProvider,
                                 Object registeredToken) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.sharedKernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.registeredToken = requireNonNull(registeredToken, "registeredToken");
  }

  @Override
  public void close() {
    if (isClosed()) {
      return;
    }

    sharedKernelProvider.unregister(registeredToken);
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public Kernel getKernel() {
    checkState(!isClosed(), "Closed already.");

    return kernel;
  }
}
