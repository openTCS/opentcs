/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles method invocations for the RMI proxy and forwards them to the remote
 * kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link KernelServicePortalBuilder} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
class ProxyInvocationHandler
    implements InvocationHandler,
               RemoteKernelConnection,
               org.opentcs.util.eventsystem.EventSource<org.opentcs.util.eventsystem.TCSEvent> {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ProxyInvocationHandler.class);
  /**
   * The host on which the remote kernel is running.
   */
  private final String hostName;
  /**
   * The port at which we can reach the remote RMI registry.
   */
  private final int port;
  /**
   * The user name used with the remote kernel.
   */
  private final String userName;
  /**
   * The password used with the remote kernel.
   */
  private final String password;
  /**
   * An event filter for the remote kernel.
   */
  private final org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter;
  /**
   * The time to wait between event polls with the remote kernel (in ms).
   */
  private final long eventPollInterval;
  /**
   * The time to wait for events to arrive when polling (in ms).
   */
  private final long eventPollTimeout;
  /**
   * This proxy's event hub for dispatching polled events.
   */
  private final org.opentcs.util.eventsystem.EventHub<org.opentcs.util.eventsystem.TCSEvent> eventHub
      = new org.opentcs.util.eventsystem.SynchronousEventHub<>();
  /**
   * Provides socket factories used for RMI.
   */
  private final SocketFactoryProvider socketFactoryProvider;
  /**
   * The remote kernel.
   */
  private volatile RemoteKernel remoteKernel;
  /**
   * Our client ID with the remote kernel.
   */
  private volatile ClientID clientID;
  /**
   * The task polling the remote kernel for new events.
   */
  private volatile EventPollerTask eventPollerTask;
  /**
   * The current state of the remote kernel connection.
   */
  private State currentState = State.DISCONNECTED;

  /**
   * Creates a new instance.
   *
   * @param socketFactoryProvider Provides socket factories used for RMI.
   * @param hostName The host on which the RMI registry listing the remote kernel is running.
   * @param port The port on which the RMI registry is listening.
   * @param userName The user name to use when logging in with the remote kernel.
   * @param password The password to use when logging in.
   * @param eventFilter An event filter for filtering events with the remote kernel.
   * @param eventPollInterval The time to wait between event polls with the remote kernel (in ms).
   * @param eventPollTimeout The time to wait for events to arrive when polling (in ms).
   * @throws KernelUnavailableException If the remote kernel is not reachable for some reason.
   * @throws CredentialsException If the client login with the remote kernel failed, e.g. because of
   * incorrect login data.
   * @see RemoteKernel#pollEvents(ClientID, long)
   * @deprecated Use {@link #ProxyInvocationHandler(java.lang.String, int, java.lang.String, java.lang.String, long, long)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  ProxyInvocationHandler(
      @Nonnull SocketFactoryProvider socketFactoryProvider,
      @Nonnull String hostName,
      int port,
      @Nonnull String userName,
      @Nonnull String password,
      @Nonnull org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter,
      long eventPollInterval,
      long eventPollTimeout)
      throws CredentialsException {
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    this.hostName = requireNonNull(hostName, "hostName");
    this.port = checkInRange(port, 0, 65535, "port");
    this.userName = requireNonNull(userName, "userName");
    this.password = requireNonNull(password, "password");
    this.eventFilter = requireNonNull(eventFilter, "eventFilter");
    this.eventPollInterval = eventPollInterval;
    this.eventPollTimeout = eventPollTimeout;
  }

  /**
   * Creates a new instance.
   *
   * @param socketFactoryProvider Provides socket factories used for RMI.
   * @param hostName The host on which the RMI registry listing the remote kernel is running.
   * @param port The port on which the RMI registry is listening.
   * @param userName The user name to use when logging in with the remote kernel.
   * @param password The password to use when logging in.
   * @param eventPollInterval The time to wait between event polls with the remote kernel (in ms).
   * @param eventPollTimeout The time to wait for events to arrive when polling (in ms).
   * @throws KernelUnavailableException If the remote kernel is not reachable for some reason.
   * @throws CredentialsException If the client login with the remote kernel failed, e.g. because of
   * incorrect login data.
   * @see RemoteKernel#pollEvents(ClientID, long)
   */
  @SuppressWarnings("deprecation")
  ProxyInvocationHandler(
      @Nonnull SocketFactoryProvider socketFactoryProvider,
      @Nonnull String hostName,
      int port,
      @Nonnull String userName,
      @Nonnull String password,
      long eventPollInterval,
      long eventPollTimeout)
      throws CredentialsException {
    this(socketFactoryProvider,
         hostName,
         port,
         userName,
         password,
         new org.opentcs.util.eventsystem.AcceptingTCSEventFilter(),
         eventPollInterval,
         eventPollTimeout);
  }

  // Implementation of interface InvocationHandler starts here.
  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    try {
      if (RemoteKernelConnection.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      }
      else if (org.opentcs.util.eventsystem.EventSource.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      }
      else if (Kernel.class.equals(method.getDeclaringClass())) {
        // Make sure we're logged in, first.
        if (!loggedIn()) {
          throw new KernelUnavailableException("not logged in");
        }
        Method remoteMethod = RemoteMethods.getRemoteKernelMethod(method);
        Object[] extArgs;
        if (args == null) {
          extArgs = new Object[1];
          extArgs[0] = clientID;
        }
        else {
          extArgs = new Object[args.length + 1];
          extArgs[0] = clientID;
          System.arraycopy(args, 0, extArgs, 1, args.length);
        }
        return remoteMethod.invoke(remoteKernel, extArgs);
      }
      else {
        throw new org.opentcs.access.UnsupportedKernelOpException("Unexpected declaring class: "
            + method.getDeclaringClass().getName());
      }
    }
    catch (InvocationTargetException exc) {
      // Wrap RemoteExceptions in KernelUnavailableExceptions. This is
      // necessary because invoke() (i.e. this method) may not throw
      // RemoteException (unchecked in Kernel and not a RuntimeException).
      if (exc.getCause() instanceof RemoteException) {
        // Before throwing the wrapped exception, shut down the connection to
        // the kernel properly.
        logout();
        throw new KernelUnavailableException("remote kernel unreachable",
                                             exc.getCause());
      }
      throw exc.getCause();
    }
  }

  // Implementation of interface EventSource<TCSObjectEvent> starts here.
  @Override
  @Deprecated
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener,
      org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> filter) {
    eventHub.addEventListener(listener, filter);
  }

  @Override
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener) {
    eventHub.addEventListener(listener);
  }

  @Override
  public void removeEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener) {
    eventHub.removeEventListener(listener);
  }

  // Implementation of interface RemoteKernelConnection starts here.
  @Override
  public void login()
      throws CredentialsException {
    if (loggedIn()) {
      LOG.warn("Already logged in, doing nothing.");
      return;
    }
    try {
      // Look up the remote kernel with the RMI registry.
      Registry registry = LocateRegistry.getRegistry(hostName,
                                                     port,
                                                     socketFactoryProvider.getClientSocketFactory());
      RemoteKernel kernel = (RemoteKernel) registry.lookup(RemoteKernel.REGISTRATION_NAME);
      // Login, save the client ID and set the event filter.
      clientID = kernel.login(userName, password);
      kernel.setEventFilter(clientID, eventFilter);
      remoteKernel = kernel;
      // Start polling for events.
      eventPollerTask = new EventPollerTask(eventPollInterval, eventPollTimeout);
      Thread eventPollerThread = new Thread(eventPollerTask, "eventPoller");
      eventPollerThread.start();
    }
    catch (RemoteException | NotBoundException exc) {
      throw new KernelUnavailableException("Exception logging in with remote kernel", exc);
    }
    setConnectionState(State.LOGGED_IN);
  }

  @Override
  public void logout() {
    if (!loggedIn()) {
      LOG.warn("Not logged in, doing nothing.");
      return;
    }
    // Stop polling for events.
    eventPollerTask.terminateAndWait();
    // Forget the remote kernel and the client ID.
    remoteKernel = null;
    clientID = null;
    setConnectionState(State.DISCONNECTED);
  }

  @Override
  public State getConnectionState() {
    return currentState;
  }

  /**
   * Checks whether we're logged in or not.
   *
   * @return <code>true</code> if, and only if, we're logged in with the remote
   * kernel, i.e. we could call methods with it.
   */
  private boolean loggedIn() {
    return remoteKernel != null && clientID != null;
  }

  /**
   * Sets this proxy's connection state and emits an event for it.
   *
   * @param newState The new state.
   */
  private void setConnectionState(State newState) {
    currentState = requireNonNull(newState, "newState");
    emitStateEvent(newState);
  }

  /**
   * Generates an event for a state change.
   *
   * @param newState The state entered.
   */
  private void emitStateEvent(RemoteKernelConnection.State newState) {
    eventHub.processEvent(new TCSProxyStateEvent(newState));
  }

  /**
   * A task polling the remote kernel for events in regular intervals.
   */
  private class EventPollerTask
      extends CyclicTask {

    /**
     * The poll timeout.
     */
    private final long timeout;

    /**
     * Creates a new EventPollerTask.
     *
     * @param pollInterval The time to wait between polls in ms.
     * @param pollTimeout The timeout in ms for which to wait for events to
     * arrive with each polling call.
     */
    private EventPollerTask(long pollInterval, long pollTimeout) {
      super(pollInterval);
      if (pollTimeout < 1) {
        throw new IllegalArgumentException("pollTimeout < 1: " + pollTimeout);
      }
      timeout = pollTimeout;
    }

    @Override
    protected void runActualTask() {
      try {
        boolean doLogOut = false;
        LOG.debug("Polling remote kernel for events");
        List<org.opentcs.util.eventsystem.TCSEvent> events = remoteKernel.pollEvents(clientID,
                                                                                     timeout);
        for (org.opentcs.util.eventsystem.TCSEvent curEvent : events) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Processing fetched event: " + curEvent);
          }
          // Forward received events to all registered listeners.
          eventHub.processEvent(curEvent);

          // Check if the kernel notifies us about a state change.
          if (curEvent instanceof org.opentcs.access.TCSKernelStateEvent) {
            org.opentcs.access.TCSKernelStateEvent stateEvent
                = (org.opentcs.access.TCSKernelStateEvent) curEvent;
            if (Kernel.State.SHUTDOWN.equals(stateEvent.getEnteredState())) {
              // If the kernel switches to SHUTDOWN, remember to log out.
              doLogOut = true;
            }
            else {
              // If the kernel switches to any other state, do not log out.
              doLogOut = false;
            }
          }
        }
        if (doLogOut) {
          LOG.info("Logging out (triggered by fly-by state event)...");
          logout();
        }
      }
      catch (RemoteException | CredentialsException exc) {
        LOG.error("Exception polling events, logging out", exc);
        // Remember the connection problem by shutting it down properly.
        logout();
      }
      catch (Exception exc) {
        LOG.error("Caught unhandled exception", exc);
      }
    }
  }
}
