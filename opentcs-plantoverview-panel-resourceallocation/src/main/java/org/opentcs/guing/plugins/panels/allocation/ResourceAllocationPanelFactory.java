/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.plugins.panels.allocation.I18nPlantOverviewPanelResourceAllocation.BUNDLE_PATH;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.ResourceBundle;
import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 * Provides a {@link ResourceAllocationPanel} for the plant overview if the kernel is in operating
 * state.
 */
public class ResourceAllocationPanelFactory
    implements
      PluggablePanelFactory {

  /**
   * This class's bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * The provider for the panel this factory wants to create.
   */
  private final Provider<ResourceAllocationPanel> panelProvider;

  /**
   * Creates a new instance.
   *
   * @param panelProvider the provider for the panel
   */
  @Inject
  public ResourceAllocationPanelFactory(Provider<ResourceAllocationPanel> panelProvider) {
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return (state == Kernel.State.OPERATING);
  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("resourceAllocationPanelFactory.panelDescription");
  }

  @Override
  public PluggablePanel createPanel(Kernel.State state) {
    if (!providesPanel(state)) {
      return null;
    }

    return panelProvider.get();
  }
}
