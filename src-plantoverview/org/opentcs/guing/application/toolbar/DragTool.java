/*
 *
 * Created on 06.08.2012 12:48:59
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
