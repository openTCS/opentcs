/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.SimVehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages attachment and detachment of communication adapters to vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AttachmentManager {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AttachmentManager.class);
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
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param controllerPool The vehicle controller pool.
   * @param commAdapterRegistry The comm adapter registry.
   */
  @Inject
  public AttachmentManager(@Nonnull LocalKernel kernel,
                           @Nonnull LocalVehicleControllerPool controllerPool,
                           @Nonnull VehicleCommAdapterRegistry commAdapterRegistry) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
  }

  /**
   * Attaches an adapter to a vehicle.
   *
   * @param vehicleEntry The vehicle model.
   * @param factory The factory that provides the adapter to be assigned.
   */
  public void attachAdapterToVehicle(@Nonnull VehicleEntry vehicleEntry,
                                     @Nonnull VehicleCommAdapterFactory factory) {
    requireNonNull(vehicleEntry, "vehicleEntry");
    requireNonNull(factory, "factory");

    detachAdapterFromVehicle(vehicleEntry, true);

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

    // Set initial vehicle position if related property is set
    // XXX This should actually be done within the kernel, after the comm adapter has been created.
    if (commAdapter instanceof SimVehicleCommAdapter) {
      Vehicle vehicle = vehicleEntry.getVehicle();
      String initialPos = vehicle.getProperties().get(ObjectPropConstants.VEHICLE_INITIAL_POSITION);
      if (initialPos != null) {
        ((SimVehicleCommAdapter) commAdapter).initVehiclePosition(initialPos);
      }
    }
  }

  public void detachAdapterFromVehicle(@Nonnull VehicleEntry vehicleEntry,
                                       boolean doDetachVehicleController) {
    requireNonNull(vehicleEntry, "vehicleEntry");

    VehicleCommAdapter commAdapter = vehicleEntry.getCommAdapter();
    if (commAdapter != null) {
      commAdapter.disable();
      // Let the adapter know cleanup time is here.
      vehicleEntry.setCommAdapter(null);
      commAdapter.terminate();
      vehicleEntry.setCommAdapterFactory(new NullVehicleCommAdapterFactory());
      vehicleEntry.setSelectedTabIndex(0);
      vehicleEntry.setProcessModel(new VehicleProcessModel(vehicleEntry.getVehicle()));
    }
    if (doDetachVehicleController) {
      controllerPool.detachVehicleController(vehicleEntry.getVehicle().getName());
    }
  }

  public void autoAttachAdapterToVehicle(@Nonnull VehicleEntry vehicleEntry) {
    requireNonNull(vehicleEntry, "vehicleEntry");

    // Do not auto-attach if there is already a comm adapter attached to the vehicle.
    if (vehicleEntry.getCommAdapter() != null) {
      return;
    }

    Vehicle veh = getUpdatedVehicle(vehicleEntry.getVehicle());
    Optional<String> prefAdapterName
        = Optional.ofNullable(veh.getProperties().get(Vehicle.PREFERRED_ADAPTER));
    boolean foundFactory = false;
    if (prefAdapterName.isPresent()) {
      for (VehicleCommAdapterFactory factory : commAdapterRegistry.getFactories()) {
        if (prefAdapterName.get().equals(factory.getClass().getName())) {
          attachAdapterToVehicle(vehicleEntry, factory);
          foundFactory = true;
          // XXX Is it necessary to update the model here, at all?
//          ((VehicleTableModel) vehicleTable.getModel()).update(vehicleModel, null);
          break;
        }
      }
      if (!foundFactory) {
        LOG.info("Couldn't autoattach preferred adapter {} to {}, as the adapter doesn't exist.",
                 prefAdapterName.get(),
                 vehicleEntry.getVehicle().getName());
      }
    }
    if (!foundFactory) {
      List<VehicleCommAdapterFactory> factories
          = commAdapterRegistry.findFactoriesFor(vehicleEntry.getVehicle());
      // Attach the first adapter that is available.
      if (!factories.isEmpty()) {
        attachAdapterToVehicle(vehicleEntry, factories.get(0));
      }
    }
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
}
