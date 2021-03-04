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
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateGroupAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateStaticRouteAction;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.app.AboutAction;
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
import org.opentcs.guing.application.action.view.AddBitmapAction;
import org.opentcs.guing.application.action.view.AddDrawingViewAction;
import org.opentcs.guing.application.action.view.AddTransportOrderSequenceView;
import org.opentcs.guing.application.action.view.AddTransportOrderView;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.action.view.LocationThemeAction;
import org.opentcs.guing.application.action.view.PauseAllVehiclesAction;
import org.opentcs.guing.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.guing.application.action.view.VehicleThemeAction;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.VehicleThemeManager;

/**
 * A custom ActionMap for the plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewActionMap
    extends ActionMap {

  @Inject
  public ViewActionMap(OpenTCSView view,
                       UndoRedoManager undoRedoManager,
                       LocationThemeManager locationThemeManager,
                       VehicleThemeManager vehicleThemeManager,
                       ActionFactory actionFactory,
                       CreateTransportOrderAction createTransportOrderAction,
                       FindVehicleAction findVehicleAction,
                       PauseAllVehiclesAction pauseAllVehiclesAction,
                       CreateGroupAction createGroupAction,
                       AboutAction aboutAction) {
    requireNonNull(view, "view");
    requireNonNull(undoRedoManager, "undoRedoManager");
    requireNonNull(locationThemeManager, "locationThemeManager");
    requireNonNull(vehicleThemeManager, "vehicleThemeManager");
    requireNonNull(actionFactory, "actionFactory");
    requireNonNull(createTransportOrderAction, "createTransportOrderAction");
    requireNonNull(findVehicleAction, "findVehicleAction");
    requireNonNull(pauseAllVehiclesAction, "pauseAllVehiclesAction");
    requireNonNull(createGroupAction, "createGroupAction");
    requireNonNull(aboutAction, "aboutAction");

    // --- Menu File ---
    put(NewModelAction.ID, new NewModelAction(view));
    put(LoadModelAction.ID, new LoadModelAction(view));
    put(SaveModelAction.ID, new SaveModelAction(view));
    put(SaveModelAsAction.ID, new SaveModelAsAction(view));
    put(CloseFileAction.ID, new CloseFileAction(view));

    // --- Menu Synchronize ---
    put(PersistInKernelAction.ID, new PersistInKernelAction(view));
    put(LoadModelFromKernelAction.ID, new LoadModelFromKernelAction(view));

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

    // Menu View -> Location theme
    action = actionFactory.createLocationThemeAction(null);
    put(LocationThemeAction.ID, action);
    action.putValue(Action.NAME, LocationThemeAction.UNDEFINED);

    for (LocationTheme curTheme : locationThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionFactory.createLocationThemeAction(curTheme);
      put(id, action);
      action.putValue(Action.NAME, id);
    }

    // Menu View -> Vehicle theme
    action = actionFactory.createVehicleThemeAction(null);
    put(VehicleThemeAction.ID, action);
    action.putValue(Action.NAME, VehicleThemeAction.UNDEFINED);

    for (VehicleTheme curTheme : vehicleThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionFactory.createVehicleThemeAction(curTheme);
      put(id, action);
      action.putValue(Action.NAME, id);
    }

    put(AddBitmapAction.ID, new AddBitmapAction(view));
    put(RestoreDockingLayoutAction.ID, new RestoreDockingLayoutAction(view));

    put(FindVehicleAction.ID, findVehicleAction);
    put(PauseAllVehiclesAction.ID, pauseAllVehiclesAction);
    put(CreateGroupAction.ID, createGroupAction);

    // --- Menu Help ---
    put(AboutAction.ID, aboutAction);
  }

}
