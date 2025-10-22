// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a vehicle's acceptable order types with the kernel when it changes.
 */
public class VehicleAcceptableOrderTypesAdapter
    implements
      AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(
      VehicleAcceptableOrderTypesAdapter.class
  );
  /**
   * The vehicle model.
   */
  private final VehicleModel model;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The vehicle's acceptable order types the last time we checked them.
   */
  private Set<AcceptableOrderType> previousAcceptableOrderTypes;

  /**
   * Creates a new instance.
   *
   * @param portalProvider A kernel provider.
   * @param model The vehicle model.
   */
  public VehicleAcceptableOrderTypesAdapter(
      SharedKernelServicePortalProvider portalProvider,
      VehicleModel model
  ) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.model = requireNonNull(model, "model");
    this.previousAcceptableOrderTypes = getAcceptableOrderTypes();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }

    Set<AcceptableOrderType> acceptableOrderTypes = getAcceptableOrderTypes();
    if (previousAcceptableOrderTypes.equals(acceptableOrderTypes)) {
      LOG.debug("Ignoring vehicle properties update as the acceptable order types did not change");
      return;
    }

    previousAcceptableOrderTypes = acceptableOrderTypes;
    new Thread(() -> updateAcceptableOrderTypesInKernel(acceptableOrderTypes)).start();
  }

  private Set<AcceptableOrderType> getAcceptableOrderTypes() {
    return model.getPropertyAcceptableOrderTypes().getItems().stream()
        .map(orderType -> new AcceptableOrderType(orderType.getName(), orderType.getPriority()))
        .collect(Collectors.toSet());
  }

  private void updateAcceptableOrderTypesInKernel(Set<AcceptableOrderType> acceptableOrderTypes) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        Vehicle vehicle
            = portal.getVehicleService().fetch(Vehicle.class, model.getName()).orElseThrow();
        if (vehicle.getAcceptableOrderTypes().size() == acceptableOrderTypes.size()
            && vehicle.getAcceptableOrderTypes().containsAll(acceptableOrderTypes)) {
          LOG.debug("Ignoring vehicle properties update. Already up do date.");
          return;
        }
        portal.getVehicleService().updateVehicleAcceptableOrderTypes(
            vehicle.getReference(),
            acceptableOrderTypes
        );
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
