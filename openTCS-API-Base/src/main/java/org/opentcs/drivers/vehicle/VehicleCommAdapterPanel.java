/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/**
 * A base class for panels associated with comm adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class VehicleCommAdapterPanel
    extends JPanel
    implements PropertyChangeListener {
  
  /**
   * Returns the title for this comm adapter panel.
   * The default implementation returns the accessible name from the panel's accessible context.
   *
   * @return The title for this comm adapter panel.
   */
  public String getTitle() {
    return getAccessibleContext().getAccessibleName();
  }
}
