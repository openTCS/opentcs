/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.util.Cursors;

/**
 * A listener for dragging of the drawing view and single objects inside the
 * view.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewDragScrollListener
    extends MouseAdapter {

  /**
   * The scroll pane enclosing the drawing view.
   */
  private final DrawingViewScrollPane scrollPane;
  /**
   * The combo box for selecting the zoom level.
   */
  private final JComboBox<ZoomItem> zoomComboBox;
  /**
   * The button for enabling object selection.
   */
  private final JToggleButton selectionTool;
  /**
   * The button for enabling dragging.
   */
  private final JToggleButton dragTool;
  /**
   * The status panel to display the current mouse position in.
   */
  private final StatusPanel statusPanel;
  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;
  /**
   * A default cursor for the drawing view.
   */
  private final Cursor defaultCursor;
  /**
   * The start position of drag movements.
   */
  private final Point startPoint = new Point();
  /**
   * Start coordinate for measuring.
   * XXX Is this redundant, or is it used for something different than startPoint?
   */
  private final Point2D.Double fMouseStartPoint = new Point2D.Double();
  /**
   * Current coordinate for measuring.
   */
  private final Point2D.Double fMouseCurrentPoint = new Point2D.Double();
  /**
   * End coordinate for measuring.
   */
  private final Point2D.Double fMouseEndPoint = new Point2D.Double();

  /**
   * Creates a new instance.
   *
   * @param scrollPane The scroll pane enclosing the drawing view.
   * @param zoomComboBox The combo box for selecting the zoom level.
   * @param selectionTool The button for enabling object selection.
   * @param dragTool The button for enabling dragging.
   * @param statusPanel The status panel to display the current mouse position
   * in.
   * @param modelManager The manager keeping/providing the currently loaded
   * model.
   */
  public ViewDragScrollListener(DrawingViewScrollPane scrollPane,
                                JComboBox<ZoomItem> zoomComboBox,
                                JToggleButton selectionTool,
                                JToggleButton dragTool,
                                StatusPanel statusPanel,
                                ModelManager modelManager) {
    this.scrollPane = requireNonNull(scrollPane, "scrollPane");
    this.zoomComboBox = requireNonNull(zoomComboBox, "zoomComboBox");
    this.selectionTool = requireNonNull(selectionTool, "selectionTool");
    this.dragTool = requireNonNull(dragTool, "dragTool");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.defaultCursor = scrollPane.getDrawingView().getCursor();
  }

  @Override
  public void mouseDragged(final MouseEvent evt) {
    final OpenTCSDrawingView drawingView = scrollPane.getDrawingView();
    if (drawingView.vehicleDragged()) {
      drawingView.setCursor(Cursors.getDragVehicleCursor());
    }

    if (!(drawingView.getParent() instanceof JViewport)) {
      return;
    }

    final JViewport viewport = (JViewport) drawingView.getParent();
    Point cp = SwingUtilities.convertPoint(drawingView, evt.getPoint(), viewport);
    int dx = startPoint.x - cp.x;
    int dy = startPoint.y - cp.y;
    Point vp = viewport.getViewPosition();
    vp.translate(dx, dy);
    if (dragTool.isSelected()) {
      drawingView.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
    }
    else {
      viewport.revalidate();
      Figure figure = drawingView.findFigure(evt.getPoint());
      if (figure != null) {
        int x;
        int y;
        if (dx < 0) {
          x = evt.getX() + (int) figure.getDrawingArea().width;
        }
        else {
          x = evt.getX() - (int) figure.getDrawingArea().width;
        }
        if (dy < 0) {
          y = evt.getY() + (int) figure.getDrawingArea().height;
        }
        else {
          y = evt.getY() - (int) figure.getDrawingArea().height;
        }
        drawingView.scrollRectToVisible(new Rectangle(x, y, 1, 1));
        fMouseCurrentPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
        showPositionStatus(false);
        startPoint.setLocation(cp);
      }
    }
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Rectangle2D.Double drawingArea = drawingView.getDrawing().getDrawingArea();
        scrollPane.getHorizontalRuler().setPreferredWidth((int) drawingArea.width);
        scrollPane.getVerticalRuler().setPreferredHeight((int) drawingArea.height);
      }
    });
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    final OpenTCSDrawingView drawingView = scrollPane.getDrawingView();
    if (dragIsSelected()) {
      drawingView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    Container c = drawingView.getParent();
    if (c instanceof JViewport) {
      JViewport viewPort = (JViewport) c;
      Point cp = SwingUtilities.convertPoint(drawingView, evt.getPoint(), viewPort);
      startPoint.setLocation(cp);
    }
    fMouseCurrentPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    fMouseStartPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    showPositionStatus(false);
  }

  @Override
  public void mouseReleased(MouseEvent evt) {
    if (dragIsSelected()) {
      return;
    }

    final OpenTCSDrawingView drawingView = scrollPane.getDrawingView();
    Figure fig = drawingView.findFigure(evt.getPoint());
    if (fig instanceof LabeledPointFigure) {
      drawingView.createPossibleTransportOrder(fig);
    }
    fMouseEndPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    if (evt.getButton() != 2) {
      showPositionStatus(true);
    }
    else {
      showPositionStatus(false);
    }
  }

  @Override
  public void mouseExited(MouseEvent evt) {
    dragIsSelected();
    clearPositionStatus();
  }

  @Override
  public void mouseEntered(MouseEvent evt) {
    dragIsSelected();
  }

  @Override
  public void mouseMoved(MouseEvent evt) {
    final OpenTCSDrawingView drawingView = scrollPane.getDrawingView();
    fMouseCurrentPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    showPositionStatus(false);
  }

  @Override
  public void mouseClicked(MouseEvent evt) {
    if (evt.getButton() == 2) {
      if (dragTool.isSelected()) {
        selectionTool.setSelected(true);
      }
      else if (selectionTool.isSelected()) {
        dragTool.setSelected(true);
      }
      // Sets the correct cursor
      dragIsSelected();
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.isControlDown()) {
      int zoomLevel = zoomComboBox.getSelectedIndex();
      int notches = e.getWheelRotation();
      if (zoomLevel != -1) {
        if (notches < 0) {
          if (zoomLevel > 0) {
            zoomLevel--;
            zoomComboBox.setSelectedIndex(zoomLevel);
          }
        }
        else {
          if (zoomLevel < zoomComboBox.getItemCount() - 1) {
            zoomLevel++;
            zoomComboBox.setSelectedIndex(zoomLevel);
          }
        }
      }
    }
  }

  /**
   * Checks whether the drag tool is selected.
   *
   * @return true if the drag tool is selected, false otherwise.
   */
  private boolean dragIsSelected() {
    final OpenTCSDrawingView drawingView = scrollPane.getDrawingView();
    if (!selectionTool.isSelected() && dragTool.isSelected()) {
      drawingView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      return true;
    }
    else if (selectionTool.isSelected() && !dragTool.isSelected()) {
      drawingView.setCursor(defaultCursor);
      return false;
    }
    else {
      return false;
    }
  }

  /**
   * Clears the mouse position information in the status panel.
   */
  private void clearPositionStatus() {
    statusPanel.setPositionText("");
  }

  /**
   * Displays the current mouse position or covered area in the status panel.
   *
   * @param showCoveredArea Whether to display the dimensions of the covered
   * area instead of the current mouse coordinates.
   */
  private void showPositionStatus(boolean showCoveredArea) {
    double x = fMouseCurrentPoint.x;
    double y = -fMouseCurrentPoint.y;

    if (showCoveredArea) {
      double w = Math.abs(fMouseEndPoint.x - fMouseStartPoint.x);
      double h = Math.abs(fMouseEndPoint.y - fMouseStartPoint.y);
      statusPanel.setPositionText(
          String.format("X %.0f Y %.0f W %.0f H %.0f", x, y, w, h));
    }
    else {
      List<LayoutModel> layouts = modelManager.getModel().getLayoutModels();

      if (!layouts.isEmpty()) {
        LayoutModel layout = layouts.iterator().next();

        LengthProperty lpx
            = (LengthProperty) layout.getProperty(LayoutModel.SCALE_X);
        LengthProperty lpy
            = (LengthProperty) layout.getProperty(LayoutModel.SCALE_Y);
        double scaleX = (double) lpx.getValue();
        double scaleY = (double) lpy.getValue();
        double xmm = x * scaleX;
        double ymm = y * scaleY;
        statusPanel.setPositionText(
            String.format("X %.0f (%.0fmm) Y %.0f (%.0fmm)", x, xmm, y, ymm));
      }
      else {
        statusPanel.setPositionText(String.format("X %.0f Y %.0f", x, y));
      }
    }
  }
}
