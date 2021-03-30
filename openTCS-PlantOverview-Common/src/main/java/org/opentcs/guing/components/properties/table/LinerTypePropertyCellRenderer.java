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
import org.opentcs.guing.components.properties.type.LinerTypeProperty;
import org.opentcs.guing.model.elements.PathModel.Type;

/**
 * A cell renderer for a {@link LinerTypeProperty}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LinerTypePropertyCellRenderer
    extends StandardPropertyCellRenderer {

  public LinerTypePropertyCellRenderer() {
    super();
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                                                                hasFocus, row, column);

    if (value instanceof LinerTypeProperty
        && ((LinerTypeProperty) value).getValue() instanceof Type) {
      LinerTypeProperty property = (LinerTypeProperty) value;
      Type type = (Type) property.getValue();
      label.setText(type.getDescription());
    }

    decorate(table, row, column, label, value);

    return this;
  }
}
