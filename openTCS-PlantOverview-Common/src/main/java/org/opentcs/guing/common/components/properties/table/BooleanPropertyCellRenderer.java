/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import org.opentcs.guing.base.components.properties.type.BooleanProperty;
import org.opentcs.guing.base.components.properties.type.MultipleDifferentValues;

/**
 * A cell renderer for a boolean property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BooleanPropertyCellRenderer
    extends JCheckBox
    implements javax.swing.table.TableCellRenderer {

  /**
   * Creates a new instance of BooleanCellRenderer.
   */
  public BooleanPropertyCellRenderer() {
    super();
    setHorizontalAlignment(JCheckBox.LEFT);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    AttributesTable attributesTable = (AttributesTable) table;
    boolean editable = attributesTable.isEditable(row);

    if (isSelected) {
      setForeground(table.getSelectionForeground());
      super.setBackground(table.getSelectionBackground());
    }
    else {
      setForeground(table.getForeground());
      setBackground(editable ? table.getBackground() : StandardPropertyCellRenderer.BG_UNEDITABLE);
    }

    if (value instanceof BooleanProperty) {
      BooleanProperty property = (BooleanProperty) value;

      if (property.getValue() instanceof MultipleDifferentValues) {
        setBackground(AbstractPropertyCellEditor.DIFFERENT_VALUE_COLOR);
      }
      else if (property.getValue() instanceof Boolean) {
        setToolTipText(property.getHelptext());
        setSelected((boolean) property.getValue());
      }
      else {
        setEnabled(false);
      }
    }

    return this;
  }
}
