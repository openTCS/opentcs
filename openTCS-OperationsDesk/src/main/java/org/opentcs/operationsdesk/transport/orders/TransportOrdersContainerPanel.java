/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.orders;

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
import javax.inject.Provider;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.data.order.TransportOrder.State.BEING_PROCESSED;
import static org.opentcs.data.order.TransportOrder.State.DISPATCHABLE;
import static org.opentcs.data.order.TransportOrder.State.FAILED;
import static org.opentcs.data.order.TransportOrder.State.FINISHED;
import static org.opentcs.data.order.TransportOrder.State.RAW;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.guing.common.util.IconToolkit;
import org.opentcs.operationsdesk.exchange.TransportOrderUtil;
import org.opentcs.operationsdesk.transport.CreateTransportOrderPanel;
import org.opentcs.operationsdesk.transport.FilterButton;
import org.opentcs.operationsdesk.transport.FilteredRowSorter;
import org.opentcs.operationsdesk.transport.OrdersTable;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a table of the kernel's transport orders.
 *
 * @author Sven Liebing (ifak e.V. Magdeburg)
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrdersContainerPanel
    extends JPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrdersContainerPanel.class);
  /**
   * The path containing the icons.
   */
  private static final String ICON_PATH = "/org/opentcs/guing/res/symbols/panel/";
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * A helper for creating transport orders with the kernel.
   */
  private final TransportOrderUtil orderUtil;
  /**
   * Provides panels for entering new transport orders.
   */
  private final Provider<CreateTransportOrderPanel> orderPanelProvider;
  /**
   * A factory for creating transport order views.
   */
  private final TransportViewFactory transportViewFactory;
  /**
   * The table showing the transport orders.
   */
  private JTable fTable;
  /**
   * The table's model.
   */
  private TransportOrderTableModel tableModel;
  /**
   * The sorter for the table.
   */
  private FilteredRowSorter<TransportOrderTableModel> sorter;
  /**
   * Holds the transport orders.
   */
  private final TransportOrdersContainer transportOrdersContainer;

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides a access to a portal.
   * @param orderUtil A helper for creating transport orders with the kernel.
   * @param orderPanelProvider Provides panels for entering new transport orders.
   * @param transportViewFactory A factory for creating transport order views.
   * @param transportOrderContainer Maintains a set of transport order on the kernel side.
   */
  @Inject
  public TransportOrdersContainerPanel(SharedKernelServicePortalProvider portalProvider,
                                       TransportOrderUtil orderUtil,
                                       Provider<CreateTransportOrderPanel> orderPanelProvider,
                                       TransportViewFactory transportViewFactory,
                                       TransportOrdersContainer transportOrderContainer) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
    this.orderPanelProvider = requireNonNull(orderPanelProvider, "orderPanelProvider");
    this.transportViewFactory = requireNonNull(transportViewFactory, "transportViewFactory");
    this.transportOrdersContainer = requireNonNull(transportOrderContainer, "transportOrderContainer");

    initComponents();
  }

  /**
   * Initializes this panel's contents.
   */
  public void initView() {
    tableModel.containerInitialized(transportOrdersContainer.getTransportOrders());
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new TransportOrderTableModel();
    transportOrdersContainer.addListener(tableModel);
    fTable = new OrdersTable(tableModel);
    fTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    sorter = new FilteredRowSorter<>(tableModel);
    // Sort the table by the creation instant.
    sorter.setSortKeys(Arrays.asList(
        new RowSorter.SortKey(TransportOrderTableModel.COLUMN_CREATION_TIME, SortOrder.DESCENDING)
    ));
    // ...but prevent manual sorting.
    for (int i = 0; i < fTable.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    fTable.setRowSorter(sorter);

    // Hide the column that shows the creation time.
    fTable.removeColumn(fTable.getColumnModel()
        .getColumn(fTable.convertColumnIndexToView(TransportOrderTableModel.COLUMN_CREATION_TIME)));

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    JToolBar toolBar = createToolBar(createFilterButtons());
    addControlButtons(toolBar);
    add(toolBar, BorderLayout.NORTH);

    fTable.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
          if (evt.getClickCount() == 2) {
            showSelectedTransportOrder();
          }
        }

        if (evt.getButton() == MouseEvent.BUTTON3) {
          if (fTable.getSelectedRow() != -1) {
            showPopupMenuForSelectedTransportOrder(evt.getX(), evt.getY());
          }
        }
      }
    });
  }

  private void showSelectedTransportOrder() {
    getSelectedTransportOrder().ifPresent(transportOrder -> {
      DialogContent content = transportViewFactory.createTransportOrderView(transportOrder);
      StandardContentDialog dialog
          = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                      content,
                                      true,
                                      StandardContentDialog.CLOSE);
      dialog.setTitle(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TODETAIL_PATH)
          .getString("transportOrdersContainerPanel.dialog_createTransportOrder.title"));
      dialog.setVisible(true);
    });
  }

  private void createTransportOrderWithPattern() {
    getSelectedTransportOrder().ifPresent(transportOrder -> {
      CreateTransportOrderPanel content = orderPanelProvider.get();
      content.setPattern(transportOrder);
      StandardContentDialog dialog
          = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                      content);
      dialog.setVisible(true);

      if (dialog.getReturnStatus() == StandardContentDialog.RET_OK) {
        orderUtil.createTransportOrder(content.getDestinationModels(),
                                       content.getActions(),
                                       content.getPropertiesList(),
                                       content.getSelectedDeadline(),
                                       content.getSelectedVehicle(),
                                       content.getSelectedType());
      }
    });
  }

  private void createCopyOfSelectedTransportOrder() {
    getSelectedTransportOrder().ifPresent(
        transportOrder -> orderUtil.createTransportOrder(transportOrder)
    );
  }

  private void showPopupMenuForSelectedTransportOrder(int x, int y) {
    boolean singleRowSelected = fTable.getSelectedRowCount() <= 1;
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TRANSPORTORDER_PATH);
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(bundle.getString("transportOrdersContainerPanel.table_orders.popupMenuItem_showDetails.text"));
    item.setEnabled(singleRowSelected);
    item.addActionListener((ActionEvent evt) -> showSelectedTransportOrder());

    menu.add(new JSeparator());

    item = menu.add(bundle.getString("transportOrdersContainerPanel.table_orders.popupMenuItem_orderAsTemplate.text"));
    item.setEnabled(singleRowSelected);
    item.addActionListener((ActionEvent evt) -> createTransportOrderWithPattern());

    item = menu.add(bundle.getString("transportOrdersContainerPanel.table_orders.popupMenuItem_copyOrder.text"));
    item.setEnabled(singleRowSelected);
    item.addActionListener((ActionEvent evt) -> createCopyOfSelectedTransportOrder());

    menu.show(fTable, x, y);
  }

  private void addControlButtons(JToolBar toolBar) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TRANSPORTORDER_PATH);

    toolBar.add(new JToolBar.Separator());

    JButton button = new JButton(
        IconToolkit.instance().getImageIconByFullPath(ICON_PATH + "table-row-delete-2.16x16.png")
    );
    button.addActionListener((ActionEvent e) -> withdrawTransportOrder());
    button.setToolTipText(
        bundle.getString("transportOrdersContainerPanel.button_withdrawSelectedOrders.tooltipText")
    );
    toolBar.add(button);
  }

  private Optional<TransportOrder> getSelectedTransportOrder() {
    int row = fTable.convertRowIndexToModel(fTable.getSelectedRow());
    if (row == -1) {
      return Optional.empty();
    }

    return Optional.of(tableModel.getEntryAt(row));
  }

  private List<JToggleButton> createFilterButtons() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TRANSPORTORDER_PATH);
    JToggleButton button;
    List<JToggleButton> buttons = new ArrayList<>();
    IconToolkit iconkit = IconToolkit.instance();

    button = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterRaw.16x16.gif"),
                              createFilterForState(RAW),
                              sorter);

    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterRawOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterActivated.16x16.gif"),
                           createFilterForState(DISPATCHABLE),
                           sorter);
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterDispatchableOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterProcessing.16x16.gif"),
                           createFilterForState(BEING_PROCESSED),
                           sorter);
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterProcessedOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterFinished.16x16.gif"),
                           createFilterForState(FINISHED),
                           sorter);
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterFinishedOrders.tooltipText"));
    buttons.add(button);

    button = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterFailed.16x16.gif"),
                              createFilterForState(FAILED),
                              sorter);
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterFailedOrders.tooltipText"));
    buttons.add(button);

    return buttons;
  }

  private RowFilter<Object, Object> createFilterForState(TransportOrder.State state) {
    return new RowFilter<Object, Object>() {
      @Override
      public boolean include(Entry<? extends Object, ? extends Object> entry) {
        TransportOrder order = ((TransportOrderTableModel) entry.getModel()).getEntryAt((int) entry.getIdentifier());
        return order.getState() != state;
      }
    };
  }

  private JToolBar createToolBar(List<JToggleButton> filterButtons) {
    JToolBar toolBar = new JToolBar();

    for (JToggleButton button : filterButtons) {
      toolBar.add(button);
    }

    return toolBar;
  }

  private void withdrawTransportOrder() {
    int[] indices = fTable.getSelectedRows();
    List<TransportOrder> toWithdraw = new ArrayList<>();

    for (int i = 0; i < indices.length; i++) {
      int modelIndex = fTable.convertRowIndexToModel(indices[i]);
      TransportOrder order = tableModel.getEntryAt(modelIndex);
      toWithdraw.add(order);
    }

    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      for (TransportOrder order : toWithdraw) {
        sharedPortal.getPortal().getDispatcherService()
            .withdrawByTransportOrder(order.getReference(), false);
      }
    }
    catch (KernelRuntimeException exc) {
      LOG.warn("Exception withdrawing transport order", exc);
    }
  }
}
