/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.event.ToolAdapter;
import org.jhotdraw.draw.event.ToolEvent;
import org.jhotdraw.draw.event.ToolListener;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateGroupAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.draw.BringToFrontAction;
import org.opentcs.guing.application.action.draw.DefaultPathSelectedAction;
import org.opentcs.guing.application.action.draw.DefaultPointSelectedAction;
import org.opentcs.guing.application.action.draw.SelectSameAction;
import org.opentcs.guing.application.action.draw.SendToBackAction;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.action.view.PauseAllVehiclesAction;
import org.opentcs.guing.application.toolbar.DragTool;
import org.opentcs.guing.application.toolbar.MultipleSelectionTool;
import org.opentcs.guing.application.toolbar.OpenTCSConnectionTool;
import org.opentcs.guing.application.toolbar.SelectionToolFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.ResetInteractionToolCommand;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.ResourceBundleUtil;
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
   * A list of all separators.
   */
  private final List<JToolBar.Separator> separatorList = new ArrayList<>();
  /**
   * A tool bar for actions creating new items.
   */
  private final JToolBar toolBarCreation = new JToolBar();
  /**
   * A tool bar for actions regarding alignment.
   */
  private final JToolBar toolBarAlignment = new JToolBar();
  /**
   * A toggle button for the selection tool.
   */
  private final JToggleButton selectionToolButton;
  /**
   * The actual selection tool.
   */
  private MultipleSelectionTool selectionTool;
  /**
   * A toggle button for the drag tool.
   */
  private final JToggleButton dragToolButton;
  /**
   * The actual drag tool.
   */
  private DragTool dragTool;
  /**
   * A button for creating points.
   * Available in modelling mode only.
   */
  private final JPopupButton buttonCreatePoint;
  /**
   * A button for creating locations.
   * Available in modelling mode only.
   */
  private final JToggleButton buttonCreateLocation;
  /**
   * A button for creating paths.
   * Available in modelling mode only.
   */
  private final JPopupButton buttonCreatePath;
  /**
   * A button for creating location links.
   * Available in modelling mode only.
   */
  private final JToggleButton buttonCreateLink;
  /**
   * A button for creating location types.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateLocationType;
  /**
   * A button for creating vehicles.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateVehicle;
  /**
   * A button for creating blocks.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateBlock;
//  /**
//   * A button for creating static routes.
//   * Available in modelling mode only.
//   */
//  private final JButton buttonCreateStaticRoute;
  /**
   * A button for creating groups.
   * Available in modelling mode.
   */
  private final JButton buttonCreateGroup;
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
   * A button for pausing/unpausing all vehicles.
   * Available in operating mode.
   */
  private final JToggleButton buttonPauseAllVehicles;

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

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

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

    // --- Create Point Figure (only in Modelling mode) ---
    LabeledPointFigure lpf = crsObjFactory.createPointFigure();
    CreationTool creationTool = new CreationTool(lpf);
    buttonCreatePoint = pointToolButton(toolBarCreation, editor, creationTool);
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Location Figure (only in Modelling mode) ---
    LabeledLocationFigure llf = crsObjFactory.createLocationFigure();
    creationTool = new CreationTool(llf);
    buttonCreateLocation = addToolButton(toolBarCreation,
                                         editor,
                                         creationTool,
                                         "openTCS.createLocation",
                                         labels);
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Path Figure (only in Modelling mode) ---
    PathConnection pc = crsObjFactory.createPathConnection();
    OpenTCSConnectionTool connectionTool = new OpenTCSConnectionTool(pc);
    buttonCreatePath = pathToolButton(toolBarCreation, editor, connectionTool);
    connectionTool.setToolDoneAfterCreation(false);

    // --- Create Link (only in Modelling mode) ---
    LinkConnection lc = crsObjFactory.createLinkConnection();
    connectionTool = new OpenTCSConnectionTool(lc);
    buttonCreateLink = addToolButton(toolBarCreation,
                                     editor,
                                     connectionTool,
                                     "openTCS.createLink",
                                     labels);
    connectionTool.setToolDoneAfterCreation(false);

    JToolBar.Separator sep = new JToolBar.Separator();
    separatorList.add(sep);
    toolBarCreation.add(sep);

    // --- Location Type: No Figure, just creates a tree entry (only in Modelling mode) ---
    buttonCreateLocationType = new JButton(actionMap.get(CreateLocationTypeAction.ID));
    labels.configureNamelessButton(buttonCreateLocationType, CreateLocationTypeAction.ID);
    toolBarCreation.add(buttonCreateLocationType);

    // --- Create Vehicle Figure (only in Modelling mode) ---
    buttonCreateVehicle = new JButton(actionMap.get(CreateVehicleAction.ID));
    labels.configureNamelessButton(buttonCreateVehicle, CreateVehicleAction.ID);
    toolBarCreation.add(buttonCreateVehicle);

    // --- Create Block (only in Modelling mode) ---
    buttonCreateBlock = new JButton(actionMap.get(CreateBlockAction.ID));
    labels.configureNamelessButton(buttonCreateBlock, CreateBlockAction.ID);
    toolBarCreation.add(buttonCreateBlock);

