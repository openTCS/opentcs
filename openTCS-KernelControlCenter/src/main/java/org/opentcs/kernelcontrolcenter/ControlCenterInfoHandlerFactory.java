/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
