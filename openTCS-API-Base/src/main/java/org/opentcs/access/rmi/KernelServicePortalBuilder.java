/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RemoteKernelServicePortal;
import org.opentcs.access.rmi.services.RemoteKernelServicePortalProxy;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.util.ClassMatcher;

/**
 * Builds {@link KernelServicePortal} instances for connections to remote portals.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class KernelServicePortalBuilder {

  /**
   * Provides socket factories used for RMI.
   */
  private SocketFactoryProvider socketFactoryProvider = new NullSocketFactoryProvider();
  /**
   * The user name for logging in.
   */
  private String userName = RemoteKernelServicePortal.GUEST_USER;
  /**
   * The password for logging in.
   */
  private String password = RemoteKernelServicePortal.GUEST_PASSWORD;
  /**
   * The event filter to be applied for the built portal.
   */
  private Predicate<Object> eventFilter = new ClassMatcher(Object.class);

  /**
   * Creates a new instance.
   */
  public KernelServicePortalBuilder() {
  }

  /**
   * Returns the socket factory provider used for RMI.
   *
   * @return The socket factory provider used for RMI.
   */
  public SocketFactoryProvider getSocketFactoryProvider() {
    return socketFactoryProvider;
  }

  /**
   * Sets the socket factory provider used for RMI.
   *
   * @param socketFactoryProvider The socket factory provider.
   * @return This instance.
   */
  public KernelServicePortalBuilder setSocketFactoryProvider(
      @Nonnull SocketFactoryProvider socketFactoryProvider) {
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    return this;
  }

  /**
   * Returns the user name used for logging in.
   * The default value is {@link RemoteKernelServicePortal#GUEST_USER}.
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
  public KernelServicePortalBuilder setUserName(@Nonnull String userName) {
    this.userName = requireNonNull(userName, "userName");
    return this;
  }

  /**
   * Returns the password used for logging in.
   * The default value is {@link RemoteKernelServicePortal#GUEST_PASSWORD}.
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
  public KernelServicePortalBuilder setPassword(@Nonnull String password) {
    this.password = requireNonNull(password, "password");
    return this;
  }

  /**
   * Returns the event filter to be applied for the built portal.
   *
   * @return The event filter to be applied for the built portal.
   */
  public Predicate<Object> getEventFilter() {
    return eventFilter;
  }

  /**
   * Sets the event filter to be applied for the built portal.
   *
   * @param eventFilter The event filter.
   * @return This instance.
   */
  public KernelServicePortalBuilder setEventFilter(@Nonnull Predicate<Object> eventFilter) {
    this.eventFilter = requireNonNull(eventFilter, "eventFilter");
    return this;
  }

  /**
   * Builds and returns a {@link KernelServicePortal} with the configured parameters.
   *
   * @return A {@link KernelServicePortal} instance.
   * @throws KernelUnavailableException If the remote portal is not reachable for some reason.
   * @throws CredentialsException If the client login with the remote portal failed, e.g. because of
   * incorrect login data.
   */
  public KernelServicePortal build()
      throws ServiceUnavailableException, CredentialsException {
    RemoteKernelServicePortalProxy portal = new RemoteKernelServicePortalProxy(
        userName,
        password,
        socketFactoryProvider,
        eventFilter);
    return portal;
  }
}
