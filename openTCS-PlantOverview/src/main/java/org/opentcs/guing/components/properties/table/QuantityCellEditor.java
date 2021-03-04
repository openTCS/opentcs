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
import javax.swing.JTable;
import javax.swing.JTextField;
import org.opentcs.guing.components.properties.type.AbstractQuantity;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.slf4j.LoggerFactory;

/**
 * Ein CellEditor fï¿½r Attribute vom Typ {
 *
 * @see AbstractQuantity}. Der Editor umfasst ein Textfeld zur schnellen Eingabe
 * sowie den Button mit drei Punkten, bei dessen Anklicken sich ein
 * DetailsDialog zum komfortablen Bearbeiten des Attributs ï¿½ffnet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class QuantityCellEditor
    extends AbstractPropertyCellEditor {

  /**
   * Creates a new instance of QuantityCellEditor
   *
   * @param textField
   * @param umh
   */
  public QuantityCellEditor(JTextField textField, UserMessageHelper umh) {
    super(textField, umh);
    setStyle(textField);
  }

  /**
   * Konfiguriert das Aussehen des Textfeldes.
   *
   * @param textField
   */
  private void setStyle(JTextField textField) {
    setClickCountToStart(1);
    textField.setHorizontalAlignment(JTextField.LEFT);
  }

  /**
   * Liefert das Attribut.
   *
   * @return
   */
  protected AbstractQuantity<?> property() {
    return (AbstractQuantity<?>) fProperty;
  }

  /**
   * Extrahiert aus dem String des Textfeldes den Wert und die Einheit. Wurde
   * die Eingabe durch den Benutzer nicht korrekt getï¿½tigt, wird eine Exception
   * ausgelï¿½st. In diesem Fall erfolgt keine ï¿½nderung des Attributs.
   * Unterklassen kï¿½nnen diese Methode ï¿½berschreiben, um ein toleranteres
   * Verhalten zu implementieren.
   *
   * @param text
   */
  protected void extractQuantity(String text)
      throws IllegalArgumentException {
    int blankIndex = text.indexOf(' ');

    if (blankIndex == -1) {
      userMessageHelper.showMessageDialog(
          ResourceBundleUtil.getBundle().getString("QuantityCellEditor.errorMsg"),
          ResourceBundleUtil.getBundle().getString("QuantityCellEditor.errorTitle"),
          UserMessageHelper.Type.ERROR);
      return;
    }

    String valueString = text.substring(0, blankIndex);
    String unitString = text.substring(blankIndex + 1);

    if (property().getModel() instanceof LayoutModel) {
      if (valueString.equals("0.0") || valueString.equals("0")) {
        userMessageHelper.showMessageDialog(
            ResourceBundleUtil.getBundle().getString("VisualLayout.scaleInvalid.msg"),
            ResourceBundleUtil.getBundle().getString("VisualLayout.scaleInvalid.title"),
            UserMessageHelper.Type.ERROR);
        return;
      }
    }

    try {
      double newValue = Double.parseDouble(valueString);
      if (!property().getValidRange().isValueValid(newValue)) {
        return;
      }

      double oldValue = (double) property().getValue();
      String oldUnit = property().getUnit().toString();
      property().setValueAndUnit(newValue, unitString);

      if (newValue != oldValue || !unitString.equals(oldUnit)) {
        markProperty();
      }
    }
    catch (NumberFormatException e) {
      // String remains "<different values>"
      property().setValue(text);
    }
    catch (IllegalArgumentException e) {
      // Caught if an illegal unit string is entered by the user.
      // TODO Let the user know.
      return;
    }
    catch (ClassCastException e) {
      // Change from "<different values>" to valid value
      double newValue = Double.parseDouble(valueString);
      property().setValueAndUnit(newValue, unitString);
      markProperty();
    }
  }

  @Override
  public boolean stopCellEditing() {
    // ChangeState.DETAIL_CHANGED is unwanted at this point and is set in
    // StandardDetailsDialog. If we wouldn't change it here the model value
    // would be copied to the layout. By changing the ChangeState to CHANGED
    // it will only be saved in the model.
    if (property().getChangeState() == ModelAttribute.ChangeState.DETAIL_CHANGED) {
      property().setChangeState(ModelAttribute.ChangeState.CHANGED);
    }

    return super.stopCellEditing();
  }

  @Override  // DefaultCellEditor
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    setValue(value);
    ((JTextField) getComponent()).setText(property().toString());

    return fComponent;
  }

  @Override  // DefaultCellEditor
  public Object getCellEditorValue() {
    JTextField textField = (JTextField) getComponent();
    String text = textField.getText();

    try {
      extractQuantity(text);
    }
    catch (IllegalArgumentException e) {
      LoggerFactory.getLogger(QuantityCellEditor.class).error("Exception", e);
    }

    return property();
  }
}
