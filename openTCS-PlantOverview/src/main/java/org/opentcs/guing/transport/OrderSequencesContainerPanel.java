/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.util.I18nPlantOverview;
import static org.opentcs.guing.util.I18nPlantOverview.TRANSPORTORDER_PATH;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a table of the kernel's order sequences.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequencesContainerPanel
    extends JPanel
    implements EventHandler {

  /**
   * The path to the icons.
   */
  private static final String ICON_PATH = "/org/opentcs/guing/res/symbols/panel/";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderSequencesContainerPanel.class);
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
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
  private FilterTableModel fTableModel;
  /**
   * All known order sequences (unfiltered).
   */
  private final List<OrderSequence> fOrderSequences = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides a access to a portal.
   * @param transportViewFactory A factory for order sequence views.
   * @param dialogParent The parent component for dialogs shown by this instance.
   */
  @Inject
  public OrderSequencesContainerPanel(SharedKernelServicePortalProvider portalProvider,
                                      TransportViewFactory transportViewFactory,
                                      @ApplicationFrame Component dialogParent) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.transportViewFactory = requireNonNull(transportViewFactory, "transportViewFactory");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

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
    setOrderSequences(fetchSequencesIfOnline());
  }

  private Set<OrderSequence> fetchSequencesIfOnline() {
    if (portalProvider.portalShared()) {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getTransportOrderService()
            .fetchObjects(OrderSequence.class);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching sequences from kernel", exc);
      }
    }

    return new HashSet<>();
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TO_SEQUENCE_PATH);

    String[] columns = {
      "Name",
      bundle.getString("orderSequencesContainerPanel.table_orderSequences.column_intendedVehicle.headerText"),
      bundle.getString("orderSequencesContainerPanel.table_orderSequences.column_executingVehicle.headerText"),
      "Index",
      bundle.getString("orderSequencesContainerPanel.table_orderSequences.column_complete.headerText"),
      bundle.getString("orderSequencesContainerPanel.table_orderSequences.column_finished.headerText"),
      bundle.getString("orderSequencesContainerPanel.table_orderSequences.column_failureFatal.headerText")
    };
    fTableModel = new FilterTableModel(new DefaultTableModel(columns, 0));
    fTableModel.setColumnIndexToFilter(5);  // Column "Finished"
    fTable = new OrdersTable(fTableModel);

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
    JMenuItem item = menu.add(ResourceBundleUtil.getBundle(TRANSPORTORDER_PATH)
        .getString("orderSequencesContainerPanel.table_sequences.popupMenuItem_showDetails.text"));
    item.addActionListener((ActionEvent evt) -> showOrderSequence());

    menu.show(fTable, x, y);
  }

  private Optional<OrderSequence> getSelectedOrderSequence() {
    int row = fTable.getSelectedRow();

    if (row == -1) {
      return Optional.empty();
    }

    return Optional.of(fOrderSequences.get(fTableModel.realRowIndex(row)));
  }

  private void handleObjectEvent(TCSObjectEvent evt) {
    if (evt.getCurrentOrPreviousObjectState() instanceof OrderSequence) {
      switch (evt.getType()) {
        case OBJECT_CREATED:
          orderSequenceAdded((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_MODIFIED:
          orderSequenceChanged((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_REMOVED:
          orderSequenceRemoved((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        default:
          LOG.warn("Unhandled event type: {}", evt.getType());
      }
    }
  }

  private void setOrderSequences(Set<OrderSequence> orderSequences) {
    SwingUtilities.invokeLater(() -> {
      fOrderSequences.clear();
      fTableModel.setRowCount(0);

      for (OrderSequence sequence : orderSequences) {
        fOrderSequences.add(sequence);
        fTableModel.addRow(toTableRow(sequence));
      }
    });
  }

  private void orderSequenceAdded(OrderSequence os) {
    SwingUtilities.invokeLater(() -> {
      fOrderSequences.add(0, os);
      fTableModel.insertRow(0, toTableRow(os));
    });
  }

  private void orderSequenceChanged(OrderSequence os) {
    SwingUtilities.invokeLater(() -> {
      int rowIndex = fOrderSequences.indexOf(os);
      Vector<Object> values = toTableRow(os);

      for (int i = 0; i < values.size(); i++) {
        fTableModel.setValueAt(values.elementAt(i), rowIndex, i);
      }
    });
  }

  private void orderSequenceRemoved(OrderSequence os) {
    SwingUtilities.invokeLater(() -> {
      int i = fOrderSequences.indexOf(os);
      fTableModel.removeRow(i);
      fOrderSequences.remove(i);
    });
  }

  private List<FilterButton> createFilterButtons() {
    List<FilterButton> buttons = new LinkedList<>();

    FilterButton b1 = new FilterButton(
        IconToolkit.instance().getImageIconByFullPath(ICON_PATH + "filterFinished.16x16.gif"),
        fTableModel,
        Boolean.FALSE
    );
    buttons.add(b1);
    b1.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverview.TO_SEQUENCE_PATH)
        .getString("orderSequencesContainerPanel.button_filterFinishedOrderSequences.tooltipText"));

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
   * Transforms the content of an order sequence to a table row.
   *
   * @param os The order sequence.
   * @return The table row contents.
   */
  private Vector<Object> toTableRow(OrderSequence os) {
    Vector<Object> row = new Vector<>();

    row.addElement(os.getName());

    if (os.getIntendedVehicle() != null) {
      row.addElement(os.getIntendedVehicle().getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH)
          .getString("orderSequencesContainerPanel.table_orderSequences.column_intendedVehicle.determinedAutomatic.text"));
    }

    if (os.getProcessingVehicle() != null) {
      row.addElement(os.getProcessingVehicle().getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle(I18nPlantOverview.TRANSPORTORDER_PATH)
          .getString("orderSequencesContainerPanel.table_orderSequences.column_intendedVehicle.determinedAutomatic.text"));
    }

    row.addElement(os.getFinishedIndex());

    row.addElement(os.isComplete());

    row.addElement(os.isFinished());

    row.addElement(os.isFailureFatal());

    return row;
  }
}
