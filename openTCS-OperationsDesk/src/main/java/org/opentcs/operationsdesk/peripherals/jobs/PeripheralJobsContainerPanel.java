/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.guing.common.util.IconToolkit;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.PERIPHERALJOB_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.gui.StringTableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a table of the kernel's peripheral jobs.
 */
public class PeripheralJobsContainerPanel
    extends JPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobsContainerPanel.class);
  /**
   * The path containing the icons.
   */
  private static final String ICON_PATH = "/org/opentcs/guing/res/symbols/panel/";
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * Maintains a set of all peripheral jobs existing on the kernel side.
   */
  private final PeripheralJobsContainer peripheralJobsContainer;

  /**
   * Factory for creating peripheral job views.
   */
  private final PeripheralJobViewFactory peripheralJobViewFactory;
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
   * @param portalProvider Provides access to a kernel service portal.
   * @param peripheralJobsContainer Maintains a set of all peripheral jobs existing on the kernel
   * side.
   * @param peripheralJobViewFactory The factory for creating peripheral job views.
   */
  @Inject
  public PeripheralJobsContainerPanel(SharedKernelServicePortalProvider portalProvider,
                                      PeripheralJobsContainer peripheralJobsContainer,
                                      PeripheralJobViewFactory peripheralJobViewFactory) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.peripheralJobsContainer = requireNonNull(peripheralJobsContainer,
                                                  "peripheralJobsContainer");
    this.peripheralJobViewFactory = requireNonNull(peripheralJobViewFactory,
                                                   "peripheralJobViewFactory");

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

    add(createToolBar(), BorderLayout.NORTH);
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

  private JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();

    JButton button = new JButton(
        IconToolkit.instance().getImageIconByFullPath(ICON_PATH + "table-row-delete-2.16x16.png")
    );
    button.addActionListener((ActionEvent e) -> withdrawSelectedJobs());
    button.setToolTipText(
        ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PERIPHERALJOB_PATH)
            .getString("peripheralJobsContainerPanel.button_withdrawSelectedJobs.tooltipText")
    );
    toolBar.add(button);

    return toolBar;
  }

  private void showSelectedJob() {
    getSelectedJob().ifPresent(job -> {
      DialogContent content = peripheralJobViewFactory.createPeripheralJobView(job);
      StandardContentDialog dialog
          = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                      content,
                                      true,
                                      StandardContentDialog.CLOSE);
      dialog.setVisible(true);
    });
  }

  private Optional<PeripheralJob> getSelectedJob() {
    int rowIndex = table.getSelectedRow();
    if (rowIndex == -1) {
      return Optional.empty();
    }
    return Optional.of(tableModel.getEntryAt(table.convertRowIndexToModel(rowIndex)));
  }

  private void showPopupMenuForSelectedJob(int x, int y) {
    boolean singleRowSelected = table.getSelectedRowCount() <= 1;
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(PERIPHERALJOB_PATH);
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(
        bundle.getString(
            "peripheralJobsContainerPanel.table_peripheralJobs.popupMenuItem_showDetails.text"
        )
    );
    item.setEnabled(singleRowSelected);
    item.addActionListener(event -> showSelectedJob());

    menu.show(table, x, y);
  }

  private void withdrawSelectedJobs() {
    List<PeripheralJob> toBeWithdrawn = new ArrayList<>();

    for (int i : table.getSelectedRows()) {
      toBeWithdrawn.add(tableModel.getEntryAt(table.convertRowIndexToModel(i)));
    }

    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      for (PeripheralJob job : toBeWithdrawn) {
        sharedPortal.getPortal().getPeripheralDispatcherService()
            .withdrawByPeripheralJob(job.getReference());
      }
    }
    catch (IllegalArgumentException | KernelRuntimeException exc) {
      LOG.warn("Exception withdrawing transport order", exc);
    }
  }

}
