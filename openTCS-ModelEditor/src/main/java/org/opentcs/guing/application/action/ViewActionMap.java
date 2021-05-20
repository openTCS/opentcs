/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ActionMap;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.app.AboutAction;
import org.opentcs.guing.application.action.file.LoadModelAction;
import org.opentcs.guing.application.action.file.ModelPropertiesAction;
import org.opentcs.guing.application.action.file.NewModelAction;
import org.opentcs.guing.application.action.file.SaveModelAction;
import org.opentcs.guing.application.action.file.SaveModelAsAction;
import org.opentcs.guing.application.action.synchronize.LoadModelFromKernelAction;
import org.opentcs.guing.application.action.synchronize.PersistInKernelAction;
import org.opentcs.guing.application.action.view.AddBitmapAction;
import org.opentcs.guing.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.ClearSelectionAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.CopyAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.CutAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.DeleteAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.DuplicateAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.PasteAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.SelectAllAction;
import org.opentcs.thirdparty.jhotdraw.application.action.edit.UndoRedoManager;
import org.opentcs.thirdparty.jhotdraw.application.action.file.CloseFileAction;

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
   * @param aboutAction The action to show the about window
   * @param modelPropertiesAction The action to show some model properties.
   * @param loadModelFromKernelAction The action to load the current kernel model.
   */
  @Inject
  public ViewActionMap(OpenTCSView view,
                       UndoRedoManager undoRedoManager,
                       AboutAction aboutAction,
                       ModelPropertiesAction modelPropertiesAction,
                       LoadModelFromKernelAction loadModelFromKernelAction) {
    requireNonNull(view, "view");
    requireNonNull(undoRedoManager, "undoRedoManager");
    requireNonNull(aboutAction, "aboutAction");

    // --- Menu File ---
    put(NewModelAction.ID, new NewModelAction(view));
    put(LoadModelAction.ID, new LoadModelAction(view));
    put(SaveModelAction.ID, new SaveModelAction(view));
    put(SaveModelAsAction.ID, new SaveModelAsAction(view));
    put(ModelPropertiesAction.ID, modelPropertiesAction);
    put(CloseFileAction.ID, new CloseFileAction(view));

    // --- Menu Synchronize ---
    put(PersistInKernelAction.ID, new PersistInKernelAction(view));
    put(LoadModelFromKernelAction.ID, loadModelFromKernelAction);

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

    // --- Menu View ---
    put(AddBitmapAction.ID, new AddBitmapAction(view));
    put(RestoreDockingLayoutAction.ID, new RestoreDockingLayoutAction(view));

    // --- Menu Help ---
    put(AboutAction.ID, aboutAction);
  }

}
