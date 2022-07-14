/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Color;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.opentcs.guing.base.components.properties.type.ColorProperty;
import static org.opentcs.guing.common.util.I18nPlantOverview.PROPERTIES_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A cell editor for a color property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ColorPropertyCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor,
               java.awt.event.ActionListener {

  /**
   * The button to use for the editor.
   */
  protected JButton fButton;
  /**
   * The color property.
   */
  protected ColorProperty fColorProperty;
  /**
   * The parent table.
   */
  protected JTable fTable;

  /**
   * Creates a new instance of ColorPropertyCellEditor.
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(PROPERTIES_PATH);

    Frame parent = JOptionPane.getFrameForComponent(fTable);
    Color newColor = JColorChooser.showDialog(parent,
                                              bundle.getString("colorPropertyCellEditor.dialog_colorSelection.title"),
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
