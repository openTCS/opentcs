/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import javax.swing.JTable;
import javax.swing.JTextField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty.Unit;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * A test for the {@link QuantityCellEditor}.
 */
class QuantityCellEditorTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;
  private LengthProperty lp;

  QuantityCellEditorTest() {
  }

  @BeforeEach
  void setUp() {
    textField = new JTextField();
    lp = new LengthProperty(mock(ModelComponent.class), 10, LengthProperty.Unit.CM);
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), lp, true, 0, 0);
  }

  @Test
  void testPropertyTextFieldContent() {
    assertEquals(textField.getText(), "10.0 cm");
  }

  @Test
  void allowValueInRange() {
    textField.setText("100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(100.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  void disallowValueOutOfRange() {
    // Value out of range, changes mustn't be saved to the property
    textField.setText("-100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  void allowKnownUnit() {
    textField.setText("100 mm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(100.0, lp.getValue());
    assertEquals(Unit.MM, lp.getUnit());
  }

  @Test
  void disallowUnknownUnit() {
    // Unknown unit, changes mustn't be saved to the property
    textField.setText("100 liter");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  void disallowWrongFormat() {
    // Strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  void disallowEmptyInputString() {
    // Empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  void disallowCharactersInValue() {
    // Values mixed with text not allowed, changes musnt be saved to the property
    textField.setText("55asd.5 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }
}
