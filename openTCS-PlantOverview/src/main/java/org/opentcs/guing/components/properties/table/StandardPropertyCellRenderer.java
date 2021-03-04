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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.Property;

/**
 * Ein CellRenderer für alle Attribute außer vom Typ {
 *
 * @see BooleanProperty}. Dargestellt wird lediglich der Text, den ein Attribut
 * in der
 * <code>toString()</code> Methode zurückliefert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StandardPropertyCellRenderer
    extends javax.swing.table.DefaultTableCellRenderer {

  public final static Color BG_UNEDITABLE = new Color(0xE0E0E0);

  /**
   * Creates a new instance of StringPropertyCellRenderer
   */
  public StandardPropertyCellRenderer() {
    super();
    setStyle();
  }

  /**
   * Konfiguriert das Aussehen des Labels.
   */
  protected final void setStyle() {
    setFont(new Font("Dialog", Font.PLAIN, 12));
    setHorizontalAlignment(JLabel.LEFT);
    setBorder(null);
  }

  /**
   * Liefert die Komponente zur Darstellung des Attributs (hier das Label mit
   * dem Text, den das Attribut in seiner toString()- Methode liefert. Ist das
   * Attribut in der Zeile nicht veränderbar, so wird für das Label
   * <code>enabled(false)</code> gesetzt.
   *
   * @return
   */
  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                                                                hasFocus, row, column);
    label.setText(value.toString());

    if (value instanceof Property) {
      label.setToolTipText(((Property) value).getHelptext());
    }

    AttributesTable attributesTable = (AttributesTable) table;
    boolean editable = attributesTable.isEditable(row);

    switch (column) {
      case 0:
        label.setBackground(BG_UNEDITABLE);
        label.setForeground(Color.darkGray);
        break;

      case 1:
        if (((AbstractProperty) value).isCollectionAndHasDifferentValues()) {
          label.setBackground(AbstractPropertyCellEditor.DIFFERENT_VALUE_COLOR);
        }
        else {
          label.setBackground(editable ? Color.white : BG_UNEDITABLE);
        }
        label.setForeground(editable ? Color.blue : Color.darkGray);
        break;

      default:
    }

    return this;
  }
}
