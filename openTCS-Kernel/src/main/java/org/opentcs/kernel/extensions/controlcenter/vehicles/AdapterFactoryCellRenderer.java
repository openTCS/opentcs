/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * ListCellRenderer for the adapter combo box.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
final class AdapterFactoryCellRenderer
    implements ListCellRenderer<VehicleCommAdapterFactory> {

  /**
   * A default renderer for creating the label.
   */
  private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
  
  /**
   * Creates a new instance.
   */
  AdapterFactoryCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends VehicleCommAdapterFactory> list,
                                                VehicleCommAdapterFactory value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    JLabel label =
        (JLabel) defaultRenderer.getListCellRendererComponent(list,
                                                              value,
                                                              index,
                                                              isSelected,
                                                              cellHasFocus);
    if (value != null) {
      label.setText(value.getAdapterDescription());
    }
    else {
      label.setText(" ");
    }
    return label;
  }
}
