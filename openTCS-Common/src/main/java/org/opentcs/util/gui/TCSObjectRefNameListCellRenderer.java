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
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This class provides a representation of a TCSObjectReference as a JLabel
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 * @deprecated use {@link StringListCellRenderer} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Class will be removed.")
public class TCSObjectRefNameListCellRenderer
    extends JLabel
    implements ListCellRenderer<TCSObjectReference<?>> {

  public TCSObjectRefNameListCellRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends TCSObjectReference<?>> list,
      TCSObjectReference<?> value, int index,
      boolean isSelected, boolean cellHasFocus) {
    if (value == null) {
      setText("");
    }
    else {
      setText(value.getName());
    }
    return this;
  }

}
