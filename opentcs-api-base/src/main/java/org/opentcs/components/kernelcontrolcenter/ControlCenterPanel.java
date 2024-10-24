// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernelcontrolcenter;

import javax.swing.JPanel;
import org.opentcs.components.Lifecycle;

/**
 * A panel that can be plugged into the kernel control center.
 */
public abstract class ControlCenterPanel
    extends
      JPanel
    implements
      Lifecycle {

  /**
   * Returns a title for this panel.
   *
   * @return A title for this panel.
   */
  public String getTitle() {
    return getAccessibleContext().getAccessibleName();
  }
}
