/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.common.components.dialogs.DetailsDialog;
import org.opentcs.guing.common.components.properties.PropertyUndoActivity;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;

/**
 * A cell editor wrapped in an undo manager.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class UndoableCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor {

  /**
   * The undo manager.
   */
  protected UndoRedoManager fUndoRedoManager;
  /**
   * The actual cell editor.
   */
  protected TableCellEditor fWrappedCellEditor;
  /**
   * The undo activity.
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
   * Set the undo manager.
   *
   * @param undoManager the undo manager.
   */
  public void setUndoManager(UndoRedoManager undoManager) {
    fUndoRedoManager = undoManager;
  }

  /**
   * Sets the details dialog that is used to edit the property.
   *
   * @param detailsDialog the details dialog that is used to edit the property.
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
   * Returns the actual cell editor.
   *
   * @return the actual cell editor.
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
