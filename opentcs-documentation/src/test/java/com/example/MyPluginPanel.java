// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
// tag::tutorial_gettingstarted_MyPluginPanel[]
package com.example;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exemplary plugin panel.
 */
public final class MyPluginPanel
    extends
      PluggablePanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MyPluginPanel.class);
  /**
   * Provides access to a remote kernel's service portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * Provides access to a remote kernel's services.
   */
  private SharedKernelServicePortal portal;
  /**
   * Indicates whether this panel is initialized.
   */
  private boolean initialized;

  @Inject
  public MyPluginPanel(SharedKernelServicePortalProvider portalProvider) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JButton buttonEnable = new JButton("Enable all vehicles");
    buttonEnable.addActionListener(e -> {
      updateIntegrationLevels(Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    });
    add(buttonEnable);

    JButton buttonDisable = new JButton("Disable all vehicles");
    buttonDisable.addActionListener(e -> {
      updateIntegrationLevels(Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    });
    add(buttonDisable);
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    // Get access to the kernel's services.
    try {
      portal = portalProvider.register();
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Kernel connection unavailable", exc);
      return;
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
      return;
    }

    portal.close();
    portal = null;

    initialized = false;
  }

  private void updateIntegrationLevels(Vehicle.IntegrationLevel level) {
    VehicleService vehicleService = portal.getPortal().getVehicleService();
    for (Vehicle vehicle : vehicleService.fetch(Vehicle.class)) {
      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(), level);
    }
  }
}
// end::tutorial_gettingstarted_MyPluginPanel[]
