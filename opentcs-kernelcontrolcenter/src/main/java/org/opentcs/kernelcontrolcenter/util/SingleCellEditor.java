// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernelcontrolcenter.util;

import static org.opentcs.kernelcontrolcenter.I18nKernelControlCenter.BUNDLE_PATH;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 * A cell editor for maintaining different editors in one column.
 */
public final class SingleCellEditor
    implements
      TableCellEditor {

  /**
   * The TableCellEditors for every cell.
   */
  private final Map<Integer, TableCellEditor> editors;
  /**
   * The current editor.
   */
  private TableCellEditor editor;
  /**
   * The default editor.
   */
  private final TableCellEditor defaultEditor;
  /**
   * The table associated with the editors.
   */
  private final JTable table;
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Constructs a SingleCellEditor.
   *
   * @param table The JTable associated
   */
  public SingleCellEditor(JTable table) {
    this.table = table;
    editors = new HashMap<>();
    defaultEditor = new DefaultCellEditor(new JTextField());
  }

  /**
   * Assigns an editor to a row.
   *
   * @param row table row
   * @param rowEditor table cell editor
   */
  public void setEditorAt(int row, TableCellEditor rowEditor) {
    editors.put(row, rowEditor);
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable whichTable,
      Object value,
      boolean isSelected,
      int row,
      int column
  ) {
    return editor.getTableCellEditorComponent(
        whichTable,
        value,
        isSelected,
        row,
        column
    );
  }

  @Override
  public Object getCellEditorValue() {
    return editor.getCellEditorValue();
  }

  @Override
  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }

  @Override
  public boolean isCellEditable(EventObject anEvent) {
    if (anEvent instanceof KeyEvent) {
      return false;
    }
    selectEditor((MouseEvent) anEvent);
    return editor.isCellEditable(anEvent);
  }

  @Override
  public void addCellEditorListener(CellEditorListener l) {
    editor.addCellEditorListener(l);
  }

  @Override
  public void removeCellEditorListener(CellEditorListener l) {
    editor.removeCellEditorListener(l);
  }

  @Override
  public boolean shouldSelectCell(EventObject anEvent) {
    selectEditor((MouseEvent) anEvent);
    return editor.shouldSelectCell(anEvent);
  }

  /**
   * Sets the current editor.
   *
   * @param e A MouseEvent
   */
  public void selectEditor(MouseEvent e) {
    int row;
    if (e == null) {
      row = table.getSelectionModel().getAnchorSelectionIndex();
    }
    else {
      row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
    }
    editor = editors.get(row);
    if (editor == null) {
      editor = defaultEditor;
    }
    table.changeSelection(row, table.getColumn(BUNDLE.getString("vehicleTableModel.column_adapter.headerText")).getModelIndex(), false, false);
  }
}
