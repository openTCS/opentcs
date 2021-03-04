/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.toolbar;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.TransformEdit;
import org.jhotdraw.draw.tool.DefaultDragTracker;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.liner.BezierLinerEdit;

/**
 * Utility to follow the drags made by the user.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSDragTracker
    extends DefaultDragTracker {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The affected figures.
   */
  private Set<Figure> transformedFigures;
  /**
   * Indicates whether the user is dragging the mouse or not.
   */
  private boolean isDragging;

  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current state.
   */
  @Inject
  public OpenTCSDragTracker(ApplicationState appState) {
    this.appState = requireNonNull(appState, "appState");
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    DrawingView view = editor.findView((Container) evt.getSource());
    view.requestFocus();
    anchor = new Point(evt.getX(), evt.getY());
    isWorking = true;
    fireToolStarted(view);
    view = getView();

    if (evt.isShiftDown()) {
      view.toggleSelection(anchorFigure);

      if (!view.isFigureSelected(anchorFigure)) {
        anchorFigure = null;
      }
    }
    else if (!view.isFigureSelected(anchorFigure)) {
      view.clearSelection();
      view.addToSelection(anchorFigure);
    }

    if (!view.getSelectedFigures().isEmpty()) {
      dragRect = null;
      transformedFigures = new HashSet<>();

      for (Figure f : view.getSelectedFigures()) {
        if (f.isTransformable()) {
          transformedFigures.add(f);

          if (dragRect == null) {
            dragRect = f.getBounds();
          }
          else {
            dragRect.add(f.getBounds());
          }
        }
      }

      if (dragRect != null) {
        anchorPoint = previousPoint = view.viewToDrawing(anchor);
        anchorOrigin = previousOrigin = new Point2D.Double(dragRect.x, dragRect.y);
      }
    }
  }

  @Override
  public void mouseDragged(MouseEvent evt) {
    OpenTCSDrawingView drawingView = (OpenTCSDrawingView) getView();

    switch (appState.getOperationMode()) {
      case MODELLING:
        if (!transformedFigures.isEmpty()) {
          if (!isDragging) {
            isDragging = true;
            updateCursor(editor.findView((Container) evt.getSource()), new Point(evt.getX(), evt.getY()));
          }

          Point2D.Double currentPoint = drawingView.viewToDrawing(new Point(evt.getX(), evt.getY()));
          double offsetX = currentPoint.x - previousPoint.x;
          double offsetY = currentPoint.y - previousPoint.y;
          dragRect.x += offsetX;
          dragRect.y += offsetY;
          Rectangle2D.Double constrainedRect = (Rectangle2D.Double) dragRect.clone();

          if (drawingView.getConstrainer() != null) {
            drawingView.getConstrainer().constrainRectangle(constrainedRect);
          }

          AffineTransform tx = new AffineTransform();
          tx.translate(
              constrainedRect.x - previousOrigin.x,
              constrainedRect.y - previousOrigin.y);

          for (Figure f : transformedFigures) {
            f.willChange();
            f.transform(tx);
            f.changed();
          }

          previousPoint = currentPoint;
          previousOrigin = new Point2D.Double(constrainedRect.x, constrainedRect.y);
        }

        break;

      case OPERATING:
      default:
    }
  }

  @Override // DefaultDragTracker
  public void mouseReleased(MouseEvent evt) {
    isWorking = false;
    DrawingView view = getView();

    if (transformedFigures != null && !transformedFigures.isEmpty()) {
      isDragging = false;
      int x = evt.getX();
      int y = evt.getY();
      updateCursor(editor.findView((Container) evt.getSource()), new Point(x, y));
      Point2D.Double newPoint = view.viewToDrawing(new Point(x, y));
      Figure dropTarget = getDrawing().findFigureExcept(newPoint, transformedFigures);

      if (dropTarget != null) {
        boolean snapBack = dropTarget.handleDrop(newPoint, transformedFigures, view);

        if (snapBack) {
          AffineTransform tx = new AffineTransform();
          tx.translate(
              anchorOrigin.x - previousOrigin.x,
              anchorOrigin.y - previousOrigin.y);

          for (Figure f : transformedFigures) {
            f.willChange();
            f.transform(tx);
            f.changed();
          }

          Rectangle r = new Rectangle(anchor.x, anchor.y, 0, 0);
          r.add(evt.getX(), evt.getY());
          maybeFireBoundsInvalidated(r);
          fireToolDone();

          return;
        }
      }

      AffineTransform tx = new AffineTransform();
      tx.translate(
          -anchorOrigin.x + previousOrigin.x,
          -anchorOrigin.y + previousOrigin.y);

      if (!tx.isIdentity()) {
        getDrawing().fireUndoableEditHappened(
            new TransformEdit(transformedFigures, tx));
      }

      // On changes on a path
      Object[] aFigures = transformedFigures.toArray();

      if (aFigures[0] instanceof PathConnection) {
        getDrawing().fireUndoableEditHappened(new BezierLinerEdit((PathConnection) aFigures[0]));
      }
    }

    Rectangle r = new Rectangle(anchor.x, anchor.y, 0, 0);
    r.add(evt.getX(), evt.getY());
    maybeFireBoundsInvalidated(r);
    transformedFigures = null;
    fireToolDone();
  }
}
