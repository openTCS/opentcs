/**
 * (c): IML.
 *
 */
package org.opentcs.guing.transport;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.exchange.OrderSequenceDispatcher;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Eine Ansicht für Transportauftragsketten. Teil dieser Ansicht ist die
 * Tabelle, in der die Transportauftragsketten dargestellt sind.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class OrderSequencesContainerPanel
    extends JPanel
    implements OrderSequenceListener {

  private final String fIconPath = "/org/opentcs/guing/res/symbols/panel/";
  /**
   * Die Tabelle, in der die Transportauftragsketten dargestellt werden.
   */
  protected JTable fTable;
  /**
   * Das TableModel.
   */
  protected FilterTableModel fTableModel;
  /**
   * Die Liste der Filterbuttons.
   */
  protected Vector<FilterButton> fFilterButtons;
  /**
   * Die Anwendung.
   */
  protected OpenTCSView fOpenTCSView;
  /**
   * Die Transportauftragsketten
   */
  protected Vector<OrderSequence> fOrderSequences;
  /**
   * The proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The view to work with.
   */
  public OrderSequencesContainerPanel(OpenTCSView openTCSView) {
    fOpenTCSView = openTCSView;
    this.kernelProxyManager = DefaultKernelProxyManager.instance();
    initComponents();
  }

  /**
   * Liefert den Dispatcher für Transportauftragsketten.
   *
   * @return den Dispatcher
   */
  protected OrderSequenceDispatcher getDispatcher() {
    OpenTCSEventDispatcher d = (OpenTCSEventDispatcher) fOpenTCSView.getSystemModel().getEventDispatcher();
    if (d == null) {
      return null;
    }

    return d.getOrderSequenceDispatcher();
  }

  /**
   * Liefert die Leitsteuerung.
   *
   * @return die Leitsteuerung
   */
  private Kernel getKernel() {
    return kernelProxyManager.kernel();
  }

  /**
   * Holt sich alle Transportauftragsketten aus der Leitsteuerung und stellt
   * diese dar.
   */
  public void initView() {
    OrderSequenceDispatcher dispatcher = getDispatcher();
    if (dispatcher != null) {
      setOrderSequences(dispatcher.getOrderSequences());
    }
  }

  /**
   * Deletes all OrderSequences, eg after changing the kernel state.
   */
  public void clearOrderSequences() {
    if (fOrderSequences != null) {
      if (fTableModel != null) {
        for (int i = 0; i < fOrderSequences.size(); i++) {
          fTableModel.removeRow(i);
        }
      }
      fOrderSequences.clear();
    }
  }

  /**
   * Initialisiert die verschiedenen Komponenten.
   */
  protected final void initComponents() {
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
    fTableModel.setColumnIndexToFilter(5);	// Column "Finished"
    fTable = new OrdersTable(fTableModel);

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    fFilterButtons = createFilterButtons();
    JToolBar toolBar = createToolBar(fFilterButtons);
//		addControlButtons(toolBar);
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

  /**
   * Zeigt Details zur ausgewählten Transportauftragskette.
   */
  protected void showOrderSequence() {
    try {
      OrderSequence os = getSelectedOrderSequence();
      os = getKernel().getTCSObject(OrderSequence.class, os.getReference());
      DialogContent content = new OrderSequenceView(os);
      StandardContentDialog dialog = new StandardContentDialog(fOpenTCSView, content, true, StandardContentDialog.CLOSE);
      dialog.setTitle(ResourceBundleUtil.getBundle().getString("OrderSequencesContainerPanel.orderSequence"));
      dialog.setVisible(true);
    }
    catch (CredentialsException e) {
    }
  }

  /**
   * Zeigt ein Kontextmenü zu dem angewählten Transportauftrag.
   *
   * @param x die x-Position des auslösenden Mausklicks
   * @param y die y-Position des auslösenden Mausklicks
   */
  protected void showPopupMenu(int x, int y) {
    int row = fTable.rowAtPoint(new Point(x, y));
    fTable.setRowSelectionInterval(row, row);
    OrderSequence os = getSelectedOrderSequence();

    try {
      os = getKernel().getTCSObject(OrderSequence.class, os.getReference());
    }
    catch (CredentialsException e) {
    }

    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = menu.add(ResourceBundleUtil.getBundle().getString("TransportOrdersContainerPanel.popup.showDetails"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent evt) {
        showOrderSequence();
      }
    });

    menu.show(fTable, x, y);
  }

  /**
   * Liefert die selektierte Transportauftragskette.
   */
  protected OrderSequence getSelectedOrderSequence() {
    int row = fTable.getSelectedRow();

    if (row == -1) {
      return null;
    }

    int index = fTableModel.realRowIndex(row);

    return fOrderSequences.elementAt(index);
  }

  /**
   * Setzt die Liste der Transportauftragsketten.
   *
   * @param orderSequences
   */
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

  @Override // OrderSequenceListener
  public void orderSequenceAdded(OrderSequence os) {
    fOrderSequences.insertElementAt(os, 0);
    fTableModel.insertRow(0, toTableRow(os));
  }

  @Override // OrderSequenceListener
  public void orderSequenceChanged(OrderSequence os) {
    int rowIndex = fOrderSequences.indexOf(os);
    Vector<Object> values = toTableRow(os);

    for (int i = 0; i < values.size(); i++) {
      fTableModel.setValueAt(values.elementAt(i), rowIndex, i);
    }
  }

  /**
   * Erzeugt die Filterbuttons.
   */
  protected Vector<FilterButton> createFilterButtons() {
    Vector<FilterButton> buttons = new Vector<>();
    IconToolkit iconkit = IconToolkit.instance();

    FilterButton b1 = new FilterButton(iconkit.getImageIconByFullPath(fIconPath + "filterFinished.16x16.gif"), fTableModel, Boolean.FALSE);
    buttons.add(b1);
    b1.setToolTipText(ResourceBundleUtil.getBundle().getString("OrderSequencesContainerPanel.filterOrderSequences"));

    return buttons;
  }

  /**
   * Initialisiert die Toolleiste.
   */
  protected JToolBar createToolBar(Vector<FilterButton> filterButtons) {
    JToolBar toolBar = new JToolBar();
    Enumeration<FilterButton> e = filterButtons.elements();

    while (e.hasMoreElements()) {
      FilterButton button = e.nextElement();
      toolBar.add(button);
    }

    return toolBar;
  }

  @Override
  public void orderSequenceRemoved(final OrderSequence os) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        int i = fOrderSequences.indexOf(os);
        fTableModel.removeRow(i);
        fOrderSequences.removeElementAt(i);
      }
    });
  }

  /**
   * Wandelt die Werte einer Transportauftragskette in eine Tabellenzeile um.
   *
   * @param os
   * @return
   */
  private Vector toTableRow(OrderSequence os) {
    Vector<Object> row = new Vector<>();
    // Spalte 0: Name
    String name = os.getName();
    row.addElement(name);

    // Spalte 2: Gewünschtes Fahrzeug
    TCSObjectReference<Vehicle> intendedVehicle = os.getIntendedVehicle();

    if (intendedVehicle != null) {
      row.addElement(intendedVehicle.getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle().getString("TransportOrdersContainerPanel.table.determineAutomatic"));
    }

    // Spalte 3: Ausführendes Fahrzeug
    TCSObjectReference<Vehicle> processingVehicle = os.getProcessingVehicle();

    if (processingVehicle != null) {
      row.addElement(processingVehicle.getName());
    }
    else {
      row.addElement(ResourceBundleUtil.getBundle().getString("TransportOrdersContainerPanel.table.determineAutomatic"));
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
//	TCSObjectReference<TransportOrder> nextUnfinishedOrder = os.getNextUnfinishedOrder();
//	List<TCSObjectReference<TransportOrder>> orders = os.getOrders();
    return row;
  }
}
