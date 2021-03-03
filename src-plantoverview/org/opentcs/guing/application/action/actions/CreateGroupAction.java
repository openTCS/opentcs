/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.CreateGroupPanel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to create a group.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CreateGroupAction extends AbstractAction {
  
  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createGroup";
  /**
   * The GUI manager instance we're working with.
   */
  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The GUI manager instance we're working with.
   */
  public CreateGroupAction(OpenTCSView openTCSView) {
    this.openTCSView = openTCSView;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CreateGroupPanel panel = new CreateGroupPanel(openTCSView);
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }
  
}
