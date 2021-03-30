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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Ein CellEditor für Attribute vom Typ {
 *
 * @see StringProperty}. Der Editor umfasst ein Textfeld zur schnellen Eingabe
 * sowie den Button mit drei Punkten, bei dessen Anklicken sich ein
 * DetailsDialog zum komfortablen Bearbeiten des Attributs öffnet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StringPropertyCellEditor
    extends AbstractPropertyCellEditor {

  /**
   * Creates a new instance of StringPropertyCellEditor
   *
   * @param textField
   * @param umh
   */
  public StringPropertyCellEditor(JTextField textField, UserMessageHelper umh) {
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

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    setValue(value);
    JTextField textField = (JTextField) getComponent();
    if (value instanceof StringProperty) {
      textField.setDocument(new PlainDocument());
    }
    textField.setText(property().getText());

    return fComponent;
  }

  @Override
  public Object getCellEditorValue() {
    JTextField textField = (JTextField) getComponent();
    String newText = textField.getText();
    String oldText = property().getText();
    property().setText(newText);

    if (!newText.equals(oldText)) {
      markProperty();
    }

    return property();
  }

  /**
   * Liefert das Attribut.
   *
   * @return
   */
  protected StringProperty property() {
    return (StringProperty) fProperty;
  }

  private class JTextFieldLimit
      extends PlainDocument {

    private final int limit;

    JTextFieldLimit(int limit) {
      super();
      this.limit = limit;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr)
        throws BadLocationException {
      if (str == null) {
        return;
      }

      if ((getLength() + str.length()) <= limit || str.equals(getText(0, getLength()))) {
        super.insertString(offset, str, attr);
      }
    }
  }
}
