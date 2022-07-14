/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.opentcs.guing.base.components.properties.type.AbstractProperty;
import org.opentcs.guing.base.components.properties.type.ModelAttribute;
import org.opentcs.guing.base.components.properties.type.Selectable;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * A cell editor for a selection property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SelectionPropertyCellEditor
    extends AbstractPropertyCellEditor {

  /**
   * Creates a new instance of ComboBoxCellEditor
   *
   * @param comboBox
   * @param umh
   */
  public SelectionPropertyCellEditor(JComboBox<?> comboBox, UserMessageHelper umh) {
    super(comboBox, umh);
    comboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
  }

  @Override
  @SuppressWarnings("unchecked")
  public JComboBox<Object> getComponent() {
    return (JComboBox<Object>) super.getComponent();
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    setValue(value);
    JComboBox<Object> comboBox = getComponent();
    comboBox.setModel(new DefaultComboBoxModel<>(((Selectable) property()).getPossibleValues().toArray()));
    comboBox.setSelectedItem(property().getValue());

    return fComponent;
  }

  @Override
  public Object getCellEditorValue() {
    if (property().getChangeState() == ModelAttribute.ChangeState.DETAIL_CHANGED) {
      Object value = property().getValue();  // DEBUG
    }
    else {
      Object selectedItem = getComponent().getSelectedItem();
      Object oldValue = property().getValue();
      property().setValue(selectedItem);

      if (!selectedItem.equals(oldValue)) {
        markProperty();
      }
    }

    return property();
  }

  /**
   * Return the property for this editor.
   *
   * @return the property for this editor.
   */
  protected AbstractProperty property() {
    return (AbstractProperty) fProperty;
  }
}
