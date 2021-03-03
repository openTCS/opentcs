/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.GuiManager.OperationMode;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.course.VehicleAction;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.IconToolkit;

/**
 * Ein Fahrzeug-Objekt in der Baumansicht. <p> <b>Entwurfsmuster:</b> Befehl.
 * VehicleUserObject ist ein konkreter Befehl.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleUserObject
		extends AbstractUserObject {

	/**
	 * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
	 */
	public VehicleUserObject(VehicleModel model) {
		super(model);
	}

  @Override
  public VehicleModel getModelComponent() {
    return (VehicleModel) super.getModelComponent();
  }

	@Override	// AbstractUserObject
	public void doubleClicked() {
		OpenTCSView.instance().figureSelected(getModelComponent());
	}

	@Override	// AbstractUserObject
	public JPopupMenu getPopupMenu() {
		if (OpenTCSView.instance().getOperationMode() == OperationMode.OPERATING) {
			return VehicleAction.createVehicleMenu(getModelComponent());
		}

		return null;
	}

	@Override	// AbstractUserObject
	public ImageIcon getIcon() {
		return IconToolkit.instance().createImageIcon("tree/vehicle.18x18.png");
	}
}