//    // --- Create Static Route (only in Modelling mode) ---
//    buttonCreateStaticRoute = new JButton(actionMap.get(CreateStaticRouteAction.ID));
//    labels.configureNamelessButton(buttonCreateStaticRoute, CreateStaticRouteAction.ID);
//    toolBarCreation.add(buttonCreateStaticRoute);
    // --- Create Group (both modes) ---
    buttonCreateGroup = new JButton(actionMap.get(CreateGroupAction.ID));
    labels.configureNamelessButton(buttonCreateGroup, CreateGroupAction.ID);
    toolBarCreation.add(buttonCreateGroup);

    sep = new JToolBar.Separator();
    separatorList.add(sep);
    toolBarCreation.add(sep);

    // --- Create Transport Order (only in Operating mode) ---
    buttonCreateOrder = new JButton(actionMap.get(CreateTransportOrderAction.ID));
    labels.configureNamelessButton(buttonCreateOrder, CreateTransportOrderAction.ID);
    toolBarCreation.add(buttonCreateOrder);

    toolBarCreation.addSeparator();

    // --- Find Vehicle (only in Operating mode) ---
    buttonFindVehicle = new JButton(actionMap.get(FindVehicleAction.ID));
    labels.configureNamelessButton(buttonFindVehicle, FindVehicleAction.ID);
    toolBarCreation.add(buttonFindVehicle);

    toolBarCreation.addSeparator();

    // --- Pause All Vehicles (only in Operating mode) ---
    buttonPauseAllVehicles = new JToggleButton(actionMap.get(PauseAllVehiclesAction.ID));
    labels.configureNamelessButton(buttonPauseAllVehicles, PauseAllVehiclesAction.ID);
    toolBarCreation.add(buttonPauseAllVehicles);

    toolBarCreation.setName(labels.getString("toolBarCreation.title"));
    toolBarList.add(toolBarCreation);

