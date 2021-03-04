/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a vehicle's processable categories with the kernel when it changes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class VehicleProcessableCategoriesAdapter
    implements AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleProcessableCategoriesAdapter.class);
  /**
   * The vehicle model.
   */
  private final VehicleModel model;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The state of the plant overview.
   */
  private final ApplicationState applicationState;
  /**
   * The vehicle's processable categories the last time we checked them.
   */
  private Set<String> previousProcessableCategories;

  /**
   * Creates a new instance.
   *
   * @param portalProvider A kernel provider.
   * @param applicationState Keeps the plant overview's state.
   * @param model The vehicle model.
   */
  public VehicleProcessableCategoriesAdapter(SharedKernelServicePortalProvider portalProvider,
                                             ApplicationState applicationState,
                                             VehicleModel model) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.applicationState = requireNonNull(applicationState, "applicationState");
    this.model = requireNonNull(model, "model");
    this.previousProcessableCategories = getProcessableCategories();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }
    if (applicationState.getOperationMode() != OperationMode.OPERATING) {
      LOG.debug("Ignoring vehicle properties update because the application is not in operating "
          + "mode.");
      return;
    }

    Set<String> processableCategories = getProcessableCategories();
    if (previousProcessableCategories.size() == processableCategories.size()
        && previousProcessableCategories.containsAll(processableCategories)) {
      LOG.debug("Ignoring vehicle properties update because the processable categories did not "
          + "change");
      return;
    }

    previousProcessableCategories = processableCategories;
    new Thread(() -> updateProcessableCategoriesInKernel(processableCategories)).start();
  }

  private Set<String> getProcessableCategories() {
    return model.getPropertyProcessableCategories().getItems();
  }

  private void updateProcessableCategoriesInKernel(Set<String> processableCategories) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        Vehicle vehicle = portal.getVehicleService().fetchObject(Vehicle.class, model.getName());
        if (vehicle.getProcessableCategories().size() == processableCategories.size()
            && vehicle.getProcessableCategories().containsAll(processableCategories)) {
          LOG.debug("Ignoring vehicle properties update. Already up do date.");
          return;
        }
        portal.getVehicleService().updateVehicleProcessableCategories(vehicle.getReference(),
                                                                      processableCategories);
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
