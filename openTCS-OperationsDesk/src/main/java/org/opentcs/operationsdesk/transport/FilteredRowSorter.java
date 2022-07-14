/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A table model sorter that implements some convinience methods for easier filtering.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @param <T> The table model class
 */
public class FilteredRowSorter<T extends TableModel>
    extends TableRowSorter<T> {

  /**
   * Keeps all the active filters.
   */
  private final List<RowFilter<Object, Object>> filters = new LinkedList<>();

  public FilteredRowSorter(T model) {
    super(model);
  }

  /**
   * Add a new filter to the sorter.
   *
   * @param filter Filter to add.
   */
  public void addRowFilter(RowFilter<Object, Object> filter) {
    requireNonNull(filter, "filter");
    filters.add(filter);
    setRowFilter(RowFilter.andFilter(filters));
  }

  /**
   * Removes a filter from the sorter.
   *
   * @param filter Filter to remove.
   */
  public void removeRowFilter(RowFilter<Object, Object> filter) {
    requireNonNull(filter, "filter");
    filters.remove(filter);
    setRowFilter(RowFilter.andFilter(filters));
  }
}
