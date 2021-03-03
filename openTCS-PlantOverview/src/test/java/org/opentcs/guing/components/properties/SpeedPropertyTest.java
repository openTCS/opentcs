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
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A test for a speed property. Min value is 0 and max value is
 * <code>Double.MAX_VALUE</code>.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class SpeedPropertyTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;
  private SpeedProperty sp;

  @Before
  public void setUp() {
    textField = new JTextField();
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
    sp = new SpeedProperty(mock(ModelComponent.class), 10, SpeedProperty.Unit.KM_H);
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), sp, true, 0, 0);
  }

  @After
  public void tearDown() {
    textField = null;
    quantityCellEditor = null;
    sp = null;
  }

  @Test
  public void speedPropertyTest() {   
    assertEquals(textField.getText(), "10.0 " + Unit.KM_H.toString());
    textField.setText("50 " + Unit.KM_H.toString());
    quantityCellEditor.getCellEditorValue();
    assertEquals(50.0, sp.getValue());
    assertEquals(SpeedProperty.Unit.KM_H, sp.getUnit());
  }

  @Test
  public void speedPropertyUnits() {
    for (SpeedProperty.Unit unit : sp.getPossibleUnits()) {
      textField.setText("100 " + unit.toString());
      quantityCellEditor.getCellEditorValue();
      assertEquals(100.0, sp.getValue());
      assertEquals(unit, sp.getUnit());
    }
  }

  @Test
  public void speedPropertyNegativeValue() {
    //negative values not allowed, changes mustn't be saved to the property
    textField.setText("-100 " + Unit.KM_H.toString());
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, sp.getValue());
    assertEquals(SpeedProperty.Unit.KM_H, sp.getUnit());
  }

  @Test
  public void speedPropertyUnknownUnit() {
    //unknown unit, changes mustn't be saved to the property
    textField.setText("100 litre");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, sp.getValue());
    assertEquals(SpeedProperty.Unit.KM_H, sp.getUnit());
  }

  @Test
  public void speedPropertyNoBlank() {
    //strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100" + Unit.KM_H.toString());
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, sp.getValue());
    assertEquals(SpeedProperty.Unit.KM_H, sp.getUnit());
  }

  @Test
  public void speedPropertyEmtpy() {
    //empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, sp.getValue());
    assertEquals(SpeedProperty.Unit.KM_H, sp.getUnit());
  }
  
  @Test
  public void speedPropertyConversion() {
    textField.setText("10000 " + Unit.MM_S.toString());
    quantityCellEditor.getCellEditorValue();
    assertEquals(10, sp.getValueByUnit(SpeedProperty.Unit.M_S), 0);
    assertEquals(36, sp.getValueByUnit(SpeedProperty.Unit.KM_H), 0);
    assertEquals(Unit.MM_S, sp.getUnit());
  }
}
