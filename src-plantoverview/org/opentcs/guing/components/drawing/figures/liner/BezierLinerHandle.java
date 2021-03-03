/**
 * (c): IML.
 *
 */
package org.opentcs.guing.components.drawing.figures.liner;

import java.awt.Point;
import java.util.Objects;
import org.jhotdraw.draw.Drawing;
import org.opentcs.guing.components.drawing.figures.PathConnection;

/**
 * A non-interactive Handle which draws the outline of a BezierFigure
 * to make adjustments easier.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
@Deprecated
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
