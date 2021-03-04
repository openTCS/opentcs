/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages attachment and detachment of communication adapters to vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AttachmentManager
    implements Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AttachmentManager.class);
  /**
   * This class's configuration.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The kernel.
   */
  private final LocalKernel kernel;
  /**
   * The vehicle controller pool.
   */
  private final LocalVehicleControllerPool controllerPool;
  /**
   * The comm adapter registry.
   */
  private final VehicleCommAdapterRegistry commAdapterRegistry;
  /**
   * The pool of vehicle entries.
   */
  private final VehicleEntryPool vehicleEntryPool;
  /**
   * Whether the attachment manager is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param controllerPool The vehicle controller pool.
   * @param commAdapterRegistry The comm adapter registry.
   * @param vehicleEntryPool The pool of vehicle entries.
   * @param configuration This class's configuration.
   */
  @Inject
  public AttachmentManager(@Nonnull LocalKernel kernel,
                           @Nonnull LocalVehicleControllerPool controllerPool,
                           @Nonnull VehicleCommAdapterRegistry commAdapterRegistry,
                           @Nonnull VehicleEntryPool vehicleEntryPool,
                           @Nonnull KernelApplicationConfiguration configuration) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
    this.vehicleEntryPool = requireNonNull(vehicleEntryPool, "vehicleEntryPool");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized.");
      return;
    }

    commAdapterRegistry.initialize();
    vehicleEntryPool.initialize();

    if (configuration.autoAttachDriversOnStartup()) {
      autoAttachAllAdapters();
    }
    if (configuration.autoEnableDriversOnStartup()) {
      autoEnableAllAdapters();
    }

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

    // Detach all attached drivers to clean up.
    detachAllAdapters();
    vehicleEntryPool.terminate();
    commAdapterRegistry.terminate();

    initialized = false;
  }

  /**
   * Attaches an adapter to a vehicle.
   *
   * @param vehicleName The vehicle name.
   * @param factory The factory that provides the adapter to be assigned.
   */
  public void attachAdapterToVehicle(@Nonnull String vehicleName,
                                     @Nonnull VehicleCommAdapterFactory factory) {
    requireNonNull(vehicleName, "vehicleName");
    requireNonNull(factory, "factory");

    VehicleEntry vehicleEntry = vehicleEntryPool.getEntryFor(vehicleName);
    if (vehicleEntry == null) {
      LOG.warn("No vehicle entry found for '{}'. Entries: {}",
               vehicleName,
               vehicleEntryPool);
      return;
    }

    detachAdapterFromVehicle(vehicleName, true);

    VehicleCommAdapter commAdapter = factory.getAdapterFor(vehicleEntry.getVehicle());
    if (commAdapter == null) {
      LOG.warn("Factory {} did not provide adapter for vehicle {}, ignoring.",
               factory,
               vehicleEntry.getVehicle().getName());
      return;
    }

    commAdapter.initialize();
    controllerPool.attachVehicleController(vehicleEntry.getVehicle().getName(), commAdapter);

    vehicleEntry.setCommAdapterFactory(factory);
    vehicleEntry.setCommAdapter(commAdapter);
    vehicleEntry.setProcessModel(commAdapter.getProcessModel());

    kernel.setTCSObjectProperty(vehicleEntry.getVehicle().getReference(),
                                Vehicle.PREFERRED_ADAPTER,
                                factory.getClass().getName());
  }

  public void detachAdapterFromVehicle(@Nonnull String vehicleName,
                                       boolean doDetachVehicleController) {
    requireNonNull(vehicleName, "vehicleName");

    VehicleEntry vehicleEntry = vehicleEntryPool.getEntryFor(vehicleName);
    if (vehicleEntry == null) {
      LOG.warn("No vehicle entry found for '{}'. Entries: {}",
               vehicleName,
               vehicleEntryPool);
      return;
    }

    VehicleCommAdapter commAdapter = vehicleEntry.getCommAdapter();
    if (commAdapter != null) {
      commAdapter.disable();
      // Let the adapter know cleanup time is here.
      vehicleEntry.setCommAdapter(null);
      commAdapter.terminate();
      vehicleEntry.setCommAdapterFactory(new NullVehicleCommAdapterFactory());
      vehicleEntry.setProcessModel(new VehicleProcessModel(vehicleEntry.getVehicle()));
    }
    if (doDetachVehicleController) {
      controllerPool.detachVehicleController(vehicleEntry.getVehicle().getName());
    }
  }

  public void autoAttachAdapterToVehicle(@Nonnull String vehicleName) {
    requireNonNull(vehicleName, "vehicleName");

    VehicleEntry vehicleEntry = vehicleEntryPool.getEntryFor(vehicleName);
    if (vehicleEntry == null) {
      LOG.warn("No vehicle entry found for '{}'. Entries: {}",
               vehicleName,
               vehicleEntryPool);
      return;
    }

    // Do not auto-attach if there is already a comm adapter attached to the vehicle.
    if (vehicleEntry.getCommAdapter() != null) {
      return;
    }

    Vehicle vehicle = getUpdatedVehicle(vehicleEntry.getVehicle());
    String prefAdapter = vehicle.getProperties().get(Vehicle.PREFERRED_ADAPTER);
    VehicleCommAdapterFactory factory = findFactoryWithName(prefAdapter);
    if (factory != null) {
      attachAdapterToVehicle(vehicleName, factory);
    }
    else {
      if (!Strings.isNullOrEmpty(prefAdapter)) {
        LOG.info("Couldn't autoattach preferred adapter {} to {}, as the adapter doesn't exist. "
            + "Attaching the first adapter that is available.",
                 prefAdapter,
                 vehicleEntry.getVehicle().getName());
      }
      List<VehicleCommAdapterFactory> factories
          = commAdapterRegistry.findFactoriesFor(vehicleEntry.getVehicle());
      if (!factories.isEmpty()) {
        attachAdapterToVehicle(vehicleName, factories.get(0));
      }
    }
  }

  public void autoAttachAllAdapters() {
    vehicleEntryPool.getEntries().forEach((vehicleName, entry) -> {
      autoAttachAdapterToVehicle(vehicleName);
    });
  }

  /**
   * Returns a fresh copy of a vehicle from the kernel.
   *
   * @param vehicle The old vehicle instance.
   * @return The fresh vehicle instance.
   */
  private Vehicle getUpdatedVehicle(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return kernel.getTCSObjects(Vehicle.class).stream()
        .filter(updatedVehicle -> Objects.equals(updatedVehicle.getName(), vehicle.getName()))
        .findFirst().orElse(vehicle);
  }

  private void autoEnableAllAdapters() {
    vehicleEntryPool.getEntries().values().stream()
        .map(entry -> entry.getCommAdapter())
        .filter(adapter -> adapter != null)
        .filter(adapter -> !adapter.isEnabled())
        .forEach(adapter -> adapter.enable());
  }

  private void detachAllAdapters() {
    LOG.debug("Detaching vehicle communication adapters...");
    vehicleEntryPool.getEntries().forEach((vehicleName, entry) -> {
      detachAdapterFromVehicle(vehicleName, false);
    });
    LOG.debug("Detached vehicle communication adapters");
  }

  @Nullable
  private VehicleCommAdapterFactory findFactoryWithName(@Nullable String name) {
    return commAdapterRegistry.getFactories().stream()
        .filter(factory -> factory.getClass().getName().equals(name))
        .findFirst()
        .orElse(null);
  }
}
