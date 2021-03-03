/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a kernel for clients in the plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationKernelProvider
    implements SharedKernelProvider {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationKernelProvider.class);
  /**
   * The registered clients.
   */
  private final Set<Object> clients = new HashSet<>();
  /**
   * The kernel proxy manager taking care of the kernel connection.
   */
  private final KernelProxyManager kernelProxyManager;
  /**
   * A provider for dialogs to be shown when trying to connect to the kernel.
   */
  private final Provider<ConnectToServerDialog> dialogProvider;

  /**
   * Creates a new instance.
   *
   * @param kernelProxyManager The kernel proxy manager taking care of the
   * kernel connection.
   * @param dialogProvider A provider for dialogs to be shown when trying to
   * connect to the kernel.
   */
  @Inject
  public ApplicationKernelProvider(KernelProxyManager kernelProxyManager,
                                   Provider<ConnectToServerDialog> dialogProvider) {
    this.kernelProxyManager = requireNonNull(kernelProxyManager, "kernelProxyManager");
    this.dialogProvider = requireNonNull(dialogProvider, "dialogProvider");
  }

  @Override
  public synchronized boolean register(Object client) {
    requireNonNull(client, "client");

    if (!kernelShared()) {
      LOG.debug("Initiating kernel connection for new client...");
      connectKernel();
    }
    return clients.add(client);
  }

  @Override
  public synchronized boolean unregister(Object client) {
    requireNonNull(client, "client");

    if (clients.remove(client)) {
      if (clients.isEmpty()) {
        LOG.debug("Last client left. Terminating kernel connection...");
        kernelProxyManager.disconnect();
      }
      return true;
    }
    return false;
  }

  @Override
  public synchronized Kernel getKernel() {
    return kernelProxyManager.kernel();
  }

  @Override
  public synchronized boolean kernelShared() {
    return kernelProxyManager.isConnected();
  }

  @Override
  public synchronized String getKernelDescription() {
    return kernelShared()
        ? kernelProxyManager.getHost() + ":" + kernelProxyManager.getPort()
        : "-";
  }

  /**
   * Establishes a connection to the kernel.
   *
   * @return Whether a connection was established or not.
   */
  private boolean connectKernel() {
    // If connection parameters are given in the system properties, try
    // connecting with them.
    ConnectionParamSet connParamSet = ConnectionParamSet.getParamSet(System.getProperties());
    if (connParamSet != null) {
      boolean didConnect = kernelProxyManager.connect(connParamSet);
      if (didConnect) {
        return true;
      }
    }

    // If we are not connected, yet, show a dialog for entering the connection
    // parameters.
    if (!kernelProxyManager.isConnected()) {
      ConnectToServerDialog dialog = dialogProvider.get();
      dialog.setVisible(true);
      return dialog.getReturnStatus() == ConnectToServerDialog.RET_OK;
    }
    return false;
  }
}
