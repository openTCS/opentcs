/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComboBox;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A wide combobox which sets the selected item when receiving an update event from a
 * {@link LocalPeripheralEntry}.
 */
public class PeripheralAdapterComboBox
    extends JComboBox<PeripheralCommAdapterDescription>
    implements PropertyChangeListener {

  /**
   * Creates a new instance.
   */
  public PeripheralAdapterComboBox() {
  }

  @Override
  public PeripheralCommAdapterDescription getSelectedItem() {
    return (PeripheralCommAdapterDescription) super.getSelectedItem();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof LocalPeripheralEntry)) {
      return;
    }

    LocalPeripheralEntry entry = (LocalPeripheralEntry) evt.getSource();
    if (Objects.equals(entry.getAttachedCommAdapter(), getModel().getSelectedItem())) {
      return;
    }

    super.setSelectedItem(entry.getAttachedCommAdapter());
  }

}
