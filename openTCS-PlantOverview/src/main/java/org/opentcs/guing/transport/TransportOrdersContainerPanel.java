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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
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
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
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
    extends JPanel
    implements EventHandler {

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
  private FilterTableModel fTableModel;
  /**
   * All known transport orders (unfiltered).
   */
  private final List<TransportOrder> fTransportOrders = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides a access to a portal.
   * @param orderUtil A helper for creating transport orders with the kernel.
   * @param orderPanelProvider Provides panels for entering new transport orders.
   * @param transportViewFactory A factory for creating transport order views.
   */
  @Inject
  public TransportOrdersContainerPanel(SharedKernelServicePortalProvider portalProvider,
                                       TransportOrderUtil orderUtil,
                                       Provider<CreateTransportOrderPanel> orderPanelProvider,
                                       TransportViewFactory transportViewFactory) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
    this.orderPanelProvider = requireNonNull(orderPanelProvider, "orderPanelProvider");
    this.transportViewFactory = requireNonNull(transportViewFactory, "transportViewFactory");

    initComponents();
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      handleObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof OperationModeChangeEvent) {
      initView();
    }
    else if (event instanceof SystemModelTransitionEvent) {
      initView();
    }
    else if (event instanceof KernelStateChangeEvent) {
      initView();
    }
  }

  /**
   * Initializes this panel's contents.
   */
  public void initView() {
    setTransportOrders(fetchOrdersIfOnline());
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH);
    String[] columns = {
      "Name",
      bundle.getString("transportOrdersContainerPanel.table_orders.column_source.headerText"),
      bundle.getString("transportOrdersContainerPanel.table_orders.column_destination.headerText"),
      bundle.getString("transportOrdersContainerPanel.table_orders.column_intendedVehicle.headerText"),
      bundle.getString("transportOrdersContainerPanel.table_orders.column_executingVehicle.headerText"),
      "Status",
      bundle.getString("transportOrdersContainerPanel.table_orders.column_orderSequence.headerText")
    };
    fTableModel = new FilterTableModel(new DefaultTableModel(columns, 0));
    fTableModel.setColumnIndexToFilter(5); // Column "Status"
    fTable = new OrdersTable(fTableModel);
    fTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

  private Set<TransportOrder> fetchOrdersIfOnline() {
    if (portalProvider.portalShared()) {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getTransportOrderService()
            .fetchObjects(TransportOrder.class);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching transport orders", exc);
      }
    }

    return new HashSet<>();
  }

  private void showSelectedTransportOrder() {
    getSelectedTransportOrder().ifPresent(transportOrder -> {
      DialogContent content = transportViewFactory.createTransportOrderView(transportOrder);
      StandardContentDialog dialog
          = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                      content,
                                      true,
                                      StandardContentDialog.CLOSE);
      dialog.setTitle(ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH)
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH);
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH);

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
    int row = fTable.getSelectedRow();

    if (row == -1) {
      return Optional.empty();
    }

    return Optional.of(fTransportOrders.get(fTableModel.realRowIndex(row)));
  }

  private void handleObjectEvent(TCSObjectEvent evt) {
    if (evt.getCurrentOrPreviousObjectState() instanceof TransportOrder) {
      switch (evt.getType()) {
        case OBJECT_CREATED:
          transportOrderAdded((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_MODIFIED:
          transportOrderChanged((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_REMOVED:
          transportOrderRemoved((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        default:
          LOG.warn("Unhandled event type: {}", evt.getType());
      }
    }
  }

  private void setTransportOrders(Set<TransportOrder> transportOrders) {
    SwingUtilities.invokeLater(() -> {
      fTransportOrders.clear();
      fTableModel.setRowCount(0);

      for (TransportOrder order : transportOrders) {
        fTransportOrders.add(order);
        fTableModel.addRow(toTableRow(order));
      }
    });
  }

  private void transportOrderAdded(TransportOrder order) {
    SwingUtilities.invokeLater(() -> {
      fTransportOrders.add(0, order);
      fTableModel.insertRow(0, toTableRow(order));
    });
  }

  private void transportOrderChanged(TransportOrder order) {
    SwingUtilities.invokeLater(() -> {
      int rowIndex = fTransportOrders.indexOf(order);
      Vector<String> values = toTableRow(order);

      for (int i = 0; i < values.size(); i++) {
        fTableModel.setValueAt(values.elementAt(i), rowIndex, i);
      }
      fTransportOrders.set(rowIndex, order);
    });
  }

  private void transportOrderRemoved(TransportOrder order) {
    SwingUtilities.invokeLater(() -> {
      int i = fTransportOrders.indexOf(order);
      fTableModel.removeRow(i);
      fTransportOrders.remove(i);
    });
  }

  private List<FilterButton> createFilterButtons() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH);
    FilterButton button;
    List<FilterButton> buttons = new ArrayList<>();
    IconToolkit iconkit = IconToolkit.instance();

    button = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterRaw.16x16.gif"),
                              fTableModel, "RAW");
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterRawOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterActivated.16x16.gif"),
                           fTableModel, "DISPATCHABLE");
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterDispatchableOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterProcessing.16x16.gif"),
                           fTableModel, "BEING_PROCESSED");
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterProcessedOrders.tooltipText"));
    buttons.add(button);

    button
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterFinished.16x16.gif"),
                           fTableModel, "FINISHED");
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterFinishedOrders.tooltipText"));
    buttons.add(button);

    button = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterFailed.16x16.gif"),
                              fTableModel, "FAILED");
    button.setToolTipText(bundle.getString("transportOrdersContainerPanel.button_filterFailedOrders.tooltipText"));
    buttons.add(button);

    return buttons;
  }

  private JToolBar createToolBar(List<FilterButton> filterButtons) {
    JToolBar toolBar = new JToolBar();

    for (FilterButton button : filterButtons) {
      toolBar.add(button);
    }

    return toolBar;
  }

  /**
   * Transforms the content of a transport order to a table row.
   *
   * @param transportOrder The transport order.
   * @return The table row contents.
   */
  private Vector<String> toTableRow(TransportOrder transportOrder) {
    Vector<String> row = new Vector<>();
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH);

    row.addElement(transportOrder.getName());

    Vector<DriveOrder> driveOrders = new Vector<>(transportOrder.getAllDriveOrders());

    if (driveOrders.size() == 1) {
      row.addElement("");
    }
    else {
      row.addElement(driveOrders.firstElement().getDestination().getDestination().getName());
    }

    row.addElement(driveOrders.lastElement().getDestination().getDestination().getName());

    if (transportOrder.getIntendedVehicle() != null) {
      row.addElement(transportOrder.getIntendedVehicle().getName());
    }
    else {
      row.addElement(bundle.getString("transportOrdersContainerPanel.table_orders.column_intendedVehicle.determinedAutomatic.text"));
    }

    if (transportOrder.getProcessingVehicle() != null) {
      row.addElement(transportOrder.getProcessingVehicle().getName());
    }
    else {
      row.addElement("?");
    }

    row.addElement(transportOrder.getState().toString());

    if (transportOrder.getWrappingSequence() != null) {
      row.addElement(transportOrder.getWrappingSequence().getName());
    }
    else {
      row.addElement("-");
    }

    return row;
  }

  private void withdrawTransportOrder() {
    int[] indices = fTable.getSelectedRows();
    List<TransportOrder> toWithdraw = new ArrayList<>();

    for (int i = 0; i < indices.length; i++) {
      int realIndex = fTableModel.realRowIndex(indices[i]);
      TransportOrder order = fTransportOrders.get(realIndex);
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
