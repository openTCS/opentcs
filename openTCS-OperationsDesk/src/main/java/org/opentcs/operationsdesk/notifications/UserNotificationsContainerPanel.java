/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.operationsdesk.transport.FilteredRowSorter;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.gui.StringTableCellRenderer;

/**
 * Shows a table of the most recent user notifications.
 */
public class UserNotificationsContainerPanel
    extends JPanel {

  /**
   * A formatter for timestamps.
   */
  private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  /**
   * A factory for creating user notification views.
   */
  private final UserNotificationViewFactory notificationViewFactory;
  /**
   * The table showing the user notifications.
   */
  private JTable fTable;
  /**
   * The table's model.
   */
  private UserNotificationTableModel tableModel;
  /**
   * The sorter for the table.
   */
  private FilteredRowSorter<UserNotificationTableModel> sorter;
  /**
   * Maintains a list of the most recent user notifications.
   */
  private final UserNotificationsContainer userNotificationsContainer;

  /**
   * Creates a new instance.
   *
   * @param notificationViewFactory A factory for creating user notification views.
   * @param userNotificationsContainer Maintains a list of the most recent user notifications.
   */
  @Inject
  public UserNotificationsContainerPanel(UserNotificationViewFactory notificationViewFactory,
                                         UserNotificationsContainer userNotificationsContainer) {
    this.notificationViewFactory = requireNonNull(notificationViewFactory,
                                                  "notificationViewFactory");
    this.userNotificationsContainer = requireNonNull(userNotificationsContainer,
                                                     "userNotificationsContainer");

    initComponents();
  }

  /**
   * Initializes this panel's contents.
   */
  public void initView() {
    tableModel.containerInitialized(userNotificationsContainer.getUserNotifications());
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new UserNotificationTableModel();
    userNotificationsContainer.addListener(tableModel);
    fTable = new JTable(tableModel);
    fTable.setFocusable(false);
    fTable.setRowSelectionAllowed(true);
    fTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    sorter = new FilteredRowSorter<>(tableModel);
    // Sort the table by the creation instant.
    sorter.setSortKeys(Arrays.asList(
        new RowSorter.SortKey(UserNotificationTableModel.COLUMN_TIME, SortOrder.DESCENDING)
    ));
    // ...but prevent manual sorting.
    for (int i = 0; i < fTable.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    fTable.setRowSorter(sorter);

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    TableCellRenderer timeRenderer = new StringTableCellRenderer<Instant>(instant -> {
      if (instant == null) {
        return "-";
      }
      return TIMESTAMP_FORMAT.format(Date.from(instant));
    });
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_TIME))
        .setCellRenderer(timeRenderer);

    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_TIME))
        .setMaxWidth(130);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_TIME))
        .setPreferredWidth(130);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_LEVEL))
        .setMaxWidth(100);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_LEVEL))
        .setPreferredWidth(100);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_SOURCE))
        .setMaxWidth(130);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_SOURCE))
        .setPreferredWidth(130);
    fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(UserNotificationTableModel.COLUMN_TEXT))
        .setPreferredWidth(300);
    fTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    fTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
          if (evt.getClickCount() == 2) {
            showSelectedUserNotification();
          }
        }

        if (evt.getButton() == MouseEvent.BUTTON3) {
          if (fTable.getSelectedRow() != -1) {
            showPopupMenuForSelectedUserNotification(evt.getX(), evt.getY());
          }
        }
      }
    });
  }

  private void showSelectedUserNotification() {
    getSelectedUserNotification().ifPresent(userNotification -> {
      DialogContent content = notificationViewFactory.createUserNotificationView(userNotification);
      StandardContentDialog dialog
          = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                      content,
                                      true,
                                      StandardContentDialog.CLOSE);
      dialog.setTitle(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.UNDETAIL_PATH)
          .getString("userNotificationView.title"));
      dialog.setVisible(true);
    });
  }

  private void showPopupMenuForSelectedUserNotification(int x, int y) {
    boolean singleRowSelected = fTable.getSelectedRowCount() <= 1;
    ResourceBundleUtil bundle
        = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.USERNOTIFICATION_PATH);
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(
        bundle.getString(
            "userNotificationsContainerPanel.table_notifications.popupMenuItem_showDetails.text"
        )
    );
    item.setEnabled(singleRowSelected);
    item.addActionListener((ActionEvent evt) -> showSelectedUserNotification());

    menu.show(fTable, x, y);
  }

  private Optional<UserNotification> getSelectedUserNotification() {
    int row = fTable.convertRowIndexToModel(fTable.getSelectedRow());
    if (row == -1) {
      return Optional.empty();
    }

    return Optional.of(tableModel.getEntryAt(row));
  }
}
