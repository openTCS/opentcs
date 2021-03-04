/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.management.AttachmentEvent;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.ProcessModelEvent;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a pool of {@link LocalVehicleEntry}s with an entry for every {@link Vehicle} object in
 * the kernel.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LocalVehicleEntryPool
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocalVehicleEntryPool.class);
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
  private final Map<String, LocalVehicleEntry> entries = new TreeMap<>();
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
  public LocalVehicleEntryPool(KernelServicePortal servicePortal,
                               @ServiceCallWrapper CallWrapper callWrapper,
                               @ApplicationEventBus EventSource eventSource) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.eventSource = requireNonNull(eventSource, "eventSource");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized.");
      return;
    }

    eventSource.subscribe(this);

    try {

      Set<Vehicle> vehicles = callWrapper.call(() -> servicePortal.getVehicleService().fetchObjects(Vehicle.class));
      for (Vehicle vehicle : vehicles) {
        AttachmentInformation ai
            = callWrapper.call(() -> servicePortal.getVehicleService().fetchAttachmentInformation(vehicle.getReference()));
        VehicleProcessModelTO processModel
            = callWrapper.call(() -> servicePortal.getVehicleService().fetchProcessModel(vehicle.getReference()));
        LocalVehicleEntry entry = new LocalVehicleEntry(ai, processModel);
        entries.put(vehicle.getName(), entry);
      }
    }
    catch (Exception ex) {
      LOG.warn("Error initializing local vehicle entry pool", ex);
      entries.clear();
      return;
    }

    LOG.debug("Initialized vehicle entry pool: {}", entries);
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Not initialized.");
      return;
    }

    eventSource.unsubscribe(this);

    entries.clear();
    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof ProcessModelEvent) {
      ProcessModelEvent e = (ProcessModelEvent) event;
      LocalVehicleEntry entry = getEntryFor(e.getUpdatedProcessModel().getVehicleName());
      if (entry == null) {
        return;
      }
      entry.setProcessModel(e.getUpdatedProcessModel());
    }
    else if (event instanceof AttachmentEvent) {
      AttachmentEvent e = (AttachmentEvent) event;
      LocalVehicleEntry entry = getEntryFor(e.getVehicleName());
      if (entry == null) {
        return;
      }
      entry.setAttachmentInformation(e.getUpdatedAttachmentInformation());
    }
  }

  @Nonnull
  public Map<String, LocalVehicleEntry> getEntries() {
    return entries;
  }

  @Nullable
  public LocalVehicleEntry getEntryFor(String vehicleName) {
    return vehicleName == null ? null : entries.get(vehicleName);
  }
}
