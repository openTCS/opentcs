/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to show a list of all vehicles.
 *  
 * @author Heinz Huber (Fraunhofer IML)
 */
public class ShowVehiclesAction
    extends AbstractAction {

  public final static String ID = "actions.showVehicles";
  private final GuiManager view;

  /**
   * Creates a new instance.
   *
   * @param view
   */
  public ShowVehiclesAction(GuiManager view) {
    this.view = view;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.showVehicles();
  }
}
