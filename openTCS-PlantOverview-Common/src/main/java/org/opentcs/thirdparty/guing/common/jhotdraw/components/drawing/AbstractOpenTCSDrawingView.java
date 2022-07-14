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
package org.opentcs.thirdparty.guing.common.jhotdraw.components.drawing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.AbstractFigure;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.GridConstrainer;
import org.jhotdraw.draw.event.CompositeFigureEvent;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.gui.datatransfer.ClipboardUtil;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.components.EditableComponent;
import org.opentcs.guing.common.components.drawing.BezierLinerEditHandler;
import org.opentcs.guing.common.components.drawing.OffsetListener;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.common.components.drawing.course.Origin;
import org.opentcs.guing.common.components.drawing.figures.BitmapFigure;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.OriginFigure;
import org.opentcs.guing.common.components.drawing.figures.PathConnection;
import org.opentcs.guing.common.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.common.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.ModelComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DrawingView implementation for the openTCS plant overview.
 *
 */
public abstract class AbstractOpenTCSDrawingView
    extends DefaultDrawingView
    implements OpenTCSDrawingView,
               EditableComponent,
               PropertyChangeListener {

  public static final String FOCUS_GAINED = "focusGained";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOpenTCSDrawingView.class);
  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;
  /**
   * This listener sets the position of the invisible offsetFigure after the
   * frame was resized.
   */
  private ComponentListener offsetListener;
  /**
   * Flag whether the labels shall be drawn.
   */
  private boolean labelsVisible = true;
  /**
   * A pointer to a figure that shall be highlighted by a red circle.
   */
  private Figure fFocusFigure;
  /**
   * Prohibits a loop in repaint().
   */
  private boolean doRepaint = true;
  /**
   * Background figures set to this DrawingView.
   */
  private final List<BitmapFigure> bitmapFigures = new ArrayList<>();
  /**
   * Handles edits of bezier liners.
   */
  private final BezierLinerEditHandler bezierLinerEditHandler = new BezierLinerEditHandler();

  /**
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param modelManager Provides the current system model.
   */
  public AbstractOpenTCSDrawingView(ApplicationState appState, ModelManager modelManager) {
    this.appState = requireNonNull(appState, "appState");
    this.modelManager = requireNonNull(modelManager, "modelManager");

    // Set a dummy tool tip text to turn tooltips on
    setToolTipText(" ");
    setBackground(Color.LIGHT_GRAY);
    setAutoscrolls(true);
    GridConstrainer gridConstrainer = new ExtendedGridConstrainer(10, 10);
    gridConstrainer.setMajorGridSpacing(10);
    setVisibleConstrainer(gridConstrainer);
    setConstrainerVisible(true);
  }

  // ###
  // Methods of EventHandler start here.
  // ###
  @Override
  public void onEvent(Object event) {
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
  }

  // ###
  // Methods of PropertyChangeListener start here.
  // ###
  @Override // PropertyChangeListener
  public void propertyChange(PropertyChangeEvent evt) {
  }

  // ###
  // Methods of DrawingView start here.
  // ###
  @Override // DrawingView
  public void setDrawing(Drawing newValue) {
    Drawing oldValue = this.getDrawing();

    if (oldValue != null) {
      oldValue.removeUndoableEditListener(bezierLinerEditHandler);
    }
    if (newValue != null) {
      SystemModel model = modelManager.getModel();
      if (model != null) {
        Origin origin = model.getDrawingMethod().getOrigin();
        OriginFigure originFigure = origin.getFigure();
        if (!newValue.contains(originFigure)) {
          newValue.add(originFigure);
        }
      }

      newValue.addUndoableEditListener(bezierLinerEditHandler);
    }

    super.setDrawing(newValue);
  }

  @Override
  public OpenTCSDrawingEditor getEditor() {
    return (OpenTCSDrawingEditor) super.getEditor();
  }

  @Override // DrawingView
  public void addToSelection(Figure figure) {
    refreshDetailLevel();

    super.addToSelection(figure);

    getEditor().figuresSelected(new ArrayList<>(getSelectedFigures()));
    repaint();
  }

  @Override // DrawingView
  public void addToSelection(Collection<Figure> figures) {
    refreshDetailLevel();

    super.addToSelection(figures);
  }

  @Override // DrawingView
  public void removeFromSelection(Figure figure) {
    super.removeFromSelection(figure);

    // The OpenTCSDrawingEditor may be null here, although it shouldn't.
    // A possible explanation at the moment is: When restoring the default
    // layout all drawing views are removed and recreated. Currently this also
    // means that all bitmap figures are removed, which fire an event to all
    // listeners, that they were removed. It seems that one of the
    // removed drawing views receives this event. It may be possible
    // JHotDraw just didn't remove the view from the listener list.
    if (getEditor() != null) {
      getEditor().figuresSelected(new ArrayList<>(getSelectedFigures()));
      repaint();
    }
  }

  @Override // DrawingView
  public void selectAll() {
    super.selectAll();
    getEditor().figuresSelected(new ArrayList<>(getSelectedFigures()));
  }

  @Override // DrawingView
  public void clearSelection() {
    super.clearSelection();

    if (getSelectionCount() > 0) {
      getEditor().figuresSelected(new ArrayList<>(getSelectedFigures()));
    }
  }

  @Override // DrawingView
  public void setScaleFactor(final double newValue) {
    if (newValue == getScaleFactor()) {
      return;
    }

    // Save the (drawing) coordinates of the current center point to jump back to there after the
    // zoom.
    Rectangle2D.Double visibleViewRect = viewToDrawing(getVisibleRect());
    final int centerX = (int) (visibleViewRect.getCenterX() + 0.5);
    final int centerY = (int) -(visibleViewRect.getCenterY() + 0.5);
    for (BitmapFigure bmFigure : bitmapFigures) {
      bmFigure.setScaleFactor(getScaleFactor(), newValue);
    }

    SwingUtilities.invokeLater(() -> {
      super.setScaleFactor(newValue);
      scrollTo(centerX, centerY);
    });
  }

  @Override // DrawingView
  public void addNotify(DrawingEditor editor) {
    addOffsetListener((OpenTCSDrawingEditor) editor);

    super.addNotify(editor);
  }

  @Override // DrawingView
  public void removeNotify(DrawingEditor editor) {
    // XXX Is setting the scale factor (to invalidate the handles) really necessary?
    setScaleFactor(getScaleFactor());

    super.removeNotify(editor);
  }

  // ###
  // Methods of JComponent start here.
  // ###
  @Override
  public void processKeyEvent(KeyEvent e) {
    if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
      // Cut, copy, paste and duplicate
      if (e.getKeyCode() == KeyEvent.VK_X
          || e.getKeyCode() == KeyEvent.VK_C
          || e.getKeyCode() == KeyEvent.VK_V
          || e.getKeyCode() == KeyEvent.VK_D) {
        if (!appState.hasOperationMode(OperationMode.MODELLING)) {
          return;
        }
        processCutPasteKeyEvent();
      }
    }

    super.processKeyEvent(e);
  }

  // ###
  // Methods not declared in interfaces or superclasses start here.
  // ###
  @Override
  public void drawingOptionsChanged() {
    repaintDrawingArea();
  }

  private void repaintDrawingArea() {
    // We need to add the visible rect to the dirty area before we repaint.
    repaintDrawingArea(viewToDrawing(getVisibleRect()));
  }

  @Override
  public boolean isLabelsVisible() {
    return labelsVisible;
  }

  @Override
  public void setLabelsVisible(boolean newValue) {
    labelsVisible = newValue;

    if (getDrawing() == null) {
      return;
    }

    for (Figure figure : getDrawing().getChildren()) {
      if (figure instanceof LabeledFigure) {
        LabeledFigure lf = (LabeledFigure) figure;
        lf.setLabelVisible(newValue);
      }
    }
    // Repaint the whole layout.
    repaintDrawingArea();
  }

  /**
   * Returns the background images for this drawing view.
   *
   * @return List containing the associated bitmap figures.
   */
  public List<BitmapFigure> getBackgroundBitmaps() {
    return bitmapFigures;
  }

  @Override
  public void addBackgroundBitmap(File file) {
    addBackgroundBitmap(new BitmapFigure(file));
  }

  @Override
  public void addBackgroundBitmap(BitmapFigure bitmapFigure) {
    bitmapFigures.add(bitmapFigure);
    getDrawing().add(bitmapFigure);
    getDrawing().sendToBack(bitmapFigure);
  }

  @Override
  public void scrollTo(Figure figure) {
    if (figure == null) {
      return;
    }

    fFocusFigure = figure;

    scrollRectToVisible(computeVisibleRectangleForFigure(figure));

    repaint();
  }

  @Override
  public void updateBlock(BlockModel block) {
    for (Figure figure : ModelComponentUtil.getChildFigures(block, modelManager.getModel())) {
      ((AbstractFigure) figure).fireFigureChanged();
    }
  }

  @Override
  public boolean containsPointOnScreen(Point p) {
    return (p.x >= getLocationOnScreen().x && p.x < (getLocationOnScreen().x + getWidth())
            && p.y >= getLocationOnScreen().y && p.y < (getLocationOnScreen().y + getHeight()));
  }

  @Override
  public void zoomViewToWindow() {
    // 1. Zoom to 100%
    setScaleFactor(1.0);
    // 2. Zoom delayed
    SwingUtilities.invokeLater(
        () -> setScaleFactor(computeZoomLevelToDisplayAllFigures(getDrawing()))
    );
  }

  @Override
  protected void drawDrawing(Graphics2D gr) {
    if (getDrawing() == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) gr.create();
    AffineTransform tx = g2d.getTransform();
    tx.translate(getDrawingToViewTransform().getTranslateX(),
                 getDrawingToViewTransform().getTranslateY());
    tx.scale(getScaleFactor(), getScaleFactor());
    g2d.setTransform(tx);

    getDrawing().setFontRenderContext(g2d.getFontRenderContext());
    try {
      getDrawing().draw(g2d);
    }
    catch (ConcurrentModificationException e) {
      LOG.warn("Exception from JHotDraw caught while calling DefaultDrawing.draw(), continuing.");
      // TODO What to do when it is catched?
    }

    g2d.dispose();
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
    List<Figure> selectedFigures = new CopyOnWriteArrayList<>(getSelectedFigures());

    for (Figure figure : selectedFigures) {
      if (figure instanceof SimpleLineConnection) {
        // A Path may only be selected if the connected start and end Points are selected, too
        SimpleLineConnection lineConnection = (SimpleLineConnection) figure;
        Figure startFigure = lineConnection.getStartFigure();
        Figure endFigure = lineConnection.getEndFigure();

        if (!selectedFigures.contains(startFigure)
            || !selectedFigures.contains(endFigure)) {
          removeFromSelection(figure);
        }
      }
    }

    if (getSelectedFigures().isEmpty()) {
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

  /**
   * Moves the view so that the given points will be in the middle of the
   * drawing.
   *
   * @param xCenter The x coord that shall be in the middle.
   * @param yCenter The y coord that shall be in the middle.
   */
  private void scrollTo(final int xCenter, final int yCenter) {
    SwingUtilities.invokeLater(() -> {
      Point2D.Double pCenterView = new Point2D.Double(xCenter, -yCenter); // Vorzeichen!
      Point pCenterDrawing = drawingToView(pCenterView);
      JViewport viewport = (JViewport) getParent();
      int xUpperLeft = pCenterDrawing.x - viewport.getSize().width / 2;
      int yUpperLeft = pCenterDrawing.y - viewport.getSize().height / 2;
      Point pUpperLeft = new Point(xUpperLeft, yUpperLeft);
      Rectangle rCenter = new Rectangle(pUpperLeft, viewport.getSize());
      scrollRectToVisible(rCenter);
    });
  }

  @Override
  protected void drawConstrainer(Graphics2D g) {
    // The super-implementation draws only for positive coordinates, which we don't want.
    getConstrainer().draw(g, this);
  }

  /**
   * Draws a focus circle around the currently selected figure.
   *
   * @param g2d
   */
  protected void highlightFocus(Graphics2D g2d) {
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

    // After drawing the RadialGradientPaint the drawing area needs to be
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    loopProofRepaintDrawingArea();

    // after 3 seconds the RadialGradientPaint is removed
    new Thread(() -> {
      try {
        synchronized (AbstractOpenTCSDrawingView.this) {
          AbstractOpenTCSDrawingView.this.wait(3000);
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
    }).start();
  }

  protected void loopProofRepaintDrawingArea() {
    if (doRepaint) {
      repaintDrawingArea();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }
  }

  protected ModelManager getModelManager() {
    return modelManager;
  }

  /**
   * The detailLevel indicates what type of Handles are supposed to be shouwn. In Operating mode,
   * we require the type of handles for our figures that do not allow moving, that is
   * indicated by -1.
   * In modelling mode we require the regular Handles, indicated by 0.
   */
  private void refreshDetailLevel() {
    if (OperationMode.OPERATING.equals(appState.getOperationMode())) {
      setHandleDetailLevel(-1);
    }
    else {
      setHandleDetailLevel(0);
    }
  }

  /**
   * Enables the listener for updating the offset figures.
   */
  private void addOffsetListener(OpenTCSDrawingEditor newEditor) {
    offsetListener = new OffsetListener(newEditor);
    addComponentListener(offsetListener);
  }

  /**
   * Computes the rectangle to draw for scrolling to the given figure.
   *
   * @param figure The figure to be shown.
   * @return The rectangle area to be drawn.
   */
  private Rectangle computeVisibleRectangleForFigure(Figure figure) {
    // The rectangle that encloses the figure
    Rectangle2D.Double bounds = computeBounds(figure);

    final int margin = 50;

    Point pCenterDrawing = drawingToView(new Point2D.Double(bounds.getCenterX() - margin,
                                                            bounds.getCenterY() - margin));
    Dimension dBounds = new Dimension((int) (bounds.getWidth() + 2 * margin),
                                      (int) (bounds.getHeight() + 2 * margin));

    return new Rectangle(pCenterDrawing, dBounds);
  }

  /**
   * Computes the rectangle enclosing the given figure.
   *
   * @param figure The figure.
   * @return The rectangle enclosing the given figure.
   */
  protected Rectangle2D.Double computeBounds(Figure figure) {
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

    return bounds;
  }

  /**
   * Computes a fitting zoom level for displaying all figures currently in the drawing.
   *
   * @param drawing The drawing.
   * @return The zoom level.
   */
  private double computeZoomLevelToDisplayAllFigures(Drawing drawing) {
    // Rectangle that contains all figures
    Rectangle2D.Double drawingArea = drawing.getDrawingArea();
    double wDrawing = drawingArea.width + 2 * 20;
    double hDrawing = drawingArea.height + 2 * 20;
    // The currently visible rectangle
    Rectangle visibleRect = getComponent().getVisibleRect();

    double xFactor = visibleRect.width / wDrawing;
    double yFactor = visibleRect.height / hDrawing;

    // Find the smallest, and limit to 400%.
    double newZoom = Math.min(Math.min(xFactor, yFactor), 4.0);

    return roundToTwoDecimalPlaces(newZoom);
  }

  /**
   * Rounds the given value so that its decimal representation has only two places.
   *
   * @param value The value.
   * @return The rounded value.
   */
  private double roundToTwoDecimalPlaces(double value) {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    DecimalFormat twoDForm = new DecimalFormat("#.##", symbols);
    return Double.parseDouble(twoDForm.format(value));
  }

  public abstract class AbstractExtendedEventHandler
      extends DefaultDrawingView.EventHandler {

    @Override // CompositeFigureListener
    public void figureRemoved(CompositeFigureEvent evt) {
      if (evt.getChildFigure() instanceof BitmapFigure) {
        BitmapFigure bmFigure = (BitmapFigure) evt.getChildFigure();

        if (!bmFigure.isTemporarilyRemoved()) {
          BitmapFigure childFigure = (BitmapFigure) evt.getChildFigure();
          bitmapFigures.remove(childFigure);
        }
      }

      super.figureRemoved(evt);
    }

    @Override
    public void focusGained(FocusEvent e) {
      super.focusGained(e);

      if (getEditor() != null) {
        List<BitmapFigure> figuresToRemove = new ArrayList<>();

        for (Figure fig : getDrawing().getFiguresFrontToBack()) {
          if (fig instanceof BitmapFigure) {
            figuresToRemove.add((BitmapFigure) fig);
          }
          // Commented out on 2020-07-17 by Martin Grzenia:
          // During the integration of layers this block caused some problems. When a layer is 
          // hidden, setVisible(false) is called for all figures contained in that particular layer.
          // This block caused all figures to be shown again once the drawing view gained focus.
          // The purpose of this block is not quite clear, but it seems a bit strange at least.
          //if (shouldShowFigure(fig)) {
          //  ((AbstractFigure) fig).setVisible(true);
          //}
        }

        for (BitmapFigure figure : figuresToRemove) {
          figure.setTemporarilyRemoved(true);
          getDrawing().remove(figure);
        }

        for (BitmapFigure bmFigure : bitmapFigures) {
          bmFigure.setTemporarilyRemoved(false);
          getDrawing().add(bmFigure);
          getDrawing().sendToBack(bmFigure);
        }
      }
    }

    @Override // FigureListener
    public void figureRequestRemove(FigureEvent evt) {
      super.figureRequestRemove(evt);

      if (evt.getFigure() instanceof BitmapFigure) {
        BitmapFigure bmFigure = (BitmapFigure) evt.getFigure();

        if (!bmFigure.isTemporarilyRemoved()) {
          BitmapFigure figure = (BitmapFigure) evt.getFigure();
          bitmapFigures.remove(figure);
        }
      }
    }

    protected abstract boolean shouldShowFigure(Figure figure);
  }
}