//    // --- 2. ToolBar: Attributes ---
//    // TODO: Diesen Toolbar "später" wieder einfügen, sobald es freie Grafikelemente im Modell gibt
//    // Pick, Apply
//    // Color: Stroke, Fill, Text
//    // Stroke: Decoration, Width, Dashes, Type, Placement, Cap, Join
//    // Font: Font; Bold, Italic, Underline
//    toolBarAttributes = new JToolBar();
//    ButtonFactory.addAttributesButtonsTo(toolBarAttributes, editor);
//    toolBarAttributes.setName(labels.getString("toolBarAttributes.title"));
//    toolBarList.add(toolBarAttributes);
    // --- 3. ToolBar: Alignment (nur im Modelling Mode) ---
    // Align: West, East, Horizontal; North, South, Vertical
    // Move: West, East, North, South
    // Bring to front, Send to back
    ButtonFactory.addAlignmentButtonsTo(toolBarAlignment, editor);
    toolBarAlignment.setName(labels.getString("toolBarAlignment.title"));
    toolBarList.add(toolBarAlignment);
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

  public JPopupButton getButtonCreatePath() {
    return buttonCreatePath;
  }

  public JToggleButton getButtonCreateLink() {
    return buttonCreateLink;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof OperationModeChangeEvent) {
      handleModeChange((OperationModeChangeEvent) event);
    }
    if (event instanceof ResetInteractionToolCommand) {
      handleToolReset((ResetInteractionToolCommand) event);
    }
  }

  private void handleModeChange(OperationModeChangeEvent evt) {
    setOperationMode(evt.getNewMode());
  }

  private void handleToolReset(ResetInteractionToolCommand evt) {
    selectionToolButton.setSelected(true);
  }

  public void setOperationMode(OperationMode mode) {
//    toolBarAttributes.setVisible(mode == OperationMode.MODELLING);
    toolBarAlignment.setVisible(mode == OperationMode.MODELLING);
    // Items in Toolbar "Create"
    buttonCreatePoint.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLocation.setVisible(mode == OperationMode.MODELLING);
    buttonCreatePath.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLink.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLocationType.setVisible(mode == OperationMode.MODELLING);
    buttonCreateVehicle.setVisible(mode == OperationMode.MODELLING);
    buttonCreateBlock.setVisible(mode == OperationMode.MODELLING);
    buttonCreateGroup.setVisible(mode == OperationMode.MODELLING);
//    buttonCreateStaticRoute.setVisible(mode == OperationMode.MODELLING);

    for (JToolBar.Separator sep : separatorList) {
      sep.setVisible(mode == OperationMode.MODELLING);
    }

    buttonCreateOrder.setEnabled(mode == OperationMode.OPERATING);
    buttonFindVehicle.setEnabled(mode == OperationMode.OPERATING);
    buttonPauseAllVehicles.setVisible(mode == OperationMode.OPERATING);
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

    // Selection Actions
    LinkedList<Action> selectionActions = new LinkedList<>();
    // 2014-04-03 HH: Group/Ungroup disabled to avoid confusion with kernel groups
//  selectionActions.add(new GroupAction(editor));
//  selectionActions.add(new UngroupAction(editor));
//  selectionActions.add(null); // separator

    selectionActions.add(new BringToFrontAction(editor));
    selectionActions.add(new SendToBackAction(editor));

    selectionTool = selectionToolFactory.createMultipleSelectionTool(drawingActions, selectionActions);

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

    ResourceBundleUtil.getBundle().configureToolBarButton(toggleButton, "openTCS.selectionTool");
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

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(button, "openTCS.dragTool");

    button.setSelected(false);
    button.addItemListener(new ToolButtonListener(dragTool, editor));
//    button.setFocusable(false);

    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    group.add(button);
    toolBar.add(button);
    return button;
  }

  /**
   * Configures a JPopupButton with all available Point types.
   *
   * @param toolBar
   * @param editor OpenTCSDrawingEditor
   * @param tool CreationTool
   * @param labelKey
   * @param labels
   * @return
   */
  private JPopupButton pointToolButton(JToolBar toolBar,
                                       DrawingEditor editor,
                                       Tool tool) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    JPopupButton popupButton = new JPopupButton();
    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    popupButton.setAction(new DefaultPointSelectedAction(editor, tool, popupButton, group),
                          new Rectangle(0, 0, 16, 16));
    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

    for (PointModel.PointType type : PointModel.PointType.values()) {
      DefaultPointSelectedAction action
          = new DefaultPointSelectedAction(editor, tool, type, popupButton, group);
      popupButton.add(action);
      action.setEnabled(true);
    }

    labels.configureNamelessButton(popupButton, "point.type." + PointModel.PointType.values()[0].name());
    popupButton.setFocusable(true);

    group.add(popupButton);
    toolBar.add(popupButton);

    return popupButton;
  }

  /**
   * Method addSelectionToolButton must have been invoked prior to this on the
   * JToolBar.
   *
   * @param toolBar
   * @param editor
   * @param tool
   * @param labelKey
   * @param labels
   * @return
   */
  private JToggleButton addToolButton(JToolBar toolBar,
                                      DrawingEditor editor,
                                      Tool tool,
                                      String labelKey,
                                      ResourceBundleUtil labels) {
    JToggleButton toggleButton = new JToggleButton();

    labels.configureToolBarButton(toggleButton, labelKey);
    toggleButton.addItemListener(new ToolButtonListener(tool, editor));
//    toggleButton.setFocusable(false);

    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    group.add(toggleButton);
    toolBar.add(toggleButton);

    return toggleButton;
  }

  /**
   * Configures a JPopupButton with all available path types.
   *
   * @param toolBar
   * @param editor
   * @param tool
   * @param labels
   * @param types
   * @return
   */
  private JPopupButton pathToolButton(JToolBar toolBar,
                                      DrawingEditor editor,
                                      Tool tool) {
    JPopupButton popupButton = new JPopupButton();
    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    popupButton.setAction(new DefaultPathSelectedAction(editor, tool, popupButton, group),
                          new Rectangle(0, 0, 16, 16));
    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

    for (PathModel.LinerType type : PathModel.LinerType.values()) {
      DefaultPathSelectedAction action
          = new DefaultPathSelectedAction(editor, tool, type, popupButton, group);
      popupButton.add(action);
      action.setEnabled(true);
    }

    ResourceBundleUtil.getBundle().configureNamelessButton(
        popupButton, "path.type." + PathModel.LinerType.values()[0].name());
    popupButton.setFocusable(true);

    group.add(popupButton);
    toolBar.add(popupButton);

    return popupButton;
  }
}
