/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.table;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.PointTypeProperty;
import org.opentcs.guing.model.elements.PointModel.Type;

/**
 * A cell renderer for a {@link PointTypeProperty}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PointTypePropertyCellRenderer
    extends StandardPropertyCellRenderer {

  public PointTypePropertyCellRenderer() {
    super();
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                                                                hasFocus, row, column);

    if (value instanceof PointTypeProperty 
        && ((PointTypeProperty) value).getValue() instanceof Type) {
      PointTypeProperty property = (PointTypeProperty) value;
      Type type = (Type) property.getValue();
      label.setText(type.getDescription());
    }

    decorate(table, row, column, label, value);

    return this;
  }
}
