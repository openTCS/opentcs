/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.notification.UserNotification;

/**
 * A table model for holding {@link org.opentcs.drivers.vehicle.VehicleNotification} instances in
 * rows.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
final class LogTableModel
    extends AbstractTableModel {

  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    ResourceBundle.getBundle("org/opentcs/kernelcontrolcenter/Bundle")
    .getString("LogTableModel.TimeStamp"),
    ResourceBundle.getBundle("org/opentcs/kernelcontrolcenter/Bundle")
    .getString("LogTableModel.Message")
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    String.class
  };
  /**
   * A {@link DateFormat} instance for formatting notifications' time stamps.
   */
  private final DateTimeFormatter dateFormat = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());
  /**
   * The buffer for holding the model data.
   */
  private final Set<UserNotification> values
      = new TreeSet<>((n1, n2) -> n1.getTimestamp().compareTo(n2.getTimestamp()));
  /**
   * The actual data displayed in the table.
   */
  private List<UserNotification> filteredValues = new ArrayList<>();
  /**
   * The predicate used for filtering table rows.
   */
  private Predicate<UserNotification> filterPredicate = (notification) -> true;

  /**
   * Creates a new instance.
   */
  public LogTableModel() {
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (row < 0 || row >= filteredValues.size()) {
      return null;
    }
    switch (column) {
      case 0:
        return dateFormat.format(filteredValues.get(row).getTimestamp());
      case 1:
        return filteredValues.get(row).getText();
      default:
        return new IllegalArgumentException("Column out of bounds.");
    }
  }

  /**
   * Adds a row to the model, containing the given notification.
   *
   * @param notification The notification to be added in a new row.
   */
  public void addRow(UserNotification notification) {
    requireNonNull(notification, "notification");

    values.add(notification);
    updateFilteredValues();
    fireTableDataChanged();
  }

  /**
   * Removes the given notification from the internal data set and from the current data model.
   *
   * @param notification The notification to be removed.
   */
  public void removeRow(UserNotification notification) {
    if (values.contains(notification)) {
      values.remove(notification);
      updateFilteredValues();
      fireTableDataChanged();
    }
  }

  /**
   * Removes all notifications from the model.
   */
  public void clear() {
    if (!values.isEmpty()) {
      values.clear();
      updateFilteredValues();
      fireTableDataChanged();
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  /**
   * Returns the notification object representing the indexed row.
   *
   * @param row The row for which to fetch the notification object.
   * @return The notification object representing the indexed row.
   */
  public UserNotification getRow(int row) {
    return filteredValues.get(row);
  }

  /**
   * Filters the notifications and shows only errors and warnings.
   *
   * @param predicate The predicate used for filtering table rows.
   */
  public void filterMessages(Predicate<UserNotification> predicate) {
    this.filterPredicate = requireNonNull(predicate, "predicate");

    updateFilteredValues();
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return filteredValues.size();
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

  private void updateFilteredValues() {
    filteredValues = values.stream().filter(filterPredicate).collect(Collectors.toList());
  }
}
