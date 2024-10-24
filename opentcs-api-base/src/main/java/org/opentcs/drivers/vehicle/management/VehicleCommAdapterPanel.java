// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.management;

import javax.swing.JPanel;

/**
 * A base class for panels associated with comm adapters.
 */
public abstract class VehicleCommAdapterPanel
    extends
      JPanel {

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
  public abstract void processModelChange(
      String attributeChanged,
      VehicleProcessModelTO processModel
  );
}
