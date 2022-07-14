/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.StatusPanel;
import org.opentcs.guing.common.components.drawing.DrawingViewScrollPane;
import org.opentcs.guing.common.components.drawing.ZoomItem;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.common.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.common.components.drawing.figures.PathConnection;
import org.opentcs.guing.common.components.drawing.figures.liner.TripleBezierLiner;
import org.opentcs.guing.common.components.drawing.figures.liner.TupelBezierLiner;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;
import org.opentcs.operationsdesk.exchange.TransportOrderUtil;
import org.opentcs.operationsdesk.util.Cursors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ViewDragScrollListener.class);
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
   * A helper for creating transport orders.
   */
  private final TransportOrderUtil orderUtil;
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
   * The figure a user may have pressed on / want to drag.
   */
  private Figure pressedFigure;

  /**
   * Creates a new instance.
   *
   * @param scrollPane The scroll pane enclosing the drawing view.
   * @param zoomComboBox The combo box for selecting the zoom level.
   * @param selectionTool The button for enabling object selection.
   * @param dragTool The button for enabling dragging.
   * @param statusPanel The status panel to display the current mouse position in.
   * @param modelManager The manager keeping/providing the currently loaded model.
   * @param orderUtil A helper for creating transport orders.
   */
  public ViewDragScrollListener(DrawingViewScrollPane scrollPane,
                                JComboBox<ZoomItem> zoomComboBox,
                                JToggleButton selectionTool,
                                JToggleButton dragTool,
                                StatusPanel statusPanel,
                                ModelManager modelManager,
                                TransportOrderUtil orderUtil) {
    this.scrollPane = requireNonNull(scrollPane, "scrollPane");
    this.zoomComboBox = requireNonNull(zoomComboBox, "zoomComboBox");
    this.selectionTool = requireNonNull(selectionTool, "selectionTool");
    this.dragTool = requireNonNull(dragTool, "dragTool");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
    this.defaultCursor = scrollPane.getDrawingView().getComponent().getCursor();
  }

  @Override
  public void mouseDragged(final MouseEvent evt) {
    final DrawingView drawingView = scrollPane.getDrawingView();
    if (vehicleDragged()) {
      drawingView.setCursor(Cursors.getDragVehicleCursor());
    }

    if (!(drawingView.getComponent().getParent() instanceof JViewport)) {
      return;
    }

    final JViewport viewport = (JViewport) drawingView.getComponent().getParent();
    Point cp = SwingUtilities.convertPoint(drawingView.getComponent(), evt.getPoint(), viewport);

    if (dragTool.isSelected()) {
      int dx = startPoint.x - cp.x;
      int dy = startPoint.y - cp.y;
      Point vp = viewport.getViewPosition();
      vp.translate(dx, dy);
      drawingView.getComponent().scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
    }
    else { // The selection tool is selected
      viewport.revalidate();

      if (isMovableFigure(pressedFigure)) {
        if (!isFigureCompletelyInView(pressedFigure, viewport, drawingView)) {
          // If the figure exceeds the current view, start scrolling as soon as the mouse is 
          // hitting the view bounds.
          drawingView.getComponent().scrollRectToVisible(new Rectangle(evt.getX(), evt.getY(), 1, 1));
        }

        fMouseCurrentPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
        showPositionStatus(false);
        startPoint.setLocation(cp);
      }
    }
    SwingUtilities.invokeLater(() -> {
      Rectangle2D.Double drawingArea = drawingView.getDrawing().getDrawingArea();
      scrollPane.getHorizontalRuler().setPreferredWidth((int) drawingArea.width);
      scrollPane.getVerticalRuler().setPreferredHeight((int) drawingArea.height);
    });
  }

  private boolean isMovableFigure(Figure figure) {
    return (figure instanceof LabeledPointFigure)
        || (figure instanceof LabeledLocationFigure)
        || ((figure instanceof PathConnection)
            && (((PathConnection) figure).getLiner() instanceof TupelBezierLiner))
        || ((figure instanceof PathConnection)
            && (((PathConnection) figure).getLiner() instanceof TripleBezierLiner));
  }

  private boolean isFigureCompletelyInView(Figure figure,
                                           JViewport viewport,
                                           DrawingView drawingView) {
    Rectangle viewPortBounds = viewport.getViewRect();
    Rectangle figureBounds = drawingView.drawingToView(figure.getDrawingArea());

    return (figureBounds.getMinX() > viewPortBounds.getMinX())
        && (figureBounds.getMinY() > viewPortBounds.getMinY())
        && (figureBounds.getMaxX() < viewPortBounds.getMaxX())
        && (figureBounds.getMaxY() < viewPortBounds.getMaxY());
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    final DrawingView drawingView = scrollPane.getDrawingView();
    if (dragIsSelected()) {
      drawingView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    Container c = drawingView.getComponent().getParent();
    if (c instanceof JViewport) {
      JViewport viewPort = (JViewport) c;
      Point cp = SwingUtilities.convertPoint(drawingView.getComponent(), evt.getPoint(), viewPort);
      startPoint.setLocation(cp);
    }
    pressedFigure = drawingView.findFigure(evt.getPoint());
    fMouseCurrentPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    fMouseStartPoint.setLocation(drawingView.viewToDrawing(evt.getPoint()));
    showPositionStatus(false);
  }

  @Override
  public void mouseReleased(MouseEvent evt) {
    if (dragIsSelected()) {
      return;
    }

    final DrawingView drawingView = scrollPane.getDrawingView();
    Figure fig = drawingView.findFigure(evt.getPoint());
    if (fig instanceof LabeledPointFigure) {
      createPossibleTransportOrder((LabeledPointFigure) fig, drawingView.getSelectedFigures());
    }
    pressedFigure = null;
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
    final DrawingView drawingView = scrollPane.getDrawingView();
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
    final DrawingView drawingView = scrollPane.getDrawingView();
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
      LayoutModel layout = modelManager.getModel().getLayoutModel();

      double scaleX = (double) layout.getPropertyScaleX().getValue();
      double scaleY = (double) layout.getPropertyScaleY().getValue();
      double xmm = x * scaleX;
      double ymm = y * scaleY;
      statusPanel.setPositionText(
          String.format("X %.0f (%.0fmm) Y %.0f (%.0fmm)", x, xmm, y, ymm));
    }
  }

  /**
   * Creates a transport order, assuming a single vehicle was selected before.
   *
   * @param figure A point figure.
   */
  private void createPossibleTransportOrder(LabeledPointFigure figure,
                                            Set<Figure> selectedFigures) {
    if (selectedFigures.size() != 1) {
      LOG.debug("More than one figure selected, skipping.");
      return;
    }

    Figure nextFigure = selectedFigures.iterator().next();

    if (!(nextFigure instanceof VehicleFigure)) {
      LOG.debug("Selected figure is not a VehicleFigure, skipping.");
      return;
    }

    PointModel model = figure.getPresentationFigure().getModel();
    VehicleModel vehicleModel = (VehicleModel) nextFigure.get(FigureConstants.MODEL);

    if (vehicleModel == null) {
      LOG.warn("Selected VehicleFigure does not have a model, skipping.");
      return;
    }
    if ((Vehicle.ProcState) vehicleModel.getPropertyProcState().getValue() != Vehicle.ProcState.IDLE) {
      LOG.debug("Selected vehicle already has an order, skipping.");
      return;
    }

    orderUtil.createTransportOrder(model, vehicleModel);
  }

  /**
   * Returns if a vehicle is currently being dragged.
   *
   * @return True if yes, false otherwise.
   */
  private boolean vehicleDragged() {
    Set<Figure> selectedFigures = scrollPane.getDrawingView().getSelectedFigures();
    if (selectedFigures.size() != 1) {
      return false;
    }

    return selectedFigures.iterator().next() instanceof VehicleFigure;
  }

}
