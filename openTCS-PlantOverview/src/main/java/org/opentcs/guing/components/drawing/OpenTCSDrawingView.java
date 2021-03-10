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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
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
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.AbstractFigure;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.GridConstrainer;
import org.jhotdraw.draw.event.CompositeFigureEvent;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.gui.datatransfer.ClipboardUtil;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.drawing.figures.BitmapFigure;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.OriginFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.model.AbstractConnectableModelComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.FigureCloner;
import org.opentcs.guing.util.ModelComponentUtil;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DrawingView implementation for the openTCS plant overview.
 *
 */
public class OpenTCSDrawingView
    extends DefaultDrawingView
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
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;
  /**
   * A helper for cloning figures.
   */
  private final FigureCloner figureCloner;
  /**
   * This listener sets the position of the invisible offsetFigure after the
   * frame was resized.
   */
  private ComponentListener offsetListener;
  /**
   * The block areas.
   */
  private ModelComponent fBlocks;
  /**
   * Flag whether the labels shall be drawn.
   */
  private boolean labelsVisible = true;
  /**
   * Flag whether the blocks shall be drawn.
   */
  private boolean blocksVisible = true;
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
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param modelManager Provides the current system model.
   * @param figureCloner A helper for cloning figures.
   */
  @Inject
  public OpenTCSDrawingView(ApplicationState appState,
                            ModelManager modelManager,
                            FigureCloner figureCloner) {
    this.appState = requireNonNull(appState, "appState");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.figureCloner = requireNonNull(figureCloner, "figureCloner");

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
    if (evt.getPropertyName().equals(VehicleFigure.POSITION_CHANGED)) {
      scrollTo((VehicleFigure) modelManager.getModel().getFigure(fFocusVehicle));
    }
  }

  // ###
  // Methods of EditableComponent start here.
  // ###
  @Override // EditableComponent
  public void cutSelectedItems() {
    deleteSelectedFigures();
  }

  @Override // EditableComponent
  public void copySelectedItems() {
    bufferedFigures = getDrawing().sort(getSelectedFigures());
  }

  @Override // EditableComponent
  public void pasteBufferedItems() {
    clearSelection();
    List<Figure> figuresToClone = new ArrayList<>();

    for (Figure deletedFigure : bufferedFigures) {
      if (getDrawing().contains(deletedFigure)) {
        figuresToClone.add(deletedFigure);
      }
      else {
        getDrawing().add(deletedFigure);
      }
    }
    // Create clones of all buffered figures
    List<Figure> clonedFigures = figureCloner.cloneFigures(figuresToClone);
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

  @Override // EditableComponent
  public void duplicate() {
    copySelectedItems();
    pasteBufferedItems();
  }

  // ###
  // Methods of DrawingView start here.
  // ###
  @Override
  protected DefaultDrawingView.EventHandler createEventHandler() {
    return new ExtendedEventHandler();
  }

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
        // OriginFigure nur einmal zuf�gen!
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
    final int centerY = (int) -(visibleViewRect.getCenterY() + 0.5);  // Vorzeichen!
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
  public void removeAll() {
    fVehicles.clear();
    super.removeAll();
  }

  @Override
  public void processKeyEvent(KeyEvent e) {
    if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
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
  public boolean isBlocksVisible() {
    return blocksVisible;
  }

  public void setBlocksVisible(boolean newValue) {
    blocksVisible = newValue;
    // Repaint the whole layout.
    repaintDrawingArea();
  }

  private void repaintDrawingArea() {
    // We need to add the visible rect to the dirty area before we repaint.
    repaintDrawingArea(viewToDrawing(getVisibleRect()));
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
        AbstractConnectableModelComponent abstractFigure = (AbstractConnectableModelComponent) model;

        if (model instanceof LocationModel) {
          LocationModel locModel = (LocationModel) model;

          for (AbstractConnection connection : locModel.getConnections()) {
            AbstractFigure figure = (AbstractFigure) modelManager.getModel().getFigure(connection);
            figure.setVisible(visible);
          }
        }

        AbstractFigure figure = (AbstractFigure) modelManager.getModel().getFigure(abstractFigure);

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

  /**
   * Adds a background image to this drawing view.
   *
   * @param file The file with the image.
   */
  public void addBackgroundBitmap(File file) {
    addBackgroundBitmap(new BitmapFigure(file));
  }

  /**
   * Adds a background image to this drawing view.
   *
   * @param bitmapFigure The figure containing the image.
   */
  public void addBackgroundBitmap(BitmapFigure bitmapFigure) {
    bitmapFigures.add(bitmapFigure);
    getDrawing().add(bitmapFigure);
    getDrawing().sendToBack(bitmapFigure);
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

    if (fFocusVehicle == null) {
      fFocusFigure = figure;
    }

    scrollRectToVisible(computeVisibleRectangleForFigure(figure));

    repaint();
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
    VehicleFigure vFigure = (VehicleFigure) modelManager.getModel().getFigure(fFocusVehicle);
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
    VehicleFigure vFigure = (VehicleFigure) modelManager.getModel().getFigure(fFocusVehicle);
    if (vFigure != null) {
      vFigure.removePropertyChangeListener(this);
    }
    fFocusVehicle = null;
    repaint();
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
   * Message of the application that a block area was created.
   *
   * @param block The newly created block.
   */
  public void blockAdded(BlockModel block) {
    block.addBlockChangeListener(blockChangeHandler);
  }

  /**
   * Shows or hides the current route of a vehicle.
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
   * Updates the figures of a block.
   *
   * @param block The block.
   */
  public void updateBlock(BlockModel block) {
    for (Figure figure : ModelComponentUtil.getChildFigures(block, modelManager.getModel())) {
      ((AbstractFigure) figure).fireFigureChanged();
    }
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

  /**
   * Scales the view to a value so the whole model fits.
   */
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

    if (blocksVisible) {
      drawBlocks(g2d);
    }

    drawDriveOrderElements(g2d);

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

  @Override
  protected void drawTool(Graphics2D g2d) {
    super.drawTool(g2d);

    if (getEditor() == null || getEditor().getTool() == null || getEditor().getActiveView() != this) {
      return;
    }

    if (fFocusVehicle != null) {
      // Set focus on the selected vehicle and its destination point
      highlightVehicle(g2d);
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

    // After drawing the RadialGradientPaint the drawing area needs to be
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    if (doRepaint) {
      repaintDrawingArea();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }

    // after 3 seconds the RadialGradientPaint is removed
    new Thread(() -> {
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

    final Figure currentVehicleFigure = modelManager.getModel().getFigure(fFocusVehicle);
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
      Figure nextPoint = modelManager.getModel().getFigure(pointModel);
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
      Figure lastPoint = modelManager.getModel().getFigure(pointModel);
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

    // After drawing the RadialGradientPaint the drawing area needs to
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    if (doRepaint) {
      repaintDrawingArea();
      doRepaint = false;
    }
    else {
      doRepaint = true;
    }
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
        drawPathDecoration(g2d,
                           ModelComponentUtil.getChildFigures(block, modelManager.getModel()),
                           block.getColor(),
                           Strokes.BLOCK_ELEMENT);
      }
    }
  }

  /**
   * Marks the elements of the transport orders of all vehicles.
   *
   * @param g2d
   */
  private void drawDriveOrderElements(Graphics2D g2d) {
    if (!appState.hasOperationMode(OperationMode.OPERATING)) {
      return;
    }

    for (VehicleModel vehicle : fVehicles) {
      if (vehicle.getDriveOrderComponents() != null) {
        if (vehicle.getDriveOrderState() == TransportOrder.State.WITHDRAWN) {
          drawPathDecoration(g2d,
                             driveOrderFigures(vehicle.getDriveOrderComponents()),
                             Color.GRAY,
                             Strokes.PATH_ON_WITHDRAWN_ROUTE);
        }
        else {
          drawPathDecoration(g2d,
                             driveOrderFigures(vehicle.getDriveOrderComponents()),
                             vehicle.getDriveOrderColor(),
                             Strokes.PATH_ON_ROUTE);
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
                                  List<Figure> figures,
                                  Color color,
                                  Stroke stroke) {
    // Draw the basecolor of the block or transport order transparent with alpha = 192/256
    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 192));

    Path2D.Float path = new Path2D.Float();

    for (Figure figure : figures) {
      if (figure instanceof LabeledFigure) {
        path.append(((LabeledFigure) figure).getShape(), false);
      }
      else if (figure instanceof SimpleLineConnection) {
        path.append(((SimpleLineConnection) figure).getShape(), false);
      }
    }

    Stroke currentStroke = g2d.getStroke();
    g2d.setStroke(stroke);
    g2d.draw(path);
    g2d.setStroke(currentStroke);
  }

  /**
   * Looks for all figures that belong to the given drive route.
   *
   * @param driveOrder The list with elements of the drive route.
   * @return The figures.
   */
  private List<Figure> driveOrderFigures(@Nonnull List<ModelComponent> driveOrder) {
    requireNonNull(driveOrder, "driveOrder");

    List<Figure> children = getDrawing().getChildren();
    List<Figure> figures = new ArrayList<>(driveOrder.size());

    for (Figure figure : children) {
      ModelComponent model = figure.get(FigureConstants.MODEL);

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

  private void deleteSelectedFigures() {
    final List<Figure> deletedFigures = getDrawing().sort(getSelectedFigures());
    // Abort, if not all of the selected figures may be removed from the drawing
    for (Figure figure : deletedFigures) {
      if (!figure.isRemovable()) {
        LOG.warn("Figure is not removable: {}. Aborting.", figure);
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

      getDrawing().remove(figure);
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
  private Rectangle2D.Double computeBounds(Figure figure) {
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
        Figure pointFigure = modelManager.getModel().getFigure(pointModel);
        bounds.add(pointFigure.getBounds());
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

      for (Figure figure : ModelComponentUtil.getChildFigures(block, modelManager.getModel())) {
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

  private class ExtendedEventHandler
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
          if (!(fig instanceof VehicleFigure) && !(fig instanceof OriginFigure)) {
            ((AbstractFigure) fig).setVisible(true);
          }
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

        for (AbstractFigure figure : invisibleFigures) {
          figure.setVisible(false);
        }

        if (!invisibleFigures.isEmpty()) {
          repaint();
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
  }
}
