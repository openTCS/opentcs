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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * A filter for transport orders or sequences.
 * Decorates a DefaultTableModel.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FilterTableModel
    extends TableModelDecorator {

  /**
   * A list of transport order states to be filtered out.
   */
  private final List<Object> fFilters;
  /**
   * The index of the column that the filter is to be applied on.
   */
  private int fColumnIndexToFilter;
  /**
   * The indexes of the table model.
   */
  private int[] fIndices;

  /**
   * Creates a new instance.
   *
   * @param model The decorated table model.
   */
  public FilterTableModel(DefaultTableModel model) {
    fFilters = new ArrayList<>();
    setModel(model);
  }

  /**
   * Adds a filter.
   *
   * @param filter The filter.
   */
  public void addFilter(Object filter) {
    fFilters.add(filter);
    filter();
    ((DefaultTableModel) getModel()).fireTableDataChanged();
  }

  /**
   * Removes a filter.
   *
   * @param filter The filter.
   */
  public void removeFilter(Object filter) {
    fFilters.remove(filter);
    filter();
    ((DefaultTableModel) getModel()).fireTableDataChanged();
  }

  /**
   * Checks whether the given filter is set or not.
   *
   * @param filter The filter to be checked for.
   * @return <code>true</code> if, and only if, the given filter is set.
   */
  public boolean isFilterSet(Object filter) {
    return fFilters.contains(filter);
  }

  /**
   * Set the index of the column that the filter is to be applied on.
   *
   * @param index The index.
   */
  public void setColumnIndexToFilter(int index) {
    fColumnIndexToFilter = index;
  }

  /**
   * Filters the transport orders.
   */
  private void filter() {
    int rowCount = getModel().getRowCount();
    int[] help = new int[rowCount];
    int counter = 0;

    for (int i = 0; i < rowCount; i++) {
      Object value = getModel().getValueAt(i, fColumnIndexToFilter);

      if (!isFilterSet(value)) {
        help[counter] = i;
        counter++;
      }
    }

    fIndices = new int[counter];
    System.arraycopy(help, 0, fIndices, 0, counter);

    ((DefaultTableModel) getModel()).fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    if (fIndices != null) {
      return fIndices.length;
    }
    else {
      return getModel().getRowCount();
    }
  }

  /**
   * Sets the number of rows.
   *
   * @param rowCount The new number of rows.
   */
  public void setRowCount(int rowCount) {
    ((DefaultTableModel) getModel()).setRowCount(rowCount);
    filter();
  }

  /**
   * Adds a row.
   *
   * @param rowData The row to be added.
   */
  public void addRow(Vector<?> rowData) {
    ((DefaultTableModel) getModel()).addRow(rowData);
    filter();
  }

  /**
   * Removes the row with the given index.
   *
   * @param index The index.
   */
  public void removeRow(int index) {
    ((DefaultTableModel) getModel()).removeRow(index);
    filter();
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    getModel().setValueAt(value, row, column);

    if (column == fColumnIndexToFilter) {
      filter();
    }
  }

  @Override
  public Object getValueAt(int row, int column) {
    return getModel().getValueAt(fIndices[row], column);
  }

  /**
   * Adds a row at the given index.
   *
   * @param row The index.
   * @param rowData The row to be added.
   */
  public void insertRow(int row, Vector<?> rowData) {
    ((DefaultTableModel) getModel()).insertRow(row, rowData);
    filter();
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    fireTableChanged(new TableModelEvent(this, e.getFirstRow(), e.getLastRow(), e.getColumn(), e.getType()));
  }

  /**
   * Returns, for the given index from the table view, the correct index in the
   * model.
   *
   * @param rowIndex The index from the table view.
   * @return The correct index in the model.
   */
  public int realRowIndex(int rowIndex) {
    return fIndices[rowIndex];
  }
}
