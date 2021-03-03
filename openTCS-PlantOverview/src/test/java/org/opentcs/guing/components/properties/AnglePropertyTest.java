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
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A test for an angle property. Min value is 0 and max value is 360.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class AnglePropertyTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;
  private AngleProperty ap;

  @Before
  public void setUp() {
    textField = new JTextField();
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
    ap = new AngleProperty(mock(ModelComponent.class), 10, AngleProperty.Unit.DEG);
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), ap, true, 0, 0);
  }

  @After
  public void tearDown() {
    textField = null;
    quantityCellEditor = null;
    ap = null;
  }

  @Test
  public void anglePropertyTest() {   
    assertEquals(textField.getText(), "10.0 deg");
    textField.setText("50 deg");
    quantityCellEditor.getCellEditorValue();
    assertEquals(50.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }

  @Test
  public void anglePropertyUnits() {
    for (AngleProperty.Unit unit : ap.getPossibleUnits()) {
      textField.setText("100 " + unit.toString());
      quantityCellEditor.getCellEditorValue();
      assertEquals(100.0, ap.getValue());
      assertEquals(unit, ap.getUnit());
    }
  }

  @Test
  public void anglePropertyNegativeValue() {
    //negative values not allowed, changes mustn't be saved to the property
    textField.setText("-100 deg");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }

  @Test
  public void anglePropertyUnknownUnit() {
    //unknown unit, changes mustn't be saved to the property
    textField.setText("100 litre");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }

  @Test
  public void anglePropertyNoBlank() {
    //strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100deg");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }

  @Test
  public void anglePropertyEmtpy() {
    //empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }
  
  @Test
  public void anglePropertyModulo() {
    //value shall stay in [0,360]
    textField.setText("540 deg");
    quantityCellEditor.getCellEditorValue();
    assertEquals(180.0, ap.getValue());
    assertEquals(AngleProperty.Unit.DEG, ap.getUnit());
  }
  
  @Test
  public void anglePropertyConversion() {
    textField.setText("10 rad");
    quantityCellEditor.getCellEditorValue();
    assertEquals(213.0, ap.getValueByUnit(AngleProperty.Unit.DEG), 1.0);
    assertEquals(AngleProperty.Unit.RAD, ap.getUnit());
  }
}
