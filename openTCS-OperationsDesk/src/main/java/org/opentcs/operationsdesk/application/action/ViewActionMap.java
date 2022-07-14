/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ActionMap;
import org.opentcs.guing.common.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.common.application.action.file.SaveModelAction;
import org.opentcs.guing.common.application.action.file.SaveModelAsAction;
import org.opentcs.operationsdesk.application.OpenTCSView;
import org.opentcs.operationsdesk.application.action.actions.CreatePeripheralJobAction;
import org.opentcs.operationsdesk.application.action.actions.CreateTransportOrderAction;
import org.opentcs.operationsdesk.application.action.actions.FindVehicleAction;
import org.opentcs.operationsdesk.application.action.actions.PauseAllVehiclesAction;
import org.opentcs.operationsdesk.application.action.actions.ResumeAllVehiclesAction;
import org.opentcs.operationsdesk.application.action.app.AboutAction;
import org.opentcs.operationsdesk.application.action.view.AddDrawingViewAction;
import org.opentcs.operationsdesk.application.action.view.AddTransportOrderSequenceViewAction;
import org.opentcs.operationsdesk.application.action.view.AddTransportOrderViewAction;
import org.opentcs.operationsdesk.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.DeleteAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.SelectAllAction;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;
import org.opentcs.thirdparty.operationsdesk.jhotdraw.application.action.file.CloseFileAction;

/**
 * A custom ActionMap for the plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewActionMap
    extends ActionMap {

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   * @param undoRedoManager The undo redo manager
   * @param actionFactory The action factory
   * @param createTransportOrderAction The action to create transport orders
   * @param findVehicleAction The action to find vehicles
   * @param pauseAllVehiclesAction The action to pause all vehicles
   * @param resumeAllVehiclesAction The action to resume all vehicles
   * @param aboutAction The action to show the about window
   * @param modelPropertiesAction The action to show some model properties.
   * @param createPeripheralJobAction The action to create peripheral jobs.
   */
  @Inject
  public ViewActionMap(OpenTCSView view,
                       UndoRedoManager undoRedoManager,
                       ActionFactory actionFactory,
                       CreateTransportOrderAction createTransportOrderAction,
                       FindVehicleAction findVehicleAction,
                       PauseAllVehiclesAction pauseAllVehiclesAction,
                       ResumeAllVehiclesAction resumeAllVehiclesAction,
                       AboutAction aboutAction,
                       ModelPropertiesAction modelPropertiesAction,
                       CreatePeripheralJobAction createPeripheralJobAction) {
    requireNonNull(view, "view");
    requireNonNull(undoRedoManager, "undoRedoManager");
    requireNonNull(actionFactory, "actionFactory");
    requireNonNull(createTransportOrderAction, "createTransportOrderAction");
    requireNonNull(findVehicleAction, "findVehicleAction");
    requireNonNull(pauseAllVehiclesAction, "pauseAllVehiclesAction");
    requireNonNull(resumeAllVehiclesAction, "resumeAllVehiclesAction");
    requireNonNull(aboutAction, "aboutAction");
    requireNonNull(createPeripheralJobAction, "createPeripheralJobAction");

    // --- Menu File ---
    put(SaveModelAction.ID, new SaveModelAction(view));
    put(SaveModelAsAction.ID, new SaveModelAsAction(view));
    put(ModelPropertiesAction.ID, modelPropertiesAction);
    put(CloseFileAction.ID, new CloseFileAction(view));

    // --- Menu Edit ---
    // Undo, Redo
    put(UndoRedoManager.UNDO_ACTION_ID, undoRedoManager.getUndoAction());
    put(UndoRedoManager.REDO_ACTION_ID, undoRedoManager.getRedoAction());
    // Cut, Copy, Paste, Duplicate, Delete
    put(DeleteAction.ID, new DeleteAction());
    // Select all
    put(SelectAllAction.ID, new SelectAllAction());

    // --- Menu Actions ---
    // Menu item Actions -> Create ...
    put(CreateTransportOrderAction.ID, createTransportOrderAction);
    put(CreatePeripheralJobAction.ID, createPeripheralJobAction);

    // --- Menu View ---
    // Menu View -> Add drawing view
    put(AddDrawingViewAction.ID, new AddDrawingViewAction(view));

    // Menu View -> Add transport order view
    put(AddTransportOrderViewAction.ID, new AddTransportOrderViewAction(view));

    // Menu View -> Add transport order sequence view
    put(AddTransportOrderSequenceViewAction.ID, new AddTransportOrderSequenceViewAction(view));

    put(RestoreDockingLayoutAction.ID, new RestoreDockingLayoutAction(view));

    put(FindVehicleAction.ID, findVehicleAction);
    put(PauseAllVehiclesAction.ID, pauseAllVehiclesAction);
    put(ResumeAllVehiclesAction.ID, resumeAllVehiclesAction);

    // --- Menu Help ---
    put(AboutAction.ID, aboutAction);
  }

}
