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
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.Selectable;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Ein CellEditor für Attribute vom Typ {
 *
 * @see SelectionProperty}. Der Editor besteht nur aus einer ComboBox, so dass
 * der Benutzer aus einer Liste von Werten einen Wert auswählen kann. Einen
 * Button mit drei Punkten, bei dessen Anklicken sich ein DetailsDialog zum
 * komfortablen Bearbeiten des Attributs öffnet, gibt es nicht.
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
  public SelectionPropertyCellEditor(JComboBox<Object> comboBox, UserMessageHelper umh) {
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
    // Wenn das Objekt über den Popup-Dialog geändert wurde, wird dieser Wert übernommen
    if (property().getChangeState() == ModelAttribute.ChangeState.DETAIL_CHANGED) {
      Object value = property().getValue();  // DEBUG
    }
    else {
      // ...sonst den Wert direkt im Tabellenfeld auswählen
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
   * Liefert das Attribut.
   *
   * @return
   */
  protected AbstractProperty property() {
    return (AbstractProperty) fProperty;
  }
}
