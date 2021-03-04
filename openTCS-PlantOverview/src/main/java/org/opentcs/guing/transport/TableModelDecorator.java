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

import static java.util.Objects.requireNonNull;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * In a chain of data manipulators some behaviour is common. TableMap provides
 * most of this behaviour and can be subclassed by filters that only need to
 * override a handful of specific methods. TableMap implements TableModel by
 * routing all requests to its model, and TableModelListener by routing all
 * events to its listeners. Inserting a TableMap which has not been subclassed
 * into a chain of table filters should have no effect.
 *
 * @author Philip Milne (Sun)
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class TableModelDecorator
    extends AbstractTableModel
    implements TableModelListener {

  /**
   * The decorated TableModel.
   */
  private TableModel fModel;

  /**
   * Creates a new instance.
   */
  public TableModelDecorator() {
  }

  /**
   * Returns the decorated table model.
   *
   * @return The decorated table model.
   */
  public TableModel getModel() {
    return fModel;
  }

  /**
   * Sets the table model to be decorated.
   *
   * @param model The table model to be decorated.
   */
  public void setModel(TableModel model) {
    fModel = requireNonNull(model, "model");
    fModel.addTableModelListener(this);
  }

  @Override // TableModel
  public Object getValueAt(int aRow, int aColumn) {
    return fModel.getValueAt(aRow, aColumn);
  }

  @Override // AbstractTableModel
  public void setValueAt(Object aValue, int aRow, int aColumn) {
    fModel.setValueAt(aValue, aRow, aColumn);
  }

  @Override // TableModel
  public int getRowCount() {
    return (fModel == null) ? 0 : fModel.getRowCount();
  }

  @Override // TableModel
  public int getColumnCount() {
    return (fModel == null) ? 0 : fModel.getColumnCount();
  }

  @Override // AbstractTableModel
  public String getColumnName(int aColumn) {
    return fModel.getColumnName(aColumn);
  }

  @Override // AbstractTableModel
  public Class<?> getColumnClass(int aColumn) {
    return fModel.getColumnClass(aColumn);
  }

  @Override // AbstractTableModel
  public boolean isCellEditable(int row, int column) {
    return fModel.isCellEditable(row, column);
  }
}
