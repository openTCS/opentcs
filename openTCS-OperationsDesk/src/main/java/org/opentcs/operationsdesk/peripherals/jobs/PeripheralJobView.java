/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import com.google.inject.assistedinject.Assisted;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.opentcs.components.plantoverview.ObjectHistoryEntryFormatter;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.operationsdesk.transport.CompositeObjectHistoryEntryFormatter;
import org.opentcs.operationsdesk.transport.UneditableTableModel;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A view on a peripheral job.
 */
public class PeripheralJobView
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
   * The peripheral job to be shown.
   */
  private final PeripheralJob peripheralJob;

  /**
   * Creates new instance.
   *
   * @param job The peripheral job.
   * @param historyEntryFormatter A formatter for history entries.
   */
  @Inject
  public PeripheralJobView(@Nonnull @Assisted PeripheralJob job,
                           @Nonnull CompositeObjectHistoryEntryFormatter historyEntryFormatter) {
    this.peripheralJob = requireNonNull(job, "job");
    this.historyEntryFormatter = requireNonNull(historyEntryFormatter, "historyEntryFormatter");

    initComponents();
    setDialogTitle(ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH)
        .getString("peripheralJobView.title"));
  }

  @Override
  public void update() {
  }

  @Override
  public final void initFields() {
    nameTextField.setText(peripheralJob.getName());

    stateTextField.setText(peripheralJob.getState().name());

    createdTextField.setText(TIMESTAMP_FORMAT.format(Date.from(peripheralJob.getCreationTime())));

    finishedTextField.setText(!peripheralJob.getFinishedTime().equals(Instant.MAX)
        ? TIMESTAMP_FORMAT.format(Date.from(peripheralJob.getFinishedTime()))
        : "-"
    );

    if (peripheralJob.getRelatedVehicle() != null) {
      vehicleTextField.setText(peripheralJob.getRelatedVehicle().getName());
    }
    else {
      vehicleTextField.setText("-");
    }

    reservationTokenTextField.setText(peripheralJob.getReservationToken());

    if (peripheralJob.getRelatedTransportOrder() != null) {
      relatedTransportOrderTextField.setText(peripheralJob.getRelatedTransportOrder().getName());
    }
    else {
      relatedTransportOrderTextField.setText("-");
    }

    locationTextField.setText(peripheralJob.getPeripheralOperation().getLocation().getName());
    operationTextField.setText(peripheralJob.getPeripheralOperation().getOperation());
    triggerTextField.setText(peripheralJob.getPeripheralOperation().getExecutionTrigger().name());
    requireCompletionTextField.setText(
        String.valueOf(peripheralJob.getPeripheralOperation().isCompletionRequired())
    );

    propertiesTable.setModel(createPropertiesTableModel());

    historyTable.setModel(createHistoryTableModel());
    historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    historyTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    historyTable.getColumnModel().getColumn(1).setCellRenderer(new ToolTipCellRenderer());
  }

  private TableModel createPropertiesTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(
        new String[]{
          ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH)
              .getString(
                  "peripheralJobView.table_properties.column_propertiesKey.headerText"
              ),
          ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH)
              .getString(
                  "peripheralJobView.table_properties.column_propertiesValue.headerText"
              )
        }
    );
    peripheralJob.getProperties().entrySet().stream()
        .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
        .forEach(entry -> {
          tableModel.addRow(new String[]{entry.getKey(), entry.getValue()});
        });

    return tableModel;
  }

  private TableModel createHistoryTableModel() {
    DefaultTableModel tableModel = new UneditableTableModel();

    tableModel.setColumnIdentifiers(
        new String[]{
          ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH)
              .getString("peripheralJobView.table_history.column_timestamp.headerText"),
          ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.PJDETAIL_PATH)
              .getString("peripheralJobView.table_history.column_event.headerText")
        }
    );

    for (ObjectHistory.Entry entry : peripheralJob.getHistory().getEntries()) {
      tableModel.addRow(new String[]{
        TIMESTAMP_FORMAT.format(Date.from(entry.getTimestamp())),
        historyEntryFormatter.apply(entry).get()
      });
    }

    return tableModel;
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
    vehicleLabel = new javax.swing.JLabel();
    vehicleTextField = new javax.swing.JTextField();
    reservationTokenLabel = new javax.swing.JLabel();
    reservationTokenTextField = new javax.swing.JTextField();
    relatedTransportOrderLabel = new javax.swing.JLabel();
    relatedTransportOrderTextField = new javax.swing.JTextField();
    stateLabel = new javax.swing.JLabel();
    stateTextField = new javax.swing.JTextField();
    propertiesPanel = new javax.swing.JPanel();
    propertiesScrollPane = new javax.swing.JScrollPane();
    propertiesTable = new javax.swing.JTable();
    historyPanel = new javax.swing.JPanel();
    historyScrollPane = new javax.swing.JScrollPane();
    historyTable = new javax.swing.JTable();
    operationPanel = new javax.swing.JPanel();
    locationLabel = new javax.swing.JLabel();
    locationTextField = new javax.swing.JTextField();
    triggerLabel = new javax.swing.JLabel();
    triggerTextField = new javax.swing.JTextField();
    requireCompletionLabel = new javax.swing.JLabel();
    requireCompletionTextField = new javax.swing.JTextField();
    operationLabel = new javax.swing.JLabel();
    operationTextField = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/operating/dialogs/peripheralJobDetail"); // NOI18N
    generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("peripheralJobView.panel_general.border.title"))); // NOI18N
    generalPanel.setLayout(new java.awt.GridBagLayout());

    nameLabel.setFont(nameLabel.getFont());
    nameLabel.setText(bundle.getString("peripheralJobView.panel_general.label_name.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(nameLabel, gridBagConstraints);

    nameTextField.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(nameTextField, gridBagConstraints);

    createdLabel.setFont(createdLabel.getFont());
    createdLabel.setText(bundle.getString("peripheralJobView.panel_general.label_created.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(createdLabel, gridBagConstraints);

    createdTextField.setEditable(false);
    createdTextField.setColumns(12);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(createdTextField, gridBagConstraints);

    finishedLabel.setFont(finishedLabel.getFont());
    finishedLabel.setText(bundle.getString("peripheralJobView.panel_general.label_finished.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    generalPanel.add(finishedLabel, gridBagConstraints);

    finishedTextField.setEditable(false);
    finishedTextField.setColumns(12);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(finishedTextField, gridBagConstraints);

    vehicleLabel.setFont(vehicleLabel.getFont());
    vehicleLabel.setText(bundle.getString("peripheralJobView.panel_general.label_vehicle.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(vehicleLabel, gridBagConstraints);

    vehicleTextField.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(vehicleTextField, gridBagConstraints);

    reservationTokenLabel.setText(bundle.getString("peripheralJobView.panel_general.label_reservationToken.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(reservationTokenLabel, gridBagConstraints);

    reservationTokenTextField.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(reservationTokenTextField, gridBagConstraints);

    relatedTransportOrderLabel.setText(bundle.getString("peripheralJobView.panel_general.label_transportOrder.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    generalPanel.add(relatedTransportOrderLabel, gridBagConstraints);

    relatedTransportOrderTextField.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(relatedTransportOrderTextField, gridBagConstraints);

    stateLabel.setText(bundle.getString("peripheralJobView.panel_general.label_state.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    generalPanel.add(stateLabel, gridBagConstraints);

    stateTextField.setEditable(false);
    stateTextField.setColumns(14);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    generalPanel.add(stateTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    add(generalPanel, gridBagConstraints);

    propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("peripheralJobView.panel_properties.border.title"))); // NOI18N
    propertiesPanel.setLayout(new java.awt.BorderLayout());

    propertiesScrollPane.setPreferredSize(new java.awt.Dimension(150, 100));

    propertiesTable.setFont(propertiesTable.getFont());
    propertiesScrollPane.setViewportView(propertiesTable);

    propertiesPanel.add(propertiesScrollPane, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    add(propertiesPanel, gridBagConstraints);

    historyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("peripheralJobView.panel_history.border.title"))); // NOI18N
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
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    add(historyPanel, gridBagConstraints);

    operationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("peripheralJobView.panel_operation.border.title"))); // NOI18N
    operationPanel.setLayout(new java.awt.GridBagLayout());

    locationLabel.setFont(locationLabel.getFont());
    locationLabel.setText(bundle.getString("peripheralJobView.panel_operation.lable_location.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    operationPanel.add(locationLabel, gridBagConstraints);

    locationTextField.setEditable(false);
    locationTextField.setColumns(10);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    operationPanel.add(locationTextField, gridBagConstraints);

    triggerLabel.setFont(triggerLabel.getFont());
    triggerLabel.setText(bundle.getString("peripheralJobView.panel_operation.lable_trigger.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    operationPanel.add(triggerLabel, gridBagConstraints);

    triggerTextField.setEditable(false);
    triggerTextField.setColumns(14);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    operationPanel.add(triggerTextField, gridBagConstraints);

    requireCompletionLabel.setFont(requireCompletionLabel.getFont());
    requireCompletionLabel.setText(bundle.getString("peripheralJobView.panel_operation.lable_requireCompletion.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    operationPanel.add(requireCompletionLabel, gridBagConstraints);

    requireCompletionTextField.setEditable(false);
    requireCompletionTextField.setColumns(10);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    operationPanel.add(requireCompletionTextField, gridBagConstraints);

    operationLabel.setText(bundle.getString("peripheralJobView.panel_operation.lable_operation.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    operationPanel.add(operationLabel, gridBagConstraints);

    operationTextField.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    operationPanel.add(operationTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    add(operationPanel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel createdLabel;
  private javax.swing.JTextField createdTextField;
  private javax.swing.JLabel finishedLabel;
  private javax.swing.JTextField finishedTextField;
  private javax.swing.JPanel generalPanel;
  private javax.swing.JPanel historyPanel;
  private javax.swing.JScrollPane historyScrollPane;
  private javax.swing.JTable historyTable;
  private javax.swing.JLabel locationLabel;
  private javax.swing.JTextField locationTextField;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JLabel operationLabel;
  private javax.swing.JPanel operationPanel;
  private javax.swing.JTextField operationTextField;
  private javax.swing.JPanel propertiesPanel;
  private javax.swing.JScrollPane propertiesScrollPane;
  private javax.swing.JTable propertiesTable;
  private javax.swing.JLabel relatedTransportOrderLabel;
  private javax.swing.JTextField relatedTransportOrderTextField;
  private javax.swing.JLabel requireCompletionLabel;
  private javax.swing.JTextField requireCompletionTextField;
  private javax.swing.JLabel reservationTokenLabel;
  private javax.swing.JTextField reservationTokenTextField;
  private javax.swing.JLabel stateLabel;
  private javax.swing.JTextField stateTextField;
  private javax.swing.JLabel triggerLabel;
  private javax.swing.JTextField triggerTextField;
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
    ToolTipCellRenderer() {
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
