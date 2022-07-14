/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import static java.util.Objects.requireNonNull;
import java.util.Set;
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
 * Updates a vehicle's allowed order types with the kernel when it changes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class VehicleAllowedOrderTypesAdapter
    implements AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleAllowedOrderTypesAdapter.class);
  /**
   * The vehicle model.
   */
  private final VehicleModel model;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The vehicle's allowed order types the last time we checked them.
   */
  private Set<String> previousAllowedOrderTypes;

  /**
   * Creates a new instance.
   *
   * @param portalProvider A kernel provider.
   * @param model The vehicle model.
   */
  public VehicleAllowedOrderTypesAdapter(SharedKernelServicePortalProvider portalProvider,
                                         VehicleModel model) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.model = requireNonNull(model, "model");
    this.previousAllowedOrderTypes = getAllowedOrderTypes();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }

    Set<String> allowedOrderTypes = getAllowedOrderTypes();
    if (previousAllowedOrderTypes.equals(allowedOrderTypes)) {
      LOG.debug("Ignoring vehicle properties update as the allowed order types did not change");
      return;
    }

    previousAllowedOrderTypes = allowedOrderTypes;
    new Thread(() -> updateAllowedOrderTypesInKernel(allowedOrderTypes)).start();
  }

  private Set<String> getAllowedOrderTypes() {
    return model.getPropertyAllowedOrderTypes().getItems();
  }

  private void updateAllowedOrderTypesInKernel(Set<String> allowedOrderTypes) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        Vehicle vehicle = portal.getVehicleService().fetchObject(Vehicle.class, model.getName());
        if (vehicle.getAllowedOrderTypes().size() == allowedOrderTypes.size()
            && vehicle.getAllowedOrderTypes().containsAll(allowedOrderTypes)) {
          LOG.debug("Ignoring vehicle properties update. Already up do date.");
          return;
        }
        portal.getVehicleService().updateVehicleAllowedOrderTypes(vehicle.getReference(),
                                                                  allowedOrderTypes);
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
