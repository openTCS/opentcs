/*
 *
 * Created on 24.09.2013 10:42:26
*/
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;

/**
 * Action for resetting the docking layout.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class RestoreDockingLayoutAction extends AbstractAction {
  
  public static final String ID = "openTCS.restoreDockingLayout";
  private final OpenTCSView view;
  
  public RestoreDockingLayoutAction(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.restoreDockingDefaultLayout();
  }

}


