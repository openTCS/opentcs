/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import javax.swing.JPanel;

/**
 * A base class for panels associated with comm adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class VehicleCommAdapterPanel
    extends JPanel {

  /**
   * Returns the title for this comm adapter panel.
   * The default implementation returns the accessible name from the panel's accessible context.
   *
   * @return The title for this comm adapter panel.
   */
  public String getTitle() {
    return getAccessibleContext().getAccessibleName();
  }

  /**
   * Notifies a comm adapter panel that the corresponding process model changed.
   * The comm adapter panel may want to update the content its representing.
   *
   * @param attributeChanged The attribute name that chagend.
   * @param processModel The process model.
   */
  public abstract void processModelChange(String attributeChanged,
                                          VehicleProcessModelTO processModel);
}
