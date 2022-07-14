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
import javax.swing.JComponent;
import javax.swing.JTable;
import org.opentcs.guing.base.components.properties.type.BooleanProperty;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * A cell editor for boolean properties.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BooleanPropertyCellEditor
    extends AbstractPropertyCellEditor {

  /**
   * Creates a new instance of BooleanCellEditor
   *
   * @param checkBox
   * @param umh
   */
  public BooleanPropertyCellEditor(JCheckBox checkBox, UserMessageHelper umh) {
    super(checkBox, umh);
    checkBox.setHorizontalAlignment(JCheckBox.LEFT);
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    setValue(value);
    JCheckBox checkBox = (JCheckBox) getComponent();
    checkBox.setBackground(table.getBackground());

    if (property().getValue() instanceof Boolean) {
      checkBox.setSelected((boolean) property().getValue());
    }

    return fComponent;
  }

  @Override
  public Object getCellEditorValue() {
    JCheckBox checkBox = (JCheckBox) getComponent();
    boolean newValue = checkBox.isSelected();
    property().setValue(newValue);

    if (property().getValue() instanceof Boolean) {
      markProperty();
    }

    return property();
  }

  /**
   * Return the property of this editor.
   *
   * @return The property of this editor.
   */
  protected BooleanProperty property() {
    return (BooleanProperty) fProperty;
  }

  /**
   * Creates the details dialog.
   * Always returns null, does not create a details dialog.
   * 
   * @return always returns null, does not create a details dialog.
   */
  @Override
  protected JComponent createButtonDetailsDialog() {
    return null;
  }
}
