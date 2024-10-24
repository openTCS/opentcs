// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application.toolbar;

import java.awt.event.MouseEvent;
import org.jhotdraw.draw.tool.AbstractTool;

/**
 * The tool to drag the drawing.
 */
public class DragTool
    extends
      AbstractTool {

  public DragTool() {
    super();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    // Do nada.
  }
}
