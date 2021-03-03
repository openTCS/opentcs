/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.rmi.DynamicRemoteKernelProxy;
import org.opentcs.access.rmi.KernelProxy;
import org.opentcs.access.rmi.KernelUnavailableException;
import org.opentcs.util.eventsystem.AcceptingTCSEventFilter;

/**
 * The default implementation of {@link KernelProxyManager}, providing a single
 * kernel proxy.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelProxyManager
    implements KernelProxyManager {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(DefaultKernelProxyManager.class.getName());
  /**
   * The unique proxy manager.
   */
  private static final KernelProxyManager fInstance
      = new DefaultKernelProxyManager();
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
  private DefaultKernelProxyManager() {
    // Do nada
  }

  /**
   * Returns the single instance of this class.
   *
   * @return The single instance of this class.
   */
  public static KernelProxyManager instance() {
    return fInstance;
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
      log.log(Level.WARNING, "Exception trying to connect to remote kernel", e);
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
