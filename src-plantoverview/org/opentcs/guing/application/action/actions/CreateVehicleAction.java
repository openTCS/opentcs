/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to trigger the creation of a vehicle.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CreateVehicleAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createVehicle";
  /**
   * The GUI manager instance we're working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param guiManager The GUI manager instance we're working with.
   */
  public CreateVehicleAction(GuiManager guiManager) {
    this.guiManager = guiManager;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.createModelComponent(VehicleModel.class);
  }
}
