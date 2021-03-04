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
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.util.UserMessageHelper;
import org.slf4j.LoggerFactory;

/**
 * Ein CellEditor fï¿½r Attribute vom Typ {
 *
 * @see StringProperty}. Der Editor umfasst ein Textfeld zur schnellen Eingabe
 * sowie den Button mit drei Punkten, bei dessen Anklicken sich ein
 * DetailsDialog zum komfortablen Bearbeiten des Attributs ï¿½ffnet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class IntegerPropertyCellEditor
    extends AbstractPropertyCellEditor {

  /**
   * Creates a new instance of IntegerPropertyCellEditor
   *
   * @param textField
   * @param umh
   */
  public IntegerPropertyCellEditor(JFormattedTextField textField, UserMessageHelper umh) {
    super(textField, umh);
    setStyle(textField);
  }

  /**
   * Konfiguriert das Aussehen des Textfeldes.
   *
   * @param textField
   */
  protected final void setStyle(JTextField textField) {
    setClickCountToStart(1);
    textField.setHorizontalAlignment(JTextField.LEFT);
  }

  /**
   * Erzeugt die Komponente, die aus Editor und kleinem Button besteht.
   *
   * @return
   */
  @Override
  protected JComponent createComponent() {
    return (JComponent) getComponent();
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    JFormattedTextField textField = (JFormattedTextField) getComponent();
    setValue(value);

    if (property().getValue() instanceof Integer) {
      textField.setValue(new Integer((int) property().getValue()));
    }

    return fComponent;
  }

  @Override
  public Object getCellEditorValue() {
    JFormattedTextField textField = (JFormattedTextField) getComponent();

    try {
      textField.commitEdit();
    }
    catch (ParseException ex) {
      LoggerFactory.getLogger(IntegerPropertyCellEditor.class).error("ParseException: {0}",
                                                                     textField.getText());
    }

    try {
      int newValue = Integer.parseInt(textField.getText());
      int oldValue = (int) property().getValue();
      property().setValue(newValue);

      if (newValue != oldValue) {
        markProperty();
      }

      return property();
    }
    catch (NumberFormatException e) {
      return property();
    }
    catch (ClassCastException ex) {
      markProperty();
      return property();
    }
  }

  /**
   * Liefert das Attribut.
   *
   * @return
   */
  protected IntegerProperty property() {
    return (IntegerProperty) fProperty;
  }
}
