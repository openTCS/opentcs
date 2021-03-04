/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A TableCellRenderer for VehicleCommAdapterFactory instances.
 * This class provides a representation of any VehicleCommAdapterFactory instance by
 * writing its description on a JLabel.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
class VehicleCommAdapterFactoryTableCellRenderer
    extends DefaultTableCellRenderer {

  VehicleCommAdapterFactoryTableCellRenderer() {
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
    else if (value instanceof VehicleCommAdapterFactory) {
      setText(((VehicleCommAdapterFactory) value).getAdapterDescription());
    }
    else {
      throw new IllegalArgumentException("value");
    }
    return this;
  }

}
