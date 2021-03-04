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
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Ein CellEditor für ein ColorProperty.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ColorPropertyCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor,
               java.awt.event.ActionListener {

  /**
   * Für den CellEditor wird ein Button verwendet.
   */
  protected JButton fButton;
  /**
   * Das Farbattribut.
   */
  protected ColorProperty fColorProperty;
  /**
   * Die Tabelle als Parent des Farbauswahldialogs.
   */
  protected JTable fTable;

  /**
   * Creates a new instance of ColorPropertyCellEditor
   */
  public ColorPropertyCellEditor() {
    super();
    fButton = new JButton();
    fButton.setBorderPainted(false);
    fButton.addActionListener(this);
  }

  @Override
  public java.awt.Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    fTable = table;
    fColorProperty = (ColorProperty) value;
    fButton.setBackground(fColorProperty.getColor());
    return fButton;
  }

  @Override
  public Object getCellEditorValue() {
    return fColorProperty;
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    Frame parent = JOptionPane.getFrameForComponent(fTable);
    Color newColor = JColorChooser.showDialog(parent,
                                              bundle.getString("ColorPropertyCellEditor."
                                                  + "ColorPropertyWindowTitle"),
                                              fColorProperty.getColor());

    if (newColor != null) {
      Color oldColor = fColorProperty.getColor();
      fColorProperty.setColor(newColor);

      if (newColor != oldColor) {
        fColorProperty.markChanged();
      }
    }

    stopCellEditing();
  }
}
