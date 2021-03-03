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
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * A test for a percent property. Min value is 0 and max value is 100.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class PercentPropertyTest {

  private JTextField textField;
  private QuantityCellEditor quantityCellEditor;
  private PercentProperty pp;

  @Before
  public void setUp() {
    textField = new JTextField();
    quantityCellEditor = new QuantityCellEditor(textField, mock(UserMessageHelper.class));
    pp = new PercentProperty(mock(ModelComponent.class), 10.0, PercentProperty.Unit.PERCENT, false);
    quantityCellEditor.getTableCellEditorComponent(mock(JTable.class), pp, true, 0, 0);
  }

  @After
  public void tearDown() {
    textField = null;
    quantityCellEditor = null;
    pp = null;
  }

  @Test
  public void percentPropertyTest() {   
    assertEquals(textField.getText(), "10.0 %");
    textField.setText("50 %");
    quantityCellEditor.getCellEditorValue();
    assertEquals(50.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }

  @Test
  public void percentPropertyNegativeValue() {
    //negative values not allowed, changes mustn't be saved to the property
    textField.setText("-100 %");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }
  
  @Test
  public void percentPropertyGreaterValue() {
    //values greater than 100 not allowed
    textField.setText("101 %");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }

  @Test
  public void percentPropertyUnknownUnit() {
    //unknown unit, changes mustn't be saved to the property
    textField.setText("100 litre");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }

  @Test
  public void percentPropertyNoBlank() {
    //strings without a blank index not allowed, changes mustn't be saved to the property
    textField.setText("100%");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }

  @Test
  public void percentPropertyEmtpy() {
    //empty string not allowed, changes mustn't be saved to the property
    textField.setText("");
    quantityCellEditor.getCellEditorValue();
    assertEquals(10.0, pp.getValue());
    assertEquals(PercentProperty.Unit.PERCENT, pp.getUnit());
  }
}
