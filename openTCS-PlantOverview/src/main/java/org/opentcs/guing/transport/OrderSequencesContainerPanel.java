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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
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
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OrderSequenceEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine Ansicht fï¿½r Transportauftragsketten. Teil dieser Ansicht ist die
 * Tabelle, in der die Transportauftragsketten dargestellt sind.
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
   * Die Tabelle, in der die Transportauftragsketten dargestellt werden.
   */
  private JTable fTable;
  /**
   * Das TableModel.
   */
  private FilterTableModel fTableModel;
  /**
   * Die Liste der Filterbuttons.
   */
  private List<FilterButton> fFilterButtons;
  /**
   * Die Transportauftragsketten
   */
  private Vector<OrderSequence> fOrderSequences;

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides a access to a portal.
   * @param transportViewFactory A factory for order sequence views.
   * @param dialogParent The parent component for dialogs shown by this
   * instance.
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
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
    if (event instanceof KernelStateChangeEvent) {
      handleKernelStateChange((KernelStateChangeEvent) event);
    }
    if (event instanceof OrderSequenceEvent) {
      handleOrderSequenceEvent((OrderSequenceEvent) event);
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

  /**
   * Deletes all OrderSequences, eg after changing the kernel state.
   */
  public void clearOrderSequences() {
    if (fOrderSequences != null) {
      if (fTableModel != null) {
        fTableModel.setRowCount(0);
      }
      fOrderSequences.clear();
    }
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    String[] columns = {"Name",
                        bundle.getString("OrderSequencesContainerPanel.intendedVehicle"),
                        bundle.getString("OrderSequencesContainerPanel.executingVehicle"),
                        "Index",
                        bundle.getString("OrderSequencesContainerPanel.complete"),
                        bundle.getString("OrderSequencesContainerPanel.finished"),
                        bundle.getString("OrderSequencesContainerPanel.failureFatal")};
    fTableModel = new FilterTableModel(new DefaultTableModel(columns, 0));
    fTableModel.setColumnIndexToFilter(5);  // Column "Finished"
    fTable = new OrdersTable(fTableModel);

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    fFilterButtons = createFilterButtons();
    JToolBar toolBar = createToolBar(fFilterButtons);
//    addControlButtons(toolBar);
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
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      OrderSequence os = sharedPortal.getPortal().getTransportOrderService()
          .fetchObject(OrderSequence.class, getSelectedOrderSequence().getReference());
      DialogContent content = transportViewFactory.createOrderSequenceView(os);
      StandardContentDialog dialog
          = new StandardContentDialog(dialogParent, content, true, StandardContentDialog.CLOSE);
      dialog.setTitle(ResourceBundleUtil.getBundle()
          .getString("OrderSequencesContainerPanel.orderSequence"));
      dialog.setVisible(true);
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Exception fetching order sequences from kernel", e);
    }
  }

  private void showPopupMenu(int x, int y) {
    int row = fTable.rowAtPoint(new Point(x, y));
    fTable.setRowSelectionInterval(row, row);

    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(ResourceBundleUtil.getBundle()
        .getString("TransportOrdersContainerPanel.popup.showDetails"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        showOrderSequence();
      }
    });

    menu.show(fTable, x, y);
  }

  private OrderSequence getSelectedOrderSequence() {
    int row = fTable.getSelectedRow();

    if (row == -1) {
      return null;
    }

    int index = fTableModel.realRowIndex(row);

    return fOrderSequences.elementAt(index);
  }

  private void setOrderSequences(Set<OrderSequence> orderSequences) {
    if (orderSequences == null) {
      return;
    }

    fOrderSequences = new Vector<>();
    Iterator<OrderSequence> i = orderSequences.iterator();

    while (i.hasNext()) {
      OrderSequence t = i.next();
      fOrderSequences.addElement(t);
      fTableModel.addRow(toTableRow(t));
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        // XXX Clear panel?
        break;
      case LOADED:
        initView();
        break;
      default:
      // Do nada.
    }
  }

  private void handleKernelStateChange(KernelStateChangeEvent event) {
    if (event != null && KernelStateChangeEvent.State.OPERATING != event.getNewState()) {
      fTableModel.setRowCount(0);
      fTableModel.fireTableDataChanged();
    }
  }

  private void handleOrderSequenceEvent(OrderSequenceEvent evt) {
    switch (evt.getType()) {
      case SEQ_CREATED:
        orderSequenceAdded(evt.getSequence());
        break;
      case SEQ_CHANGED:
        orderSequenceChanged(evt.getSequence());
        break;
      case SEQ_REMOVED:
        orderSequenceRemoved(evt.getSequence());
        break;
      default:
      // Do nada.
    }
  }

  private void orderSequenceAdded(OrderSequence os) {
    fOrderSequences.insertElementAt(os, 0);
    fTableModel.insertRow(0, toTableRow(os));
  }

  private void orderSequenceChanged(OrderSequence os) {
    int rowIndex = fOrderSequences.indexOf(os);
    Vector<Object> values = toTableRow(os);

    for (int i = 0; i < values.size(); i++) {
      fTableModel.setValueAt(values.elementAt(i), rowIndex, i);
    }
  }

  private void orderSequenceRemoved(final OrderSequence os) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        int i = fOrderSequences.indexOf(os);
        fTableModel.removeRow(i);
        fOrderSequences.removeElementAt(i);
      }
    });
  }

  private List<FilterButton> createFilterButtons() {
    List<FilterButton> buttons = new LinkedList<>();
    IconToolkit iconkit = IconToolkit.instance();

    FilterButton b1
        = new FilterButton(iconkit.getImageIconByFullPath(ICON_PATH + "filterFinished.16x16.gif"),
                           fTableModel,
                           Boolean.FALSE);
    buttons.add(b1);
    b1.setToolTipText(ResourceBundleUtil.getBundle()
        .getString("OrderSequencesContainerPanel.filterOrderSequences"));

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
   * Wandelt die Werte einer Transportauftragskette in eine Tabellenzeile um.
   *
   * @param os
   * @return
   */
  private Vector<Object> toTableRow(OrderSequence os) {
    Vector<Object> row = new Vector<>();
    // Spalte 0: Name
    String name = os.getName();
    row.addElement(name);

    // Spalte 2: Gewï¿½nschtes Fahrzeug
    TCSObjectReference<Vehicle> intendedVehicle = os.getIntendedVehicle();

    if (intendedVehicle != null) {
      row.addElement(intendedVehicle.getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle()
          .getString("TransportOrdersContainerPanel.table.determineAutomatic"));
    }

    // Spalte 3: Ausfï¿½hrendes Fahrzeug
    TCSObjectReference<Vehicle> processingVehicle = os.getProcessingVehicle();

    if (processingVehicle != null) {
      row.addElement(processingVehicle.getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle()
          .getString("TransportOrdersContainerPanel.table.determineAutomatic"));
    }

    // Spalte 4: Index
    int finishedIndex = os.getFinishedIndex();
    row.addElement(finishedIndex);

    // Spalte 5: Complete Flag
    Boolean complete = os.isComplete();
    row.addElement(complete);

    // Spalte 6: Finished Flag
    Boolean finished = os.isFinished();
    row.addElement(finished);

    // Spalte 7: Failure Fatal Flag
    Boolean failureFatal = os.isFailureFatal();
    row.addElement(failureFatal);

    // Weitere Felder der Ordersequence:
//  TCSObjectReference<TransportOrder> nextUnfinishedOrder = os.getNextUnfinishedOrder();
//  List<TCSObjectReference<TransportOrder>> orders = os.getOrders();
    return row;
  }
}
