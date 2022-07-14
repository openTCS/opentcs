/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.guing.common.event.OperationModeChangeEvent;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.operationsdesk.event.KernelStateChangeEvent;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a set of all peripheral jobs existing on the kernel side.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobsContainer
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobsContainer.class);
  /**
   * Where we get events from.
   */
  private final EventBus eventBus;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The peripheral jobs.
   */
  private final Map<String, PeripheralJob> peripheralJobs = new HashMap<>();
  /**
   * This container's listeners.
   */
  private final Set<PeripheralJobsContainerListener> listeners = new HashSet<>();
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param eventBus Where this instance subscribes for events.
   * @param portalProvider Provides a access to a portal.
   */
  @Inject
  public PeripheralJobsContainer(@ApplicationEventBus EventBus eventBus,
                                 SharedKernelServicePortalProvider portalProvider) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public boolean isInitialized() {
    return initialized;
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
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      handleObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof OperationModeChangeEvent) {
      initJobs();
    }
    else if (event instanceof SystemModelTransitionEvent) {
      initJobs();
    }
    else if (event instanceof KernelStateChangeEvent) {
      initJobs();
    }
  }

  public void addListener(PeripheralJobsContainerListener listener) {
    listeners.add(listener);
  }

  public void removeListener(PeripheralJobsContainerListener listener) {
    listeners.remove(listener);
  }

  public Collection<PeripheralJob> getPeripheralJobs() {
    return peripheralJobs.values();
  }

  private void initJobs() {
    setPeripheralJobs(fetchJobsIfOnline());
    listeners.forEach(listener -> listener.containerInitialized(peripheralJobs.values()));
  }

  private void handleObjectEvent(TCSObjectEvent evt) {
    if (evt.getCurrentOrPreviousObjectState() instanceof PeripheralJob) {
      switch (evt.getType()) {
        case OBJECT_CREATED:
          peripheralJobAdded((PeripheralJob) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_MODIFIED:
          peripheralJobChanged((PeripheralJob) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_REMOVED:
          peripheralJobRemoved((PeripheralJob) evt.getCurrentOrPreviousObjectState());
          break;
        default:
          LOG.warn("Unhandled event type: {}", evt.getType());
      }
    }
  }

  private void peripheralJobAdded(PeripheralJob job) {
    peripheralJobs.put(job.getName(), job);
    listeners.forEach(listener -> listener.peripheralJobAdded(job));
  }

  private void peripheralJobChanged(PeripheralJob job) {
    peripheralJobs.put(job.getName(), job);
    listeners.forEach(listener -> listener.peripheralJobUpdated(job));
  }

  private void peripheralJobRemoved(PeripheralJob job) {
    peripheralJobs.remove(job.getName());
    listeners.forEach(listener -> listener.peripheralJobRemoved(job));
  }

  private void setPeripheralJobs(Set<PeripheralJob> newJobs) {
    peripheralJobs.clear();
    for (PeripheralJob job : newJobs) {
      peripheralJobs.put(job.getName(), job);
    }
  }

  private Set<PeripheralJob> fetchJobsIfOnline() {
    if (portalProvider.portalShared()) {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getPeripheralJobService().fetchObjects(PeripheralJob.class);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching peripheral jobs", exc);
      }
    }

    return new HashSet<>();
  }
}
