/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateGroupAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateStaticRouteAction;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.app.AboutAction;
import org.opentcs.guing.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.application.action.course.DispatchVehicleAction;
import org.opentcs.guing.application.action.edit.ClearSelectionAction;
import org.opentcs.guing.application.action.edit.CopyAction;
import org.opentcs.guing.application.action.edit.CutAction;
import org.opentcs.guing.application.action.edit.DeleteAction;
import org.opentcs.guing.application.action.edit.DuplicateAction;
import org.opentcs.guing.application.action.edit.PasteAction;
import org.opentcs.guing.application.action.edit.SelectAllAction;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.action.file.LoadModelAction;
import org.opentcs.guing.application.action.file.NewModelAction;
import org.opentcs.guing.application.action.file.SaveModelAction;
import org.opentcs.guing.application.action.file.SaveModelAsAction;
import org.opentcs.guing.application.action.synchronize.LoadModelFromKernelAction;
import org.opentcs.guing.application.action.synchronize.PersistInKernelAction;
import org.opentcs.guing.application.action.synchronize.SwitchToModellingAction;
import org.opentcs.guing.application.action.synchronize.SwitchToOperatingAction;
import org.opentcs.guing.application.action.view.AddBitmapAction;
import org.opentcs.guing.application.action.view.AddDrawingViewAction;
import org.opentcs.guing.application.action.view.AddTransportOrderSequenceView;
import org.opentcs.guing.application.action.view.AddTransportOrderView;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.action.view.PauseAllVehiclesAction;
import org.opentcs.guing.application.action.view.RestoreDockingLayoutAction;

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
   * @param dispatchAction The action to trigger a dispatcher run
   * @param findVehicleAction The action to find vehicles
   * @param pauseAllVehiclesAction The action to pause all vehicles
   * @param createGroupAction The action to create a group
   * @param aboutAction The action to show the about window
   * @param modellingAction The action to switch to modelling mode.
   * @param operatingingAction The action to switch to operating mode.
   */
  @Inject
  public ViewActionMap(OpenTCSView view,
                       UndoRedoManager undoRedoManager,
                       ActionFactory actionFactory,
                       CreateTransportOrderAction createTransportOrderAction,
                       DispatchVehicleAction dispatchAction,
                       FindVehicleAction findVehicleAction,
                       PauseAllVehiclesAction pauseAllVehiclesAction,
                       CreateGroupAction createGroupAction,
                       AboutAction aboutAction,
                       SwitchToModellingAction modellingAction,
                       SwitchToOperatingAction operatingingAction,
                       ModelPropertiesAction modelPropertiesAction) {
    requireNonNull(view, "view");
    requireNonNull(undoRedoManager, "undoRedoManager");
    requireNonNull(actionFactory, "actionFactory");
    requireNonNull(createTransportOrderAction, "createTransportOrderAction");
    requireNonNull(dispatchAction, "dispatchAction");
    requireNonNull(findVehicleAction, "findVehicleAction");
    requireNonNull(pauseAllVehiclesAction, "pauseAllVehiclesAction");
    requireNonNull(createGroupAction, "createGroupAction");
    requireNonNull(aboutAction, "aboutAction");
    requireNonNull(modellingAction, "modellingAction");
    requireNonNull(operatingingAction, "operatingingAction");

    // --- Menu File ---
    put(NewModelAction.ID, new NewModelAction(view));
    put(LoadModelAction.ID, new LoadModelAction(view));
    put(SaveModelAction.ID, new SaveModelAction(view));
    put(SaveModelAsAction.ID, new SaveModelAsAction(view));
    put(ModelPropertiesAction.ID, modelPropertiesAction);
    put(CloseFileAction.ID, new CloseFileAction(view));

    // --- Menu Synchronize ---
    put(PersistInKernelAction.ID, new PersistInKernelAction(view));
    put(LoadModelFromKernelAction.ID, new LoadModelFromKernelAction(view));
    put(SwitchToModellingAction.ID, modellingAction);
    put(SwitchToOperatingAction.ID, operatingingAction);

    // --- Menu Edit ---
    // Undo, Redo
    put(UndoRedoManager.UNDO_ACTION_ID, undoRedoManager.getUndoAction());
    put(UndoRedoManager.REDO_ACTION_ID, undoRedoManager.getRedoAction());
    // Cut, Copy, Paste, Duplicate, Delete
    put(CutAction.ID, new CutAction());
    put(CopyAction.ID, new CopyAction());
    put(PasteAction.ID, new PasteAction());
    put(DuplicateAction.ID, new DuplicateAction());
    put(DeleteAction.ID, new DeleteAction());
    // Select all, Clear selection
    put(SelectAllAction.ID, new SelectAllAction());
    put(ClearSelectionAction.ID, new ClearSelectionAction());

    // --- Menu Actions ---
    // Menu item Actions -> Create ...
    put(CreateLocationTypeAction.ID, new CreateLocationTypeAction(view));
    put(CreateVehicleAction.ID, new CreateVehicleAction(view));
    put(CreateBlockAction.ID, new CreateBlockAction(view));
    put(CreateStaticRouteAction.ID, new CreateStaticRouteAction(view));
    put(CreateTransportOrderAction.ID, createTransportOrderAction);
    put(DispatchVehicleAction.ID, dispatchAction);

    Action action;
    // --- Menu View ---
    // Menu View -> Add drawing view
    action = new AddDrawingViewAction(view);
    put(AddDrawingViewAction.ID, action);
    action.putValue(Action.NAME, AddDrawingViewAction.ID);

    // Menu View -> Add transport order view
    action = new AddTransportOrderView(view);
    put(AddTransportOrderView.ID, action);
    action.putValue(Action.NAME, AddTransportOrderView.ID);

    // Menu View -> Add transport order sequence view
    action = new AddTransportOrderSequenceView(view);
    put(AddTransportOrderSequenceView.ID, action);
    action.putValue(Action.NAME, AddTransportOrderSequenceView.ID);

    put(AddBitmapAction.ID, new AddBitmapAction(view));
    put(RestoreDockingLayoutAction.ID, new RestoreDockingLayoutAction(view));

    put(FindVehicleAction.ID, findVehicleAction);
    put(PauseAllVehiclesAction.ID, pauseAllVehiclesAction);
    put(CreateGroupAction.ID, createGroupAction);

    // --- Menu Help ---
    put(AboutAction.ID, aboutAction);
  }

}
