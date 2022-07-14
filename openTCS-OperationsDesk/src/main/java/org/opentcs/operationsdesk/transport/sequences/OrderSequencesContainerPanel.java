/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.sequences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.guing.common.util.IconToolkit;
import org.opentcs.operationsdesk.transport.FilterButton;
import org.opentcs.operationsdesk.transport.FilteredRowSorter;
import org.opentcs.operationsdesk.transport.OrdersTable;
import org.opentcs.operationsdesk.transport.orders.TransportViewFactory;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a table of the kernel's order sequences.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequencesContainerPanel
    extends JPanel {

  /**
   * The path to the icons.
   */
  private static final String ICON_PATH = "/org/opentcs/guing/res/symbols/panel/";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderSequencesContainerPanel.class);
  /**
   * A factory for order sequence views.
   */
  private final TransportViewFactory transportViewFactory;
  /**
   * The parent component for dialogs shown by this instance.
   */
  private final Component dialogParent;
  /**
   * The table showing the order sequences.
   */
  private JTable fTable;
  /**
   * The table's model.
   */
  private OrderSequenceTableModel tableModel;
  /**
   * The sorter for the table.
   */
  private FilteredRowSorter<OrderSequenceTableModel> sorter;
  /**
   * Holds the order sequences.
   */
  private final OrderSequencesContainer orderSequencesContainer;

  /**
   * Creates a new instance.
   *
   * @param transportViewFactory A factory for order sequence views.
   * @param dialogParent The parent component for dialogs shown by this instance.
   * @param orderSequencesContainer Maintains a set of order sequences on the kernel side.
   */
  @Inject
  public OrderSequencesContainerPanel(TransportViewFactory transportViewFactory,
                                      @ApplicationFrame Component dialogParent,
                                      OrderSequencesContainer orderSequencesContainer) {
    this.transportViewFactory = requireNonNull(transportViewFactory, "transportViewFactory");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.orderSequencesContainer = requireNonNull(orderSequencesContainer,
                                                  "orderSequencesContainer");

    initComponents();
  }

  /**
   * Initializes this panel's contents.
   */
  public void initView() {
    tableModel.containerInitialized(orderSequencesContainer.getOrderSequences());
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    tableModel = new OrderSequenceTableModel();
    orderSequencesContainer.addListener(tableModel);
    fTable = new OrdersTable(tableModel);

    sorter = new FilteredRowSorter<>(tableModel);
    // Prevent manual sorting.
    for (int i = 0; i < fTable.getColumnCount(); i++) {
      sorter.setSortable(i, false);
    }
    sorter.setSortsOnUpdates(true);
    fTable.setRowSorter(sorter);

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    JToolBar toolBar = createToolBar(createFilterButtons());
    add(toolBar, BorderLayout.NORTH);

    fTable.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
          if (evt.getClickCount() == 2) {
            showOrderSequence();
          }
        }

        if (evt.getButton() == MouseEvent.BUTTON3) {
          showPopupMenu(evt.getX(), evt.getY());
        }
      }
    });
  }

  private void showOrderSequence() {
    getSelectedOrderSequence().ifPresent(os -> {
      DialogContent content = transportViewFactory.createOrderSequenceView(os);
      StandardContentDialog dialog
          = new StandardContentDialog(dialogParent, content, true, StandardContentDialog.CLOSE);
      dialog.setVisible(true);
    });
  }

  private void showPopupMenu(int x, int y) {
    int row = fTable.rowAtPoint(new Point(x, y));
    fTable.setRowSelectionInterval(row, row);

    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TO_SEQUENCE_PATH)
        .getString("orderSequencesContainerPanel.table_sequences.popupMenuItem_showDetails.text"));
    item.addActionListener((ActionEvent evt) -> showOrderSequence());

    menu.show(fTable, x, y);
  }

  private Optional<OrderSequence> getSelectedOrderSequence() {
    int row = fTable.getSelectedRow();

    if (row == -1) {
      return Optional.empty();
    }

    return Optional.of(tableModel.getEntryAt(fTable.convertRowIndexToModel(row)));
  }

  private List<FilterButton> createFilterButtons() {

    List<FilterButton> buttons = new LinkedList<>();

    FilterButton b1 = new FilterButton(
        IconToolkit.instance().getImageIconByFullPath(ICON_PATH + "filterFinished.16x16.gif"),
        createFilter(),
        sorter
    );
    buttons.add(b1);
    b1.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.TO_SEQUENCE_PATH)
        .getString("orderSequencesContainerPanel.button_filterFinishedOrderSequences.tooltipText"));

    return buttons;
  }

  private RowFilter<Object, Object> createFilter() {
    return new RowFilter<Object, Object>() {
      @Override
      public boolean include(Entry<? extends Object, ? extends Object> entry) {
        OrderSequence os = ((OrderSequenceTableModel) entry.getModel()).getEntryAt((int) entry.getIdentifier());
        return os.isComplete();
      }
    };
  }

  private JToolBar createToolBar(List<FilterButton> filterButtons) {
    JToolBar toolBar = new JToolBar();

    for (FilterButton button : filterButtons) {
      toolBar.add(button);
    }

    return toolBar;
  }
}
