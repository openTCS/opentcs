/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Component;
import static java.util.Objects.requireNonNull;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renders values to JLabels.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @param <E> The type of the table cell values that the representer can represent.
 */
public class StringTableCellRenderer<E>
    implements TableCellRenderer {

  /**
   * The default renderer we delegate to.
   */
  private final DefaultTableCellRenderer delegate = new DefaultTableCellRenderer();
  /**
   * Returns a String representation of E.
   */
  private final Function<E, String> representer;

  /**
   * Creates an instance.
   *
   * @param representer a string representation provider for the values of the list.
   * Null value as parameter for the representer is possible.
   * The result is set as text of the JLabel.
   */
  public StringTableCellRenderer(Function<E, String> representer) {
    this.representer = requireNonNull(representer, "representer");
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {
    JLabel label = (JLabel) delegate.getTableCellRendererComponent(table,
                                                                   value,
                                                                   isSelected,
                                                                   hasFocus,
                                                                   row,
                                                                   column);
    @SuppressWarnings("unchecked")
    E val = (E) value;
    label.setText(representer.apply(val));
    return label;
  }
}
