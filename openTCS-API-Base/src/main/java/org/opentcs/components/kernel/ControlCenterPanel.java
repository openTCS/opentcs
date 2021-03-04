/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.swing.JPanel;
import org.opentcs.components.Lifecycle;

/**
 * A panel that can be plugged into the kernel control center.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class ControlCenterPanel
    extends JPanel
    implements Lifecycle {

  /**
   * Returns a title for this panel.
   *
   * @return A title for this panel.
   */
  public String getTitle() {
    return getAccessibleContext().getAccessibleName();
  }
}
