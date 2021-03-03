/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.plugins;

import javax.swing.JPanel;

/**
 * Declares methods that a pluggable panel should provide for the enclosing
 * application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class PluggablePanel
    extends JPanel {

  /**
   * Enables/plugs in this panel.
   * This method is called by the enclosing application to give the panel the
   * opportunity to initialize/update its content before it is presented to the
   * user.
   */
  public void plugIn() {
    // By default, do nothing.
  }

  /**
   * Disables/plugs out this panel.
   * This method is called before the enclosing application disposes this panel,
   * giving it the opportunity to explicitly free resources, stop threads etc..
   */
  public void plugOut() {
    // By default, do nothing.
  }
}
