// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernelcontrolcenter.peripherals;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * ListCellRenderer for the adapter combo box.
 */
final class AdapterFactoryCellRenderer
    implements
      ListCellRenderer<PeripheralCommAdapterDescription> {

  /**
   * A default renderer for creating the label.
   */
  private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

  /**
   * Creates a new instance.
   */
  AdapterFactoryCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends PeripheralCommAdapterDescription> list,
      PeripheralCommAdapterDescription value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
  ) {
    JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(
        list,
        value,
        index,
        isSelected,
        cellHasFocus
    );
    if (value != null) {
      label.setText(value.getDescription());
    }
    else {
      label.setText(" ");
    }
    return label;
  }
}
