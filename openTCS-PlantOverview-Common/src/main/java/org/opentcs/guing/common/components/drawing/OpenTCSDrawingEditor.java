/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.IncreaseHandleDetailLevelAction;
import org.jhotdraw.draw.event.CompositeFigureEvent;
import org.jhotdraw.draw.event.CompositeFigureListener;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.OffsetFigure;
import org.opentcs.guing.common.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.common.event.DrawingEditorEvent;
import org.opentcs.guing.common.event.DrawingEditorListener;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.util.CourseObjectFactory;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.draw.MoveAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.DeleteAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.SelectAllAction;
import org.opentcs.util.event.EventHandler;

/**
 * The <code>DrawingEditor</code> coordinates <code>DrawingViews</code>
 * and the <code>Drawing</code>.
 * It also offers methods to add specific unique figures to the
 * <code>Drawing</code>.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSDrawingEditor
    extends DefaultDrawingEditor
    implements EventHandler {

  /**
   * Width on the screen edge.
   */
  private static final int MARGIN = 20;
  /**
   * A factory for course objects.
   */
  private final CourseObjectFactory crsObjectFactory;
  /**
   *
   */
  private final CompositeFigureEventHandler cmpFigureEvtHandler = new CompositeFigureEventHandler();
  /**
   * Listens for figure selection, addition or removal events.
   */
  private final List<DrawingEditorListener> fDrawingEditorListeners = new ArrayList<>();
  /**
   * The drawing that contains all figures.
   */
  private Drawing fDrawing;
  // These invisible figures are moved automatically to enlarge the drawing.
  private OffsetFigure topOffsetFigure;
  private OffsetFigure bottomOffsetFigure;
  private OffsetFigure rightOffsetFigure;
  private OffsetFigure leftOffsetFigure;

  /**
   * Creates a new instance.
   *
   * @param crsObjFactory A factory for course objects.
   */
  @Inject
  public OpenTCSDrawingEditor(CourseObjectFactory crsObjFactory) {
    this.crsObjectFactory = requireNonNull(crsObjFactory, "crsObjectFactory");
  }

  @Override
  public void onEvent(Object event) {
  }

  /**
   * Creates the offset figures, sets their position to the current bounds of the
   * view and repaints the ruler to fit the current grid.
   */
  public void initializeViewport() {
    initializeRuler();
    removeOffsetFigures();
    topOffsetFigure = crsObjectFactory.createOffsetFigure();
    bottomOffsetFigure = crsObjectFactory.createOffsetFigure();
    leftOffsetFigure = crsObjectFactory.createOffsetFigure();
    rightOffsetFigure = crsObjectFactory.createOffsetFigure();

    OpenTCSDrawingView activeView = getActiveView();
    if (activeView == null) {
      return;
    }

    // Rectangle that contains all figures
    Rectangle2D.Double drawingArea = getDrawing().getDrawingArea();
    // The visible rectangle
    Rectangle visibleRect = activeView.getComponent().getVisibleRect();
    // The size of the invisible offset figures
    double wFigure = topOffsetFigure.getBounds().width;
    double hFigure = topOffsetFigure.getBounds().height;

    // When the drawing already contains figures
    double xLeft = drawingArea.x;
    double xRight = drawingArea.x + drawingArea.width;
    double yTop = drawingArea.y;
    double yBottom = drawingArea.y + drawingArea.height;

    // An empty drawing only contains the origin figure, which shall be
    // on the bottom left 
    if (visibleRect.width > drawingArea.width && visibleRect.height > drawingArea.height) {
      xLeft = -drawingArea.width / 2 - MARGIN;
      xRight = visibleRect.width + xLeft - (MARGIN + wFigure / 2);
      yBottom = -(-drawingArea.height / 2 - MARGIN);
      yTop = -(visibleRect.height - yBottom - (MARGIN + hFigure / 2));
    }

    double xCenter = (xLeft + xRight) / 2;
    double yCenter = (yBottom + yTop) / 2;

    topOffsetFigure.setBounds(new Point2D.Double(xCenter, yTop), null);
    bottomOffsetFigure.setBounds(new Point2D.Double(xCenter, yBottom), null);
    leftOffsetFigure.setBounds(new Point2D.Double(xLeft, yCenter), null);
    rightOffsetFigure.setBounds(new Point2D.Double(xRight, yCenter), null);

    getDrawing().add(topOffsetFigure);
    getDrawing().add(bottomOffsetFigure);
    getDrawing().add(leftOffsetFigure);
    getDrawing().add(rightOffsetFigure);

    // XXX Do we still need to call this?
    activeView.setScaleFactor(activeView.getScaleFactor());
//    validateViewTranslation();
  }

  protected CourseObjectFactory getCourseObjectFactory() {
    return crsObjectFactory;
  }

  private void initializeRuler() {
    OpenTCSDrawingView activeView = getActiveView();
    DrawingViewScrollPane scrollPane
        = (DrawingViewScrollPane) activeView.getComponent().getParent().getParent();
    Rectangle2D.Double drawingArea
        = activeView.getDrawing().getDrawingArea();
    scrollPane.getHorizontalRuler().setPreferredWidth((int) drawingArea.width);
    scrollPane.getVerticalRuler().setPreferredHeight((int) drawingArea.height);
  }

  /**
   * Removes the <code>OffsetFigure</code>s off the drawing.
   */
  private void removeOffsetFigures() {
    if (getDrawing() == null) {
      return;
    }

    getDrawing().remove(topOffsetFigure);
    getDrawing().remove(bottomOffsetFigure);
    getDrawing().remove(leftOffsetFigure);
    getDrawing().remove(rightOffsetFigure);
  }

  /**
   * Adds a listener.
   *
   * @param listener The listener.
   */
  public void addDrawingEditorListener(DrawingEditorListener listener) {
    requireNonNull(listener, "listener");
    fDrawingEditorListeners.add(listener);
  }

  /**
   * Removes a listener.
   *
   * @param listener The listener.
   */
  public void removeDrawingEditorListener(DrawingEditorListener listener) {
    requireNonNull(listener, "listener");
    fDrawingEditorListeners.remove(listener);
  }

  /**
   * Sets the system model.
   *
   * @param systemModel The model of the course.
   */
  public void setSystemModel(SystemModel systemModel) {
    setDrawing(systemModel.getDrawing());
    for (DrawingView drawView : getDrawingViews()) {
      ((OpenTCSDrawingView) drawView).setBlocks(
          systemModel.getMainFolder(SystemModel.FolderKey.BLOCKS)
      );
    }
  }

  public Drawing getDrawing() {
    return fDrawing;
  }

  public void setDrawing(Drawing drawing) {
    requireNonNull(drawing, "drawing");

    if (fDrawing != null) {
      fDrawing.removeCompositeFigureListener(cmpFigureEvtHandler);
    }
    fDrawing = drawing;
    fDrawing.addCompositeFigureListener(cmpFigureEvtHandler);

    // Also let the drawing views know about the new drawing.
    for (DrawingView view : getDrawingViews()) {
      view.setDrawing(drawing);
    }
  }

  @Override
  public void add(DrawingView view) {
    super.add(view);
    view.setDrawing(fDrawing);
  }

  @Override
  public OpenTCSDrawingView getActiveView() {
    return (OpenTCSDrawingView) super.getActiveView();
  }

  public Collection<OpenTCSDrawingView> getAllViews() {
    Collection<OpenTCSDrawingView> result = new LinkedList<>();
    for (DrawingView view : getDrawingViews()) {
      result.add((OpenTCSDrawingView) view);
    }
    return result;
  }

  /**
   * Notification of the <code>DrawingView</code> that a figure was added.
   *
   * @param figure The added figure.
   */
  public void figureAdded(Figure figure) {
    // Create the data model to a new point or location figure and show
    // the name in the label
    if (figure instanceof LabeledFigure) {
      LabeledFigure labeledFigure = (LabeledFigure) figure;

      if (labeledFigure.getLabel() == null) {
        // Create the label and add the figure to the data model
        TCSLabelFigure label = new TCSLabelFigure();
        Point2D.Double pos = labeledFigure.getStartPoint();
        pos.x += label.getOffset().x;
        pos.y += label.getOffset().y;
        label.setBounds(pos, pos);
        labeledFigure.setLabel(label);
      }
    }

    for (DrawingEditorListener listener : fDrawingEditorListeners) {
      listener.figureAdded(new DrawingEditorEvent(this, figure));
    }
  }

  /**
   * Notification of the <code>DrawingView</code> that a figure was removed.
   *
   * @param figure The figure that was removed.
   */
  public void figureRemoved(Figure figure) {
    for (DrawingEditorListener listener : fDrawingEditorListeners) {
      listener.figureRemoved(new DrawingEditorEvent(this, figure));
    }
  }

  /**
   * Notification of the <code>DrawingView</code> that figures were selected.
   *
   * @param figures The selected figures.
   */
  public void figuresSelected(List<Figure> figures) {
    for (DrawingEditorListener listener : fDrawingEditorListeners) {
      listener.figureSelected(new DrawingEditorEvent(this, figures));
    }
  }

  /**
   * Overrides the method from DefaultDrawingEditor to create a tool-specific
   * input map.
   * The implementation of this class creates an input map for the following
   * action ID's:
   * - DeleteAction
   * - MoveAction.West, .East, .North, .South
   *
   * SelectAll, Cut, Copy, Paste are handled by SelectAllAction etc.
   *
   * @return The input map.
   */
  @Override  // DefaultDrawingEditor
  protected InputMap createInputMap() {
    InputMap m = new InputMap();

    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteAction.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DeleteAction.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), MoveAction.West.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), MoveAction.East.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), MoveAction.North.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), MoveAction.South.ID);

    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), MoveAction.West.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), MoveAction.East.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), MoveAction.North.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), MoveAction.South.ID);

    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), MoveAction.West.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), MoveAction.East.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), MoveAction.North.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), MoveAction.South.ID);

    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), MoveAction.West.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), MoveAction.East.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), MoveAction.North.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), MoveAction.South.ID);

    return m;
  }

  @Override  // DefaultDrawingEditor
  protected ActionMap createActionMap() {
    ActionMap m = new ActionMap();

    m.put(DeleteAction.ID, new DeleteAction());
    m.put(SelectAllAction.ID, new SelectAllAction());
    m.put(IncreaseHandleDetailLevelAction.ID, new IncreaseHandleDetailLevelAction(this));

    m.put(MoveAction.East.ID, new MoveAction.East(this));
    m.put(MoveAction.West.ID, new MoveAction.West(this));
    m.put(MoveAction.North.ID, new MoveAction.North(this));
    m.put(MoveAction.South.ID, new MoveAction.South(this));

//    m.put(CutAction.ID, new CutAction());
//    m.put(CopyAction.ID, new CopyAction());
//    m.put(PasteAction.ID, new PasteAction());
    return m;
  }

  private class CompositeFigureEventHandler
      implements CompositeFigureListener {

    /**
     * Creates a new instance.
     */
    public CompositeFigureEventHandler() {
    }

    @Override
    public void figureAdded(CompositeFigureEvent e) {
      OpenTCSDrawingEditor.this.figureAdded(e.getChildFigure());
    }

    @Override
    public void figureRemoved(CompositeFigureEvent e) {
      OpenTCSDrawingEditor.this.figureRemoved(e.getChildFigure());
    }
  }
}
