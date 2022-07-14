/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.layer;

import javax.swing.SwingUtilities;
import org.opentcs.guing.common.components.layer.AbstractLayerGroupsTableModel;
import org.opentcs.guing.common.components.layer.LayerGroupEditor;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * The table model for layer groups for the Operations Desk application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class LayerGroupsTableModel
    extends AbstractLayerGroupsTableModel {

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager.
   * @param layerGroupEditor The layer group editor.
   */
  public LayerGroupsTableModel(ModelManager modelManager, LayerGroupEditor layerGroupEditor) {
    super(modelManager, layerGroupEditor);
  }

  @Override
  protected boolean isNameColumnEditable() {
    return false;
  }

  @Override
  protected boolean isVisibleColumnEditable() {
    return true;
  }

  @Override
  public void groupsInitialized() {
    // Once the layers are initialized we want to redraw the entire table to avoid any 
    // display errors.
    executeOnEventDispatcherThread(() -> fireTableDataChanged());
  }

  @Override
  public void groupsChanged() {
    // Update the entire table but don't use fireTableDataChanged() to preserve the current 
    // selection.
    executeOnEventDispatcherThread(() -> fireTableRowsUpdated(0, getRowCount() - 1));
  }

  @Override
  public void groupAdded() {
  }

  @Override
  public void groupRemoved() {
  }

  /**
   * Ensures the given runnable is executed on the EDT.
   * If the runnable is already being called on the EDT, the runnable is executed immediately.
   * Otherwise it is scheduled for execution on the EDT.
   * <p>
   * Note: Deferring a runnable by scheduling it for execution on the EDT even though it would 
   * have already been executed on the EDT may lead to exceptions due to data inconsistency.
   * </p>
   *
   * @param runnable The runnable.
   */
  private void executeOnEventDispatcherThread(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }
}
