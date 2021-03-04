/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.toolbar;

import java.awt.event.MouseEvent;
import org.jhotdraw.draw.tool.AbstractTool;

/**
 * The tool to drag the drawing.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DragTool
    extends AbstractTool {

  public DragTool() {
    super();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    // Do nada.
  }
}
