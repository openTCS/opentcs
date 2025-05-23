// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * A combo box renderer for locations.
 */
class LocationComboBoxRenderer
    extends
      JLabel
    implements
      ListCellRenderer<TCSObjectReference<Location>> {

  /**
   * Creates a new instance.
   */
  LocationComboBoxRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends TCSObjectReference<Location>> list,
      TCSObjectReference<Location> value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
  ) {
    if (value == null) {
      setText("");
    }
    else {
      setText(value.getName());
    }
    return this;
  }
}
