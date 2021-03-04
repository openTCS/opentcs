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
import javax.swing.JList;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * A cell renderer for the DetailPanel's command queue display.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class CommandListCellRenderer
    extends DefaultListCellRenderer {
  
  /**
   * Creates a new CommandListCellRenderer.
   */
  public CommandListCellRenderer() {
    // Do nada.
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    super.getListCellRendererComponent(list,
                                       null,
                                       index,
                                       isSelected,
                                       cellHasFocus);
    if (value == null) {
      setText("<null>");
    }
    else {
      final MovementCommand cmd = (MovementCommand) value;
      final String destPoint = cmd.getStep().getDestinationPoint().getName();
      final String op = cmd.getOperation();
      setText(destPoint + " (" + op + ")");
    }
    return this;
  }
}
