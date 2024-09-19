/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a vehicle's energy level threshold set.
 */
public class VehicleEnergyLevelThresholdSetAdapter
    implements
      AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(
      VehicleEnergyLevelThresholdSetAdapter.class
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
   * The vehicle's energy level threshold set the last time we checked it.
   */
  private Vehicle.EnergyLevelThresholdSet previousEnergyLevelThresholdSet;

  public VehicleEnergyLevelThresholdSetAdapter(
      SharedKernelServicePortalProvider portalProvider,
      VehicleModel model
  ) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.model = requireNonNull(model, "model");
    this.previousEnergyLevelThresholdSet = getEnergyLevelThresholdSet();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }

    EnergyLevelThresholdSet energyLevelThresholdSet = getEnergyLevelThresholdSet();
    if (Objects.equals(previousEnergyLevelThresholdSet, energyLevelThresholdSet)) {
      LOG.debug(
          "Ignoring vehicle properties update as the energy level threshold set did not change."
      );
      return;
    }

    previousEnergyLevelThresholdSet = energyLevelThresholdSet;
    new Thread(() -> updateEnergyLevelThresholdSetInKernel(energyLevelThresholdSet)).start();
  }

  private EnergyLevelThresholdSet getEnergyLevelThresholdSet() {
    return new EnergyLevelThresholdSet(
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelCritical(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelGood(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelSufficientlyRecharged(),
        model.getPropertyEnergyLevelThresholdSet().getValue().getEnergyLevelFullyRecharged()
    );
  }

  private void updateEnergyLevelThresholdSetInKernel(
      EnergyLevelThresholdSet energyLevelThresholdSet
  ) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        portal.getVehicleService().updateVehicleEnergyLevelThresholdSet(
            model.getVehicle().getReference(),
            energyLevelThresholdSet
        );
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
