/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures.liner;

import java.awt.Point;
import java.util.Objects;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Drawing;

/**
 * A Handle which allows to interactively change a control point
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class BezierLinerControlPointHandle
    extends org.jhotdraw.draw.handle.BezierControlPointHandle {

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
