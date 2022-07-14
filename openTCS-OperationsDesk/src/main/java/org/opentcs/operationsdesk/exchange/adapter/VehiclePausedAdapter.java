/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a vehicle's paused state with the kernel when it changes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehiclePausedAdapter
    implements AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehiclePausedAdapter.class);
  /**
   * The vehicle model.
   */
  private final VehicleModel model;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The vehicle's paused state the last time we checked it.
   */
  private boolean pausedPreviously;

  /**
   * Creates a new instance.
   *
   * @param portalProvider A kernel provider.
   * @param model The vehicle model.
   */
  public VehiclePausedAdapter(SharedKernelServicePortalProvider portalProvider,
                              VehicleModel model) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.model = requireNonNull(model, "model");
    this.pausedPreviously = isVehiclePaused();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }

    boolean paused = isVehiclePaused();
    if (paused == pausedPreviously) {
      return;
    }
    pausedPreviously = paused;

    new Thread(() -> updatePausedInKernel(paused)).start();
  }

  private boolean isVehiclePaused() {
    return Boolean.TRUE.equals(model.getPropertyPaused().getValue());
  }

  private void updatePausedInKernel(boolean paused) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();

      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        Vehicle vehicle = portal.getVehicleService().fetchObject(Vehicle.class, model.getName());
        portal.getVehicleService().updateVehiclePaused(vehicle.getReference(), paused);
      }
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
