/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.action;

import java.awt.Rectangle;
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
import org.jhotdraw.draw.tool.ConnectionTool;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.application.action.ToolButtonListener;
import org.opentcs.guing.common.application.toolbar.DragTool;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.common.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.common.components.drawing.figures.LinkConnection;
import org.opentcs.guing.common.components.drawing.figures.PathConnection;
import org.opentcs.guing.common.event.ResetInteractionToolCommand;
import org.opentcs.guing.common.util.CourseObjectFactory;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.modeleditor.application.action.actions.CreateBlockAction;
import org.opentcs.modeleditor.application.action.actions.CreateLocationTypeAction;
import org.opentcs.modeleditor.application.action.actions.CreateVehicleAction;
import org.opentcs.modeleditor.application.action.draw.DefaultPointSelectedAction;
import org.opentcs.modeleditor.application.toolbar.CreationToolFactory;
import org.opentcs.modeleditor.application.toolbar.MultipleSelectionTool;
import org.opentcs.modeleditor.application.toolbar.SelectionToolFactory;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.draw.SelectSameAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.ButtonFactory;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.draw.DefaultPathSelectedAction;
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
   * A tool bar for actions regarding alignment.
   */
  private final JToolBar toolBarAlignment = new JToolBar();
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
   * A button for creating points.
   */
  private final JPopupButton buttonCreatePoint;
  /**
   * A button for creating locations.
   */
  private final JToggleButton buttonCreateLocation;
  /**
   * A button for creating paths.
   */
  private final JPopupButton buttonCreatePath;
  /**
   * A button for creating location links.
   */
  private final JToggleButton buttonCreateLink;
  /**
   * A button for creating location types.
   */
  private final JButton buttonCreateLocationType;
  /**
   * A button for creating vehicles.
   */
  private final JButton buttonCreateVehicle;
  /**
   * A button for creating blocks.
   */
  private final JButton buttonCreateBlock;

  /**
   * Creates a new instance.
   *
   * @param actionMap The action map to be used
   * @param crsObjFactory A factory for course objects
   * @param editor The drawing editor
   * @param creationToolFactory The creation tool factory.
   * @param selectionToolFactory The selection tool factory
   */
  @Inject
  public ToolBarManager(ViewActionMap actionMap,
                        CourseObjectFactory crsObjFactory,
                        OpenTCSDrawingEditor editor,
                        CreationToolFactory creationToolFactory,
                        SelectionToolFactory selectionToolFactory) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(crsObjFactory, "crsObjFactory");
    requireNonNull(editor, "editor");
    requireNonNull(creationToolFactory, "creationToolFactory");
    this.selectionToolFactory = requireNonNull(selectionToolFactory, "selectionToolFactory");

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.TOOLBAR_PATH);

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

    // --- Create Point Figure ---
    LabeledPointFigure lpf = crsObjFactory.createPointFigure();
    CreationTool creationTool = creationToolFactory.createCreationTool(lpf);
    buttonCreatePoint = pointToolButton(toolBarCreation, editor, creationTool);
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Location Figure ---
    LabeledLocationFigure llf = crsObjFactory.createLocationFigure();
    creationTool = creationToolFactory.createCreationTool(llf);
    buttonCreateLocation = addToolButton(toolBarCreation,
                                         editor,
                                         creationTool,
                                         labels.getString("toolBarManager.button_createLocation.tooltipText"),
                                         ImageDirectory.getImageIcon("/toolbar/location.22.png"));
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Path Figure ---
    PathConnection pc = crsObjFactory.createPathConnection();
    ConnectionTool connectionTool = creationToolFactory.createConnectionTool(pc);
    buttonCreatePath = pathToolButton(toolBarCreation, editor, connectionTool);
    connectionTool.setToolDoneAfterCreation(false);

    // --- Create Link ---
    LinkConnection lc = crsObjFactory.createLinkConnection();
    connectionTool = creationToolFactory.createConnectionTool(lc);
    buttonCreateLink = addToolButton(toolBarCreation,
                                     editor,
                                     connectionTool,
                                     labels.getString("toolBarManager.button_createLink.tooltipText"),
                                     ImageDirectory.getImageIcon("/toolbar/link.22.png"));
    connectionTool.setToolDoneAfterCreation(false);

    toolBarCreation.addSeparator();

    // --- Location Type: No Figure, just creates a tree entry ---
    buttonCreateLocationType = new JButton(actionMap.get(CreateLocationTypeAction.ID));
    buttonCreateLocationType.setText(null);
    toolBarCreation.add(buttonCreateLocationType);

    // --- Create Vehicle Figure ---
    buttonCreateVehicle = new JButton(actionMap.get(CreateVehicleAction.ID));
    buttonCreateVehicle.setText(null);
    toolBarCreation.add(buttonCreateVehicle);

    // --- Create Block ---
    buttonCreateBlock = new JButton(actionMap.get(CreateBlockAction.ID));
    buttonCreateBlock.setText(null);
    toolBarCreation.add(buttonCreateBlock);

    toolBarCreation.addSeparator();

    toolBarCreation.setName(labels.getString("toolBarManager.toolbar_drawing.title"));
    toolBarList.add(toolBarCreation);

    // --- 3. ToolBar: Alignment ---
    // Align: West, East, Horizontal; North, South, Vertical
    // Move: West, East, North, South
    // Bring to front, Send to back
    ButtonFactory.addAlignmentButtonsTo(toolBarAlignment, editor);
    toolBarAlignment.setName(labels.getString("toolBarManager.toolbar_alignment.title"));
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
    toggleButton.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.TOOLBAR_PATH)
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
    button.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.TOOLBAR_PATH)
        .getString("toolBarManager.button_dragTool.tooltipText"));

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
    JPopupButton popupButton = new JPopupButton();
    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    popupButton.setAction(new DefaultPointSelectedAction(editor, tool, popupButton, group),
                          new Rectangle(0, 0, 16, 16));
    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

    for (PointModel.Type type : PointModel.Type.values()) {
      DefaultPointSelectedAction action
          = new DefaultPointSelectedAction(editor, tool, type, popupButton, group);
      popupButton.add(action);
      action.setEnabled(true);
    }

    popupButton.setText(null);
    popupButton.setToolTipText(PointModel.Type.values()[0].getHelptext());
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/point-halt-arrow.22.png"));
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
   * @param toolTipText
   * @param labels
   * @return
   */
  private JToggleButton addToolButton(JToolBar toolBar,
                                      DrawingEditor editor,
                                      Tool tool,
                                      String toolTipText,
                                      ImageIcon iconBase) {
    JToggleButton toggleButton = new JToggleButton();

    toggleButton.setIcon(iconBase);
    toggleButton.setText(null);
    toggleButton.setToolTipText(toolTipText);
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

    for (PathModel.Type type : PathModel.Type.values()) {
      DefaultPathSelectedAction action
          = new DefaultPathSelectedAction(editor, tool, type, popupButton, group);
      popupButton.add(action);
      action.setEnabled(true);
    }

    popupButton.setText(null);
    popupButton.setToolTipText(PathModel.Type.values()[0].getHelptext());
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/path-direct-arrow.22.png"));
    popupButton.setFocusable(true);

    group.add(popupButton);
    toolBar.add(popupButton);

    return popupButton;
  }
}
