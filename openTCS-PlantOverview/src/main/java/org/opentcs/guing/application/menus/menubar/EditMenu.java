/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JMenu;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.edit.ClearSelectionAction;
import org.opentcs.guing.application.action.edit.CopyAction;
import org.opentcs.guing.application.action.edit.CutAction;
import org.opentcs.guing.application.action.edit.DeleteAction;
import org.opentcs.guing.application.action.edit.DuplicateAction;
import org.opentcs.guing.application.action.edit.PasteAction;
import org.opentcs.guing.application.action.edit.SelectAllAction;
import org.opentcs.guing.application.action.edit.UndoRedoManager;

/**
 * The application's "Edit" menu.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EditMenu
    extends JMenu {

//  private final JMenuItem menuItemCopy;
//  private final JMenuItem menuItemCut;
//  private final JMenuItem menuItemDuplicate;
//  private final JMenuItem menuItemPaste;
  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   */
  @Inject
  public EditMenu(ViewActionMap actionMap) {
    requireNonNull(actionMap, "actionMap");

    // Undo, Redo
    add(actionMap.get(UndoRedoManager.UNDO_ACTION_ID));
    add(actionMap.get(UndoRedoManager.REDO_ACTION_ID));
    addSeparator();
    // Cut, Copy, Paste, Duplicate
//    menuItemCut = menuEdit.add(actionMap.get(CutAction.ID));
//    menuItemCopy = menuEdit.add(actionMap.get(CopyAction.ID));
//    menuItemPaste = menuEdit.add(actionMap.get(PasteAction.ID));
//    menuItemDuplicate = menuEdit.add(actionMap.get(DuplicateAction.ID));
    // Delete
    add(actionMap.get(DeleteAction.ID));
    add(actionMap.get(CopyAction.ID));
    add(actionMap.get(PasteAction.ID));
    add(actionMap.get(DuplicateAction.ID));
    add(actionMap.get(CutAction.ID));
    addSeparator();
    // Select all, Clear selection
    add(actionMap.get(SelectAllAction.ID));
    add(actionMap.get(ClearSelectionAction.ID));
  }

  /**
   * Updates the menu's items for the given mode of operation.
   *
   * @param mode The new mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    setEnabled(mode == OperationMode.MODELLING);
  }

}
