/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to find a vehicle on the drawing.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class FindVehicleAction
    extends AbstractAction {

  public final static String ID = "actions.findVehicle";
  private final GuiManager view;

  /**
   * Creates a new instance.
   *
   * @param view
   */
  public FindVehicleAction(GuiManager view) {
    this.view = view;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.findVehicle();
  }
}
