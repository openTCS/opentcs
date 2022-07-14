/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.modeleditor.application.OpenTCSView;

/**
 * Action for resetting the docking layout.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class RestoreDockingLayoutAction
    extends AbstractAction {

  public static final String ID = "openTCS.restoreDockingLayout";
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public RestoreDockingLayoutAction(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.resetWindowArrangement();
  }

}
