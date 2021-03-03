/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * A combo box renderer for locations.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class LocationComboBoxRenderer
    extends JLabel
    implements ListCellRenderer<TCSObjectReference<Location>> {

  /**
   * Creates a new instance.
   */
  public LocationComboBoxRenderer() {
    // Do nada.
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends TCSObjectReference<Location>> list,
      TCSObjectReference<Location> value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {
    if (value == null) {
      setText("");
    }
    else {
      setText(value.getName());
    }
    return this;
  }
}
