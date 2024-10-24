// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.plugins.panels.loadgenerator.I18nPlantOverviewPanelLoadGenerator.BUNDLE_PATH;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.ResourceBundle;
import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 * Creates load generator panels.
 */
public class ContinuousLoadPanelFactory
    implements
      PluggablePanelFactory {

  /**
   * This class's bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * A provider for the actual panels.
   */
  private final Provider<ContinuousLoadPanel> panelProvider;

  /**
   * Creates a new instance.
   *
   * @param panelProvider A provider for the actual panels.
   */
  @Inject
  public ContinuousLoadPanelFactory(Provider<ContinuousLoadPanel> panelProvider) {
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("continuousLoadPanelFactory.panelDescription");
  }

  @Override
  public PluggablePanel createPanel(Kernel.State state) {
    if (!providesPanel(state)) {
      return null;
    }

    return panelProvider.get();
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return Kernel.State.OPERATING.equals(state);
  }
}
