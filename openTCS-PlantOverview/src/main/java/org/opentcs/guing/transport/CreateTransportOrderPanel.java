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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.EditDriveOrderPanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.model.AbstractConnectableModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;

/**
 * Allows creation of transport orders.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CreateTransportOrderPanel
    extends DialogContent {

  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(I18nPlantOverview.CREATETO_PATH);
  /**
   * The selected deadline.
   */
  private long fSelectedDeadline;
  /**
   * The destinations to drive to.
   */
  private final List<AbstractConnectableModelComponent> fDestinationModels = new ArrayList<>();
  /**
   * The actions to perform at the destinations.
   */
  private final List<String> fActions = new ArrayList<>();
  /**
   * The transport order's properties.
   */
  private final List<Map<String, String>> fPropertiesList = new ArrayList<>();
  /**
   * The transport order's category.
   */
  private final List<String> fCategories;
  /**
   * The available vehicles.
   */
  private final List<VehicleModel> fVehicles;
  /**
   * The manager for accessing the current system model.
   */
  private final ModelManager fModelManager;
  /**
   * The transport order used as a template.
   */
  private TransportOrder fPattern;

  /**
   * Creates new instance.
   *
   * @param modelManager The manager for accessing the current system model.
   * @param categorySuggestionsPool The transport order categories to suggest.
   */
  @Inject
  public CreateTransportOrderPanel(ModelManager modelManager,
                                   OrderCategorySuggestionsPool categorySuggestionsPool) {
    this.fModelManager = requireNonNull(modelManager, "modelManager");
    requireNonNull(categorySuggestionsPool, "categorySuggestionsPool");

    initComponents();
    Object[] columnNames = {
      bundle.getString("createTransportOrderPanel.table_driveOrdersTable.column_location.headerText"),
      bundle.getString("createTransportOrderPanel.table_driveOrdersTable.column_action.headerText")
    };
    DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
    model.setColumnIdentifiers(columnNames);

    driveOrdersTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
      if (!evt.getValueIsAdjusting()) {
        updateButtons();
      }
    });

    fVehicles = fModelManager.getModel().getVehicleModels();
    Collections.sort(fVehicles, (v1, v2) -> v1.getName().compareToIgnoreCase(v2.getName()));

    fCategories = new ArrayList<>(categorySuggestionsPool.getCategorySuggestions());
    initTitle();
  }

  private void initTitle() {
    setDialogTitle(bundle.getString("transportOrdersContainerPanel.dialog.title"));
  }

  public List<AbstractConnectableModelComponent> getDestinationModels() {
    return fDestinationModels;
  }

  public List<String> getActions() {
    return fActions;
  }

  public List<Map<String, String>> getPropertiesList() {
    return fPropertiesList;
  }

  public long getSelectedDeadline() {
    return fSelectedDeadline;
  }

  public VehicleModel getSelectedVehicle() {
    if (vehicleComboBox.getSelectedIndex() == 0) {
      return null;
    }

    return fVehicles.get(vehicleComboBox.getSelectedIndex() - 1);
  }

  public String getSelectedCategory() {
    if (categoryComboBox.getSelectedItem() == null) {
      return OrderConstants.CATEGORY_NONE;
    }

    return categoryComboBox.getSelectedItem().toString();
  }

  @Override
  public void update() {
    try {
      updateFailed = false;
      SimpleDateFormat deadlineFormat = new SimpleDateFormat("dd.MM.yyyyHH:mm");
      Date date = deadlineFormat.parse(dateTextField.getText() + timeTextField.getText());
      ZonedDateTime deadline = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
      fSelectedDeadline = deadline.toInstant().toEpochMilli();
    }
    catch (ParseException e) {
      JOptionPane.showMessageDialog(this,
                                    bundle.getString("createTransportOrderPanel.optionPane_dateTimeParseError.message"),
                                    bundle.getString("createTransportOrderPanel.optionPane_dateTimeParseError.title"),
                                    JOptionPane.ERROR_MESSAGE);
      updateFailed = true;
    }

    if (fDestinationModels.isEmpty()) {
      JOptionPane.showMessageDialog(this,
                                    bundle.getString("createTransportOrderPanel.optionPane_noOrderError.message"),
                                    bundle.getString("createTransportOrderPanel.optionPane_noOrderError.title"),
                                    JOptionPane.ERROR_MESSAGE);
      updateFailed = true;
    }
  }

  @Override
  public void initFields() {
    vehicleComboBox.addItem(bundle.getString("createTransportOrderPanel.comboBox_automatic.text"));

    for (VehicleModel vehicleModel : fVehicles) {
      vehicleComboBox.addItem(vehicleModel.getName());
    }

    for (String category : fCategories) {
      categoryComboBox.addItem(category);
    }

    ZonedDateTime newDeadline = ZonedDateTime.now(ZoneId.systemDefault()).plusHours(1);
    dateTextField.setText(newDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    timeTextField.setText(newDeadline.format(DateTimeFormatter.ofPattern("HH:mm")));

    if (fPattern != null) {
      newDeadline = ZonedDateTime.ofInstant(Instant.ofEpochMilli(fPattern.getDeadline()),
                                            ZoneId.systemDefault());
      dateTextField.setText(newDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
      timeTextField.setText(newDeadline.format(DateTimeFormatter.ofPattern("HH:mm")));

      if (fPattern.getIntendedVehicle() != null) {
        vehicleComboBox.setSelectedItem(fPattern.getIntendedVehicle().getName());
      }

      categoryComboBox.setSelectedItem(fPattern.getCategory());

      List<DriveOrder> driveOrders = new LinkedList<>();
      driveOrders.addAll(fPattern.getAllDriveOrders());

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      for (DriveOrder o : driveOrders) {
        String destination = o.getDestination().getDestination().getName();
        String action = o.getDestination().getOperation();
        Map<String, String> properties = o.getDestination().getProperties();

        String[] row = new String[2];
        row[0] = destination;
        row[1] = action;
        model.addRow(row);
        AbstractConnectableModelComponent destModel = fModelManager.getModel().getLocationModel(destination);
        if (destModel == null) {
          destModel = fModelManager.getModel().getPointModel(destination);
        }
        fDestinationModels.add(destModel);
        fActions.add(action);
        fPropertiesList.add(properties);
      }
    }

    updateButtons();
  }

  public void setPattern(TransportOrder t) {
    fPattern = t;
  }

  private void updateButtons() {
    boolean state = driveOrdersTable.getSelectedRow() != -1;

    editButton.setEnabled(state);
    removeButton.setEnabled(state);
    moveUpButton.setEnabled(state);
    moveDownButton.setEnabled(state);

    if (driveOrdersTable.getRowCount() == driveOrdersTable.getSelectedRow() + 1) {
      moveDownButton.setEnabled(false);
    }

    if (driveOrdersTable.getSelectedRow() == 0) {
      moveUpButton.setEnabled(false);
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

    stationsPanel = new javax.swing.JPanel();
    driveOrdersScrollPane = new javax.swing.JScrollPane();
    driveOrdersTable = new javax.swing.JTable();
    addButton = new javax.swing.JButton();
    editButton = new javax.swing.JButton();
    removeButton = new javax.swing.JButton();
    moveUpButton = new javax.swing.JButton();
    moveDownButton = new javax.swing.JButton();
    deadlinePanel = new javax.swing.JPanel();
    dateLabel = new javax.swing.JLabel();
    dateTextField = new javax.swing.JTextField();
    timeLabel = new javax.swing.JLabel();
    timeTextField = new javax.swing.JTextField();
    categoryPanel = new javax.swing.JPanel();
    categoryLabel = new javax.swing.JLabel();
    categoryComboBox = new javax.swing.JComboBox<>();
    vehiclePanel = new javax.swing.JPanel();
    vehicleLabel = new javax.swing.JLabel();
    vehicleComboBox = new javax.swing.JComboBox<>();

    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/createTransportOrder"); // NOI18N
    stationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("createTransportOrderPanel.panel_stations.border.title"))); // NOI18N
    java.awt.GridBagLayout stationsPanelLayout = new java.awt.GridBagLayout();
    stationsPanelLayout.columnWidths = new int[] {0, 5, 0};
    stationsPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
    stationsPanel.setLayout(stationsPanelLayout);

    driveOrdersScrollPane.setPreferredSize(new java.awt.Dimension(200, 200));

    driveOrdersTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object[][] {},
        new String[] {
          "Station", "Aktion"
        }
    ) {
      boolean[] canEdit = new boolean[] {
        false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit[columnIndex];
      }
    });
    driveOrdersScrollPane.setViewportView(driveOrdersTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 11;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    stationsPanel.add(driveOrdersScrollPane, gridBagConstraints);

    addButton.setFont(addButton.getFont());
    addButton.setText(bundle.getString("createTransportOrderPanel.button_add.text")); // NOI18N
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
    stationsPanel.add(addButton, gridBagConstraints);

    editButton.setFont(editButton.getFont());
    editButton.setText(bundle.getString("createTransportOrderPanel.button_edit.text")); // NOI18N
    editButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    stationsPanel.add(editButton, gridBagConstraints);

    removeButton.setFont(removeButton.getFont());
    removeButton.setText(bundle.getString("createTransportOrderPanel.button_delete.text")); // NOI18N
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    stationsPanel.add(removeButton, gridBagConstraints);

    moveUpButton.setFont(moveUpButton.getFont());
    moveUpButton.setText(bundle.getString("createTransportOrderPanel.button_up.text")); // NOI18N
    moveUpButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        moveUpButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    stationsPanel.add(moveUpButton, gridBagConstraints);

    moveDownButton.setFont(moveDownButton.getFont());
    moveDownButton.setText(bundle.getString("createTransportOrderPanel.button_moveDown.text")); // NOI18N
    moveDownButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        moveDownButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    stationsPanel.add(moveDownButton, gridBagConstraints);

    add(stationsPanel);

    deadlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("createTransportOrderPanel.panel_deadline.border.title"))); // NOI18N
    java.awt.GridBagLayout deadlinePanelLayout = new java.awt.GridBagLayout();
    deadlinePanelLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
    deadlinePanelLayout.rowHeights = new int[] {0};
    deadlinePanel.setLayout(deadlinePanelLayout);

    dateLabel.setFont(dateLabel.getFont());
    dateLabel.setText(bundle.getString("createTransportOrderPanel.label_date.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    deadlinePanel.add(dateLabel, gridBagConstraints);

    dateTextField.setColumns(10);
    dateTextField.setFont(dateTextField.getFont());
    dateTextField.setText("31.12.2099");
    dateTextField.setToolTipText("Geben Sie das Datum im Format TT.MM.JJJJ ein!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    deadlinePanel.add(dateTextField, gridBagConstraints);

    timeLabel.setFont(timeLabel.getFont());
    timeLabel.setText(bundle.getString("createTransportOrderPanel.label_time.text")); // NOI18N
    timeLabel.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    deadlinePanel.add(timeLabel, gridBagConstraints);

    timeTextField.setColumns(10);
    timeTextField.setFont(timeTextField.getFont());
    timeTextField.setText("23:59");
    timeTextField.setToolTipText("Geben Sie die Uhrzeit im Format HH:MM ein!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    deadlinePanel.add(timeTextField, gridBagConstraints);

    add(deadlinePanel);

    categoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("createTransportOrderPanel.panel_category.border.title"))); // NOI18N
    java.awt.GridBagLayout categoryPanelLayout = new java.awt.GridBagLayout();
    categoryPanelLayout.columnWidths = new int[] {0, 5, 0};
    categoryPanelLayout.rowHeights = new int[] {0};
    categoryPanel.setLayout(categoryPanelLayout);

    categoryLabel.setFont(categoryLabel.getFont());
    categoryLabel.setText(bundle.getString("createTransportOrderPanel.label_category.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    categoryPanel.add(categoryLabel, gridBagConstraints);

    categoryComboBox.setEditable(true);
    categoryComboBox.setFont(categoryComboBox.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    categoryPanel.add(categoryComboBox, gridBagConstraints);

    add(categoryPanel);

    vehiclePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("createTransportOrderPanel.panel_vehicle.border.title"))); // NOI18N
    java.awt.GridBagLayout vehiclePanelLayout = new java.awt.GridBagLayout();
    vehiclePanelLayout.columnWidths = new int[] {0, 5, 0};
    vehiclePanelLayout.rowHeights = new int[] {0};
    vehiclePanel.setLayout(vehiclePanelLayout);

    vehicleLabel.setFont(vehicleLabel.getFont());
    vehicleLabel.setText(bundle.getString("createTransportOrderPanel.label_vehicle.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    vehiclePanel.add(vehicleLabel, gridBagConstraints);

    vehicleComboBox.setFont(vehicleComboBox.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    vehiclePanel.add(vehicleComboBox, gridBagConstraints);

    add(vehiclePanel);
  }// </editor-fold>//GEN-END:initComponents

  private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
    int index = driveOrdersTable.getSelectedRow();

    if (index == -1) {
      return;
    }

    if (index == driveOrdersTable.getRowCount() - 1) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
    model.moveRow(index, index, index + 1);
    driveOrdersTable.getSelectionModel().setSelectionInterval(index + 1, index + 1);

    AbstractConnectableModelComponent location = fDestinationModels.remove(index);
    fDestinationModels.add(index + 1, location);

    String action = fActions.remove(index);
    fActions.add(index + 1, action);

    Map<String, String> properties = fPropertiesList.remove(index);
    fPropertiesList.add(index + 1, properties);

    updateButtons();
  }//GEN-LAST:event_moveDownButtonActionPerformed

  private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
    int index = driveOrdersTable.getSelectedRow();

    if (index <= 0) {
      return;
    }

    DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
    model.moveRow(index, index, index - 1);
    driveOrdersTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);

    AbstractConnectableModelComponent location = fDestinationModels.remove(index);
    fDestinationModels.add(index - 1, location);

    String action = fActions.remove(index);
    fActions.add(index - 1, action);

    Map<String, String> properties = fPropertiesList.remove(index);
    fPropertiesList.add(index - 1, properties);

    updateButtons();
  }//GEN-LAST:event_moveUpButtonActionPerformed

  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    int index = driveOrdersTable.getSelectedRow();

    if (index == -1) {
      return;
    }

    fDestinationModels.remove(index);
    fActions.remove(index);
    fPropertiesList.remove(index);

    DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
    model.removeRow(index);
    updateButtons();
  }//GEN-LAST:event_removeButtonActionPerformed

  private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
    int index = driveOrdersTable.getSelectedRow();

    if (index == -1) {
      return;
    }

    AbstractConnectableModelComponent location = fDestinationModels.get(index);
    String action = fActions.get(index);
    EditDriveOrderPanel contentPanel = new EditDriveOrderPanel(fModelManager.getModel().getLocationModels(), location, action);
    StandardContentDialog dialog
        = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                    contentPanel);
    dialog.setVisible(true);

    Optional<LocationModel> locModel = contentPanel.getSelectedLocation();
    Optional<String> act = contentPanel.getSelectedAction();
    if (dialog.getReturnStatus() == StandardContentDialog.RET_OK
        && locModel.isPresent() && act.isPresent()) {
      location = locModel.get();
      action = act.get();

      driveOrdersTable.setValueAt(location.getName(), index, 0);
      driveOrdersTable.setValueAt(action, index, 1);

      fDestinationModels.set(index, location);
      fActions.set(index, action);
    }
  }//GEN-LAST:event_editButtonActionPerformed

  private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    EditDriveOrderPanel contentPanel = new EditDriveOrderPanel(fModelManager.getModel().getLocationModels());
    StandardContentDialog dialog
        = new StandardContentDialog(JOptionPane.getFrameForComponent(this),
                                    contentPanel);
    dialog.setVisible(true);

    Optional<LocationModel> locModel = contentPanel.getSelectedLocation();
    Optional<String> act = contentPanel.getSelectedAction();
    if (dialog.getReturnStatus() == StandardContentDialog.RET_OK
        && locModel.isPresent() && act.isPresent()) {
      int index = driveOrdersTable.getRowCount();

      LocationModel location = locModel.get();
      String action = act.get();

      String[] row = new String[2];
      row[0] = location.getName();
      row[1] = action;

      DefaultTableModel model = (DefaultTableModel) driveOrdersTable.getModel();
      model.addRow(row);

      fDestinationModels.add(location);
      fActions.add(action);
      fPropertiesList.add(new HashMap<>());

      driveOrdersTable.setRowSelectionInterval(index, index);
      updateButtons();
    }
  }//GEN-LAST:event_addButtonActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addButton;
  private javax.swing.JComboBox<String> categoryComboBox;
  private javax.swing.JLabel categoryLabel;
  private javax.swing.JPanel categoryPanel;
  private javax.swing.JLabel dateLabel;
  private javax.swing.JTextField dateTextField;
  private javax.swing.JPanel deadlinePanel;
  private javax.swing.JScrollPane driveOrdersScrollPane;
  private javax.swing.JTable driveOrdersTable;
  private javax.swing.JButton editButton;
  private javax.swing.JButton moveDownButton;
  private javax.swing.JButton moveUpButton;
  private javax.swing.JButton removeButton;
  private javax.swing.JPanel stationsPanel;
  private javax.swing.JLabel timeLabel;
  private javax.swing.JTextField timeTextField;
  private javax.swing.JComboBox<String> vehicleComboBox;
  private javax.swing.JLabel vehicleLabel;
  private javax.swing.JPanel vehiclePanel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
