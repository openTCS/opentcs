/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
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
    extends WideComboBox<VehicleCommAdapterFactory>
    implements PropertyChangeListener {

  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");

  /**
   * Creates a new instance.
   */
  public CommAdapterComboBox() {
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (Objects.equals(evt.getPropertyName(), VehicleEntry.Attribute.COMM_ADAPTER_FACTORY.name())) {
      getModel().setSelectedItem(evt.getNewValue());
    }
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
        BUNDLE.getString("CommAdapterComboBox.confirmation.driverChange.text"),
        BUNDLE.getString("CommAdapterComboBox.confirmation.driverChange.title"),
        JOptionPane.YES_NO_OPTION);
    if (reply == JOptionPane.YES_OPTION) {
      super.setSelectedItem(anObject);
    }
  }
}
