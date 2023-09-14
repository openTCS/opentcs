/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;

/**
 * A table model for user notifications.
 */
public class UserNotificationTableModel
    extends AbstractTableModel
    implements UserNotificationContainerListener {

  /**
   * The indexes of the time column.
   */
  public static final int COLUMN_TIME = 0;
  /**
   * The indexes of the level column.
   */
  public static final int COLUMN_LEVEL = 1;
  /**
   * The indexes of the source column.
   */
  public static final int COLUMN_SOURCE = 2;
  /**
   * The indexes of the text column.
   */
  public static final int COLUMN_TEXT = 3;

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.USERNOTIFICATION_PATH);

  private static final String[] COLUMN_NAMES = new String[]{
    BUNDLE.getString("userNotificationTableModel.column_time.headerText"),
    BUNDLE.getString("userNotificationTableModel.column_level.headerText"),
    BUNDLE.getString("userNotificationTableModel.column_source.headerText"),
    BUNDLE.getString("userNotificationTableModel.column_text.headerText")
  };

  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
    Instant.class,
    String.class,
    String.class,
    String.class
  };

  private final List<UserNotification> entries = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public UserNotificationTableModel() {
  }

  @Override
  public int getRowCount() {
    return entries.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount()) {
      return null;
    }

    UserNotification entry = entries.get(rowIndex);

    switch (columnIndex) {
      case COLUMN_TIME:
        return entry.getTimestamp();
      case COLUMN_LEVEL:
        return entry.getLevel().name();
      case COLUMN_SOURCE:
        if (entry.getSource() != null) {
          return entry.getSource();
        }
        else {
          return "-";
        }
      case COLUMN_TEXT:
        return entry.getText();
      default:
        throw new IllegalArgumentException("Invalid column index: " + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public void containerInitialized(List<UserNotification> notifications) {
    requireNonNull(notifications, "notifications");

    SwingUtilities.invokeLater(() -> {
      // Notifications of any change listeners must happen at the same time/in the same thread the
      // data behind the model is updated. Otherwise, there is a risk that listeners work with/
      // refer to outdated data, which can lead to runtime exceptions.
      entries.clear();
      entries.addAll(notifications);
      fireTableDataChanged();
    });
  }

  @Override
  public void userNotificationAdded(UserNotification notification) {
    requireNonNull(notification, "notification");

    SwingUtilities.invokeLater(() -> {
      entries.add(notification);
      fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    });
  }

  @Override
  public void userNotificationRemoved(UserNotification notification) {
    SwingUtilities.invokeLater(() -> {
      int row = entries.indexOf(notification);
      if (row != -1) {
        entries.remove(notification);
        fireTableRowsDeleted(row, row);
      }
    });
  }

  /**
   * Returns the user notification at the specified index.
   *
   * @param index the index to return.
   * @return the user notification at that index.
   */
  public UserNotification getEntryAt(int index) {
    if (index < 0 || index >= entries.size()) {
      return null;
    }

    return entries.get(index);
  }
}
