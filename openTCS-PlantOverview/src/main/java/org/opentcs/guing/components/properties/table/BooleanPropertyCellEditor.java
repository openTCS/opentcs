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
import javax.swing.JComponent;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Ein CellEditor für Attribute vom Typ {
 *
 * @see BooleanProperty}. Gezeigt wird eine CheckBox mit zwei möglichen
 * Zuständen. Einen Button mit drei Punkten sowie den damit verbundenen
 * DetailsDialog gibt es nicht.
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
   * Liefert das Property.
   *
   * @return
   */
  protected BooleanProperty property() {
    return (BooleanProperty) fProperty;
  }

  /**
   * Erzeugt den Button mit den drei Punkten. Liefert
   * <code>null
   * </code> zurück, da ein solcher Button nicht benötigt wird.
   *
   * @return
   */
  @Override
  protected JComponent createButtonDetailsDialog() {
    return null;
  }
}
