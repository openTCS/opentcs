/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

import java.util.ResourceBundle;
import org.opentcs.access.Kernel;
import org.opentcs.util.gui.plugins.PanelFactory;
import org.opentcs.util.gui.plugins.PluggablePanel;

/**
 * Creates statistics panels.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsPanelFactory
    implements PanelFactory {

  /**
   * This class's bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/statistics/Bundle");

  /**
   * Creates a new instance.
   */
  public StatisticsPanelFactory() {
    // Do nada.
  }

  @Override
  public void setKernel(Kernel kernel) {
    // Do nada.
  }

  @Override
  public String getPanelDescription() {
    return bundle.getString("Statistics_panel_title");
  }

  @Override
  public PluggablePanel createPanel() {
    return new StatisticsPanel();
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return true;
  }
}
