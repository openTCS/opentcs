/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application.action.view;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.guing.common.application.PluginPanelManager;

/**
 * An action to add a plugin panel.
 */
public class AddPluginPanelAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "view.addPluginPanel";
  private final PluggablePanelFactory factory;
  private final PluginPanelManager pluginPanelManager;

  /**
   * Creates a new instance.
   *
   * @param pluginPanelManager The openTCS view
   * @param factory The pluggable panel factory
   */
  public AddPluginPanelAction(PluginPanelManager pluginPanelManager,
                              PluggablePanelFactory factory) {
    this.pluginPanelManager = requireNonNull(pluginPanelManager, "pluginPanelManager");
    this.factory = requireNonNull(factory, "factory");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
    pluginPanelManager.showPluginPanel(factory, item.isSelected());
  }
}
