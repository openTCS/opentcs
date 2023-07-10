/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link TableCellRenderer} for {@link PeripheralCommAdapterDescription} instances.
 * This class provides a representation of any PeripheralCommAdapterDescription instance by writing
 * its actual description on a JLabel.
 */
class CommAdapterFactoryTableCellRenderer
    extends DefaultTableCellRenderer {

  CommAdapterFactoryTableCellRenderer() {
  }

  @Override
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column)
      throws IllegalArgumentException {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value == null) {
      setText("");
    }
    else if (value instanceof PeripheralCommAdapterDescription) {
      setText(((PeripheralCommAdapterDescription) value).getDescription());
    }
    else {
      throw new IllegalArgumentException("value");
    }
    return this;
  }

}
