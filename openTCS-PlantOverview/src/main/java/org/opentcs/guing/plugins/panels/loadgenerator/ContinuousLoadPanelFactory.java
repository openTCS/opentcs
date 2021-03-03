/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 * Creates load generator panels.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ContinuousLoadPanelFactory
    implements PluggablePanelFactory {

  /**
   * This classe's bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle");
  /**
   * A reference to the shared kernel provider.
   */
  private final SharedKernelProvider kernelProvider;
  /**
   * A provider for the actual panels.
   */
  private final Provider<ContinuousLoadPanel> panelProvider;

  /**
   * Creates a new instance.
   * 
   * @param kernelProvider The application's kernel provider.
   * @param panelProvider A provider for the actual panels.
   */
  @Inject
  public ContinuousLoadPanelFactory(SharedKernelProvider kernelProvider,
                                    Provider<ContinuousLoadPanel> panelProvider) {
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("Continuous_load");
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
    return kernelProvider != null && Kernel.State.OPERATING.equals(state);
  }
}
