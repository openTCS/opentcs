/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.table;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.components.dialogs.DetailsDialog;
import org.opentcs.guing.components.properties.PropertyUndoActivity;
import org.opentcs.guing.components.properties.type.Property;

/**
 * Ein Undo-Umwickler für CellEditoren.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class UndoableCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor {

  /**
   * Der Undo-Manager.
   */
  protected UndoRedoManager fUndoRedoManager;
  /**
   * Der eigentliche CellEditor.
   */
  protected TableCellEditor fWrappedCellEditor;
  /**
   * Das Undo.
   */
  protected PropertyUndoActivity fUndoActivity;

  /**
   * Creates a new instance of UndoableCellEditor
   *
   * @param cellEditor
   */
  public UndoableCellEditor(TableCellEditor cellEditor) {
    super();
    fWrappedCellEditor = cellEditor;
  }

  /**
   * Setzt den Undo-Manager.
   *
   * @param undoManager
   */
  public void setUndoManager(UndoRedoManager undoManager) {
    fUndoRedoManager = undoManager;
  }

  /**
   * Setzt den Dialog, mit dessen Hilfe der Eigenschaftswert komfortabel
   * bearbeitet werden kann.
   *
   * @param detailsDialog
   */
  public void setDetailsDialog(DetailsDialog detailsDialog) {
    if (fWrappedCellEditor instanceof AbstractPropertyCellEditor) {
      ((AbstractPropertyCellEditor) fWrappedCellEditor).setDetailsDialog(detailsDialog);
    }
  }

  /**
   * Sets the focus to the actual component (JTextField etc.).
   */
  public void setFocusToComponent() {
    if (fWrappedCellEditor instanceof AbstractPropertyCellEditor) {
      ((AbstractPropertyCellEditor) fWrappedCellEditor).setFocusToComponent();
    }
  }

  /**
   *
   * @return
   */
  public TableCellEditor getWrappedCellEditor() {
    return fWrappedCellEditor;
  }

  @Override
  public Object getCellEditorValue() {
    Property property = (Property) fWrappedCellEditor.getCellEditorValue();
    fUndoActivity.snapShotAfterModification();
    fUndoRedoManager.addEdit(fUndoActivity);

    return property;
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    Property property = (Property) value;
    fUndoActivity = new PropertyUndoActivity(property);
    fUndoActivity.snapShotBeforeModification();

    return fWrappedCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
  }

  @Override
  public void addCellEditorListener(CellEditorListener l) {
    fWrappedCellEditor.addCellEditorListener(l);
  }

  @Override
  public void removeCellEditorListener(CellEditorListener l) {
    fWrappedCellEditor.removeCellEditorListener(l);
  }

  @Override
  public boolean isCellEditable(EventObject anEvent) {
    return fWrappedCellEditor.isCellEditable(anEvent);
  }

  @Override
  public boolean shouldSelectCell(EventObject anEvent) {
    return fWrappedCellEditor.shouldSelectCell(anEvent);
  }

  @Override
  public boolean stopCellEditing() {
    return fWrappedCellEditor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    fWrappedCellEditor.cancelCellEditing();
  }
}
