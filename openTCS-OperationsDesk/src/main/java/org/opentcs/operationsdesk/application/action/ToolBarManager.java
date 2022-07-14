/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.event.ToolAdapter;
import org.jhotdraw.draw.event.ToolEvent;
import org.jhotdraw.draw.event.ToolListener;
import org.opentcs.guing.common.application.action.ToolButtonListener;
import org.opentcs.guing.common.application.toolbar.DragTool;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.event.ResetInteractionToolCommand;
import org.opentcs.guing.common.util.CourseObjectFactory;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.operationsdesk.application.action.actions.CreateTransportOrderAction;
import org.opentcs.operationsdesk.application.action.actions.FindVehicleAction;
import org.opentcs.operationsdesk.application.action.actions.PauseAllVehiclesAction;
import org.opentcs.operationsdesk.application.action.actions.ResumeAllVehiclesAction;
import org.opentcs.operationsdesk.application.toolbar.MultipleSelectionTool;
import org.opentcs.operationsdesk.application.toolbar.SelectionToolFactory;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.draw.SelectSameAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;

/**
 * Sets up and manages a list of tool bars in the graphical user interface.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ToolBarManager
    implements EventHandler {

  /**
   * A factory for selectiont tools.
   */
  private final SelectionToolFactory selectionToolFactory;
  /**
   * A list of all toolbars.
   */
  private final List<JToolBar> toolBarList
      = Collections.synchronizedList(new LinkedList<JToolBar>());
  /**
   * A tool bar for actions creating new items.
   */
  private final JToolBar toolBarCreation = new JToolBar();
  /**
   * A toggle button for the selection tool.
   */
  private final JToggleButton selectionToolButton;
  /**
   * A toggle button for the drag tool.
   */
  private final JToggleButton dragToolButton;
  /**
   * The actual drag tool.
   */
  private DragTool dragTool;
  /**
   * A button for creating transport orders.
   * Available in operating mode.
   */
  private final JButton buttonCreateOrder;
  /**
   * A button for finding vehicles.
   * Available in operating mode.
   */
  private final JButton buttonFindVehicle;
  /**
   * A button for pausing all vehicles.
   * Available in operating mode.
   */
  private final JButton buttonPauseAllVehicles;
  /**
   * A button for resuming all vehicles.
   * Available in operating mode.
   */
  private final JButton buttonResumeAllVehicles;

  /**
   * Creates a new instance.
   *
   * @param actionMap The action map to be used
   * @param crsObjFactory A factory for course objects
   * @param editor The drawing editor
   * @param selectionToolFactory The selection tool factory
   */
  @Inject
  public ToolBarManager(ViewActionMap actionMap,
                        CourseObjectFactory crsObjFactory,
                        OpenTCSDrawingEditor editor,
                        SelectionToolFactory selectionToolFactory) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(crsObjFactory, "crsObjFactory");
    requireNonNull(editor, "editor");
    this.selectionToolFactory = requireNonNull(selectionToolFactory,
                                               "selectionToolFactory");

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TOOLBAR_PATH);

    // --- 1. ToolBar: Creation ---
    // Selection, Drag | Create Point, Location, Path, Link | 
    // Create Location Type, Vehicle, Block, Static Route | 
    // Create Transport Order | Find, Show Vehicles
    toolBarCreation.setActionMap(actionMap);
    // --- Selection Tool ---
    selectionToolButton = addSelectionToolButton(toolBarCreation, editor);
    // --- Drag Tool ---
    dragToolButton = addDragToolButton(toolBarCreation, editor);

    toolBarCreation.addSeparator();

    // --- Create Transport Order (only in Operating mode) ---
    buttonCreateOrder = new JButton(actionMap.get(CreateTransportOrderAction.ID));
    buttonCreateOrder.setText(null);
    toolBarCreation.add(buttonCreateOrder);

    toolBarCreation.addSeparator();

    // --- Find Vehicle (only in Operating mode) ---
    buttonFindVehicle = new JButton(actionMap.get(FindVehicleAction.ID));
    buttonFindVehicle.setText(null);
    toolBarCreation.add(buttonFindVehicle);

    toolBarCreation.addSeparator();

    // --- Pause All Vehicles (only in Operating mode) ---
    buttonPauseAllVehicles = new JButton(actionMap.get(PauseAllVehiclesAction.ID));
    buttonPauseAllVehicles.setText(null);
    toolBarCreation.add(buttonPauseAllVehicles);

    // --- Resume All Vehicles (only in Operating mode) ---
    buttonResumeAllVehicles = new JButton(actionMap.get(ResumeAllVehiclesAction.ID));
    buttonResumeAllVehicles.setText(null);
    toolBarCreation.add(buttonResumeAllVehicles);

    toolBarCreation.setName(labels.getString("toolBarManager.toolbar_drawing.title"));
    toolBarList.add(toolBarCreation);
  }

  public List<JToolBar> getToolBars() {
    return toolBarList;
  }

  public JToolBar getToolBarCreation() {
    return toolBarCreation;
  }

  public JToggleButton getSelectionToolButton() {
    return selectionToolButton;
  }

  public JToggleButton getDragToolButton() {
    return dragToolButton;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof ResetInteractionToolCommand) {
      handleToolReset((ResetInteractionToolCommand) event);
    }
  }

  private void handleToolReset(ResetInteractionToolCommand evt) {
    selectionToolButton.setSelected(true);
  }

  /**
   * Adds the selection tool to the given toolbar.
   *
   * @param toolBar The toolbar to add to.
   * @param editor The DrawingEditor.
   */
  private JToggleButton addSelectionToolButton(JToolBar toolBar,
                                               DrawingEditor editor) {
    LinkedList<Action> drawingActions = new LinkedList<>();
    // Drawing Actions
    drawingActions.add(new SelectSameAction(editor));

    MultipleSelectionTool selectionTool
        = selectionToolFactory.createMultipleSelectionTool(drawingActions, new LinkedList<>());

    ButtonGroup buttonGroup;

    if (toolBar.getClientProperty("toolButtonGroup") instanceof ButtonGroup) {
      buttonGroup = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    }
    else {
      buttonGroup = new ButtonGroup();
      toolBar.putClientProperty("toolButtonGroup", buttonGroup);
    }

    // Selection tool
    editor.setTool(selectionTool);
    final JToggleButton toggleButton = new JToggleButton();

    if (!(toolBar.getClientProperty("toolHandler") instanceof ToolListener)) {
      ToolListener toolHandler = new ToolAdapter() {
        @Override
        public void toolDone(ToolEvent event) {
          toggleButton.setSelected(true);
        }
      };

      toolBar.putClientProperty("toolHandler", toolHandler);
    }

    toggleButton.setIcon(ImageDirectory.getImageIcon("/toolbar/select-2.png"));
    toggleButton.setText(null);
    toggleButton.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TOOLBAR_PATH)
        .getString("toolBarManager.button_selectionTool.tooltipText"));

    toggleButton.setSelected(true);
    toggleButton.addItemListener(new ToolButtonListener(selectionTool, editor));
//    toggleButton.setFocusable(false);
    buttonGroup.add(toggleButton);
    toolBar.add(toggleButton);

    return toggleButton;
  }

  /**
   *
   * @param toolBar
   * @param editor
   */
  private JToggleButton addDragToolButton(JToolBar toolBar, DrawingEditor editor) {
    final JToggleButton button = new JToggleButton();
    dragTool = new DragTool();
    editor.setTool(dragTool);

    if (!(toolBar.getClientProperty("toolHandler") instanceof ToolListener)) {
      ToolListener toolHandler = new ToolAdapter() {
        @Override
        public void toolDone(ToolEvent event) {
          button.setSelected(true);
        }
      };
      toolBar.putClientProperty("toolHandler", toolHandler);
    }

    URL url = getClass().getResource(ImageDirectory.DIR + "/toolbar/cursor-opened-hand.png");
    button.setIcon(new ImageIcon(url));
    button.setText(null);
    button.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TOOLBAR_PATH)
        .getString("toolBarManager.button_dragTool.tooltipText"));

    button.setSelected(false);
    button.addItemListener(new ToolButtonListener(dragTool, editor));
//    button.setFocusable(false);

    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    group.add(button);
    toolBar.add(button);
    return button;
  }
}
