// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.figures.liner;

import java.awt.Point;
import java.util.Objects;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Drawing;

/**
 * A Handle which allows to interactively change a control point
 */
public class BezierLinerControlPointHandle
    extends
      org.jhotdraw.draw.handle.BezierControlPointHandle {

  public BezierLinerControlPointHandle(BezierFigure owner, int index, int coord) {
    super(owner, index, coord);
  }

  @Override  // BezierControlPointHandle
  public void trackEnd(Point anchor, Point lead, int modifiersEx) {
    super.trackEnd(anchor, lead, modifiersEx);
    // Fire edit event to update the control points of the Path figure
    Drawing drawing = Objects.requireNonNull(view.getDrawing());
    drawing.fireUndoableEditHappened(new BezierLinerEdit(getBezierFigure()));
  }
}
