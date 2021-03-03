/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.table;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.BooleanProperty;

/**
 * Ein CellRenderer für Attribute vom Typ {
 *
 * @see BooleanProperty}. Ein solches Attribut wird nicht durch einen Text
 * dargestellt, sondern durch ein Kästchen mit einem Häkchen (abhängig vom
 * Zustand des Attributs). Zum Einsatz kommt daher eine Checkbox.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BooleanPropertyCellRenderer
    extends JCheckBox
    implements javax.swing.table.TableCellRenderer {

  /**
   * Creates a new instance of BooleanCellRenderer
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

      if (property.getValue() instanceof Boolean) {
        setToolTipText(property.getHelptext());
        setSelected((boolean) property.getValue());
        if (property.isCollectionAndHasDifferentValues()) {
          setBackground(AbstractPropertyCellEditor.DIFFERENT_VALUE_COLOR);
        }
      }
      else {
        setEnabled(false);
      }
    }

    return this;
  }
}
