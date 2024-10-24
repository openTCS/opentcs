// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

import org.opentcs.components.plantoverview.PluggablePanelFactory;

/**
 */
public interface PluginPanelManager {

  /**
   * Shows or hides the specific {@code PanelFactory}.
   *
   * @param factory The factory resp. panel that shall be shown / hidden.
   * @param visible True to set it visible, false otherwise.
   */
  void showPluginPanel(PluggablePanelFactory factory, boolean visible);
}
