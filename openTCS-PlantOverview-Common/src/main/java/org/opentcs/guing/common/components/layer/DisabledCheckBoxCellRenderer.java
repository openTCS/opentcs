/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer for a disabled check box.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DisabledCheckBoxCellRenderer
    implements TableCellRenderer {

  private final JCheckBox checkBox;

  public DisabledCheckBoxCellRenderer() {
    checkBox = new JCheckBox();
    checkBox.setHorizontalAlignment(SwingConstants.CENTER);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {
    Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
    checkBox.setBackground(bg);
    checkBox.setEnabled(false);
    checkBox.setSelected(value == Boolean.TRUE);
    return checkBox;
  }
}
