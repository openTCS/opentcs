/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A view on a transport order.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TransportOrderView
    extends DialogContent {

  /**
   * Der anzuzeigende Transportauftrag.
   */
  private final TransportOrder fTransportOrder;
  /**
   * Sämtliche Fahraufträge.
   */
  private List<DriveOrder> fDriveOrders;

  /**
   * Creates new instance.
   *
   * @param order The transport order.
   */
  public TransportOrderView(TransportOrder order) {
    fTransportOrder = Objects.requireNonNull(order, "order is null");
    initComponents();
    setDialogTitle(ResourceBundleUtil.getBundle().getString("TransportOrderView.detailedView"));
  }

  /**
   * Liefert den Transportauftrag.
   *
   * @return den Transportauftrag
   */
  public TransportOrder getTransportOrder() {
    return fTransportOrder;
  }

  @Override
  public void update() {
    // Do nada.
  }

  @Override
  public final void initFields() {
    // --- Allgemeine Angaben ---
    // ID
    int id = getTransportOrder().getId();
    idTextField.setText(Integer.toString(id));
    // Name
    String name = getTransportOrder().getName();
    nameTextField.setText(name);
    // Erzeugt
    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    long creationTime = getTransportOrder().getCreationTime();
    createdTextField.setText(f.format(new Date(creationTime)));
    // Beendet
    long finishedTime = getTransportOrder().getFinishedTime();

    if (finishedTime > 0) {
      finishedTextField.setText(f.format(new Date(finishedTime)));
    }
    // Frist
    long deadline = getTransportOrder().getDeadline();
    deadlineTextField.setText(f.format(new Date(deadline)));
    // Fahrzeug
    TCSObjectReference<Vehicle> processingVehicle = getTransportOrder().getProcessingVehicle();

    if (processingVehicle != null) {
      vehicleTextField.setText(processingVehicle.getName());
    }

    // --- Fahraufträge ---
    // Tabelle Route
    DefaultTableModel tableModel = new UneditableTableModel();
    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle().getString("TransportOrderView.target"),
      "Operation",
      "Status"});
    // Past drive orders
    fDriveOrders = new LinkedList<>();
    fDriveOrders.addAll(getTransportOrder().getPastDriveOrders());
    // Current drive order
    if (getTransportOrder().getCurrentDriveOrder() != null) {
      fDriveOrders.add(getTransportOrder().getCurrentDriveOrder());
    }
    // Future drive orders
    fDriveOrders.addAll(getTransportOrder().getFutureDriveOrders());

    for (DriveOrder o : fDriveOrders) {
      String[] row = new String[3];
      row[0] = o.getDestination().getLocation().getName();
      row[1] = o.getDestination().getOperation();
      row[2] = o.getState().toString();
      tableModel.addRow(row);
    }

    driveOrdersTable.setModel(tableModel);
    driveOrdersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
          driveOrdersTableSelectionChanged();
        }
      }
    });

    driveOrdersScrollPane.setPreferredSize(new Dimension(200, 150));
		//	List<DriveOrder> allDriveOrders = getTransportOrder().getAllDriveOrders();
    //	TCSObjectReference<Vehicle> intendedVehicle = getTransportOrder().getIntendedVehicle();
    //	State state = getTransportOrder().getState();
    //	TCSObjectReference<OrderSequence> wrappingSequence = getTransportOrder().getWrappingSequence();

    // Route
    tableModel = new UneditableTableModel();
    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle().getString("TransportOrderView.route"),
      ResourceBundleUtil.getBundle().getString("TransportOrderView.destination")});
    routeTable.setModel(tableModel);

    // --- Abhängigkeiten ---
    tableModel = new UneditableTableModel();
    tableModel.setColumnIdentifiers(new String[] {ResourceBundleUtil.getBundle().getString("TransportOrderView.to")});

    Iterator<TCSObjectReference<TransportOrder>> iDependencies = getTransportOrder().getDependencies().iterator();
    TCSObjectReference<TransportOrder> refTransportOrder;

    while (iDependencies.hasNext()) {
      refTransportOrder = iDependencies.next();
      String[] row = new String[1];
      row[0] = refTransportOrder.getName();
      tableModel.addRow(row);
    }

    dependenciesTable.setModel(tableModel);

    // --- Zurückweisungen ---
    tableModel = new UneditableTableModel();
    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle().getString("CreateTransportOrderPanel.vehicle"),
      ResourceBundleUtil.getBundle().getString("TransportOrderView.reason")});
    ResourceBundleUtil.getBundle().getString("TransportOrderView.timestamp");
    Iterator<Rejection> iRejections = getTransportOrder().getRejections().iterator();
    Rejection rejection;

    while (iRejections.hasNext()) {
      rejection = iRejections.next();
      String[] row = new String[3];
      row[0] = rejection.getVehicle().getName();
      row[1] = rejection.getReason();
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
      row[2] = sdf.format(rejection.getTimestamp());
      tableModel.addRow(row);
    }

    rejectionsTable.setModel(tableModel);
    rejectionsTable.getColumnModel().getColumn(0).setCellRenderer(new LineWrapCellRenderer());
    rejectionsTable.getColumnModel().getColumn(1).setCellRenderer(new LineWrapCellRenderer());
  }

  /**
   * Wird aufgerufen, wenn sich die Auswahl in der Tabelle der Fahraufträge
   * geändert hat.
   */
  private void driveOrdersTableSelectionChanged() {
    int index = driveOrdersTable.getSelectedRow();
    DriveOrder o = fDriveOrders.get(index);
    DefaultTableModel m = (DefaultTableModel) routeTable.getModel();

    while (m.getRowCount() > 0) {
      m.removeRow(0);
    }

    Route route = o.getRoute();

    if (route == null) {
      return;
    }

    long costs = route.getCosts();
    costsTextField.setText(Long.toString(costs));
    Iterator<Step> i = route.getSteps().iterator();

    while (i.hasNext()) {
      Step step = i.next();
      String[] row = new String[2];

      if (step.getPath() != null) {
        row[0] = step.getPath().getName();
      }
      else {
        row[0] = "";
      }

      row[1] = step.getDestinationPoint().getName();
      m.addRow(row);
    }
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        generalPanel = new javax.swing.JPanel();
        idLabel = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        createdLabel = new javax.swing.JLabel();
        createdTextField = new javax.swing.JTextField();
        finishedLabel = new javax.swing.JLabel();
        finishedTextField = new javax.swing.JTextField();
        deadlineLabel = new javax.swing.JLabel();
        deadlineTextField = new javax.swing.JTextField();
        vehicleLabel = new javax.swing.JLabel();
        vehicleTextField = new javax.swing.JTextField();
        dependenciesPanel = new javax.swing.JPanel();
        dependenciesScrollPane = new javax.swing.JScrollPane();
        dependenciesTable = new javax.swing.JTable();
        rejectionsPanel = new javax.swing.JPanel();
        rejectionsScrollPane = new javax.swing.JScrollPane();
        rejectionsTable = new javax.swing.JTable();
        driveOrdersPanel = new javax.swing.JPanel();
        driveOrdersScrollPane = new javax.swing.JScrollPane();
        driveOrdersTable = new javax.swing.JTable();
        routeScrollPane = new javax.swing.JScrollPane();
        routeTable = new javax.swing.JTable();
        driveOrdersLabel = new javax.swing.JLabel();
        routeLabel = new javax.swing.JLabel();
        costsLabel = new javax.swing.JLabel();
        costsTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TransportOrderView.generalPanel.title"))); // NOI18N
        generalPanel.setLayout(new java.awt.GridBagLayout());

        idLabel.setFont(idLabel.getFont());
        idLabel.setText("Id:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        generalPanel.add(idLabel, gridBagConstraints);

        idTextField.setColumns(10);
        idTextField.setEditable(false);
        idTextField.setFont(idTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(idTextField, gridBagConstraints);

        nameLabel.setFont(nameLabel.getFont());
        nameLabel.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
        generalPanel.add(nameLabel, gridBagConstraints);

        nameTextField.setColumns(10);
        nameTextField.setEditable(false);
        nameTextField.setFont(nameTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(nameTextField, gridBagConstraints);

        createdLabel.setFont(createdLabel.getFont());
        createdLabel.setText(bundle.getString("TransportOrderView.created")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        generalPanel.add(createdLabel, gridBagConstraints);

        createdTextField.setColumns(10);
        createdTextField.setEditable(false);
        createdTextField.setFont(createdTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(createdTextField, gridBagConstraints);

        finishedLabel.setFont(finishedLabel.getFont());
        finishedLabel.setText(bundle.getString("TransportOrderView.finished")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
        generalPanel.add(finishedLabel, gridBagConstraints);

        finishedTextField.setColumns(10);
        finishedTextField.setEditable(false);
        finishedTextField.setFont(finishedTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(finishedTextField, gridBagConstraints);

        deadlineLabel.setFont(deadlineLabel.getFont());
        deadlineLabel.setText(bundle.getString("TransportOrderView.deadline")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        generalPanel.add(deadlineLabel, gridBagConstraints);

        deadlineTextField.setColumns(10);
        deadlineTextField.setEditable(false);
        deadlineTextField.setFont(deadlineTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(deadlineTextField, gridBagConstraints);

        vehicleLabel.setFont(vehicleLabel.getFont());
        vehicleLabel.setText(bundle.getString("TransportOrderView.vehicle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
        generalPanel.add(vehicleLabel, gridBagConstraints);

        vehicleTextField.setColumns(10);
        vehicleTextField.setEditable(false);
        vehicleTextField.setFont(vehicleTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        generalPanel.add(vehicleTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        add(generalPanel, gridBagConstraints);

        dependenciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TransportOrderView.dependenciesPanel.title"))); // NOI18N
        dependenciesPanel.setLayout(new java.awt.BorderLayout());

        dependenciesScrollPane.setPreferredSize(new java.awt.Dimension(150, 100));

        dependenciesTable.setFont(dependenciesTable.getFont());
        dependenciesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dependenciesScrollPane.setViewportView(dependenciesTable);

        dependenciesPanel.add(dependenciesScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(dependenciesPanel, gridBagConstraints);

        rejectionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TransportOrderView.rejectionsPanel.title"))); // NOI18N
        rejectionsPanel.setLayout(new java.awt.BorderLayout());

        rejectionsScrollPane.setPreferredSize(new java.awt.Dimension(150, 100));

        rejectionsTable.setFont(rejectionsTable.getFont());
        rejectionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        rejectionsScrollPane.setViewportView(rejectionsTable);

        rejectionsPanel.add(rejectionsScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(rejectionsPanel, gridBagConstraints);

        driveOrdersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TransportOrderView.driveOrdersPanel.title"))); // NOI18N
        driveOrdersPanel.setLayout(new java.awt.GridBagLayout());

        driveOrdersScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        driveOrdersScrollPane.setViewportView(driveOrdersTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        driveOrdersPanel.add(driveOrdersScrollPane, gridBagConstraints);

        routeScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        routeScrollPane.setViewportView(routeTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        driveOrdersPanel.add(routeScrollPane, gridBagConstraints);

        driveOrdersLabel.setFont(driveOrdersLabel.getFont());
        driveOrdersLabel.setText(bundle.getString("TransportOrderView.tos")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        driveOrdersPanel.add(driveOrdersLabel, gridBagConstraints);

        routeLabel.setFont(routeLabel.getFont());
        routeLabel.setText("Route:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        driveOrdersPanel.add(routeLabel, gridBagConstraints);

        costsLabel.setFont(costsLabel.getFont());
        costsLabel.setText(bundle.getString("TransportOrderView.costs")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        driveOrdersPanel.add(costsLabel, gridBagConstraints);

        costsTextField.setColumns(5);
        costsTextField.setEditable(false);
        costsTextField.setFont(costsTextField.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        driveOrdersPanel.add(costsTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(driveOrdersPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel costsLabel;
    private javax.swing.JTextField costsTextField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JTextField createdTextField;
    private javax.swing.JLabel deadlineLabel;
    private javax.swing.JTextField deadlineTextField;
    private javax.swing.JPanel dependenciesPanel;
    private javax.swing.JScrollPane dependenciesScrollPane;
    private javax.swing.JTable dependenciesTable;
    private javax.swing.JLabel driveOrdersLabel;
    private javax.swing.JPanel driveOrdersPanel;
    private javax.swing.JScrollPane driveOrdersScrollPane;
    private javax.swing.JTable driveOrdersTable;
    private javax.swing.JLabel finishedLabel;
    private javax.swing.JTextField finishedTextField;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel rejectionsPanel;
    private javax.swing.JScrollPane rejectionsScrollPane;
    private javax.swing.JTable rejectionsTable;
    private javax.swing.JLabel routeLabel;
    private javax.swing.JScrollPane routeScrollPane;
    private javax.swing.JTable routeTable;
    private javax.swing.JLabel vehicleLabel;
    private javax.swing.JTextField vehicleTextField;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * A cell renderer that supports multiple lines.
   */
  private class LineWrapCellRenderer
      extends JTextArea
      implements TableCellRenderer {

    /**
     * Creates a new LineWrapCellRenderer.
     */
    public LineWrapCellRenderer() {
      this.setWrapStyleWord(true);
      this.setLineWrap(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {

      setText((String) value);
      setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

      if (table.getRowHeight(row) != getPreferredSize().height) {
        table.setRowHeight(row, getPreferredSize().height);
      }

      return this;
    }
  }
}
