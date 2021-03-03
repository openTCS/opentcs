/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Zeigt eine Ansicht aller im System verfügbaren Fahrzeuge.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AllVehiclesPanel
    extends DialogContent {

  /**
   * Creates a new instance of VehiclesPanel.
   */
  public AllVehiclesPanel() {
    setDialogTitle(ResourceBundleUtil.getBundle().getString("actions.showVehicles.text"));
    setModal(false);
    setPreferredSize(new Dimension(400, 200));
    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
  }

  @Override // DialogContent
  public void update() {
    // wird nicht benötigt
  }

  @Override // DialogContent
  public void initFields() {
    removeAll();

    SystemModel systemModel = OpenTCSView.instance().getSystemModel();

    for (VehicleModel vehicle : systemModel.getVehicleModels()) {
      add(new SingleVehicleView(vehicle));
    }
  }
}
