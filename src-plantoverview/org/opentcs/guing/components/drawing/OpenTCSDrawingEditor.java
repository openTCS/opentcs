/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.IncreaseHandleDetailLevelAction;
import org.opentcs.guing.application.action.draw.MoveAction;
import org.opentcs.guing.application.action.edit.CopyAction;
import org.opentcs.guing.application.action.edit.CutAction;
import org.opentcs.guing.application.action.edit.DeleteAction;
import org.opentcs.guing.application.action.edit.PasteAction;
import org.opentcs.guing.application.action.edit.SelectAllAction;
import org.opentcs.guing.event.DrawingEditorEvent;
import org.opentcs.guing.event.DrawingEditorListener;
import org.opentcs.guing.model.SystemModel;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class OpenTCSDrawingEditor
    extends DefaultDrawingEditor {

  /**
   * Die Zeichnung, die eine Liste von Figures verwaltet.
   */
  private Drawing fDrawing;
  /**
   * Die Applikationsmodellierung, die horcht, ob im OpenTCSDrawingEditor ein
   * Figure-Objekt selektiert, hinzugefügt oder gelöscht wurde.
   */
  private DrawingEditorListener fDrawingEditorListener;

  /**
   * Creates a new instance.
   */
  @Inject
  public OpenTCSDrawingEditor() {
    // Do nada.
  }

  public Drawing getDrawing() {
    return fDrawing;
  }

  /**
   * Sets a listener.
   *
   * @param listener The listener.
   */
  public void setDrawingEditorListener(DrawingEditorListener listener) {
    fDrawingEditorListener = Objects.requireNonNull(listener);
  }

  /**
   * Setzt das Modell des Fahrkurses.
   *
   * @param systemModel das Modell des Fahrkurses
   */
  public void setSystemModel(SystemModel systemModel) {
    setDrawing(systemModel.getDrawing());
    for (DrawingView drawView : getDrawingViews()) {
      ((OpenTCSDrawingView) drawView).setBlocks(systemModel.getMainFolder(SystemModel.BLOCKS));
      ((OpenTCSDrawingView) drawView).setStaticRoutes(systemModel.getMainFolder(SystemModel.STATIC_ROUTES));
    }
  }

  /**
   * Setzt das Drawing.
   *
   * @param drawing
   */
  public void setDrawing(Drawing drawing) {
    fDrawing = drawing;
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

  /**
   * Benachrichtigung vom View, dass ein Figure-Objekt hinzugefügt wurde.
   *
   * @param figure das hinzugefügte Figure
   */
  public void figureAdded(Figure figure) {
    fDrawingEditorListener.figureAdded(new DrawingEditorEvent(this, figure));
  }

  public void figureRemoved(Figure figure) {
    fDrawingEditorListener.figureRemoved(new DrawingEditorEvent(this, figure));
  }

  /**
   * Benachrichtigung der View, dass mehrere Figure-Objekte selektiert wurden.
   *
   * @param figures Die selektierten Figures.
   */
  public void figuresSelected(List<Figure> figures) {
    fDrawingEditorListener.figureSelected(new DrawingEditorEvent(this, figures));
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
  @Override	// DefaultDrawingEditor
  protected InputMap createInputMap() {
    InputMap m = new InputMap();

    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteAction.ID);
    m.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DeleteAction.ID);
    // Alle Verschiebungen über MoveAction, nicht über MoveConstrainedAction
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

  @Override	// DefaultDrawingEditor
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
}
