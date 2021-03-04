/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.data.TCSObject;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This class implements the ListCellRenderer for all TCSObjects.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @deprecated use {@link StringListCellRenderer} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Class will be removed.")
public class TCSObjectNameListCellRenderer
    implements ListCellRenderer<TCSObject<?>> {

  /**
   * A default renderer for creating the label.
   */
  private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

  /**
   * Creates a new instance.
   */
  public TCSObjectNameListCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends TCSObject<?>> list,
                                                TCSObject<?> value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    JLabel label
        = (JLabel) defaultRenderer.getListCellRendererComponent(list,
                                                                value,
                                                                index,
                                                                isSelected,
                                                                cellHasFocus);
    label.setOpaque(true);
    if (value == null) {
      label.setText("");
    }
    else {
      label.setText(value.getName());
    }
    return label;
  }
}
