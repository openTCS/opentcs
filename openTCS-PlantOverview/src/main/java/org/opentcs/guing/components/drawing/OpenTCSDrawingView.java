/**
 * (c): IML, JHotDraw.
 *
 *
 * Extended by IML: 1. Show Blocks and Pathes as overlay 2. Switch labels on/off
 *
 * @(#)DefaultDrawingView.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.components.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jhotdraw.draw.AbstractFigure;
import org.jhotdraw.draw.AttributeKey;
import static org.jhotdraw.draw.AttributeKeys.CANVAS_FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.CANVAS_FILL_OPACITY;
import static org.jhotdraw.draw.AttributeKeys.CANVAS_HEIGHT;
import static org.jhotdraw.draw.AttributeKeys.CANVAS_WIDTH;
import org.jhotdraw.draw.Constrainer;
import org.jhotdraw.draw.DefaultDrawingViewTransferHandler;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import static org.jhotdraw.draw.DrawingView.ACTIVE_HANDLE_PROPERTY;
import static org.jhotdraw.draw.DrawingView.CONSTRAINER_VISIBLE_PROPERTY;
import static org.jhotdraw.draw.DrawingView.DRAWING_PROPERTY;
import static org.jhotdraw.draw.DrawingView.INVISIBLE_CONSTRAINER_PROPERTY;
import static org.jhotdraw.draw.DrawingView.VISIBLE_CONSTRAINER_PROPERTY;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.CompositeFigureEvent;
import org.jhotdraw.draw.event.CompositeFigureListener;
import org.jhotdraw.draw.event.FigureAdapter;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.draw.event.FigureListener;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.jhotdraw.draw.event.HandleEvent;
import org.jhotdraw.draw.event.HandleListener;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.gui.datatransfer.ClipboardUtil;
import org.jhotdraw.util.ReversedList;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.drawing.figures.BitmapFigure;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.OriginFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.event.StaticRouteChangeEvent;
import org.opentcs.guing.event.StaticRouteChangeListener;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DrawingView implementation for the openTCS plant overview.
 *
 */
