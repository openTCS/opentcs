/**
 * (c): IML, 2014.
 */
package org.opentcs.guing.components.properties;

import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import org.opentcs.guing.components.properties.table.QuantityCellEditor;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LengthProperty.Unit;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A test for the various property types.
 *
 * @author pseifert
 */
public class CellEditorTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;

  @Before
  public void setUp() {
    textField = new JTextField();
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
  }

  @After
  public void tearDown() {
    textField = null;
    quantityCellEditor = null;
  }

  @Test
  public void lengthPropertyTest() { //min: 0, max: n/a
    LengthProperty lp = new LengthProperty(mock(ModelComponent.class), 10, Unit.CM);
    //init
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), lp, true, 0, 0);
    assertEquals(textField.getText(), "10.0 cm");
    //empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
    //strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
    //unknown unit, changes mustn't be saved to the property
    textField.setText("100 litre");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
    //negative values not allowed, changes mustn't be saved to the property
    textField.setText("-100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
    //finally a normal value should be saved
    textField.setText("100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(100.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
    for (Unit unit : lp.getPossibleUnits()) {
      textField.setText("100 " + unit.toString());
      quantityCellEditor.getCellEditorValue();
      assertEquals(100.0, lp.getValue());
      assertEquals(unit, lp.getUnit());
    }
  }
}
