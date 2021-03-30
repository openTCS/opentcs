/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Eine Tabelle für Transportaufträge und Fahraufträge, die weder das Editieren
 * noch das Selektieren von Zellen erlaubt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OrdersTable
    extends JTable {

  /**
   * Creates a new instance of OrdersTable.
   *
   * @param tableModel das Tabellenmodell
   */
  public OrdersTable(TableModel tableModel) {
    super(tableModel);

    setRowSelectionAllowed(true);
    setFocusable(false);
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  @Override
  public TableCellEditor getCellEditor(int row, int column) {
    TableModel tableModel = getModel();
    Object value = tableModel.getValueAt(row, column);
    TableCellEditor editor = getDefaultEditor(value.getClass());

    return editor;
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    TableModel tableModel = getModel();
    Object value = tableModel.getValueAt(row, column);
    TableCellRenderer renderer = getDefaultRenderer(value.getClass());

    return renderer;
  }
}
