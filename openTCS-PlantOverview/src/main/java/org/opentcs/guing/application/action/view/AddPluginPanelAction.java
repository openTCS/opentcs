/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.guing.application.OpenTCSView;

/**
 * An action to add a plugin panel.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddPluginPanelAction
    extends AbstractAction {

  public final static String ID = "view.addPluginPanel";
  private final PluggablePanelFactory factory;
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   * @param factory The pluggable panel factory
   */
  public AddPluginPanelAction(OpenTCSView view, PluggablePanelFactory factory) {
    this.view = Objects.requireNonNull(view, "view is null");
    this.factory = Objects.requireNonNull(factory, "panelID is null");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
    view.showPluginPanel(factory, item.isSelected());
  }
}
