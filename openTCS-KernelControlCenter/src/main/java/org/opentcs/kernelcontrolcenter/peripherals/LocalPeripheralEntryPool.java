/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentEvent;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.drivers.peripherals.management.PeripheralProcessModelEvent;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pool of {@link LocalPeripheralEntry}'s for every location in the kernel that represents a
 * peripheral device.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LocalPeripheralEntryPool
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocalPeripheralEntryPool.class);
  /**
   * The service portal to use for kernel interactions.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * Where this instance registers for application events.
   */
  private final EventSource eventSource;
  /**
   * The entries of this pool.
   */
  private final Map<TCSResourceReference<Location>, LocalPeripheralEntry> entries = new HashMap<>();
  /**
   * Whether the pool is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param servicePortal The service portal to use for kernel interactions.
   * @param callWrapper The call wrapper to use for service calls.
   * @param eventSource Where this instance registers for application events.
   */
  @Inject
  public LocalPeripheralEntryPool(KernelServicePortal servicePortal,
                                  @ServiceCallWrapper CallWrapper callWrapper,
                                  @ApplicationEventBus EventSource eventSource) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.eventSource = requireNonNull(eventSource, "eventSource");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    try {
      initializeEntryMap();
      LOG.debug("Initialized peripheral entry pool: {}", entries);
    }
    catch (Exception e) {
      LOG.warn("Error initializing peripheral entry pool.", e);
      entries.clear();
      return;
    }

    eventSource.subscribe(this);

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

    eventSource.unsubscribe(this);
    entries.clear();

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof PeripheralProcessModelEvent) {
      onPeripheralProcessModelEvent((PeripheralProcessModelEvent) event);
    }
    else if (event instanceof PeripheralAttachmentEvent) {
      onPeripheralAttachmentEvent((PeripheralAttachmentEvent) event);
    }
  }

  public Map<TCSResourceReference<Location>, LocalPeripheralEntry> getEntries() {
    return entries;
  }

  private void initializeEntryMap()
      throws Exception {
    Set<Location> locations
        = callWrapper.call(() -> servicePortal.getPlantModelService().fetchObjects(Location.class));
    for (Location location : locations) {
      PeripheralAttachmentInformation ai = callWrapper.call(
          () -> servicePortal.getPeripheralService().fetchAttachmentInformation(location.getReference())
      );
      PeripheralProcessModel processModel = callWrapper.call(
          () -> servicePortal.getPeripheralService().fetchProcessModel(location.getReference()));
      entries.put(location.getReference(), new LocalPeripheralEntry(location.getReference(),
                                                                    ai.getAttachedCommAdapter(),
                                                                    processModel));
    }
  }

  private void onPeripheralProcessModelEvent(PeripheralProcessModelEvent event) {
    if (!entries.containsKey(event.getLocation())) {
      LOG.warn("Received an event for an unknown location: {}", event.getLocation().getName());
      return;
    }

    entries.get(event.getLocation()).setProcessModel(event.getProcessModel());
  }

  private void onPeripheralAttachmentEvent(PeripheralAttachmentEvent event) {
    if (!entries.containsKey(event.getLocation())) {
      LOG.warn("Received an event for an unknown location: {}", event.getLocation().getName());
      return;
    }

    entries.get(event.getLocation())
        .setAttachedCommAdapter(event.getAttachmentInformation().getAttachedCommAdapter());
  }
}
