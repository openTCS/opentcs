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
import org.jhotdraw.draw.Drawing;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A non-interactive Handle which draws the outline of a BezierFigure
 * to make adjustments easier.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @deprecated Scheduled for removal with release 4.0.0. Not used anywhere.
 */
@Deprecated
@ScheduledApiChange(when = "4.0.0")
public class BezierLinerHandle
    extends org.jhotdraw.draw.handle.BezierOutlineHandle {

  public BezierLinerHandle(PathConnection owner) {
    super(owner);
    owner.addFigureListener(this);
  }

  @Override	// BezierOutlineHandle
  public boolean contains(Point p) {
    return getBounds().contains(p);
  }

  @Override	// BezierOutlineHandle
  public void trackEnd(Point anchor, Point lead, int modifiersEx) {
    Drawing drawing = Objects.requireNonNull(view.getDrawing());
    drawing.fireUndoableEditHappened(new BezierLinerEdit((PathConnection) getOwner()));
  }
}
