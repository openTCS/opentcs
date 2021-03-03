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
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;

/**
 * Action for pausing all vehicles..
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class PauseAllVehiclesAction
    extends AbstractAction {

  public final static String ID = "openTCS.pauseAllVehicles";
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view
   */
  public PauseAllVehiclesAction(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.pauseAllVehicles();
  }
}
