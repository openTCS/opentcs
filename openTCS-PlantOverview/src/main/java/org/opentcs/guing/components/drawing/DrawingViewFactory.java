/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JToggleButton;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for drawing views.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DrawingViewFactory {

  /**
   * A provider for drawing views.
   */
  private final Provider<OpenTCSDrawingView> drawingViewProvider;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * The status panel to display the current mouse position in.
   */
  private final StatusPanel statusPanel;
  /**
   * The manager keeping/providing the currently loaded model.
   */
  private final ModelManager modelManager;

  @Inject
  public DrawingViewFactory(Provider<OpenTCSDrawingView> drawingViewProvider,
                            OpenTCSDrawingEditor drawingEditor,
                            StatusPanel statusPanel,
                            ModelManager modelManager) {
    this.drawingViewProvider = requireNonNull(drawingViewProvider, "drawingViewProvider");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  /**
   * Creates and returns a new drawing view along with its placard panel, both
   * wrapped in a scroll pane.
   *
   * @param systemModel The system model.
   * @param selectionToolButton The selection tool button in the tool bar.
   * @param dragToolButton The drag tool button in the tool bar.
   * @param linkCreationToolButton The link creation tool button in the tool bar.
   * @param pathCreationToolButton The path creation tool button in the tool bar.
   * @return A new drawing view, wrapped in a scroll pane.
   */
  public DrawingViewScrollPane createDrawingView(SystemModel systemModel,
                                                 JToggleButton selectionToolButton,
                                                 JToggleButton dragToolButton,
                                                 JToggleButton linkCreationToolButton,
                                                 JPopupButton pathCreationToolButton) {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(selectionToolButton, "selectionToolButton");
    requireNonNull(dragToolButton, "dragToolButton");

    OpenTCSDrawingView drawingView = drawingViewProvider.get();
    drawingEditor.add(drawingView);
    drawingEditor.setActiveView(drawingView);
    for (VehicleModel vehicle : systemModel.getVehicleModels()) {
      drawingView.displayDriveOrders(vehicle, vehicle.getDisplayDriveOrders());
    }
    drawingView.setBlocks(systemModel.getMainFolder(SystemModel.FolderKey.BLOCKS));
    drawingView.setStaticRoutes(systemModel.getMainFolder(SystemModel.FolderKey.STATIC_ROUTES));

    DrawingViewPlacardPanel placardPanel = new DrawingViewPlacardPanel(drawingView);

    DrawingViewScrollPane scrollPane = new DrawingViewScrollPane(drawingView, placardPanel);
    scrollPane.originChanged(systemModel.getDrawingMethod().getOrigin());

    // --- Listens to draggings in the drawing ---
    ViewDragScrollListener dragScrollListener
        = new ViewDragScrollListener(scrollPane,
                                     placardPanel.getZoomComboBox(),
                                     selectionToolButton,
                                     dragToolButton,
                                     linkCreationToolButton,
                                     pathCreationToolButton,
                                     statusPanel,
                                     modelManager);
    drawingView.addMouseListener(dragScrollListener);
    drawingView.addMouseMotionListener(dragScrollListener);
    drawingView.addMouseWheelListener(dragScrollListener);

    return scrollPane;
  }
}
