/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 * A cell editor for maintaining different editors in one column.
 * @author Philipp Seifert (Fraunhofer IML)
 */
final class SingleCellEditor
    implements TableCellEditor {

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
    editors.put(new Integer(row), rowEditor);
  }

  @Override
  public Component getTableCellEditorComponent(JTable whichTable,
                                               Object value,
                                               boolean isSelected,
                                               int row,
                                               int column) {
    return editor.getTableCellEditorComponent(whichTable,
                                              value,
                                              isSelected,
                                              row,
                                              column);
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
      row = table.rowAtPoint(e.getPoint());
    }
    editor = editors.get(new Integer(row));
    if (editor == null) {
      editor = defaultEditor;
    }
    table.changeSelection(row, table.getColumn("Adapter").getModelIndex(), false, false);
  }
}
