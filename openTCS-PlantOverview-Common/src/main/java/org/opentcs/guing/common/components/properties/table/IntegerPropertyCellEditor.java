/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Component;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.opentcs.guing.base.components.properties.type.IntegerProperty;
import org.opentcs.guing.common.util.UserMessageHelper;
import org.slf4j.LoggerFactory;

/**
 * A cell editor for an integer property.
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
   * Initialises the style for the text field.
   *
   * @param textField
   */
  protected final void setStyle(JTextField textField) {
    setClickCountToStart(1);
    textField.setHorizontalAlignment(JTextField.LEFT);
  }

  /**
   * Create the component for this editor.
   *
   * @return the component for this editor.
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
      textField.setValue(property().getValue());
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
   * Returns the property for this editor.
   *
   * @return the property for this editor.
   */
  protected IntegerProperty property() {
    return (IntegerProperty) fProperty;
  }
}
