// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernelcontrolcenter;

import javax.swing.JTextArea;

/**
 * A factory providing {@link ControlCenterInfoHandler} instances.
 */
public interface ControlCenterInfoHandlerFactory {

  /**
   * Creates a new ControlCenterInfoHandler.
   *
   * @param textArea The text area.
   * @return A new ControlCenterInfoHandler.
   */
  ControlCenterInfoHandler createHandler(JTextArea textArea);
}
