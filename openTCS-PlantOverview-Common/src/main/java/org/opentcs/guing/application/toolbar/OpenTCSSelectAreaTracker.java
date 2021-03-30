/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.toolbar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.tool.DefaultSelectAreaTracker;
import org.opentcs.guing.application.ApplicationState;

/**
 * Utility to track area selections made by the user.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSSelectAreaTracker
    extends DefaultSelectAreaTracker {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The bounds of the rubberband.
   */
  private final Rectangle rubberband = new Rectangle();
  /**
   * Rubberband stroke.
   */
  private final Stroke rubberbandStroke = new BasicStroke();
  /**
   * Rubberband color. When this is null, the tracker does not draw the
   * rubberband.
   */
  private final Color rubberbandColor = Color.MAGENTA;
  /**
   * The hover handles, are the handles of the figure over which the mouse
   * pointer is currently hovering.
   */
  private final List<Handle> hoverHandles = new LinkedList<>();
  /**
   * The hover Figure is the figure, over which the mouse is currently hovering.
   */
  private Figure hoverFigure = null;

  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current state.
   */
  @Inject
  public OpenTCSSelectAreaTracker(ApplicationState appState) {
    this.appState = requireNonNull(appState, "appState");
  }

  @Override // DefaultSelectAreaTracker
  public void mousePressed(MouseEvent evt) {
    super.mousePressed(evt);
    clearRubberBand();
  }

  @Override // DefaultSelectAreaTracker
  public void mouseReleased(MouseEvent evt) {
    selectGroup();
    clearRubberBand();
  }

  @Override // DefaultSelectAreaTracker
  public void mouseDragged(MouseEvent evt) {
    Rectangle invalidatedArea = (Rectangle) rubberband.clone();
    rubberband.setBounds(
        Math.min(anchor.x, evt.getX()),
        Math.min(anchor.y, evt.getY()),
        Math.abs(anchor.x - evt.getX()),
        Math.abs(anchor.y - evt.getY()));

    if (invalidatedArea.isEmpty()) {
      invalidatedArea = (Rectangle) rubberband.clone();
    }
    else {
      invalidatedArea = invalidatedArea.union(rubberband);
    }

    fireAreaInvalidated(invalidatedArea);
  }

  @Override // DefaultSelectAreaTracker
  public void mouseMoved(MouseEvent evt) {
    clearRubberBand();
    Point point = evt.getPoint();
    DrawingView view = editor.findView((Container) evt.getSource());
    updateCursor(view, point);

    if (view == null || editor.getActiveView() != view) {
      clearHoverHandles();
    }
    else {
      // Search first, if one of the selected figures contains
      // the current mouse location, and is selectable. 
      // Only then search for other
      // figures. This search sequence is consistent with the
      // search sequence of the SelectionTool.
      Figure figure = null;
      Point2D.Double p = view.viewToDrawing(point);

      for (Figure f : view.getSelectedFigures()) {
        if (f != null && f.contains(p)) {
          figure = f;
        }
      }

      if (figure == null) {
        figure = view.findFigure(point);

        while (figure != null && !figure.isSelectable()) {
          figure = view.getDrawing().findFigureBehind(p, figure);
        }
      }

      updateHoverHandles(view, figure);
    }
  }

  private void clearRubberBand() {
    if (!rubberband.isEmpty()) {
      fireAreaInvalidated(rubberband);
      rubberband.width = -1;
    }
  }

  /**
   * Overrides DefaultSelectAreaTracker.
   */
  private void selectGroup() {
    Collection<Figure> figures = getView().findFiguresWithin(rubberband);

    for (Figure f : figures) {
      if (f.isSelectable()) {
        getView().addToSelection(f);
      }
    }
  }

  @Override // DefaultSelectAreaTracker
  public void draw(Graphics2D g) {
    g.setStroke(rubberbandStroke);
    g.setColor(rubberbandColor);
    g.drawRect(rubberband.x, rubberband.y, rubberband.width - 1, rubberband.height - 1);

    if (!hoverHandles.isEmpty()) { /// && !getView().isFigureSelected(hoverFigure)) {
      for (Handle h : hoverHandles) {
        h.draw(g);
      }
    }
  }

  @Override
  protected void updateHoverHandles(DrawingView drawingView, Figure figure) {
    if (figure != hoverFigure) {
      Rectangle r = null;

      if (hoverFigure != null) {
        for (Handle h : hoverHandles) {
          if (r == null) {
            r = h.getDrawingArea();
          }
          else {
            r.add(h.getDrawingArea());
          }

          h.setView(null);
          h.dispose();
        }

        hoverHandles.clear();
      }

      hoverFigure = figure;

      if (hoverFigure != null && figure.isSelectable()) {
        switch (appState.getOperationMode()) {
          case MODELLING:
          case OPERATING:
            hoverHandles.addAll(hoverFigure.createHandles(-1));

            for (Handle h : hoverHandles) {
              h.setView(drawingView);

              if (r == null) {
                r = h.getDrawingArea();
              }
              else {
                r.add(h.getDrawingArea());
              }
            }

            break;
          default:
          // Do nada.
        }
      }

      if (r != null) {
        r.grow(1, 1);
        fireAreaInvalidated(r);
      }
    }
  }
}