public abstract class OpenTCSDrawingView
    extends JComponent
    implements DrawingView,
               EditableComponent,
               PropertyChangeListener,
               EventHandler {

  public static final String FOCUS_GAINED = "focusGained";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSDrawingView.class);
  /**
   * Decoration of paths, points and locations that are part of a block.
   */
  private static final BasicStroke BLOCK_STROKE = new BasicStroke(4.0f);
  /**
   * Decoration of paths and points that are part of a static route.
   */
  private static final BasicStroke STATIC_ROUTE_STROKE = new BasicStroke(6.0f);
  /**
   * Decoration of paths that are part of a transport order.
   */
  private static final float[] PATH_DASH = {10.0f, 5.0f};
  private static final BasicStroke PATH_STROKE = new BasicStroke(
      6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, PATH_DASH, 0.0f);
  /**
   * Decoration of paths that are part of a withdrawn transport order.
   */
  private static final float[] WITHDRAWN_PATH_DASH = {8.0f, 4.0f, 2.0f, 4.0f};
  private static final BasicStroke WITHDRAWN_PATH_STROKE
      = new BasicStroke(6.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f,
                        WITHDRAWN_PATH_DASH,
                        0.0f);
  /**
   * The actual drawing.
   */
  private Drawing drawing;
  /**
   * Holds the selected figures in an ordered put. The ordering reflects the
   * sequence that was used to select the figures.
   */
  private final Set<Figure> selectedFigures = new LinkedHashSet<>();
  private final List<Handle> selectionHandles = new LinkedList<>();
  private boolean isConstrainerVisible = true;
  private Constrainer visibleConstrainer = new GridConstrainer(10, 10);
  private Constrainer invisibleConstrainer = new GridConstrainer();
  private Handle activeHandle;
  private final List<Handle> secondaryHandles = new LinkedList<>();
  private boolean handlesAreValid = true;
  private transient Dimension cachedPreferredSize;
  private double zoomX = 1.0;
  private double zoomY = 1.0;
  private final Point translation = new Point(0, 0);
  private int detailLevel;
  private OpenTCSDrawingEditor editor;
  private BufferedImage backgroundTile;
  private final FigureListener handleInvalidator = new HandleInvalidator();
  private transient Rectangle2D.Double cachedDrawingArea;
  /**
   * Holds the drawing area (in view coordinates) which is in the drawing
   * buffer.
   */
  protected final Rectangle bufferedArea = new Rectangle();
  /**
   * Holds the drawing area (in view coordinates) which has not been redrawn yet
   * in the drawing buffer.
   */
  protected final Rectangle dirtyArea = new Rectangle(0, 0, -1, -1);
  /**
   * This listener sets the position of the invisible offsetFigure after the
   * frame was resized.
   */
  private ComponentListener offsetListener;
  private boolean paintEnabled = true;

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The view we're working with.
   */
  private final OpenTCSView fOpenTCSView;
  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;
  /**
   * A helper for creating transport orders with the kernel.
   */
  private final TransportOrderUtil orderUtil;
  /**
   * The block areas.
   */
  private ModelComponent fBlocks;
  /**
   * The static routes.
   */
  private ModelComponent fStaticRoutes;
  /**
   * Flag whether the labels shall be drawn.
   */
  private boolean labelsVisible = true;
  /**
   * Flag whether the blocks shall be drawn.
   */
  private boolean blocksVisible = true;
  /**
   * Flag whether the static routes shall be drawn.
   */
  private boolean staticRoutesVisible = false;
  /**
   * Contains the vehicle on the drawing, for which transport order shall
   * be drawn.
   */
  private final List<VehicleModel> fVehicles = new ArrayList<>();
  /**
   * A pointer to a figure that shall be highlighted by a red circle.
   */
  private Figure fFocusFigure;
  /**
   * The Static Route the view should highlight.
   */
  private StaticRouteModel fFocusStaticRoute;
  /**
   * The vehicle the view should highlight and follow.
   */
  private VehicleModel fFocusVehicle;
  /**
   * Prohibits a loop in repaint().
   */
  private boolean doRepaint = true;
  /**
   * Background figures set to this DrawingView.
   */
  private final List<BitmapFigure> bitmapFigures = new ArrayList<>();
  /**
   * Contains figures that are in a group and set to invisible for this
   * DrawingView.
   */
  private final List<AbstractFigure> invisibleFigures = new ArrayList<>();
  /**
   * An event handler for CompositeFigureEvents.
   */
  private final CompositeFigureEventHandler cmpFigureEvtHandler
      = new CompositeFigureEventHandler();
  /**
   * An event handler for FigureEvents.
   */
  private final FigureEventHandler figureEventHandler = new FigureEventHandler();
  /**
   * An event handler for HandleEvents.
   */
  private final HandleEventHandler handleEventHandler = new HandleEventHandler();

  /**
   * Contains figures currently in the buffer (eg when copying or cutting figures).
   */
  private List<Figure> bufferedFigures = new ArrayList<>();
  /**
   * Handles edits of bezier liners.
   */
  private final BezierLinerEditHandler bezierLinerEditHandler = new BezierLinerEditHandler();
  /**
   * Handles events for blocks.
   */
  private final BlockChangeHandler blockChangeHandler = new BlockChangeHandler();
  /**
   * Handles events for static routes.
   */
  private final StaticRouteChangeHandler routeChangeHandler = new StaticRouteChangeHandler();

  /**
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param opentcsView The view to be used.
   * @param modelManager Provides the current system model.
   * @param orderUtil A helper for creating transport orders with the kernel.
   */
  public OpenTCSDrawingView(ApplicationState appState,
                            OpenTCSView opentcsView,
                            ModelManager modelManager,
                            TransportOrderUtil orderUtil) {
    this.appState = requireNonNull(appState, "appState");
    this.fOpenTCSView = requireNonNull(opentcsView, "opentcsView");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");

    // Set a dummy tool tip text to turn tooltips on
    setToolTipText(" ");
    setFocusable(true);
    addFocusListener(new FocusHandler());
    setTransferHandler(new DefaultDrawingViewTransferHandler());
    setBackground(Color.LIGHT_GRAY);
    setOpaque(true);
    setAutoscrolls(true);
  }

  @Override
  public void removeAll() {
    fVehicles.clear();
    super.removeAll();
  }

  @Override
  public void processKeyEvent(KeyEvent e) {
    if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
      if (e.getKeyCode() == KeyEvent.VK_X // Cut
          || e.getKeyCode() == KeyEvent.VK_C // Copy
          || e.getKeyCode() == KeyEvent.VK_V // Paste
          || e.getKeyCode() == KeyEvent.VK_D) // Duplicate
      {
        if (!appState.hasOperationMode(OperationMode.MODELLING)) {
          return;
        }
        processCutPasteKeyEvent();
      }
    }

    super.processKeyEvent(e);
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        removeAll();
        break;
      default:
      // Do nada.
    }
  }

  private void processCutPasteKeyEvent() {
    for (Figure figure : new ArrayList<>(selectedFigures)) {
      if (figure instanceof PathConnection) {
        // A Path may only be selected if the connected start and end Points are selected, too
        PathConnection pathConnection = (PathConnection) figure;
        Figure startFigure = pathConnection.getStartFigure();
        Figure endFigure = pathConnection.getEndFigure();

        if (!selectedFigures.contains(startFigure)
            || !selectedFigures.contains(endFigure)) {
          selectedFigures.remove(figure);
        }
      }
      else if (figure instanceof LinkConnection) {
        // A Link may only be selected if the connected Point and Location are selected, too
        LinkConnection linkConnection = (LinkConnection) figure;
        Figure startFigure = linkConnection.getStartFigure();
        Figure endFigure = linkConnection.getEndFigure();

        if (!selectedFigures.contains(startFigure)
            || !selectedFigures.contains(endFigure)) {
          selectedFigures.remove(figure);
        }
      }
    }

    if (selectedFigures.isEmpty()) {
      ClipboardUtil.getClipboard().setContents(new Transferable() {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[0];
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
          return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
          throw new UnsupportedOperationException("Not supported yet.");
        }
      }, null);
    }
  }

  public boolean isBlocksVisible() {
    return blocksVisible;
  }

  public void setBlocksVisible(boolean newValue) {
    blocksVisible = newValue;
    // Repaint the whole layout.
    dirtyArea.add(getVisibleRect());
    repaint();
  }

  /**
   * Sets the visibility status of the given group members.
   *
   * @param components The items that shall be shown / hidden.
   * @param visible Visible or not.
   */
  public void setGroupVisible(List<ModelComponent> components, boolean visible) {
    for (ModelComponent model : components) {
      if (!(model instanceof GroupModel)) {
        AbstractFigureComponent abstractFigure = (AbstractFigureComponent) model;

        if (model instanceof LocationModel) {
          LocationModel locModel = (LocationModel) model;

          for (AbstractConnection connection : locModel.getConnections()) {
            AbstractFigure figure = (AbstractFigure) connection.getFigure();
            figure.setVisible(visible);
          }
        }

        AbstractFigure figure = (AbstractFigure) abstractFigure.getFigure();

        if (visible) {
          invisibleFigures.remove(figure);
        }
        else {
          invisibleFigures.add(figure);
        }

        figure.setVisible(visible);
      }
    }

    repaint();
  }

  public boolean isLabelsVisible() {
    return labelsVisible;
  }

  public void setLabelsVisible(boolean newValue) {
    labelsVisible = newValue;

    if (drawing == null) {
      return;
    }

    for (Figure figure : drawing.getChildren()) {
      if (figure instanceof LabeledFigure) {
        LabeledFigure lf = (LabeledFigure) figure;
        lf.setLabelVisible(newValue);
      }
    }
    // Repaint the whole layout.
    dirtyArea.add(getVisibleRect());
    repaint();
  }

  public boolean isStaticRoutesVisible() {
    return staticRoutesVisible;
  }

  public void setStaticRoutesVisible(boolean newValue) {
    staticRoutesVisible = newValue;
    // Repaint the whole layout.
    dirtyArea.add(getVisibleRect());
    repaint();
  }

  /**
   * Returns if there are currently some figures in the buffer.
   *
   * @return True if there are some, false if not.
   */
  public boolean hasBufferedFigures() {
    return !bufferedFigures.isEmpty();
  }

  /**
   * Draws the background of the drawing view.
   *
   * @param g2d
   */
  private void drawBackground(Graphics2D g2d) {
    if (drawing == null) {
      // there is no drawing and thus no canvas
      g2d.setColor(getBackground());
      g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    else if (drawing.get(CANVAS_WIDTH) == null
        || drawing.get(CANVAS_HEIGHT) == null) {
      // the canvas is infinitely large
      Color canvasColor = drawing.get(CANVAS_FILL_COLOR);
      double canvasOpacity = drawing.get(CANVAS_FILL_OPACITY);

      if (canvasColor != null) {
        if (canvasOpacity == 1) {
          g2d.setColor(new Color(canvasColor.getRGB()));
          g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        else {
          Point r = drawingToView(new Point2D.Double(0, 0));
          g2d.setPaint(getBackgroundPaint(r.x, r.y));
          g2d.fillRect(0, 0, getWidth(), getHeight());
          g2d.setColor(new Color(canvasColor.getRGB() & 0xfffff | ((int) (canvasOpacity * 256) << 24), true));
          g2d.fillRect(0, 0, getWidth(), getHeight());
        }
      }
      else {
        Point r = drawingToView(new Point2D.Double(0, 0));
        g2d.setPaint(getBackgroundPaint(r.x, r.y));
        g2d.fillRect(0, 0, getWidth(), getHeight());
      }
    }
    else {
      // the canvas has a fixed size
      g2d.setColor(getBackground());
      g2d.fillRect(0, 0, getWidth(), getHeight());
      Rectangle r = drawingToView(new Rectangle2D.Double(0, 0, drawing.get(CANVAS_WIDTH),
                                                         drawing.get(CANVAS_HEIGHT)));
      g2d.setPaint(getBackgroundPaint(r.x, r.y));
      g2d.fillRect(r.x, r.y, r.width, r.height);
    }
  }

  /**
   * Returns the background images for this drawing view.
   *
   * @return List containing the associated bitmap figures.
   */
  public List<BitmapFigure> getBackgroundBitmaps() {
    return bitmapFigures;
  }

  /**
   * Adds a background image to this drawing view.
   *
   * @param file The file with the image.
   */
  public void addBackgroundBitmap(File file) {
    BitmapFigure fig = new BitmapFigure(file);
    bitmapFigures.add(fig);
    drawing.add(fig);
    drawing.sendToBack(fig);
  }

  /**
   * Adds a background image to this drawing view.
   *
   * @param bitmapFigure The figure containing the image.
   */
  public void addBackgroundBitmap(BitmapFigure bitmapFigure) {
    bitmapFigures.add(bitmapFigure);
    drawing.add(bitmapFigure);
    drawing.sendToBack(bitmapFigure);
  }

  /**
   * Scrolls to the given figure. Normally called when the user clicks on
   * a model component in the TreeView and wants to see the corresponding
   * figure.
   *
   * @param figure The figure to be scrolled to.
   */
  public void scrollTo(Figure figure) {
    if (figure == null) {
      return;
    }
    // The rectangle that encloses the figure
    Rectangle2D.Double bounds = figure.getBounds();

    if (figure instanceof LabeledFigure) {
      // Also show the label
      LabeledFigure lf = (LabeledFigure) figure;
      TCSLabelFigure label = lf.getLabel();

      if (label != null) {
        Rectangle2D.Double labelBounds = label.getBounds();
        bounds.add(labelBounds);
      }
    }
    else if (figure instanceof VehicleFigure) {
      // Also show the target point
      VehicleModel vehicleModel = ((VehicleFigure) figure).getModel();
      PointModel pointModel = vehicleModel.getNextPoint();

      if (pointModel != null) {
        bounds.add(pointModel.getFigure().getBounds());
      }
    }

    if (fFocusVehicle == null) {
      fFocusFigure = figure;
      fFocusStaticRoute = null;
    }

    final int margin = 50;
    double xCenter = bounds.getCenterX() - margin;
    double yCenter = bounds.getCenterY() - margin;
    double wBounds = bounds.getWidth() + 2 * margin;
    double hBounds = bounds.getHeight() + 2 * margin;

    Point2D.Double pCenterView = new Point2D.Double(xCenter, yCenter);
    Point pCenterDrawing = drawingToView(pCenterView);
    Dimension dBounds = new Dimension((int) wBounds, (int) hBounds);

    Rectangle rCenter = new Rectangle(pCenterDrawing, dBounds);
    scrollRectToVisible(rCenter);

    repaint();
  }

  /**
   * Moves the view so that the given points will be in the middle of the
   * drawing.
   *
   * @param xCenter The x coord that shall be in the middle.
   * @param yCenter The y coord that shall be in the middle.
   */
  private void scrollTo(final int xCenter, final int yCenter) {

    Runnable doScroll = new Runnable() {
      @Override
      public void run() {
        Point2D.Double pCenterView = new Point2D.Double(xCenter, -yCenter); // Vorzeichen!
        Point pCenterDrawing = drawingToView(pCenterView);
        JViewport viewport = (JViewport) getParent();
        int xUpperLeft = pCenterDrawing.x - viewport.getSize().width / 2;
        int yUpperLeft = pCenterDrawing.y - viewport.getSize().height / 2;
        Point pUpperLeft = new Point(xUpperLeft, yUpperLeft);
        Rectangle rCenter = new Rectangle(pUpperLeft, viewport.getSize());
        scrollRectToVisible(rCenter);
      }
    };

    SwingUtilities.invokeLater(doScroll);
  }

  /**
   * Scales and positions the drawing according to the given bookmark.
   *
   * @param bookmark The bookmark.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void scaleAndScrollTo(org.opentcs.data.model.visualization.ViewBookmark bookmark) {
    scaleAndScrollTo(bookmark.getViewScaleX(),
                     bookmark.getCenterX(),
                     bookmark.getCenterY());
  }

  /**
   * Scales the drawing and moves to the given point afterwards.
   *
   * @param newScale The new scale factor.
   * @param xCenter The x coord that shall be in the middle.
   * @param yCenter The y coord that shall be in the middle.
   */
  private void scaleAndScrollTo(final double newScale,
                                final int xCenter,
                                final int yCenter) {
    final Runnable doScroll = new Runnable() {
      @Override
      public void run() {
        scrollTo(xCenter, yCenter);
      }
    };

    double oldScale = zoomX;
    zoomX = zoomY = newScale;
    validateViewTranslation();
    dirtyArea.setBounds(bufferedArea);
    revalidate();
    repaint();
    firePropertyChange("scaleFactor", oldScale, newScale);
    SwingUtilities.invokeLater(doScroll);
  }

  /**
   * Fixes the view on the vehicle and marks it and its destination with a
   * colored circle.
   *
   * @param model The vehicle model.
   */
  public void followVehicle(@Nonnull final VehicleModel model) {
    requireNonNull(model, "model");

    stopFollowVehicle();
    fFocusVehicle = model;
    fFocusVehicle.setViewFollows(true);
    VehicleFigure vFigure = fFocusVehicle.getFigure();
    if (vFigure != null) {
      vFigure.addPropertyChangeListener(this);
      scrollTo(vFigure);
    }
  }

  /**
   * Releases the view and stops following the current vehicle.
   */
  public void stopFollowVehicle() {
    if (fFocusVehicle == null) {
      return;
    }

    fFocusVehicle.setViewFollows(false);
    VehicleFigure vFigure = fFocusVehicle.getFigure();
    if (vFigure != null) {
      vFigure.removePropertyChangeListener(this);
    }
    fFocusVehicle = null;
    repaint();
  }

  public void highlightStaticRoute(StaticRouteModel staticRoute) {
    fFocusStaticRoute = staticRoute;
  }

  @Override // PropertyChangeListener
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(VehicleFigure.POSITION_CHANGED)) {
      scrollTo(fFocusVehicle.getFigure());
    }
  }

  protected static void setViewRenderingHints(Graphics2D g) {
    // Set rendering hints for speed
    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                       RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                       RenderingHints.VALUE_STROKE_NORMALIZE);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                       RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                       RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING,
                       RenderingHints.VALUE_RENDER_SPEED);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                       RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }

  /**
   * Returns the bounds of the canvas on the drawing view.
   *
   * @return The current bounds of the canvas on the drawing view.
   */
  private Rectangle getCanvasViewBounds() {
    // Position of the zero coordinate point on the view
    int x = -translation.x;
    int y = -translation.y;

    int w = getWidth();
    int h = getHeight();

    if (getDrawing() != null) {
      Double cw = getDrawing().get(CANVAS_WIDTH);
      Double ch = getDrawing().get(CANVAS_HEIGHT);

      if (cw != null && ch != null) {
        Point lowerRight = drawingToView(new Point2D.Double(cw, ch));
        w = lowerRight.x - x;
        h = lowerRight.y - y;
      }
    }

    return new Rectangle(x, y, w, h);
  }

  /**
   * Draws the canvas. If the {@code AttributeKeys.CANVAS_FILL_OPACITY} is not
   * fully opaque, the canvas area is filled with the background paint before
   * the {@code AttributeKeys.CANVAS_FILL_COLOR} is drawn.
   */
  private void drawCanvas(Graphics2D gr) {
    if (drawing == null) {
      return;
    }

    Graphics2D g = (Graphics2D) gr.create();
    AffineTransform tx = g.getTransform();
    tx.translate(-translation.x, -translation.y);
    tx.scale(zoomX, zoomY);
    g.setTransform(tx);

    drawing.setFontRenderContext(g.getFontRenderContext());
    drawing.drawCanvas(g);
    g.dispose();
  }

  private void drawConstrainer(Graphics2D g) {
    getConstrainer().draw(g, this);
  }

  protected void drawDrawing(Graphics2D gr) {
    if (drawing == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) gr.create();
    AffineTransform tx = g2d.getTransform();
    tx.translate(-translation.x, -translation.y);
    tx.scale(zoomX, zoomY);
    g2d.setTransform(tx);

    if (blocksVisible) {
      drawBlocks(g2d);
    }

    drawStaticRoutes(g2d);
    drawDriveOrderElements(g2d);

    drawing.setFontRenderContext(g2d.getFontRenderContext());
    try {
      drawing.draw(g2d);
    }
    catch (ConcurrentModificationException e) {
      LOG.warn("Exception from JHotDraw caught while calling DefaultDrawing.draw(). "
          + "Continuing drawing the course.");
      // TODO What to do when it is catched?
    }

    g2d.dispose();
  }

  /**
   *
   * @param g2d
   */
  private void drawHandles(Graphics2D g2d) {
    if (editor == null || editor.getActiveView() != this) {
      return;
    }

    validateHandles();

    for (Handle h : getSelectionHandles()) {
      h.draw(g2d);
    }

    for (Handle h : getSecondaryHandles()) {
      h.draw(g2d);
    }
  }

  private void drawTool(Graphics2D g2d) {
    if (editor == null || editor.getTool() == null || editor.getActiveView() != this) {
      return;
    }

    editor.getTool().draw(g2d);

    if (fFocusVehicle != null) {
      // Set focus on the selected vehicle and its destination point
      highlightVehicle(g2d);
    }
    else if (fFocusStaticRoute != null) {
      highlightStaticRoute(g2d);
    }
    else {
      // Set focus on the selected figure
      highlightFocus(g2d);
    }
  }

  /**
   * Draws a focus circle around the currently selected figure.
   *
   * @param g2d
   */
  private void highlightFocus(Graphics2D g2d) {
    if (fFocusFigure == null || !fFocusFigure.isVisible()) {
      return;
    }

    final Figure currentFocusFigure = fFocusFigure;
    Rectangle2D.Double bounds = fFocusFigure.getBounds();
    double xCenter = bounds.getCenterX();
    double yCenter = bounds.getCenterY();

    if (fFocusFigure instanceof PathConnection) {
      xCenter = ((PathConnection) fFocusFigure).getCenter().x;
      yCenter = ((PathConnection) fFocusFigure).getCenter().y;
    }

    Point2D.Double pCenterView = new Point2D.Double(xCenter, yCenter);
    Point pCenterDrawing = drawingToView(pCenterView);

    // Create a radial gradient, transparent in the middle.
    Point2D center
        = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);
    float radius = 30;
    float[] dist = {0.0f, 0.7f, 0.8f, 1.0f};
    Color[] colors = {
      new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
      new Color(1.0f, 1.0f, 1.0f, 0.0f),
      new Color(1.0f, 0.0f, 0.0f, 0.7f), // Circle: red
      new Color(0.9f, 0.9f, 0.9f, 0.5f) // Background
    };
    RadialGradientPaint p
        = new RadialGradientPaint(center, radius, dist, colors,
                                  MultipleGradientPaint.CycleMethod.NO_CYCLE);

    Graphics2D gFocus = (Graphics2D) g2d.create();
    gFocus.setPaint(p);
    gFocus.fillRect(0, 0, getWidth(), getHeight());
    gFocus.dispose();

    Rectangle visibleRect = getVisibleRect();
    dirtyArea.add(visibleRect);

    // After drawing the RadialGradientPaint the drawing area needs to be
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    if (doRepaint) {
      repaint();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }

    // after 3 seconds the RadialGradientPaint is removed
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          synchronized (OpenTCSDrawingView.this) {
            OpenTCSDrawingView.this.wait(3000);
          }
        }
        catch (InterruptedException ex) {
        }

        // prevents repainting of the drawing area if in the 3 second wait
        // time another figure was selected
        if (fFocusFigure == currentFocusFigure) {
          fFocusFigure = null;
          repaint();
        }
      }
    }).start();
  }

  /**
   * Sets a radial gradient for the vehicle, its current and next position.
   *
   * @param g2d
   */
  private void highlightVehicle(Graphics2D g2d) {
    if (fFocusVehicle == null) {
      return;
    }

    final Figure currentVehicleFigure = fFocusVehicle.getFigure();
    if (currentVehicleFigure == null) {
      return;
    }

    Rectangle2D.Double bounds = currentVehicleFigure.getBounds();
    double xCenter = bounds.getCenterX();
    double yCenter = bounds.getCenterY();
    Point2D.Double pCenterView = new Point2D.Double(xCenter, yCenter);
    Point pCenterDrawing = drawingToView(pCenterView);

    // radial gradient for the vehicle
    Point2D center
        = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);
    float radius = 30;
    float[] dist = {0.0f, 0.7f, 0.8f, 1.0f};
    Color[] colors = {
      new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
      new Color(1.0f, 1.0f, 1.0f, 0.0f),
      new Color(1.0f, 0.0f, 0.0f, 0.7f), // Circle: red
      new Color(0f, 0f, 0f, 0f) // Background
    };
    RadialGradientPaint paint
        = new RadialGradientPaint(center, radius, dist, colors,
                                  MultipleGradientPaint.CycleMethod.NO_CYCLE);

    Graphics2D gVehicle = (Graphics2D) g2d.create();
    gVehicle.setPaint(paint);
    gVehicle.fillRect(0, 0, getWidth(), getHeight());
    gVehicle.dispose();

    // radial gradient for the next position
    PointModel pointModel = fFocusVehicle.getNextPoint();

    if (pointModel != null) {
      Figure nextPoint = pointModel.getFigure();
      bounds = nextPoint.getBounds();
      xCenter = bounds.getCenterX();
      yCenter = bounds.getCenterY();
      pCenterView = new Point2D.Double(xCenter, yCenter);
      pCenterDrawing = drawingToView(pCenterView);
      center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);

      radius = 20;
      Color[] colorsGreen = {
        new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
        new Color(1.0f, 1.0f, 1.0f, 0.0f),
        new Color(0.0f, 1.0f, 0.0f, 0.7f), // Circle: green
        new Color(0f, 0f, 0f, 0f) // Background
      };
      paint = new RadialGradientPaint(center, radius, dist, colorsGreen,
                                      MultipleGradientPaint.CycleMethod.NO_CYCLE);

      Graphics2D gNextPosition = (Graphics2D) g2d.create();
      gNextPosition.setPaint(paint);
      gNextPosition.fillRect(0, 0, getWidth(), getHeight());
      gNextPosition.dispose();
    }

    // radial gradient for last position
    pointModel = fFocusVehicle.getPoint();

    if (pointModel != null && fFocusVehicle.getPrecisePosition() != null) {
      Figure lastPoint = pointModel.getFigure();
      bounds = lastPoint.getBounds();
      xCenter = bounds.getCenterX();
      yCenter = bounds.getCenterY();
      pCenterView = new Point2D.Double(xCenter, yCenter);
      pCenterDrawing = drawingToView(pCenterView);
      center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);

      radius = 20;
      Color[] colorsBlue = {
        new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
        new Color(1.0f, 1.0f, 1.0f, 0.0f),
        new Color(0.0f, 0.0f, 1.0f, 0.7f), // Circle: blue
        new Color(0f, 0f, 0f, 0f) // Background
      };
      paint = new RadialGradientPaint(center, radius, dist, colorsBlue,
                                      MultipleGradientPaint.CycleMethod.NO_CYCLE);

      Graphics2D gCurrentPosition = (Graphics2D) g2d.create();
      gCurrentPosition.setPaint(paint);
      gCurrentPosition.fillRect(0, 0, getWidth(), getHeight());
      gCurrentPosition.dispose();
    }

    Rectangle visibleRect = getVisibleRect();
    dirtyArea.add(visibleRect);

    // After drawing the RadialGradientPaint the drawing area needs to
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    if (doRepaint) {
      repaint();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }
  }

  /**
   * Sets a radial gradient for the start and end Points of a Static Route.
   *
   * @param g2d
   */
  private void highlightStaticRoute(Graphics2D g2d) {
    if (fFocusStaticRoute == null) {
      return;
    }

    PointModel startPoint = fFocusStaticRoute.getStartPoint();
    PointModel endPoint = fFocusStaticRoute.getEndPoint();

    if (startPoint == null) {
      return;
    }

    final Figure startFigure = startPoint.getFigure();
    Rectangle2D.Double bounds = startFigure.getBounds();
    double xCenter = bounds.getCenterX();
    double yCenter = bounds.getCenterY();
    Point2D.Double pCenterView = new Point2D.Double(xCenter, yCenter);
    Point pCenterDrawing = drawingToView(pCenterView);

    // Create a radial gradient, transparent in the middle.
    Point2D center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);
    float radius = 15f;
    float[] dist = {0.0f, 0.7f, 0.8f, 1.0f};
    Color[] colorsStart = {
      new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
      new Color(1.0f, 1.0f, 1.0f, 0.0f),
      new Color(0.0f, 0.0f, 1.0f, 0.7f), // Circle: blue
      new Color(0.9f, 0.9f, 0.9f, 0.5f) // Background
    };
    RadialGradientPaint paint
        = new RadialGradientPaint(center, radius, dist, colorsStart,
                                  MultipleGradientPaint.CycleMethod.NO_CYCLE);

    Graphics2D gFocusStart = (Graphics2D) g2d.create();
    gFocusStart.setPaint(paint);
    gFocusStart.fillRect(0, 0, getWidth(), getHeight());
    gFocusStart.dispose();

    if (endPoint != null && endPoint != startPoint) {
      Figure endFigure = endPoint.getFigure();
      bounds = endFigure.getBounds();
      xCenter = bounds.getCenterX();
      yCenter = bounds.getCenterY();
      pCenterView = new Point2D.Double(xCenter, yCenter);
      pCenterDrawing = drawingToView(pCenterView);
      // Create a radial gradient, transparent in the middle.
      center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);
      radius = 20f;
      Color[] colorsEnd = {
        new Color(1.0f, 0.0f, 0.0f, 1.0f), // Focus: 100% transparent
        new Color(1.0f, 1.0f, 1.0f, 0.0f),
        new Color(0.0f, 1.0f, 0.5f, 0.7f), // Circle: green
        new Color(0.9f, 0.9f, 0.9f, 0.5f) // Background
      };
      paint = new RadialGradientPaint(center, radius, dist, colorsEnd,
                                      MultipleGradientPaint.CycleMethod.NO_CYCLE);

      Graphics2D gFocusEnd = (Graphics2D) g2d.create();
      gFocusEnd.setPaint(paint);
      gFocusEnd.fillRect(0, 0, getWidth(), getHeight());
      gFocusEnd.dispose();
    }

    Rectangle visibleRect = getVisibleRect();
    dirtyArea.add(visibleRect);

    // After drawing the RadialGradientPaint the drawing area needs to be
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    if (doRepaint) {
      repaint();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }

    // after 3 seconds the RadialGradientPaint is removed
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          synchronized (OpenTCSDrawingView.this) {
            OpenTCSDrawingView.this.wait(3000);
          }
        }
        catch (InterruptedException ex) {
          LOG.warn("Unexpected exception", ex);
        }

        fFocusStaticRoute = null;
        repaint();
      }
    }).start();
  }

  /**
   * Marks the elements of all blocks.
   *
   * @param g2d
   */
  private void drawBlocks(Graphics2D g2d) {
    if (fBlocks == null) {
      return;
    }

    List<ModelComponent> blockComps = fBlocks.getChildComponents();
    synchronized (blockComps) {
      for (ModelComponent blockComp : blockComps) {
        BlockModel block = (BlockModel) blockComp;
        drawPathDecoration(g2d, block.figures(), block.getColor(), BLOCK_STROKE);
      }
    }
  }

  /**
   * Marks the elements of all static routes.
   *
   * @param g2d
   */
  private void drawStaticRoutes(Graphics2D g2d) {
    if (fStaticRoutes == null) {
      return;
    }

    if (fFocusStaticRoute != null) {
      // Only draw selected Static Route
      drawPathDecoration(g2d,
                         fFocusStaticRoute.figures(),
                         fFocusStaticRoute.getColor(),
                         STATIC_ROUTE_STROKE);
    }
    else if (staticRoutesVisible) {
      // Draw all Static Routes
      for (ModelComponent routeComp : fStaticRoutes.getChildComponents()) {
        StaticRouteModel route = (StaticRouteModel) routeComp;
        drawPathDecoration(g2d,
                           route.figures(),
                           route.getColor(),
                           STATIC_ROUTE_STROKE);
      }
    }
  }

  /**
   * Marks the elements of the transport orders of all vehicles.
   *
   * @param g2d
   */
  private void drawDriveOrderElements(Graphics2D g2d) {
    for (VehicleModel vehicle : fVehicles) {
      List<FigureComponent> driveOrderComponents = vehicle.getDriveOrderComponents();

      if (driveOrderComponents != null) {
        List<Figure> figures = getFigures(driveOrderComponents);
        Color driveOrderColor = vehicle.getDriveOrderColor();
        TransportOrder.State driveOrderState = vehicle.getDriveOrderState();

        if (!appState.hasOperationMode(OperationMode.MODELLING)) {
          if (driveOrderState == TransportOrder.State.WITHDRAWN) {
            drawPathDecoration(g2d,
                               (new LinkedList<>(figures)).iterator(),
                               Color.GRAY,
                               WITHDRAWN_PATH_STROKE);
          }
          else {
            drawPathDecoration(g2d,
                               (new LinkedList<>(figures)).iterator(),
                               driveOrderColor,
                               PATH_STROKE);
          }
        }
      }
    }
  }

  /**
   * Draws a decoration around the elements of a block or transport order.
   *
   * @param g2d The graphics object to draw.
   * @param figures The figures that belong to the block or transport order.
   * @param color The color to draw.
   */
  private void drawPathDecoration(Graphics2D g2d,
                                  Iterator<Figure> figures,
                                  Color color,
                                  Stroke stroke) {
    // Draw the basecolor of the block or transport order transparent with alpha = 192/256
    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 192));

    Path2D.Float path = new Path2D.Float();

    while (figures.hasNext()) {
      Figure figure = figures.next();

      if (figure instanceof LabeledFigure) {
        Shape shape = ((LabeledFigure) figure).getShape();
        path.append(shape, false);
      }
      else if (figure instanceof SimpleLineConnection) {
        Shape shape = ((SimpleLineConnection) figure).getShape();
        path.append(shape, false);
      }
    }

    Stroke currentStroke = g2d.getStroke();
    g2d.setStroke(stroke);
    g2d.draw(path);
    g2d.setStroke(currentStroke);
  }

  /**
   *
   * @param r
   */
  private void repaintDrawingArea(Rectangle2D.Double r) {
    Rectangle vr = drawingToView(r);
    vr.grow(1, 1);
    dirtyArea.add(vr);
    repaint(vr);
  }

  /**
   * Gets the currently active selection handles.
   */
  private List<Handle> getSelectionHandles() {
    validateHandles();

    return Collections.unmodifiableList(selectionHandles);
  }

  /**
   * Gets the currently active secondary handles.
   */
  private List<Handle> getSecondaryHandles() {
    validateHandles();
    List<Handle> result = Collections.unmodifiableList(secondaryHandles);

    if (!result.isEmpty()) {
      LOG.info("Secondary handles: {}", result.size());
    }

    return result;
  }

  /**
   * Invalidates the handles.
   */
  private void invalidateHandles() {
    if (!handlesAreValid) {
      return;
    }

    handlesAreValid = false;
    Rectangle invalidatedArea = null;

    for (Handle handle : selectionHandles) {
      handle.removeHandleListener(handleEventHandler);

      if (invalidatedArea == null) {
        invalidatedArea = handle.getDrawingArea();
      }
      else {
        invalidatedArea.add(handle.getDrawingArea());
      }

      handle.dispose();
    }

    for (Handle handle : secondaryHandles) {
      handle.removeHandleListener(handleEventHandler);

      if (invalidatedArea == null) {
        invalidatedArea = handle.getDrawingArea();
      }
      else {
        invalidatedArea.add(handle.getDrawingArea());
      }

      handle.dispose();
    }

    selectionHandles.clear();
    secondaryHandles.clear();
    setActiveHandle(null);

    if (invalidatedArea != null) {
      repaint(invalidatedArea);
    }
  }

  /**
   * Validates the handles.
   */
  private void validateHandles() {
    // Validate handles only if they are invalid, and if
    // the DrawingView has a OpenTCSDrawingEditor.
    if (handlesAreValid || editor == null) {
      return;
    }

    refreshDetailLevel();
    handlesAreValid = true;
    selectionHandles.clear();
    Rectangle invalidatedArea = null;

    for (Figure figure : getSelectedFigures()) {
      for (Handle handle : figure.createHandles(detailLevel)) {
        handle.setView(this);
        selectionHandles.add(handle);
        handle.addHandleListener(handleEventHandler);

        if (invalidatedArea == null) {
          invalidatedArea = handle.getDrawingArea();
        }
        else {
          invalidatedArea.add(handle.getDrawingArea());
        }
      }
    }
    if (invalidatedArea != null) {
      repaint(invalidatedArea);
    }
  }

  /**
   * Notify all listenerList that have registered interest for notification on
   * this event type. Also notify listeners who listen for
   * {@link EditableComponent#SELECTION_EMPTY_PROPERTY}.
   */
  private void fireSelectionChanged(Set<Figure> oldValue, Set<Figure> newValue) {
    if (listenerList.getListenerCount() > 0) {
      FigureSelectionEvent event = null;
      // Notify all listeners that have registered interest for
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == FigureSelectionListener.class) {
          // Lazily create the event:
          if (event == null) {
            event = new FigureSelectionEvent(this, oldValue, newValue);
          }

          ((FigureSelectionListener) listeners[i + 1]).selectionChanged(event);
        }
      }
    }

    firePropertyChange(EditableComponent.SELECTION_EMPTY_PROPERTY,
                       oldValue.isEmpty(),
                       newValue.isEmpty());
  }

  private void invalidateCachedDimensions() {
    cachedPreferredSize = null;
    cachedDrawingArea = null;
  }

  private Rectangle2D.Double getDrawingArea() {
    if (cachedDrawingArea == null) {
      if (drawing != null) {
        try {
          cachedDrawingArea = drawing.getDrawingArea();
        }
        catch (NullPointerException ex) {
          LOG.warn("NullPointerException in OpenTCSDrawingView.getDrawingArea()", ex);
          cachedDrawingArea = new Rectangle2D.Double();
        }
      }
      else {
        LOG.warn("No Drawing in OpenTCSDrawingView.getDrawingArea()");
        cachedDrawingArea = new Rectangle2D.Double();
      }
    }

    return (Rectangle2D.Double) cachedDrawingArea.clone();
  }

  /**
   * Updates the view translation taking into account the current dimension of
   * the view JComponent, the size of the drawing, and the scale factor.
   */
  public void validateViewTranslation() {
    if (getDrawing() == null) {
      translation.x = 0;
      translation.y = 0;
      return;
    }

    Point oldTranslation = (Point) translation.clone();
    int width = getWidth();
    int height = getHeight();
    Insets insets = getInsets();
    // The rectangle that contains all figures
    Rectangle2D.Double da = getDrawingArea();
    Rectangle r = new Rectangle((int) (da.x * zoomX),
                                (int) (da.y * zoomY),
                                (int) (da.width * zoomX),
                                (int) (da.height * zoomY));
    Double cwd = getDrawing().get(CANVAS_WIDTH);
    Double chd = getDrawing().get(CANVAS_HEIGHT);

    if (cwd == null || chd == null) {
      // The canvas size is not explicitly specified.
      // Place the canvas at the top left
      translation.x = insets.top;
      translation.y = insets.left;
    }
    else {
      // The canvas size is explicitly specified.
      int cw = (int) (cwd * zoomX);
      int ch = (int) (chd * zoomY);

      //Place the canvas at the center
      if (cw < width) {
        translation.x
            = insets.left + (width - insets.left - insets.right - cw) / -2;
      }

      if (ch < height) {
        translation.y
            = insets.top + (height - insets.top - insets.bottom - ch) / -2;
      }
    }

    if (r.y + r.height - translation.y > (height - insets.bottom)) {
      // We cut off the lower part of the drawing -> shift the canvas up
      translation.y = r.y + r.height - (height - insets.bottom);
    }

    if (Math.min(0, r.y) - translation.y < insets.top) {
      // We cut off the upper part of the drawing -> shift the canvas down
      translation.y = Math.min(0, r.y) - insets.top;
    }

    if (r.x + r.width - translation.x > (width - insets.right)) {
      // We cut off the right part of the drawing -> shift the canvas left
      translation.x = r.x + r.width - (width - insets.right);
    }

    if (Math.min(0, r.x) - translation.x < insets.left) {
      // We cut off the left part of the drawing -> shift the canvas right
      translation.x = Math.min(0, r.x) - insets.left;
    }

    if (!oldTranslation.equals(translation)) {
      bufferedArea.translate(
          oldTranslation.x - translation.x,
          oldTranslation.y - translation.y);
      fireViewTransformChanged();
    }
  }

  private void fireViewTransformChanged() {
    for (Handle handle : selectionHandles) {
      handle.viewTransformChanged();
    }

    for (Handle handle : secondaryHandles) {
      handle.viewTransformChanged();
    }
  }

  /**
   * Returns the current translation.
   *
   * @return A point representing the offset.
   */
  public Point getTranslation() {
    return translation;
  }

  /**
   * Returns a paint for drawing the background of the drawing area.
   *
   * @return Paint.
   */
  private Paint getBackgroundPaint(int x, int y) {
    if (backgroundTile == null) {
      backgroundTile = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = backgroundTile.createGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, 16, 16);
      g.setColor(new Color(0xdfdfdf));
      g.fillRect(0, 0, 8, 8);
      g.fillRect(8, 8, 8, 8);
      g.dispose();
    }

    return new TexturePaint(
        backgroundTile,
        new Rectangle(x, y, backgroundTile.getWidth(), backgroundTile.getHeight()));
  }

  /**
   * Sets the elements of the blocks.
   *
   * @param blocks A <code>ModelComponent</code> which childs must be
   * <code>BlockModels</code>.
   */
  public void setBlocks(ModelComponent blocks) {
    fBlocks = blocks;

    synchronized (fBlocks) {
      for (ModelComponent blockComp : fBlocks.getChildComponents()) {
        BlockModel block = (BlockModel) blockComp;
        block.addBlockChangeListener(blockChangeHandler);
      }
    }
  }

  /**
   * Sets the figures, that belong to the static route.
   *
   * @param staticRoutes A <code>ModelComponent</code> which childs must be
   * <code>StaticRouteModels</code>.
   */
  public void setStaticRoutes(ModelComponent staticRoutes) {
    fStaticRoutes = staticRoutes;
    for (ModelComponent routeComp : staticRoutes.getChildComponents()) {
      StaticRouteModel route = (StaticRouteModel) routeComp;
      route.addStaticRouteChangeListener(routeChangeHandler);
    }
  }

  /**
   * Looks for all figures that belong to the given drive route.
   *
   * @param driveOrder The list with elements of the drive route.
   * @return The figures.
   */
  private List<Figure> getFigures(List<FigureComponent> driveOrder) {
    if (driveOrder == null) {
      return null;
    }

    List<Figure> children = getDrawing().getChildren();
    List<Figure> figures = new ArrayList<>();

    for (Figure figure : children) {
      FigureComponent model = figure.get(FigureConstants.MODEL);

      if (model == null) {
        continue;
      }

      if (driveOrder.contains(model)) {
        figures.add(figure);
      }
    }

    return figures;
  }

  /**
   * Message of the application that a block area was created.
   *
   * @param block The newly created block.
   */
  public void blockAdded(BlockModel block) {
    block.addBlockChangeListener(blockChangeHandler);
  }

  /**
   * Shows or hides the current route of a vehicle
   *
   * @param vehicle The vehicle
   * @param visible <code>true</code> to set it to visible, <code>false</code> otherwise.
   */
  public void displayDriveOrders(VehicleModel vehicle, boolean visible) {
    requireNonNull(vehicle, "vehicle");

    if (visible) {
      if (!fVehicles.contains(vehicle)) {
        fVehicles.add(vehicle);
      }
    }
    else {
      fVehicles.remove(vehicle);
    }
  }

  /**
   * Updates the figures of a Block or a Static Route.
   *
   * @param block
   */
  public void updateBlock(FiguresFolder block) {
    Iterator<Figure> figureIter = block.figures();

    while (figureIter.hasNext()) {
      ((AbstractFigure) figureIter.next()).fireFigureChanged();
    }
  }

  @Override // Component
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    // Side effect: Changes view Translation.
    validateViewTranslation();
  }

  @Override // Container
  public void invalidate() {
    invalidateCachedDimensions();
    super.invalidate();
  }

  @Override // JComponent
  public String getToolTipText(MouseEvent evt) {
    if (editor != null && editor.getTool() != null) {
      return editor.getTool().getToolTipText(this, evt);
    }

    return null;
  }

  /**
   * Paints the drawing view. Uses rendering hints for fast painting. Paints the
   * canvasColor, the grid, the drawing, the handles and the current tool.
   */
  @Override // JComponent
  public void paintComponent(Graphics gr) {
    Graphics2D g2d = (Graphics2D) gr;
    setViewRenderingHints(g2d);
    drawBackground(g2d);
    drawCanvas(g2d);
    drawConstrainer(g2d);
//    drawOffsetRectangle(g2d);

    drawDrawingImpl(g2d);

    drawHandles(g2d);
    drawTool(g2d);
  }

  /**
   * Actually draws the drawing.
   */
  protected abstract void drawDrawingImpl(Graphics2D g2d);

  /**
   * Prints the drawing view. Uses high quality rendering hints for printing.
   * Only prints the drawing. Doesn't print the canvasColor, the grid, the
   * handles and the tool.
   *
   * @param gr
   */
  @Override // JComponent
  public void printComponent(Graphics gr) {
    Graphics2D g2d = (Graphics2D) gr;
    // Set rendering hints for quality
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                         RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                         RenderingHints.VALUE_STROKE_NORMALIZE);
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                         RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                         RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                         RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    drawDrawing(g2d);
  }

  @Override // JComponent
  public void paint(Graphics g) {
    if (paintEnabled) {
      super.paint(g);
    }
  }

  @Override // JComponent
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    setCursor(Cursor.getPredefinedCursor(b ? Cursor.DEFAULT_CURSOR : Cursor.WAIT_CURSOR));
  }

  @Override // JComponent
  public Dimension getPreferredSize() {
    if (cachedPreferredSize == null) {
      Rectangle2D.Double r = getDrawingArea();
      Double cw = getDrawing() == null ? null : getDrawing().get(CANVAS_WIDTH);
      Double ch = getDrawing() == null ? null : getDrawing().get(CANVAS_HEIGHT);
      Insets insets = getInsets();

      if (cw == null || ch == null) {
        cachedPreferredSize = new Dimension(
            (int) Math.ceil(
                (Math.max(0, r.x) + r.width) * zoomX) + insets.left + insets.right,
            (int) Math.ceil(
                (Math.max(0, r.y) + r.height) * zoomY) + insets.top + insets.bottom);
      }
      else {
        cachedPreferredSize = new Dimension(
            (int) Math.ceil(
                (-Math.min(0, r.x) + Math.max(
                 Math.max(0, r.x) + r.width + Math.min(0, r.x), cw)) * zoomX)
            + insets.left + insets.right,
            (int) Math.ceil(
                (-Math.min(0, r.y) + Math.max(
                 Math.max(0, r.y) + r.height + Math.min(0, r.y), ch)) * zoomY)
            + insets.top + insets.bottom);
      }
    }

    return (Dimension) cachedPreferredSize.clone();
  }

  @Override // DrawingView
  public void repaintHandles() {
    validateHandles();
    Rectangle r = null;

    for (Handle h : getSelectionHandles()) {
      if (r == null) {
        r = h.getDrawingArea();
      }
      else {
        r.add(h.getDrawingArea());
      }
    }

    for (Handle h : getSecondaryHandles()) {
      if (r == null) {
        r = h.getDrawingArea();
      }
      else {
        r.add(h.getDrawingArea());
      }
    }

    if (r != null) {
      repaint(r);
    }
  }

  @Override // DrawingView
  public Drawing getDrawing() {
    return drawing;
  }

  @Override // DrawingView
  public void setDrawing(Drawing newValue) {
    Drawing oldValue = this.drawing;

    if (this.drawing != null) {
      this.drawing.removeCompositeFigureListener(cmpFigureEvtHandler);
      this.drawing.removeFigureListener(figureEventHandler);
      this.drawing.removeUndoableEditListener(bezierLinerEditHandler);
      clearSelection();
    }

    this.drawing = newValue;
    SystemModel model = modelManager.getModel();

    if (this.drawing != null) {
      if (model != null) {
        Origin origin = model.getDrawingMethod().getOrigin();
        OriginFigure originFigure = origin.getFigure();
        // OriginFigure nur einmal zuf�gen!
        if (!this.drawing.contains(originFigure)) {
          this.drawing.add(originFigure);
        }
      }

      this.drawing.addCompositeFigureListener(cmpFigureEvtHandler);
      this.drawing.addFigureListener(figureEventHandler);
      this.drawing.addUndoableEditListener(bezierLinerEditHandler);
    }

    dirtyArea.add(bufferedArea);

    firePropertyChange(DRAWING_PROPERTY, oldValue, newValue);

    // Revalidate without flickering
    revalidate();
    validateViewTranslation();
    paintEnabled = false;
    Timer t = new Timer(10, new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        repaint();
                        paintEnabled = true;
                      }
                    });
    t.setRepeats(false);
    t.start();
  }

  /**
   * Creates a transport order, assumed a vehicle was selected before.
   *
   * @param figure A point figure.
   */
  public void createPossibleTransportOrder(Figure figure) {
    if (selectedFigures.size() == 1 && figure instanceof LabeledPointFigure) {
      Iterator<Figure> it = selectedFigures.iterator();
      Figure nextFigure = it.next();

      if (nextFigure instanceof VehicleFigure) {
        PointModel model = (PointModel) figure.get(FigureConstants.MODEL);
        VehicleModel vehicleModel = (VehicleModel) nextFigure.get(FigureConstants.MODEL);

        if (vehicleModel != null
            && vehicleModel.getDriveOrderComponents() == null) {
          orderUtil.createTransportOrder(model, vehicleModel);
        }
      }
    }
  }

  /**
   * Returns if a vehicle is currently being dragged.
   *
   * @return True if yes, false otherwise.
   */
  public boolean vehicleDragged() {
    if (selectedFigures.size() == 1) {
      Figure next = selectedFigures.iterator().next();

      if (next instanceof VehicleFigure) {
        return true;
      }
    }

    return false;
  }

  @Override // DrawingView
  public void addToSelection(Figure figure) {

    Set<Figure> oldSelection = new HashSet<>(selectedFigures);
    refreshDetailLevel();

    if (selectedFigures.add(figure)) {
      figure.addFigureListener(handleInvalidator);
      Set<Figure> newSelection = new HashSet<>(selectedFigures);
      Rectangle invalidatedArea = null;

      if (handlesAreValid && editor != null) {
        for (Handle h : figure.createHandles(detailLevel)) {
          h.setView(this);
          selectionHandles.add(h);
          h.addHandleListener(handleEventHandler);

          if (invalidatedArea == null) {
            invalidatedArea = h.getDrawingArea();
          }
          else {
            invalidatedArea.add(h.getDrawingArea());
          }
        }
      }

      fireSelectionChanged(oldSelection, newSelection);
      editor.figuresSelected(new LinkedList<>(newSelection));

      if (invalidatedArea != null) {
        repaint(invalidatedArea);
      }
    }
  }

  /**
   * The detailLevel indicates what type of Handles are supposed to be shouwn. In Operating mode,
   * we require the type of handles for our figures that do not allow moving, that is
   * indicated by -1.
   * In modelling mode we require the regular Handles, indicated by 0.
   */
  private void refreshDetailLevel() {
    if (OperationMode.OPERATING.equals(appState.getOperationMode())) {
      detailLevel = -1;
    }
    else {
      detailLevel = 0;
    }
  }

  @Override // DrawingView
  public void addToSelection(Collection<Figure> figures) {

    Set<Figure> oldSelection = new HashSet<>(selectedFigures);
    Set<Figure> newSelection = new HashSet<>(selectedFigures);
    boolean selectionChanged = false;
    Rectangle invalidatedArea = null;
    refreshDetailLevel();

    for (Figure figure : figures) {
      if (selectedFigures.add(figure)) {
        selectionChanged = true;
        newSelection.add(figure);
        figure.addFigureListener(handleInvalidator);

        if (handlesAreValid && editor != null) {
          for (Handle h : figure.createHandles(detailLevel)) {
            h.setView(this);
            selectionHandles.add(h);
            h.addHandleListener(handleEventHandler);

            if (invalidatedArea == null) {
              invalidatedArea = h.getDrawingArea();
            }
            else {
              invalidatedArea.add(h.getDrawingArea());
            }
          }
        }
      }
    }

    if (selectionChanged) {
      fireSelectionChanged(oldSelection, newSelection);

      if (invalidatedArea != null) {
        repaint(invalidatedArea);
      }
    }
  }

  @Override // DrawingView
  public void removeFromSelection(Figure figure) {
    Set<Figure> oldSelection = new HashSet<>(selectedFigures);

    if (selectedFigures.remove(figure)) {
      Set<Figure> newSelection = new HashSet<>(selectedFigures);
      invalidateHandles();

      figure.removeFigureListener(handleInvalidator);
      fireSelectionChanged(oldSelection, newSelection);
      // The OpenTCSDrawingEditor may be null here, although it shouldn't.
      // A possible explanation at the moment is:  When restoring the default 
      // layout all drawing views are removed and recreated. Currently this also 
      // means that all bitmap figures are removed, which fire an event to all 
      // listeners, that they were removed. It seems that one of the 
      // removed drawing views receives this event. It may be possible 
      // JHotDraw just didn't remove the view from the listener list.
      if (editor != null) {
        editor.figuresSelected(new LinkedList<>(newSelection));
      }
      repaint();
    }
  }

  @Override // DrawingView
  public void toggleSelection(Figure figure) {
    if (selectedFigures.contains(figure)) {
      removeFromSelection(figure);
    }
    else {
      addToSelection(figure);
    }
  }

  @Override // DrawingView
  public void selectAll() {

    Set<Figure> oldSelection = new HashSet<>(selectedFigures);
    selectedFigures.clear();

    for (Figure figure : drawing.getChildren()) {
      if (figure.isSelectable()) {
        if (!(appState.hasOperationMode(OperationMode.MODELLING)
              && figure instanceof VehicleFigure)) {
          selectedFigures.add(figure);
        }
      }
    }

    Set<Figure> newSelection = new HashSet<>(selectedFigures);
    invalidateHandles();

    fireSelectionChanged(oldSelection, newSelection);
    editor.figuresSelected(new ArrayList<>(selectedFigures));
    repaint();
  }

  @Override // DrawingView
  public void clearSelection() {
    if (getSelectionCount() > 0) {
      Set<Figure> oldSelection = new HashSet<>(selectedFigures);
      selectedFigures.clear();
      Set<Figure> newSelection = new HashSet<>(selectedFigures);
      invalidateHandles();

      fireSelectionChanged(oldSelection, newSelection);
      editor.figuresSelected(new ArrayList<>(selectedFigures));
    }
  }

  @Override // DrawingView
  public boolean isFigureSelected(Figure checkFigure) {
    return selectedFigures.contains(checkFigure);
  }

  @Override // DrawingView
  public Set<Figure> getSelectedFigures() {
    return Collections.unmodifiableSet(selectedFigures);
  }

  @Override // DrawingView
  public int getSelectionCount() {
    return selectedFigures.size();
  }

  @Override // DrawingView
  public Handle findHandle(Point p) {
    validateHandles();

    for (Handle handle : new ReversedList<>(getSecondaryHandles())) {
      if (handle.contains(p)) {
        return handle;
      }
    }

    for (Handle handle : new ReversedList<>(getSelectionHandles())) {
      if (handle.contains(p)) {
        return handle;
      }
    }

    return null;
  }

  @Override // DrawingView
  public Collection<Handle> getCompatibleHandles(Handle master) {
    validateHandles();

    Set<Figure> owners = new HashSet<>();
    List<Handle> compatibleHandles = new LinkedList<>();
    owners.add(master.getOwner());
    compatibleHandles.add(master);

    for (Handle handle : getSelectionHandles()) {
      if (!owners.contains(handle.getOwner()) && handle.isCombinableWith(master)) {
        owners.add(handle.getOwner());
        compatibleHandles.add(handle);
      }
    }

    return compatibleHandles;
  }

  @Override // DrawingView
  public Figure findFigure(Point p) {
    return getDrawing().findFigure(viewToDrawing(p));
  }

  /**
   * Returns if a given point on the screen is contained in this drawing view.
   *
   * @param p The reference point on the screen.
   * @return Boolean if this point is contained.
   */
  public boolean containsPointOnScreen(Point p) {
    return (p.x >= getLocationOnScreen().x && p.x < (getLocationOnScreen().x + getWidth())
            && p.y >= getLocationOnScreen().y && p.y < (getLocationOnScreen().y + getHeight()));
  }

  @Override // DrawingView
  public Collection<Figure> findFigures(Rectangle r) {
    return getDrawing().findFigures(viewToDrawing(r));
  }

  @Override // DrawingView
  public Collection<Figure> findFiguresWithin(Rectangle r) {
    return getDrawing().findFiguresWithin(viewToDrawing(r));
  }

  @Override // DrawingView
  public void addFigureSelectionListener(FigureSelectionListener fsl) {
    listenerList.add(FigureSelectionListener.class, fsl);
  }

  @Override // DrawingView
  public void removeFigureSelectionListener(FigureSelectionListener fsl) {
    listenerList.remove(FigureSelectionListener.class, fsl);
  }

  @Override // DrawingView
  public Constrainer getConstrainer() {
    return isConstrainerVisible() ? visibleConstrainer : invisibleConstrainer;
  }

  @Override // DrawingView
  public Point drawingToView(Point2D.Double pDrawing) {
    Point pView = new Point(
        (int) (pDrawing.x * zoomX) - translation.x,
        (int) (pDrawing.y * zoomY) - translation.y);

    return pView;
  }

  @Override // DrawingView
  public Rectangle drawingToView(Rectangle2D.Double rDrawing) {
    Rectangle rView = new Rectangle(
        (int) (rDrawing.x * zoomX) - translation.x,
        (int) (rDrawing.y * zoomY) - translation.y,
        (int) (rDrawing.width * zoomX),
        (int) (rDrawing.height * zoomY));

    return rView;
  }

  @Override // DrawingView
  public Point2D.Double viewToDrawing(Point pView) {
    Point2D.Double pDrawing = new Point2D.Double(
        (pView.x + translation.x) / zoomX,
        (pView.y + translation.y) / zoomY);

    return pDrawing;
  }

  @Override // DrawingView
  public Rectangle2D.Double viewToDrawing(Rectangle rView) {
    Rectangle2D.Double rDrawing = new Rectangle2D.Double(
        (rView.x + translation.x) / zoomX,
        (rView.y + translation.y) / zoomY,
        rView.width / zoomX,
        rView.height / zoomY);

    return rDrawing;
  }

  @Override // DrawingView
  public JComponent getComponent() {
    return this;
  }

  @Override // DrawingView
  public double getScaleFactor() {
    // TODO: Was ist, wenn zoomX != zoomY ?
    return zoomX;
  }

  /**
   * Returns a newly created bookmark for this view's current position and zoom
   * factor.
   *
   * @return A newly created bookmark for this view's current position and zoom
   * factor.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public org.opentcs.data.model.visualization.ViewBookmark bookmark() {
    org.opentcs.data.model.visualization.ViewBookmark bookmark
        = new org.opentcs.data.model.visualization.ViewBookmark();

    // Currently visible part of the drawing in drawing coordinates.
    Rectangle2D.Double visibleViewRect = viewToDrawing(getVisibleRect());

    int centerX = (int) visibleViewRect.getCenterX();
    int centerY = (int) -visibleViewRect.getCenterY();  // signum!
    bookmark.setCenterX(centerX);
    bookmark.setCenterY(centerY);
    double zoomFactor = getScaleFactor();
    bookmark.setViewScaleX(zoomFactor);
    bookmark.setViewScaleY(zoomFactor);  // TODO: discriminate x/y
    bookmark.setViewRotation(0);  // TODO    
    bookmark.setLabel("");

    return bookmark;
  }

  /**
   * Scales the view to a value so the whole model fits.
   */
  public void zoomViewToWindow() {
    // 1. Zoom to 100%
    setScaleFactor(1.0);
    // 2. Zoom delayed
    Runnable doScaling = new Runnable() {
      @Override
      public void run() {
        // Rectangle that contains all figures
        Rectangle2D.Double drawingArea = getDrawing().getDrawingArea();
        double wDrawing = drawingArea.width + 2 * 20;
        double hDrawing = drawingArea.height + 2 * 20;
        // The currently visible rectangle
        Rectangle visibleRect = getComponent().getVisibleRect();

        double xFactor = visibleRect.width / wDrawing;
        double yFactor = visibleRect.height / hDrawing;

        double newZoom = Math.min(xFactor, yFactor);
        newZoom = Math.min(newZoom, 4.0);  // Limit to 400%

        // round it to two decimal places
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat twoDForm = new DecimalFormat("#.##", symbols);
        String sZoomFactor = twoDForm.format(newZoom);
        newZoom = Double.valueOf(sZoomFactor);

        setScaleFactor(newZoom);
        // TODO: Center drawing with MARGIN at all sides
        // Add the offset figures
      }
    };

    SwingUtilities.invokeLater(doScaling);
  }

  @Override // DrawingView
  public void setScaleFactor(final double newValue) {
    if (newValue == getScaleFactor()) {
      return;
    }

    // Save the coordinates of the current center point to jump back to there
    // after the zoom
    Rectangle visibleRect = getVisibleRect();
    // Convert to drawing coordinates
    Rectangle2D.Double visibleViewRect = viewToDrawing(visibleRect);
    final int centerX = (int) ((visibleViewRect.getCenterX() + 0.5));
    final int centerY = (int) ((-(visibleViewRect.getCenterY() + 0.5)));  // Vorzeichen!
    for (BitmapFigure bmFigure : bitmapFigures) {
      bmFigure.setScaleFactor(zoomX, newValue);
    }

    Runnable doScaling = new Runnable() {
      @Override
      public void run() {
        // TODO: Expand for zoomX != zoomY
        double oldValue = zoomX;
        zoomX = newValue;
        zoomY = newValue;
        validateViewTranslation();
        dirtyArea.setBounds(bufferedArea);
        revalidate();
        repaint();
        firePropertyChange("scaleFactor", oldValue, newValue);

        // Scroll to the old center point
        scrollTo(centerX, centerY);
      }
    };

    SwingUtilities.invokeLater(doScaling);
  }

  @Override // DrawingView
  public void setHandleDetailLevel(int newValue) {
  }

  @Override // DrawingView
  public int getHandleDetailLevel() {
    return detailLevel;
  }

  @Override // DrawingView
  public AffineTransform getDrawingToViewTransform() {
    AffineTransform t = new AffineTransform();
    t.translate(-translation.x, -translation.y);
    t.scale(zoomX, zoomY);  // ???

    return t;
  }

  @Override // DrawingView
  public DrawingEditor getEditor() {
    return editor;
  }

  @Override // DrawingView
  public void addNotify(DrawingEditor editor) {
    DrawingEditor oldValue = this.editor;
    this.editor = (OpenTCSDrawingEditor) editor;
    addOffsetListener();
    firePropertyChange("editor", oldValue, editor);
    invalidateHandles();
    repaint();
  }

  @Override // DrawingView
  public void removeNotify(DrawingEditor editor) {
    this.editor = null;
    invalidateHandles();
    repaint();
  }

  @Override // DrawingView
  public Handle getActiveHandle() {
    return activeHandle;
  }

  @Override // DrawingView
  public void setActiveHandle(Handle newValue) {
    Handle oldValue = activeHandle;

    if (oldValue != null) {
      repaint(oldValue.getDrawingArea());
    }

    activeHandle = newValue;

    if (newValue != null) {
      repaint(newValue.getDrawingArea());
    }

    firePropertyChange(ACTIVE_HANDLE_PROPERTY, oldValue, newValue);
  }

  @Override // DrawingView
  public boolean isConstrainerVisible() {
    return isConstrainerVisible;
  }

  @Override // DrawingView
  public void setConstrainerVisible(boolean newValue) {
    boolean oldValue = isConstrainerVisible;
    isConstrainerVisible = newValue;
    firePropertyChange(CONSTRAINER_VISIBLE_PROPERTY, oldValue, newValue);
    repaint();
  }

  @Override // DrawingView
  public Constrainer getVisibleConstrainer() {
    return visibleConstrainer;
  }

  @Override // DrawingView
  public void setVisibleConstrainer(Constrainer newValue) {
    Constrainer oldValue = visibleConstrainer;
    visibleConstrainer = newValue;
    firePropertyChange(VISIBLE_CONSTRAINER_PROPERTY, oldValue, newValue);
  }

  @Override // DrawingView
  public Constrainer getInvisibleConstrainer() {
    return invisibleConstrainer;
  }

  @Override // DrawingView
  public void setInvisibleConstrainer(Constrainer newValue) {
    Constrainer oldValue = invisibleConstrainer;
    invisibleConstrainer = newValue;
    firePropertyChange(INVISIBLE_CONSTRAINER_PROPERTY, oldValue, newValue);
  }

  @Override // EditableComponent
  public boolean isSelectionEmpty() {
    return selectedFigures.isEmpty();
  }

  @Override // EditableComponent
  public void cutSelectedItems() {
    deleteSelectedFigures();
  }

  @Override // EditableComponent
  public void copySelectedItems() {
    bufferedFigures = drawing.sort(getSelectedFigures());
  }

  @Override // EditableComponent
  public void pasteBufferedItems() {
    clearSelection();
    List<Figure> figuresToClone = new ArrayList<>();

    for (Figure deletedFigure : bufferedFigures) {
      if (drawing.contains(deletedFigure)) {
        figuresToClone.add(deletedFigure);
      }
      else {
        drawing.add(deletedFigure);
      }
    }
    // Create clones of all buffered figures
    List<Figure> clonedFigures = fOpenTCSView.cloneFigures(figuresToClone);
    addToSelection(clonedFigures);
    if (!clonedFigures.isEmpty()) {
      // Undo for paste
      getDrawing().fireUndoableEditHappened(new PasteEdit(this, clonedFigures));
    }
    else {
      // Undo for cut
      getDrawing().fireUndoableEditHappened(new PasteEdit(this, bufferedFigures));
    }
  }

  @Override // EditableComponent
  public void delete() {
    // Delete only in modelling mode
    if (!appState.hasOperationMode(OperationMode.MODELLING)) {
      return;
    }

    deleteSelectedFigures();

    if (!bufferedFigures.isEmpty()) {
      getDrawing().fireUndoableEditHappened(new DeleteEdit(this, bufferedFigures));
    }
  }

  private void deleteSelectedFigures() {
    final List<Figure> deletedFigures = drawing.sort(getSelectedFigures());
    // Abort, if not all of the selected figures may be removed from the drawing
    for (Figure figure : deletedFigures) {
      if (!figure.isRemovable()) {
        LOG.info("Figure is not removable: {}", figure);
        getToolkit().beep();
        return;
      }
    }

    bufferedFigures = deletedFigures;
    clearSelection();

    for (Figure figure : deletedFigures) {
      if (figure instanceof OriginChangeListener) {
        Origin ref = figure.get(FigureConstants.ORIGIN);

        if (ref != null) {
          ref.removeListener((OriginChangeListener) figure);
          figure.set(FigureConstants.ORIGIN, null);
        }
      }

      drawing.remove(figure);
    }
  }

  @Override // EditableComponent
  public void duplicate() {
    copySelectedItems();
    pasteBufferedItems();
  }

  /**
   * Refreshes the display of a static route.
   *
   * @param route
   */
  private void updateStaticRoute(StaticRouteModel route) {
    Iterator<Figure> figureIter = route.figures();

    while (figureIter.hasNext()) {
      ((AbstractFigure) figureIter.next()).fireFigureChanged();
    }
  }

  /**
   * Enables the listener for updating the offset figures.
   */
  private void addOffsetListener() {
    offsetListener = new OffsetListener((OpenTCSDrawingEditor) getEditor());
    addComponentListener(offsetListener);
  }

  private class BlockChangeHandler
      implements BlockChangeListener {

    /**
     * Creates a new instance.
     */
    public BlockChangeHandler() {
    }

    @Override // BlockChangeListener
    public void courseElementsChanged(BlockChangeEvent e) {
      BlockModel block = (BlockModel) e.getSource();
      Iterator<Figure> figures = block.figures();

      while (figures.hasNext()) {
        Figure figure = figures.next();

        if (figure instanceof AbstractFigure) {
          ((AbstractFigure) figure).fireFigureChanged();
        }
      }
    }

    @Override // BlockChangeListener
    public void colorChanged(BlockChangeEvent e) {
      updateBlock((BlockModel) e.getSource());
    }

    @Override // BlockChangeListener
    public void blockRemoved(BlockChangeEvent e) {
      BlockModel block = (BlockModel) e.getSource();
      block.removeBlockChangeListener(this);
      updateBlock(block);
    }
  }

  private class StaticRouteChangeHandler
      implements StaticRouteChangeListener {

    /**
     * Creates a new instance.
     */
    public StaticRouteChangeHandler() {
    }

    @Override // StaticRouteChangeListener
    public void pointsChanged(StaticRouteChangeEvent e) {
      updateStaticRoute((StaticRouteModel) e.getSource());
    }

    @Override // StaticRouteChangeListener
    public void colorChanged(StaticRouteChangeEvent e) {
      updateStaticRoute((StaticRouteModel) e.getSource());
    }

    @Override // StaticRouteChangeListener
    public void staticRouteRemoved(StaticRouteChangeEvent e) {
      StaticRouteModel route = (StaticRouteModel) e.getSource();
      route.removeStaticRouteChangeListener(this);
      updateStaticRoute(route);
    }
  }

  private class FocusHandler
      implements FocusListener {

    /**
     * Creates a new instance.
     */
    public FocusHandler() {
    }

    @Override
    public void focusGained(FocusEvent e) {
      //   repaintHandles();
      if (editor != null) {
        editor.setActiveView(OpenTCSDrawingView.this);
        List<BitmapFigure> figuresToRemove = new ArrayList<>();

        for (Figure fig : drawing.getFiguresFrontToBack()) {
          if (fig instanceof BitmapFigure) {
            figuresToRemove.add((BitmapFigure) fig);
          }
          if (!(fig instanceof VehicleFigure) && !(fig instanceof OriginFigure)) {
            ((AbstractFigure) fig).setVisible(true);
          }
        }

        for (BitmapFigure figure : figuresToRemove) {
          figure.setTemporarilyRemoved(true);
          drawing.remove(figure);
        }

        for (BitmapFigure bmFigure : bitmapFigures) {
          bmFigure.setTemporarilyRemoved(false);
          drawing.add(bmFigure);
          drawing.sendToBack(bmFigure);
        }

        for (AbstractFigure figure : invisibleFigures) {
          figure.setVisible(false);
        }

        if (!invisibleFigures.isEmpty()) {
          repaint();
        }
      }
    }

    @Override
    public void focusLost(FocusEvent e) {
      //   repaintHandles();
    }
  }

  private class HandleInvalidator
      extends FigureAdapter {

    /**
     * Creates a new instance.
     */
    public HandleInvalidator() {
    }

    @Override
    public void figureHandlesChanged(FigureEvent e) {
      invalidateHandles();
    }
  }

  private class CompositeFigureEventHandler
      implements CompositeFigureListener {

    /**
     * Creates a new instance.
     */
    public CompositeFigureEventHandler() {
      // Do nada.
    }

    @Override // CompositeFigureListener
    public void figureAdded(CompositeFigureEvent evt) {
      repaintDrawingArea(evt.getInvalidatedArea());
      invalidateCachedDimensions();
    }

    @Override // CompositeFigureListener
    public void figureRemoved(CompositeFigureEvent evt) {
      repaintDrawingArea(evt.getInvalidatedArea());

      if (evt.getChildFigure() instanceof BitmapFigure) {
        BitmapFigure bmFigure = (BitmapFigure) evt.getChildFigure();

        if (!bmFigure.isTemporarilyRemoved()) {
          BitmapFigure childFigure = (BitmapFigure) evt.getChildFigure();
          bitmapFigures.remove(childFigure);
        }
      }

      removeFromSelection(evt.getChildFigure());

      invalidateCachedDimensions();
    }
  }

  private class FigureEventHandler
      extends FigureAdapter {

    /**
     * Creates a new instance.
     */
    FigureEventHandler() {
      // Do nada.
    }

    @Override // FigureListener
    public void areaInvalidated(FigureEvent evt) {
      repaintDrawingArea(evt.getInvalidatedArea());
      invalidateCachedDimensions();
    }

    @Override // FigureListener
    public void attributeChanged(FigureEvent e) {
      if (e.getSource() == drawing) {
        AttributeKey<?> a = e.getAttribute();

        if (a.equals(CANVAS_HEIGHT) || a.equals(CANVAS_WIDTH)) {
          validateViewTranslation();
          repaint(); // must repaint everything
        }

        if (e.getInvalidatedArea() != null) {
          repaintDrawingArea(e.getInvalidatedArea());
        }
        else {
          repaintDrawingArea(viewToDrawing(getCanvasViewBounds()));
        }
      }
      else if (e.getInvalidatedArea() != null) {
        repaintDrawingArea(e.getInvalidatedArea());
      }
    }

    @Override // FigureListener
    public void figureChanged(FigureEvent e) {
      repaintDrawingArea(e.getInvalidatedArea());
    }

    @Override // FigureListener
    public void figureRequestRemove(FigureEvent evt) {
      if (evt.getFigure() instanceof BitmapFigure) {
        BitmapFigure bmFigure = (BitmapFigure) evt.getFigure();

        if (!bmFigure.isTemporarilyRemoved()) {
          BitmapFigure figure = (BitmapFigure) evt.getFigure();
          bitmapFigures.remove(figure);
        }
      }
    }
  }

  private class HandleEventHandler
      implements HandleListener {

    private Handle secondaryHandleOwner;

    /**
     * Creates a new instance.
     */
    public HandleEventHandler() {
      // Do nada.
    }

    @Override // HandleListener
    public void areaInvalidated(HandleEvent evt) {
      repaint(evt.getInvalidatedArea());
      invalidateCachedDimensions();
    }

    @Override // HandleListener
    public void handleRequestSecondaryHandles(HandleEvent e) {
      secondaryHandleOwner = e.getHandle();
      secondaryHandles.clear();
      secondaryHandles.addAll(secondaryHandleOwner.createSecondaryHandles());

      for (Handle h : secondaryHandles) {
        h.setView(OpenTCSDrawingView.this);
        h.addHandleListener(this);
      }

      repaint();
    }

    @Override // HandleListener
    public void handleRequestRemove(HandleEvent e) {
      selectionHandles.remove(e.getHandle());
      e.getHandle().dispose();
      invalidateHandles();
      repaint(e.getInvalidatedArea());
    }
  }

}
