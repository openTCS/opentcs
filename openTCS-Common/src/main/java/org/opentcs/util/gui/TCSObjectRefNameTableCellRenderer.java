/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This class provides a representation of a TCSObjectReference as a JLabel.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @deprecated use {@link StringTableCellRenderer} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Class will be removed.")
public class TCSObjectRefNameTableCellRenderer
    extends JLabel
    implements TableCellRenderer {

  /**
   * Creates an instance
   */
  public TCSObjectRefNameTableCellRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {

    if (value == null || !(value instanceof TCSObjectReference<?>)) {
      setText("");
    }
    else {
      TCSObjectReference<?> ref = (TCSObjectReference<?>) value;
      setText(ref.getName());
    }
    return this;
  }

}
