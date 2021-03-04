/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 * A table model for order statistics.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrdersTableModel
    extends AbstractTableModel {

  /**
   * This class's resources bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/statistics/Bundle");
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    BUNDLE.getString("Name"),
    BUNDLE.getString("Time_to_assignment"),
    BUNDLE.getString("Processing_time"),
    BUNDLE.getString("Successful"),
    BUNDLE.getString("Deadline_crossed")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    Long.class,
    Long.class,
    Boolean.class,
    Boolean.class
  };
  /**
   * The actual content.
   */
  private final List<OrderStats> orders = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public OrdersTableModel() {
  }

  /**
   * Adds statistics data at the end of the table.
   *
   * @param order The order statistics data to be added.
   */
  public void addData(OrderStats order) {
    int newIndex = orders.size();
    orders.add(order);
    fireTableRowsInserted(newIndex, newIndex);
  }

  @Override
  public int getRowCount() {
    return orders.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(int columnIndex) {
    try {
      return COLUMN_NAMES[columnIndex];
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      return "ERROR";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    OrderStats order = orders.get(rowIndex);

    switch (columnIndex) {
      case 0:
        return order.getName();
      case 1:
        return TimePeriodFormat.formatHumanReadable(
            order.getAssignmentTime() - order.getActivationTime());
      case 2:
        return TimePeriodFormat.formatHumanReadable(
            order.getFinishedTime() - order.getAssignmentTime());
      case 3:
        return order.isFinishedSuccessfully();
      case 4:
        return order.hasCrossedDeadline();
      default:
        throw new IllegalArgumentException("Invalid columnIndex: "
            + columnIndex);
    }
  }
}
