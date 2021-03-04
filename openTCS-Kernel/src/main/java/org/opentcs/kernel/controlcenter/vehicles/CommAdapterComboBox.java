/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.util.gui.WideComboBox;

/**
 * A wide combobox which sets the selected item when receiving an update event from a
 * {@link VehicleCommAdapterFactory}.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CommAdapterComboBox
    extends WideComboBox<VehicleCommAdapterFactory> {

  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");

  /**
   * Creates a new instance.
   */
  public CommAdapterComboBox() {
  }

  @Override
  public void setSelectedItem(Object anObject) {
    // If the selected item hasn't changed, do nothing.
    if (anObject == getModel().getSelectedItem()) {
      return;
    }
    // If the previously selected item was a dummy, just accept the new item.
    if (getModel().getSelectedItem() instanceof NullVehicleCommAdapterFactory) {
      super.setSelectedItem(anObject);
      return;
    }
    // Since the previously selected item was not a dummy, let the user confirm the change.
    int reply = JOptionPane.showConfirmDialog(
        null,
        bundle.getString("CommAdapterComboBox.confirmation.driverChange.text"),
        bundle.getString("CommAdapterComboBox.confirmation.driverChange.title"),
        JOptionPane.YES_NO_OPTION);
    if (reply == JOptionPane.YES_OPTION) {
      super.setSelectedItem(anObject);
    }
  }
}
