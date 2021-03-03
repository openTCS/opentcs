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
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LengthProperty.Unit;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A test for length and coordinate properties. Min value is 0 and max value
 * is <code>Double.MAX_VALUE</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LengthPropertyTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;
  private LengthProperty lp;

  @Before
  public void setUp() {
    textField = new JTextField();
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
    lp = new LengthProperty(mock(ModelComponent.class), 10, Unit.CM);
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), lp, true, 0, 0);
  }

  @After
  public void tearDown() {
    textField = null;
    quantityCellEditor = null;
    lp = null;
  }
  
  @Test
  public void coordinatePropertyTest() {
    //CoordinateProperty is a LengthProperty that accepts negative values
    lp = new CoordinateProperty(mock(ModelComponent.class), 10.0, Unit.MM);
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), lp, true, 0, 0);
    textField.setText("-100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(-100.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  public void lengthPropertyTest() { 
    assertEquals(textField.getText(), "10.0 cm");
    textField.setText("100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(100.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  public void lengthPropertyUnits() {
    for (Unit unit : lp.getPossibleUnits()) {
      textField.setText("100 " + unit.toString());
      quantityCellEditor.getCellEditorValue();
      assertEquals(100.0, lp.getValue());
      assertEquals(unit, lp.getUnit());
    }
  }

  @Test
  public void lengthPropertyNegativeValue() {
    //negative values not allowed, changes mustn't be saved to the property
    textField.setText("-100 cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  public void lengthPropertyUnknownUnit() {
    //unknown unit, changes mustn't be saved to the property
    textField.setText("100 litre");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  public void lengthPropertyNoBlank() {
    //strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100cm");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }

  @Test
  public void lengthPropertyEmtpy() {
    //empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, lp.getValue());
    assertEquals(Unit.CM, lp.getUnit());
  }
}
