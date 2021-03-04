/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.exchange;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.common.ClientConnectionMode;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ServiceCallWrapper;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls a task that periodically fetches for kernel events.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class KernelEventFetcher
    implements EventHandler,
               Lifecycle {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelEventFetcher.class);
  /**
   * The time to wait between event fetches with the service portal (in ms).
   */
  private final long eventFetchInterval = 1;
  /**
   * The time to wait for events to arrive when fetching (in ms).
   */
  private final long eventFetchTimeout = 1000;
  /**
   * The application using this event hub.
   */
  private final KernelClientApplication application;
  /**
   * The service portal to fetch events from.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use.
   */
  private final CallWrapper callWrapper;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * The task fetching the service portal for new events.
   */
  private EventFetcherTask eventFetcherTask;
  /**
   * Whether this event hub is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param application The application using this event hub.
   * @param servicePortal The service portal to fetch events from.
   * @param callWrapper The call wrapper to use for fetching events.
   * @param eventBus The application's event bus.
   */
  @Inject
  public KernelEventFetcher(KernelClientApplication application,
                            KernelServicePortal servicePortal,
                            @ServiceCallWrapper CallWrapper callWrapper,
                            @ApplicationEventBus EventBus eventBus) {
    this.application = requireNonNull(application, "application");
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.eventBus = requireNonNull(eventBus, "eventBus");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event == PortalManager.ConnectionState.DISCONNECTING) {
      onKernelDisconnect();
    }
    else if (event instanceof ClientConnectionMode) {
      ClientConnectionMode applicationState = (ClientConnectionMode) event;
      switch (applicationState) {
        case ONLINE:
          onKernelConnect();
          break;
        case OFFLINE:
          onKernelDisconnect();
          break;
        default:
          LOG.debug("Unhandled portal connection state: {}", applicationState.name());
      }
    }
  }

  private void onKernelConnect() {
    if (eventFetcherTask != null) {
      return;
    }

    eventFetcherTask = new EventFetcherTask(eventFetchInterval, eventFetchTimeout);
    Thread eventFetcherThread = new Thread(eventFetcherTask, getClass().getName() + "-fetcherTask");
    eventFetcherThread.start();
  }

  private void onKernelDisconnect() {
    if (eventFetcherTask == null) {
      return;
    }

    // Stop polling for events.
    eventFetcherTask.terminateAndWait();
    eventFetcherTask = null;
  }

  /**
   * A task fetching the service portal for events in regular intervals.
   */
  private class EventFetcherTask
      extends CyclicTask {

    /**
     * The poll timeout.
     */
    private final long timeout;

    /**
     * Creates a new instance.
     *
     * @param interval The time to wait between polls in ms.
     * @param timeout The timeout in ms for which to wait for events to arrive with each polling
     * call.
     */
    private EventFetcherTask(long interval, long timeout) {
      super(interval);
      this.timeout = checkInRange(timeout, 1, Long.MAX_VALUE, "timeout");
    }

    @Override
    protected void runActualTask() {
      boolean shutDown = false;
      try {
        LOG.debug("Fetching remote kernel for events");
        List<Object> events = callWrapper.call(() -> servicePortal.fetchEvents(timeout));

        for (Object event : events) {
          LOG.debug("Processing fetched event: {}", event);
          // Forward received events to all registered listeners.
          eventBus.onEvent(event);

          // Check if the kernel notifies us about a state change.
          if (event instanceof KernelStateTransitionEvent) {
            KernelStateTransitionEvent stateEvent = (KernelStateTransitionEvent) event;
            // If the kernel switches to SHUTDOWN, remember to shut down.
            shutDown = stateEvent.getEnteredState() == Kernel.State.SHUTDOWN;
          }
        }
      }
      catch (Exception exc) {
        LOG.error("Exception fetching events", exc);
      }

      if (shutDown) {
        application.offline();
      }
    }
  }
}
