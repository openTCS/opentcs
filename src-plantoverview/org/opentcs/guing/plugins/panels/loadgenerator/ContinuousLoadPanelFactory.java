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
  private final ResourceBundle bundle =
      ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle");
  /**
   * A reference to the kernel.
   */
  private Kernel kernel;

  /**
   * Creates a new instance.
   */
  public ContinuousLoadPanelFactory() {
    // Do nada.
  }

  @Override
  public void setKernel(Kernel kernel) {
    this.kernel = kernel;
  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("Continuous_load");
  }

  @Override
  public PluggablePanel createPanel() {
    if (kernel == null || !providesPanel(kernel.getState())) {
      return null;
    }
    else {
      return new ContinuousLoadPanel(kernel);
    }
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return Kernel.State.OPERATING.equals(state);
  }
}
