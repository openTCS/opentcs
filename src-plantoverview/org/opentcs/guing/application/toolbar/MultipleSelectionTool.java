/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.toolbar;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.jhotdraw.app.action.ActionUtil;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.tool.DelegationSelectionTool;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.jhotdraw.draw.tool.Tool;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.LayoutToModelMenuItem;
import org.opentcs.guing.application.action.ModelToLayoutMenuItem;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * The default selection tool.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class MultipleSelectionTool
    extends DelegationSelectionTool
    implements AttributesChangeListener {

  /**
   * A bit mask for the left mouse button being clicked and the ctrl key being
   * pressed.
   */
  private static final int CTRL_LEFT_MASK
      = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
  /**
   * We store the last mouse click here, to support multi-click behavior, that
   * is, a behavior that is invoked, when the user clicks on the same spot
   * multiple times, but in a longer interval than needed for a double click.
   */
  private MouseEvent lastClickEvent;
  /**
   * The tracker encapsulates the current state of the SelectionTool. Overrides
   * SelectionTool.selectAreaTracker
   */
  private SelectAreaTracker selectAreaTracker;
  /**
   * The tracker encapsulates the current state of the SelectionTool.
   */
  private DragTracker dragTracker;
  /**
   * Flag to
   */
  private boolean aligntLayoutWithModel;
  private final Collection<Action> drawingActions;
  private final Collection<Action> selectionActions;

  /**
   * Creates a new instance.
   *
   * @param drawingActions
   * @param selectionActions
   */
  public MultipleSelectionTool(Collection<Action> drawingActions,
                               Collection<Action> selectionActions) {
    super(drawingActions, selectionActions);
    this.drawingActions = drawingActions;
    this.selectionActions = selectionActions;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getInitiator() == this) {
      OpenTCSView.instance().resetSelectionTool();
    }
  }

  @Override // SelectionTool
  protected SelectAreaTracker getSelectAreaTracker() {
    if (selectAreaTracker == null) {
      selectAreaTracker = new OpenTCSSelectAreaTracker();
    }

    return selectAreaTracker;
  }

  @Override // SelectionTool
  public void setSelectAreaTracker(SelectAreaTracker newValue) {
    selectAreaTracker = newValue;
  }

  @Override // SelectionTool
  protected DragTracker getDragTracker(Figure f) {
    if (dragTracker == null) {
      dragTracker = new OpenTCSDragTracker();
    }

    dragTracker.setDraggedFigure(f);
    return dragTracker;
  }

  @Override // SelectionTool
  public void setDragTracker(DragTracker newValue) {
    dragTracker = newValue;
  }

  @Override // DelegationSelectionTool
  public void mouseClicked(MouseEvent evt) {
    if (!evt.isConsumed()) {
      if (evt.getClickCount() >= 2 && evt.getButton() == MouseEvent.BUTTON1) {
        handleDoubleClick(evt);
      }
      else if (evt.getButton() == MouseEvent.BUTTON3) {
        // Handle right click as double click
        handleDoubleClick(evt);
      }
      else if (evt.getClickCount() == 1
          && lastClickEvent != null
          && lastClickEvent.getClickCount() == 1
          && lastClickEvent.getX() == evt.getX()
          && lastClickEvent.getY() == evt.getY()) {
        // click with ctrl
        if (((evt.getModifiersEx() & CTRL_LEFT_MASK) > 0)
            && ((lastClickEvent.getModifiersEx() & CTRL_LEFT_MASK) > 0)) {
          handleMultiClick(evt);
        }
      }
    }

    lastClickEvent = evt;
  }

  @Override // DelegationSelectionTool
  protected void handleDoubleClick(MouseEvent evt) {
    DrawingView v = getView();
    Point pos = new Point(evt.getX(), evt.getY());
    Handle handle = v.findHandle(pos);

    // Special case PathConnection: Ignore double click
    if (handle != null && !(handle instanceof BezierOutlineHandle)) {
      handle.trackDoubleClick(pos, evt.getModifiersEx());
    }
    else {
      Point2D.Double p = viewToDrawing(pos);

      // Note: The search sequence used here, must be
      // consistent with the search sequence used by the
      // HandleTracker, the SelectAreaTracker and SelectionTool.
      // If possible, continue to work with the current selection
      Figure figure = null;

      if (isSelectBehindEnabled()) {
        for (Figure f : v.getSelectedFigures()) {
          if (f.contains(p)) {
            figure = f;
            break;
          }
        }
      }
      // If the point is not contained in the current selection,
      // search for a figure in the drawing.
      if (figure == null) {
        figure = v.findFigure(pos);
      }

      Figure outerFigure = figure;

      if (figure != null && figure.isSelectable()) {
        Tool figureTool = figure.getTool(p);

        if (figureTool == null) {
          figure = getDrawing().findFigureInside(p);

          if (figure != null) {
            figureTool = figure.getTool(p);
          }
        }

        if (figureTool != null) {
          setTracker(figureTool);
          figureTool.mousePressed(evt);
        }
        else {
          if (outerFigure.handleMouseClick(p, evt, getView())) {
            v.clearSelection();
            v.addToSelection(outerFigure);
          }
          else {
            v.clearSelection();
            v.addToSelection(outerFigure);
            v.setHandleDetailLevel(v.getHandleDetailLevel() + 1);
          }
        }
      }
    }

    evt.consume();
  }

  @Override
  protected void showPopupMenu(Figure figure, Point p, Component c) {
    if(OpenTCSView.instance().hasOperationMode(GuiManager.OperationMode.OPERATING)) {
      return;
    }
    
    // --- JHotDraw code starts here ---
    JPopupMenu menu = new JPopupMenu();
    JMenu submenu = null;
    String submenuName = null;
    LinkedList<Action> popupActions = new LinkedList<>();
    if (figure != null) {
      LinkedList<Action> figureActions = new LinkedList<>(
          figure.getActions(viewToDrawing(p)));
      if (popupActions.size() != 0 && figureActions.size() != 0) {
        popupActions.add(null);
      }
      popupActions.addAll(figureActions);
      if (popupActions.size() != 0 && !selectionActions.isEmpty()) {
        popupActions.add(null);
      }
      popupActions.addAll(selectionActions);
    }
    if (popupActions.size() != 0 && !drawingActions.isEmpty()) {
      popupActions.add(null);
    }
    popupActions.addAll(drawingActions);

    HashMap<Object, ButtonGroup> buttonGroups = new HashMap<>();
    for (Action a : popupActions) {
      if (a != null && a.getValue(ActionUtil.SUBMENU_KEY) != null) {
        if (submenuName == null || !submenuName.equals(a.getValue(ActionUtil.SUBMENU_KEY))) {
          submenuName = (String) a.getValue(ActionUtil.SUBMENU_KEY);
          submenu = new JMenu(submenuName);
          menu.add(submenu);
        }
      }
      else {
        submenuName = null;
        submenu = null;
      }
      if (a == null) {
        if (submenu != null) {
          submenu.addSeparator();
        }
        else {
          menu.addSeparator();
        }
      }
      else {
        AbstractButton button;

        if (a.getValue(ActionUtil.BUTTON_GROUP_KEY) != null) {
          ButtonGroup bg = buttonGroups.get(a.getValue(ActionUtil.BUTTON_GROUP_KEY));
          if (bg == null) {
            bg = new ButtonGroup();
            buttonGroups.put(a.getValue(ActionUtil.BUTTON_GROUP_KEY), bg);
          }
          button = new JRadioButtonMenuItem(a);
          bg.add(button);
          button.setSelected(a.getValue(ActionUtil.SELECTED_KEY) == Boolean.TRUE);
        }
        else if (a.getValue(ActionUtil.SELECTED_KEY) != null) {
          button = new JCheckBoxMenuItem(a);
          button.setSelected(a.getValue(ActionUtil.SELECTED_KEY) == Boolean.TRUE);
        }
        else {
          button = new JMenuItem(a);
        }

        if (submenu != null) {
          submenu.add(button);
        }
        else {
          menu.add(button);
        }
      }
    }
    // --- JHotDraw code ends here ---

    // Points and Locations get two additional entries
    JMenuItem toLayout = new ModelToLayoutMenuItem(editor, false);
    menu.add(toLayout);
    JMenuItem fromLayout = new LayoutToModelMenuItem(editor, false);
    menu.add(fromLayout);

    menu.show(c, p.x, p.y);
  }
}
