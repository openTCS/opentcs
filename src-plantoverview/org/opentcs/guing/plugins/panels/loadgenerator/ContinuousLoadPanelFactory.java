/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ResourceBundle;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.util.gui.plugins.PanelFactory;
import org.opentcs.util.gui.plugins.PluggablePanel;

/**
 * Creates load generator panels.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ContinuousLoadPanelFactory
    implements PanelFactory {

  /**
   * This classe's bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle");
  /**
   * A reference to the shared kernel provider.
   */
  private SharedKernelProvider kernelProvider;

  /**
   * Creates a new instance.
   */
  public ContinuousLoadPanelFactory() {
    // Do nada.
  }

  @Override
  public void setKernelProvider(SharedKernelProvider kernelProvider) {
    this.kernelProvider = kernelProvider;
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

    return new ContinuousLoadPanel(kernelProvider);
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return kernelProvider != null && Kernel.State.OPERATING.equals(state);
  }
}
