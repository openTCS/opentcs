/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PluginPanelManager {

  /**
   * Shows or hides the specific {@code PanelFactory}.
   *
   * @param factory The factory resp. panel that shall be shown / hidden.
   * @param visible True to set it visible, false otherwise.
   */
  public void showPluginPanel(PluggablePanelFactory factory, boolean visible);
}
