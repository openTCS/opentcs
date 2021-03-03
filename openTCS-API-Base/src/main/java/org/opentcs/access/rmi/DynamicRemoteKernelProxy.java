/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.lang.reflect.Proxy;
import java.rmi.registry.Registry;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.util.eventsystem.EventFilter;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Creates proxies that hide the details of communication with a remote kernel,
 * so a client can call methods of the proxy as if it was a local kernel
 * instance.
 * 
 * <p>
 * If the kernel you want to connect to is running on the same system as your
 * client, you usually only have to do the following:
 * </p>
 * <pre>
 * Kernel kernel = DynamicRemoteKernelProxy.getProxy("localhost",
 *                                                   new AcceptingTCSEventFilter());
 * </pre>
 * <p>
 * You can then use <code>kernel</code> to call methods on the proxy, which
 * will be wrapped by RMI calls internally and forwarded to the kernel you
 * connected to. This interface to the kernel can e.g. be used to create
 * transport orders from clients - see the developer's guide for some code examples.
 * </p>
 * 
 * <p>
 * The proxy will immediately start polling periodically for
 * events with the remote kernel; you can use custom intervals and timeouts for
 * polling by calling one of the other <code>getProxy()</code> methods. Using an
 * {@code AcceptingTCSEventFilter}
 * as its event filter as shown above will let the proxy receive every event
 * generated inside the kernel without filtering out any of them. To receive all
 * or some of these events in your own code, register an event listener with the
 * proxy by calling its
 * {@link org.opentcs.util.eventsystem.EventSource#addEventListener(org.opentcs.util.eventsystem.EventListener, org.opentcs.util.eventsystem.EventFilter) addEventListener()}
 * method; the proxy will then forward all events that it received and that are
 * not filtered by your event filter to your registered listener.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class DynamicRemoteKernelProxy {

  /**
   * The default interval for polling for events (in ms).
   */
  private static final long DEFAULT_POLL_INTERVAL = 1;
  /**
   * The default timeout for polling for events (in ms).
   */
  private static final long DEFAULT_POLL_TIMEOUT = 1000;

  /**
   * Prevents creation of instances of this class.
   */
  private DynamicRemoteKernelProxy() {
  }

  /**
   * Creates a proxy for a remote kernel registered with a RMI registry on the
   * given host and at the given port.
   * After the proxy is created, it implicitly logs in with the remote kernel.
   *
   * @param host The host running the RMI registry.
   * @param port The TCP port at which the RMI registry should be listening.
   * @param userName The user name to use when logging in with the remote
   * kernel.
   * @param password The password to use when logging in.
   * @param eventFilter An event filter for filtering events with the remote
   * kernel.
   * @param eventPollInterval The time to wait between event polls with the
   * remote kernel (in ms).
   * @param eventPollTimeout The time to wait for events to arrive when polling
   * (in ms).
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable
   * for some reason.
   * @throws CredentialsException If the client login with the remote kernel
   * failed, e.g. because of incorrect login data.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  public static KernelProxy getProxy(String host,
                                     int port,
                                     String userName,
                                     String password,
                                     EventFilter<TCSEvent> eventFilter,
                                     long eventPollInterval,
                                     long eventPollTimeout)
      throws KernelUnavailableException, CredentialsException {
    // Create an invocation handler that does the actual work.
    ProxyInvocationHandler handler =
        new ProxyInvocationHandler(host,
                                   port,
                                   userName,
                                   password,
                                   eventFilter,
                                   eventPollInterval,
                                   eventPollTimeout);
    // Return a proxy instance with the created handler.
    // Create a proxy instance with the handler and return it.
    KernelProxy proxy =
        (KernelProxy) Proxy.newProxyInstance(Kernel.class.getClassLoader(),
                                             new Class<?>[] {KernelProxy.class},
                                             handler);
    return proxy;
  }

  /**
   * Creates a proxy for a remote kernel registered with a RMI registry on the
   * given host and at the default RMI port.
   * After the proxy is created, it implicitly logs in with the remote kernel.
   *
   * @param host The host running the RMI registry.
   * @param userName The user name to use when logging in with the remote
   * kernel.
   * @param password The password to use when logging in.
   * @param eventFilter An event filter for filtering events with the remote
   * kernel.
   * @param eventPollInterval The time to wait between event polls with the
   * remote kernel (in ms).
   * @param eventPollTimeout The time to wait for events to arrive when polling
   * (in ms).
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable
   * for some reason.
   * @throws CredentialsException If the client login with the remote kernel
   * failed, e.g. because of incorrect login data.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  public static KernelProxy getProxy(String host,
                                     String userName,
                                     String password,
                                     EventFilter<TCSEvent> eventFilter,
                                     long eventPollInterval,
                                     long eventPollTimeout)
      throws KernelUnavailableException, CredentialsException {
    return getProxy(host,
                    Registry.REGISTRY_PORT,
                    userName,
                    password,
                    eventFilter,
                    eventPollInterval,
                    eventPollTimeout);
  }

  /**
   * Creates a proxy for a remote kernel registered with a RMI registry on the
   * given host at the default RMI port, using a standard user name and
   * password.
   * After the proxy is created, it implicitly logs in with the remote kernel.
   *
   * @param host The host running the RMI registry.
   * @param eventFilter An event filter for filtering events with the remote
   * kernel.
   * @param eventPollInterval The time to wait between event polls with the
   * remote kernel (in ms).
   * @param eventPollTimeout The time to wait for events to arrive when polling
   * (in ms).
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable
   * for some reason.
   * @throws CredentialsException If the client login with the remote kernel
   * failed, e.g. because the standard user name and password are not accepted.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  public static KernelProxy getProxy(String host,
                                     EventFilter<TCSEvent> eventFilter,
                                     long eventPollInterval,
                                     long eventPollTimeout)
      throws KernelUnavailableException, CredentialsException {
    return getProxy(host,
                    Registry.REGISTRY_PORT,
                    RemoteKernel.GUEST_USER,
                    RemoteKernel.GUEST_PASSWORD,
                    eventFilter,
                    eventPollInterval,
                    eventPollTimeout);
  }

  /**
   * Creates a proxy for a remote kernel registered with a RMI registry on the
   * given host at the default RMI port, using a standard user name and
   * password, a poll interval of 1 ms and a poll timeout of 1000 ms.
   * After the proxy is created, it implicitly logs in with the remote kernel.
   *
   * @param host The host running the RMI registry.
   * @param eventFilter An event filter for filtering events with the remote
   * kernel.
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable
   * for some reason.
   * @throws CredentialsException If the client login with the remote kernel
   * failed, e.g. because the standard user name and password are not accepted.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  public static KernelProxy getProxy(String host,
                                     EventFilter<TCSEvent> eventFilter) {
    return getProxy(host,
                    Registry.REGISTRY_PORT,
                    RemoteKernel.GUEST_USER,
                    RemoteKernel.GUEST_PASSWORD,
                    eventFilter,
                    DEFAULT_POLL_INTERVAL,
                    DEFAULT_POLL_TIMEOUT);
  }

  /**
   * Creates a proxy for a remote kernel registered with a RMI registry on the
   * given host at the given RMI port, using a standard user name and
   * password, a poll interval of 1 ms and a poll timeout of 1000 ms.
   * After the proxy is created, it implicitly logs in with the remote kernel.
   *
   * @param host The host running the RMI registry.
   * @param port The TCP port at which the RMI registry should be listening.
   * @param eventFilter An event filter for filtering events with the remote
   * kernel.
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable
   * for some reason.
   * @throws CredentialsException If the client login with the remote kernel
   * failed, e.g. because the standard user name and password are not accepted.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  public static KernelProxy getProxy(String host,
                                     int port,
                                     EventFilter<TCSEvent> eventFilter) {
    return getProxy(host,
                    port,
                    RemoteKernel.GUEST_USER,
                    RemoteKernel.GUEST_PASSWORD,
                    eventFilter,
                    DEFAULT_POLL_INTERVAL,
                    DEFAULT_POLL_TIMEOUT);
  }
}
