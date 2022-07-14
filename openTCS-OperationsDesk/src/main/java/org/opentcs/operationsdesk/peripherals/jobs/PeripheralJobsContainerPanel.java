/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.PERIPHERALJOB_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.gui.StringTableCellRenderer;

/**
 * Shows a table of the kernel's peripheral jobs.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobsContainerPanel
    extends JPanel {

  /**
   * Maintains a set of all peripheral jobs existing on the kernel side.
   */
  private final PeripheralJobsContainer peripheralJobsContainer;
  /**
   * The table showing the peripheral jobs.
   */
  private JTable table;
  /**
   * The table's model.
   */
  private PeripheralJobTableModel tableModel;

  /**
   * Creates a new instance.
   *
   * @param peripheralJobsContainer Maintains a set of all peripheral jobs existing on the kernel
   * side.
   */
  @Inject
  public PeripheralJobsContainerPanel(PeripheralJobsContainer peripheralJobsContainer) {
    this.peripheralJobsContainer = requireNonNull(peripheralJobsContainer,
                                                  "peripheralJobsContainer");

    initComponents();
  }

  /**
   * Initializes this panel's contents.
   */
  public void initView() {
    tableModel.containerInitialized(peripheralJobsContainer.getPeripheralJobs());
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    initPeripheralJobTable();
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  private void initPeripheralJobTable() {
    tableModel = new PeripheralJobTableModel();
    peripheralJobsContainer.addListener(tableModel);
    table = new JTable(tableModel);

    TableRowSorter<PeripheralJobTableModel> sorter = new TableRowSorter<>(tableModel);
    // Sort the table by the creation instant.
    sorter.setSortKeys(Arrays.asList(
        new RowSorter.SortKey(PeripheralJobTableModel.COLUMN_CREATION_TIME, SortOrder.DESCENDING)
    ));
    // ...but prevent manual sorting.
    for (int i = 0; i < table.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    table.setRowSorter(sorter);

    // Hide the column that shows the creation time.
    table.removeColumn(table.getColumnModel()
        .getColumn(table.convertColumnIndexToView(PeripheralJobTableModel.COLUMN_CREATION_TIME)));

    TableCellRenderer renderer = new StringTableCellRenderer<TCSObjectReference>(reference -> {
      if (reference == null) {
        return "-";
      }

      return reference.getName();
    });
    table.getColumnModel().getColumn(PeripheralJobTableModel.COLUMN_RELATED_VEHICLE)
        .setCellRenderer(renderer);
    table.getColumnModel().getColumn(PeripheralJobTableModel.COLUMN_RELATED_ORDER)
        .setCellRenderer(renderer);

    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
          if (evt.getClickCount() == 2) {
            showSelectedJob();
          }
        }

        if (evt.getButton() == MouseEvent.BUTTON3) {
          if (table.getSelectedRow() != -1) {
            showPopupMenuForSelectedJob(evt.getX(), evt.getY());
          }
        }
      }
    });
  }

  private void showSelectedJob() {
    int rowIndex = table.getSelectedRow();
    if (rowIndex > -1) {
      PeripheralJob selectedJob = tableModel.getEntryAt(table.convertRowIndexToModel(rowIndex));
      // TODO Show a dialog with details about the selected job
    }

  }

  private void showPopupMenuForSelectedJob(int x, int y) {
    boolean singleRowSelected = table.getSelectedRowCount() <= 1;
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(PERIPHERALJOB_PATH);
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(bundle.getString("peripheralJobsContainerPanel.table_peripheralJobs.popupMenuItem_showDetails.text"));
    item.setEnabled(singleRowSelected);
    item.addActionListener(event -> showSelectedJob());

    menu.show(table, x, y);
  }
}
