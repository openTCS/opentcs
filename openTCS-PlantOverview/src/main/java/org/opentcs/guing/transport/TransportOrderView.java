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

import com.google.inject.assistedinject.Assisted;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A view on a transport order.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderView
    extends DialogContent {

  /**
   * A formatter for timestamps.
   */
  private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  /**
   * A formatter for history entries.
   */
  private final ObjectHistoryEntryFormatter historyEntryFormatter;
  /**
   * The transport order to be shown.
   */
  private final TransportOrder fTransportOrder;

  /**
   * Creates new instance.
   *
   * @param order The transport order.
   * @param historyEntryFormatter A formatter for history entries.
   */
  @Inject
  public TransportOrderView(@Assisted TransportOrder order,
                            CompositeObjectHistoryEntryFormatter historyEntryFormatter) {
    this.fTransportOrder = requireNonNull(order, "order");
    this.historyEntryFormatter = requireNonNull(historyEntryFormatter, "historyEntryFormatter");

    initComponents();
    setDialogTitle(ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH)
        .getString("transportOrderView.title"));
  }

  @Override
  public void update() {
  }

  @Override
  public final void initFields() {
    nameTextField.setText(fTransportOrder.getName());

    createdTextField.setText(TIMESTAMP_FORMAT.format(new Date(fTransportOrder.getCreationTime())));

    finishedTextField.setText(fTransportOrder.getFinishedTime() != Long.MAX_VALUE
        ? TIMESTAMP_FORMAT.format(new Date(fTransportOrder.getFinishedTime()))
        : "-"
    );

    deadlineTextField.setText(fTransportOrder.getDeadline() != Long.MAX_VALUE
        ? TIMESTAMP_FORMAT.format(new Date(fTransportOrder.getDeadline()))
        : "-"
    );

    dispensableTextField.setText(Boolean.toString(fTransportOrder.isDispensable()));

    if (fTransportOrder.getProcessingVehicle() != null) {
      vehicleTextField.setText(fTransportOrder.getProcessingVehicle().getName());
    }

    categoryTextField.setText(fTransportOrder.getCategory());

    propertiesTable.setModel(createPropertiesTableModel());

    driveOrdersTable.setModel(createDriveOrdersTableModel());
    driveOrdersTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
      if (!evt.getValueIsAdjusting()) {
        driveOrdersTableSelectionChanged();
      }
    });

    driveOrdersScrollPane.setPreferredSize(new Dimension(200, 150));

    driveOrderPropertiesTable.setModel(createDriveOrderPropertiesTableModel());

    routeTable.setModel(createRouteTableModel());

    dependenciesTable.setModel(createDependenciesTableModel());

    historyTable.setModel(createHistoryTableModel());
    historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    historyTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    historyTable.getColumnModel().getColumn(1).setCellRenderer(new ToolTipCellRenderer());
  }

  private TableModel createPropertiesTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_properties.column_propertiesKey.headerText"),
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_properties.column_propertiesValue.headerText")});
    for (Entry<String, String> entry : fTransportOrder.getProperties().entrySet()) {
      tableModel.addRow(new String[] {entry.getKey(), entry.getValue()});
    }

    return tableModel;
  }

  private TableModel createDriveOrdersTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_driveOrderProperties.column_target.headerText"),
      "Operation",
      "Status"});

    for (DriveOrder o : fTransportOrder.getAllDriveOrders()) {
      String[] row = new String[3];
      row[0] = o.getDestination().getDestination().getName();
      row[1] = o.getDestination().getOperation();
      row[2] = o.getState().toString();
      tableModel.addRow(row);
    }

    return tableModel;
  }

  private TableModel createDriveOrderPropertiesTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_driveOrderProperties.column_driveOrderPropertiesKey.headerText"),
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_driveOrderProperties.column_driveOrderPropertiesValue.headerText")
    });

    return tableModel;
  }

  private TableModel createRouteTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_route.column_route.headerText"),
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_routeTable.column_destination.headerText")});

    return tableModel;
  }

  private TableModel createDependenciesTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_dependencies.column_dependentTransportOrder.headerText")});

    for (TCSObjectReference<TransportOrder> refTransportOrder
             : fTransportOrder.getDependencies()) {
      String[] row = new String[1];
      row[0] = refTransportOrder.getName();
      tableModel.addRow(row);
    }

    return tableModel;
  }

  private TableModel createHistoryTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(new String[] {
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_history.column_timestamp.headerText"),
      ResourceBundleUtil.getBundle(I18nPlantOverview.TODETAIL_PATH).getString("transportOrderView.table_history.column_event.headerText")
    });

    for (ObjectHistory.Entry entry : fTransportOrder.getHistory().getEntries()) {
      tableModel.addRow(new String[] {
        TIMESTAMP_FORMAT.format(Date.from(entry.getTimestamp())),
        historyEntryFormatter.apply(entry).get()
      });
    }

    return tableModel;
  }

  /**
   * Wird aufgerufen, wenn sich die Auswahl in der Tabelle der Fahraufträge
   * geändert hat.
   */
  private void driveOrdersTableSelectionChanged() {
    DriveOrder driveOrder
        = fTransportOrder.getAllDriveOrders().get(driveOrdersTable.getSelectedRow());
    DefaultTableModel routeTableModel = (DefaultTableModel) routeTable.getModel();
    DefaultTableModel driveOrderPropsTableModel
        = (DefaultTableModel) driveOrderPropertiesTable.getModel();

    while (routeTableModel.getRowCount() > 0) {
      routeTableModel.removeRow(0);
    }
    while (driveOrderPropsTableModel.getRowCount() > 0) {
      driveOrderPropsTableModel.removeRow(0);
    }

    for (Entry<String, String> entry : driveOrder.getDestination().getProperties().entrySet()) {
      driveOrderPropsTableModel.addRow(new String[] {entry.getKey(), entry.getValue()});
    }

    if (driveOrder.getRoute() == null) {
      return;
    }

    costsTextField.setText(Long.toString(driveOrder.getRoute().getCosts()));

    for (Step step : driveOrder.getRoute().getSteps()) {
      routeTableModel.addRow(new String[] {
        step.getPath() == null ? "" : step.getPath().getName(),
        step.getDestinationPoint().getName()
      });
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
    dispensableLabel = new javax.swing.JLabel();
    dispensableTextField = new javax.swing.JTextField();
    categoryLabel = new javax.swing.JLabel();
    categoryTextField = new javax.swing.JTextField();
    dependenciesPanel = new javax.swing.JPanel();
    dependenciesScrollPane = new javax.swing.JScrollPane();
    dependenciesTable = new javax.swing.JTable();
    propertiesPanel = new javax.swing.JPanel();
    propertiesScrollPane = new javax.swing.JScrollPane();
    propertiesTable = new javax.swing.JTable();
    historyPanel = new javax.swing.JPanel();
    historyScrollPane = new javax.swing.JScrollPane();
    historyTable = new javax.swing.JTable();
    driveOrdersPanel = new javax.swing.JPanel();
    driveOrdersScrollPane = new javax.swing.JScrollPane();
    driveOrdersTable = new javax.swing.JTable();
    driveOrdersPropertiesPanel = new javax.swing.JPanel();
    driveOrdersPropertiesScrollPane = new javax.swing.JScrollPane();
    driveOrderPropertiesTable = new javax.swing.JTable();
    routePanel = new javax.swing.JPanel();
    routeScrollPane = new javax.swing.JScrollPane();
    routeTable = new javax.swing.JTable();
    costsLabel = new javax.swing.JLabel();
    costsTextField = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/transportOrderDetail"); // NOI18N
    generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_general.border.title"))); // NOI18N
    generalPanel.setLayout(new java.awt.GridBagLayout());

    nameLabel.setFont(nameLabel.getFont());
    nameLabel.setText("Name:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(nameLabel, gridBagConstraints);

    nameTextField.setEditable(false);
    nameTextField.setColumns(10);
    nameTextField.setFont(nameTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(nameTextField, gridBagConstraints);

    createdLabel.setFont(createdLabel.getFont());
    createdLabel.setText(bundle.getString("transportOrderView.label_created.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(createdLabel, gridBagConstraints);

    createdTextField.setEditable(false);
    createdTextField.setColumns(10);
    createdTextField.setFont(createdTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(createdTextField, gridBagConstraints);

    finishedLabel.setFont(finishedLabel.getFont());
    finishedLabel.setText(bundle.getString("transportOrderView.label_finished.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    generalPanel.add(finishedLabel, gridBagConstraints);

    finishedTextField.setEditable(false);
    finishedTextField.setColumns(10);
    finishedTextField.setFont(finishedTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(finishedTextField, gridBagConstraints);

    deadlineLabel.setFont(deadlineLabel.getFont());
    deadlineLabel.setText(bundle.getString("transportOrderView.label_deadline.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(deadlineLabel, gridBagConstraints);

    deadlineTextField.setEditable(false);
    deadlineTextField.setColumns(10);
    deadlineTextField.setFont(deadlineTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(deadlineTextField, gridBagConstraints);

    vehicleLabel.setFont(vehicleLabel.getFont());
    vehicleLabel.setText(bundle.getString("transportOrderView.label_vehicle.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    generalPanel.add(vehicleLabel, gridBagConstraints);

    vehicleTextField.setEditable(false);
    vehicleTextField.setColumns(10);
    vehicleTextField.setFont(vehicleTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(vehicleTextField, gridBagConstraints);

    dispensableLabel.setFont(dispensableLabel.getFont());
    dispensableLabel.setText(bundle.getString("transportOrderView.label_dispensable.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(dispensableLabel, gridBagConstraints);

    dispensableTextField.setEditable(false);
    dispensableTextField.setColumns(10);
    dispensableTextField.setFont(dispensableTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(dispensableTextField, gridBagConstraints);

    categoryLabel.setFont(categoryLabel.getFont());
    categoryLabel.setText(bundle.getString("transportOrderView.label_category.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    generalPanel.add(categoryLabel, gridBagConstraints);

    categoryTextField.setEditable(false);
    categoryTextField.setColumns(10);
    categoryTextField.setFont(categoryTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    generalPanel.add(categoryTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    add(generalPanel, gridBagConstraints);

    dependenciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_dependencies.border.title"))); // NOI18N
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

    propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_properties.border.title"))); // NOI18N
    propertiesPanel.setLayout(new java.awt.BorderLayout());

    propertiesScrollPane.setPreferredSize(new java.awt.Dimension(150, 100));

    propertiesTable.setFont(propertiesTable.getFont());
    propertiesScrollPane.setViewportView(propertiesTable);

    propertiesPanel.add(propertiesScrollPane, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    add(propertiesPanel, gridBagConstraints);

    historyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_history.border.title"))); // NOI18N
    historyPanel.setLayout(new java.awt.BorderLayout());

    historyScrollPane.setPreferredSize(new java.awt.Dimension(150, 100));

    historyTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    historyScrollPane.setViewportView(historyTable);

    historyPanel.add(historyScrollPane, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    add(historyPanel, gridBagConstraints);

    driveOrdersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_driveOrders.border.title"))); // NOI18N
    driveOrdersPanel.setLayout(new java.awt.GridBagLayout());

    driveOrdersScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
    driveOrdersScrollPane.setViewportView(driveOrdersTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    driveOrdersPanel.add(driveOrdersScrollPane, gridBagConstraints);

    driveOrdersPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_driveOrderProperties.border.title"))); // NOI18N
    driveOrdersPropertiesPanel.setPreferredSize(new java.awt.Dimension(162, 140));
    driveOrdersPropertiesPanel.setLayout(new java.awt.BorderLayout());

    driveOrdersPropertiesScrollPane.setPreferredSize(new java.awt.Dimension(150, 50));

    driveOrderPropertiesTable.setFont(driveOrderPropertiesTable.getFont());
    driveOrdersPropertiesScrollPane.setViewportView(driveOrderPropertiesTable);

    driveOrdersPropertiesPanel.add(driveOrdersPropertiesScrollPane, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    driveOrdersPanel.add(driveOrdersPropertiesPanel, gridBagConstraints);

    routePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("transportOrderView.panel_route.border.title"))); // NOI18N
    routePanel.setPreferredSize(new java.awt.Dimension(300, 140));
    routePanel.setLayout(new java.awt.BorderLayout());

    routeScrollPane.setViewportView(routeTable);

    routePanel.add(routeScrollPane, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    driveOrdersPanel.add(routePanel, gridBagConstraints);

    costsLabel.setFont(costsLabel.getFont());
    costsLabel.setText(bundle.getString("transportOrderView.label_costs.title")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    driveOrdersPanel.add(costsLabel, gridBagConstraints);

    costsTextField.setEditable(false);
    costsTextField.setColumns(5);
    costsTextField.setFont(costsTextField.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    driveOrdersPanel.add(costsTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 0.5;
    add(driveOrdersPanel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel categoryLabel;
  private javax.swing.JTextField categoryTextField;
  private javax.swing.JLabel costsLabel;
  private javax.swing.JTextField costsTextField;
  private javax.swing.JLabel createdLabel;
  private javax.swing.JTextField createdTextField;
  private javax.swing.JLabel deadlineLabel;
  private javax.swing.JTextField deadlineTextField;
  private javax.swing.JPanel dependenciesPanel;
  private javax.swing.JScrollPane dependenciesScrollPane;
  private javax.swing.JTable dependenciesTable;
  private javax.swing.JLabel dispensableLabel;
  private javax.swing.JTextField dispensableTextField;
  private javax.swing.JTable driveOrderPropertiesTable;
  private javax.swing.JPanel driveOrdersPanel;
  private javax.swing.JPanel driveOrdersPropertiesPanel;
  private javax.swing.JScrollPane driveOrdersPropertiesScrollPane;
  private javax.swing.JScrollPane driveOrdersScrollPane;
  private javax.swing.JTable driveOrdersTable;
  private javax.swing.JLabel finishedLabel;
  private javax.swing.JTextField finishedTextField;
  private javax.swing.JPanel generalPanel;
  private javax.swing.JPanel historyPanel;
  private javax.swing.JScrollPane historyScrollPane;
  private javax.swing.JTable historyTable;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JPanel propertiesPanel;
  private javax.swing.JScrollPane propertiesScrollPane;
  private javax.swing.JTable propertiesTable;
  private javax.swing.JPanel routePanel;
  private javax.swing.JScrollPane routeScrollPane;
  private javax.swing.JTable routeTable;
  private javax.swing.JLabel vehicleLabel;
  private javax.swing.JTextField vehicleTextField;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * A cell renderer that adds a tool tip with the cell's value.
   */
  private static class ToolTipCellRenderer
      extends DefaultTableCellRenderer {

    /**
     * Creates a new instance.
     */
    public ToolTipCellRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      Component component = super.getTableCellRendererComponent(table,
                                                                value,
                                                                isSelected,
                                                                hasFocus,
                                                                row,
                                                                column);

      ((JComponent) component).setToolTipText(value.toString());

      return component;
    }
  }
}
