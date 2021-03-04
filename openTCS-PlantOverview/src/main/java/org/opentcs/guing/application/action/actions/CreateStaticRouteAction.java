/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to trigger the creation of a static route.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CreateStaticRouteAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createStaticRoute";
  /**
   * The GUI manager instance we're working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param guiManager The GUI manager instance we're working with.
   */
  public CreateStaticRouteAction(GuiManager guiManager) {
    this.guiManager = guiManager;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.createModelComponent(StaticRouteModel.class);
  }
}
