/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 * Provides a {@link ResourceAllocationPanel} for the plant overview if the kernel is in operating
 * state.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ResourceAllocationPanelFactory
    implements PluggablePanelFactory {

  /**
   * This classe's bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/allocation/Bundle");
  /**
   * The provider for the portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * The provider for the panel this factory wants to create.
   */
  private final Provider<ResourceAllocationPanel> panelProvider;

  /**
   * Creates a new instance.
   *
   * @param portalProvider the provider for access to the kernel
   * @param panelProvider the provider for the panel
   */
  @Inject
  public ResourceAllocationPanelFactory(SharedKernelServicePortalProvider portalProvider,
                                        Provider<ResourceAllocationPanel> panelProvider) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return (state == Kernel.State.OPERATING);

  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("resource_allocation");
  }

  @Override
  public PluggablePanel createPanel(Kernel.State state) {
    if (state != Kernel.State.OPERATING) {
      return null;
    }
    if (portalProvider == null || !portalProvider.portalShared()) {
      return null;
    }
    return panelProvider.get();
  }

}
