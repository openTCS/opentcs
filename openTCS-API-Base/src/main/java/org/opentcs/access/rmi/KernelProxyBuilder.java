/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.lang.reflect.Proxy;
import java.rmi.registry.Registry;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Builds {@link KernelProxy} instances for connections to remote kernels.
 *
 * <p>
 * If the kernel you want to connect to is running on a host named "foobar", you usually only have
 * to do the following:
 * </p>
 * <pre>
 * Kernel kernel = new KernelProxyBuilder().setHost("foobar").build();
 * </pre>
 * <p>
 * You can then use <code>kernel</code> to call methods on the proxy, which will be wrapped by RMI
 * calls internally and forwarded to the remote kernel you connected to.
 * This interface to the kernel can e.g. be used to create transport orders from clients - see the
 * developer's guide for code examples.
 * </p>
 * <p>
 * After being built, the proxy will immediately start polling periodically for events with the
 * remote kernel.
 * You can use a custom intervals and timeouts for polling by setting the respective build
 * parameters.
 * To receive these events in your client code, register an event listener with the proxy by calling
 * its {@link org.opentcs.util.eventsystem.EventSource#addEventListener(org.opentcs.util.eventsystem.EventListener)} method.
 * The proxy will then forward events to your registered listener.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link KernelServicePortalBuilder} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class KernelProxyBuilder {

  /**
   * Provides socket factories used for RMI.
   */
  private SocketFactoryProvider socketFactoryProvider = new NullSocketFactoryProvider();
  /**
   * The registry host.
   */
  private String host = "localhost";
  /**
   * The registry port.
   */
  private int port = Registry.REGISTRY_PORT;
  /**
   * The user name for logging in.
   */
  private String userName = RemoteKernel.GUEST_USER;
  /**
   * The password for logging in.
   */
  private String password = RemoteKernel.GUEST_PASSWORD;
  /**
   * An event filter for filtering events with the remote kernel.
   */
  @SuppressWarnings("deprecation")
  private org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter
      = new org.opentcs.util.eventsystem.AcceptingTCSEventFilter();
  /**
   * The time to wait between event polls with the remote kernel (in ms).
   */
  private long eventPollInterval = 1;
  /**
   * The time to wait for events to arrive when polling (in ms).
   */
  private long eventPollTimeout = 1000;

  /**
   * Creates a new instance.
   */
  public KernelProxyBuilder() {
  }

  public SocketFactoryProvider getSocketFactoryProvider() {
    return socketFactoryProvider;
  }

  public KernelProxyBuilder setSocketFactoryProvider(
      @Nonnull SocketFactoryProvider socketFactoryProvider) {
    this.socketFactoryProvider = socketFactoryProvider;
    return this;
  }

  /**
   * Returns the host running the RMI registry.
   * The default value is "localhost".
   *
   * @return The host running the RMI registry.
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the host running the RMI registry.
   *
   * @param host The host.
   * @return This instance.
   */
  public KernelProxyBuilder setHost(@Nonnull String host) {
    this.host = requireNonNull(host, "host");
    return this;
  }

  /**
   * Returns the port on which the RMI registry is listening.
   * The default value is {@code Registry.REGISTRY_PORT}.
   *
   * @return The port on which the RMI registry is listening.
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port on which the RMI registry is listening.
   *
   * @param port The port.
   * @return This instance.
   */
  public KernelProxyBuilder setPort(int port) {
    this.port = checkInRange(port, 1, 65535);
    return this;
  }

  /**
   * Returns the user name used for logging in.
   * The default value is {@link RemoteKernel#GUEST_USER}.
   *
   * @return The user name used for logging in.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name used for logging in.
   *
   * @param userName The user name.
   * @return This instance.
   */
  public KernelProxyBuilder setUserName(@Nonnull String userName) {
    this.userName = requireNonNull(userName, "userName");
    return this;
  }

  /**
   * Returns the password used for logging in.
   * The default value is {@link RemoteKernel#GUEST_PASSWORD}.
   *
   * @return The password used for logging in.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password used for logging in.
   *
   * @param password The password.
   * @return This instance.
   */
  public KernelProxyBuilder setPassword(@Nonnull String password) {
    this.password = requireNonNull(password, "password");
    return this;
  }

  /**
   * Returns the event filter used for filtering events on the remote kernel.
   *
   * @return The event filter used for filtering events on the remote kernel.
   * @deprecated {@link org.opentcs.util.eventsystem.EventFilter} is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> getEventFilter() {
    return eventFilter;
  }

  /**
   * Sets the event filter used for filtering events on the remote kernel.
   *
   * @param eventFilter The event filter.
   * @return This instance.
   * @deprecated {@link org.opentcs.util.eventsystem.EventFilter} is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  public KernelProxyBuilder setEventFilter(
      @Nonnull org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter) {
    this.eventFilter = requireNonNull(eventFilter, "eventFilter");
    return this;
  }

  /**
   * Returns the time to wait between event polls with the remote kernel (in ms).
   * The default value is 1.
   *
   * @return The time to wait between event polls with the remote kernel (in ms).
   */
  public long getEventPollInterval() {
    return eventPollInterval;
  }

  /**
   * Sets the time to wait between event polls with the remote kernel (in ms).
   *
   * @param eventPollInterval The poll interval.
   * @return This instance.
   */
  public KernelProxyBuilder setEventPollInterval(long eventPollInterval) {
    this.eventPollInterval = eventPollInterval;
    return this;
  }

  /**
   * Returns the time to wait for events to arrive when polling (in ms).
   * The default value is 1000.
   *
   * @return The time to wait for events to arrive when polling (in ms).
   */
  public long getEventPollTimeout() {
    return eventPollTimeout;
  }

  /**
   * Sets the time to wait for events to arrive when polling (in ms).
   *
   * @param eventPollTimeout The poll timeout.
   * @return This instance.
   */
  public KernelProxyBuilder setEventPollTimeout(long eventPollTimeout) {
    this.eventPollTimeout = eventPollTimeout;
    return this;
  }

  /**
   * Builds and returns a {@link KernelProxy} with the configured parameters.
   *
   * @return A proxy for the remote kernel.
   * @throws KernelUnavailableException If the remote kernel is not reachable for some reason.
   * @throws CredentialsException If the client login with the remote kernel failed, e.g. because of
   * incorrect login data.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  @SuppressWarnings("deprecation")
  public KernelProxy build()
      throws KernelUnavailableException, CredentialsException {
    // Create an invocation handler that does the actual work.
    ProxyInvocationHandler handler
        = new ProxyInvocationHandler(socketFactoryProvider,
                                     host,
                                     port,
                                     userName,
                                     password,
                                     eventFilter,
                                     eventPollInterval,
                                     eventPollTimeout);
    // Return a proxy instance with the created handler.
    // Create a proxy instance with the handler and return it.
    KernelProxy proxy
        = (KernelProxy) Proxy.newProxyInstance(Kernel.class.getClassLoader(),
                                               new Class<?>[] {KernelProxy.class},
                                               handler);
    proxy.login();
    return proxy;
  }
}
