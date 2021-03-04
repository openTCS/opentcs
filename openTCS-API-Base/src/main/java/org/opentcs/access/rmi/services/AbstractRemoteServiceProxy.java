/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;

/**
 * An base class for remote service proxy implementations.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @param <R> The remote service's type.
 */
abstract class AbstractRemoteServiceProxy<R extends Remote> {

  /**
   * The message to log when a service is unavailable.
   */
  private static final String SERVICE_UNAVAILABLE_MESSAGE = "Remote service unreachable";
  /**
   * The client using this service.
   */
  private ClientID clientId;
  /**
   * The corresponding remote service.
   */
  private R remoteService;
  /**
   * The the listener that is interested in updates of this service.
   */
  private ServiceListener serviceListener;

  /**
   * Retruns the client id using this service.
   *
   * @return The client id using this service.
   */
  ClientID getClientId() {
    return clientId;
  }

  /**
   * Sets the client id using this service.
   *
   * @param clientId The client id.
   * @return This remote service proxy.
   */
  AbstractRemoteServiceProxy<R> setClientId(ClientID clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Returns the remote service to delegate method invocations to.
   *
   * @return The remote service to delegate method incocations to.
   */
  R getRemoteService() {
    return remoteService;
  }

  /**
   * Sets the remote service to delegate method invocations to.
   *
   * @param remoteService The remote service.
   * @return This remote service proxy.
   */
  AbstractRemoteServiceProxy<R> setRemoteService(R remoteService) {
    this.remoteService = remoteService;
    return this;
  }

  /**
   * Returns the listener that is interested in updates of this service.
   *
   * @return The listener that is interested in updates of this service.
   */
  ServiceListener getServiceListener() {
    return serviceListener;
  }

  /**
   * Sets the listener that is interested in updates of this service.
   *
   * @param serviceListener The service listener.
   * @return This remote service proxy.
   */
  public AbstractRemoteServiceProxy<R> setServiceListener(ServiceListener serviceListener) {
    this.serviceListener = serviceListener;
    return this;
  }

  /**
   * Checks whether this service is logged in or not.
   *
   * @return {@code true} if, and only if, this service has both a client id and a remote service
   * associated to it.
   */
  boolean isLoggedIn() {
    return (getClientId() != null && getRemoteService() != null);
  }

  /**
   * Ensures that this service is available to be used.
   *
   * @throws ServiceUnavailableException If the service is not available.
   */
  void checkServiceAvailability()
      throws ServiceUnavailableException {
    if (!isLoggedIn()) {
      throw new ServiceUnavailableException(SERVICE_UNAVAILABLE_MESSAGE);
    }
  }

  /**
   * Returns a suitable {@link RuntimeException} for the given {@link RemoteException}.
   *
   * @param ex The exception to find a runtime exception for.
   * @return The runtime exception.
   */
  RuntimeException findSuitableExceptionFor(RemoteException ex) {
    if (ex.getCause() instanceof ObjectUnknownException) {
      return (ObjectUnknownException) ex.getCause();
    }
    if (ex.getCause() instanceof ObjectExistsException) {
      return (ObjectExistsException) ex.getCause();
    }
    if (ex.getCause() instanceof IllegalArgumentException) {
      return (IllegalArgumentException) ex.getCause();
    }

    if (getServiceListener() != null) {
      getServiceListener().onServiceUnavailable();
    }

    return new ServiceUnavailableException(SERVICE_UNAVAILABLE_MESSAGE, ex);
  }
}
