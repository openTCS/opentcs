/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.exchange;

import java.util.Objects;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.rmi.DynamicRemoteKernelProxy;
import org.opentcs.access.rmi.KernelProxy;
import org.opentcs.access.rmi.KernelUnavailableException;
import org.opentcs.util.eventsystem.AcceptingTCSEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link KernelProxyManager}, providing a single
 * kernel proxy.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
class DefaultKernelProxyManager
    implements KernelProxyManager {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(DefaultKernelProxyManager.class);
  /**
   * A reference to the kernel connected to.
   * <code>null</code> if no connection is currently established.
   */
  private KernelProxy kernelProxy;
  /**
   * The host the connection is currently established to.
   * <code>null</code> if no connection is currently established.
   */
  private String host;
  /**
   * The port the connection is currently established to.
   * <code>-1</code> if no connection is currently established.
   */
  private int port;

  /**
   * Creates a new instance of KernelProxy.
   */
  DefaultKernelProxyManager() {
    // Do nada
  }

  @Override
  public boolean connect(String host, int port) {
    try {
      kernelProxy = null;
      this.host = null;
      this.port = -1;
      kernelProxy = DynamicRemoteKernelProxy.getProxy(host,
                                                      port,
                                                      new AcceptingTCSEventFilter());
      this.host = host;
      this.port = port;
    }
    catch (CredentialsException | KernelUnavailableException e) {
      log.warn("Exception trying to connect to remote kernel", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean connect(ConnectionParamSet connParamSet) {
    Objects.requireNonNull(connParamSet);

    return connect(connParamSet.getHost(), connParamSet.getPort());
  }

  @Override
  public void disconnect() {
    if (kernelProxy != null) {
      KernelProxy kp = kernelProxy;
      kernelProxy = null;
      kp.logout();
    }
  }

  @Override
  public boolean isConnected() {
    return kernelProxy != null;
  }

  @Override
  public Kernel kernel() {
    return kernelProxy;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }
}
