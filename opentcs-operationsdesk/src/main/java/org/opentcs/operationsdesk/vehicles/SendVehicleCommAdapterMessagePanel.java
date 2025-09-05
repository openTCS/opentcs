// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.vehicles;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.opentcs.components.plantoverview.VehicleCommAdapterMessageSuggestions;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.InputValidationListener;
import org.opentcs.guing.common.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.common.components.dialogs.StandardDialog;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;

/**
 * Allows {@link VehicleCommAdapterMessage}s to be sent.
 */
public class SendVehicleCommAdapterMessagePanel
    extends
      DialogContent {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
      I18nPlantOverviewOperating.SEND_VEHICLE_COMM_ADAPTER_MESSAGE_PATH
  );
  private final Provider<ParameterEditorPanel> editorProvider;
  private final VehicleCommAdapterMessageSuggestions messageSuggestions;
  /**
   * The list of listeners to be notified about the validity of user input.
   */
  private final List<InputValidationListener> validationListeners = new ArrayList<>();
  /**
   * The currently loaded system model.
   */
  private final SystemModel systemModel;
  private ParametersTableModel tableModel;

  /**
   * Creates a new instance.
   *
   * @param editorProvider Provides instances of {@link ParameterEditorPanel}s.
   * @param messageSuggestions Provides suggestions for {@link VehicleCommAdapterMessage}s.
   * @param modelManager Provides access to the currently loaded system model.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public SendVehicleCommAdapterMessagePanel(
      Provider<ParameterEditorPanel> editorProvider,
      MergedVehicleCommAdapterMessageSuggestions messageSuggestions,
      ModelManager modelManager
  ) {
    this.editorProvider = requireNonNull(editorProvider, "editorProvider");
    this.messageSuggestions = requireNonNull(messageSuggestions, "messageSuggestions");
    this.systemModel = requireNonNull(modelManager, "modelManager").getModel();

    initComponents();
    initTable();
    setDialogTitle(BUNDLE.getString("sendVehicleCommAdapterMessagePanel.title"));
  }

  @Override
  public void initFields() {
    vehicleComboBox.setModel(
        new DefaultComboBoxModel<>(
            systemModel.getVehicleModels().stream()
                .map(VehicleModel::getName)
                .sorted()
                .toArray(String[]::new)
        )
    );
    messageTypeComboBox.setModel(
        new DefaultComboBoxModel<>(
            messageSuggestions.getTypeSuggestions().stream()
                .sorted()
                .toArray(String[]::new)
        )
    );
  }

  @Override
  public void update() {
  }

  /**
   * Registers the given {@link InputValidationListener} with this panel.
   *
   * @param listener The listener to register.
   */
  public void addInputValidationListener(InputValidationListener listener) {
    requireNonNull(listener, "listener");

    this.validationListeners.add(listener);
    validateInput();
  }

  /**
   * Returns the name of the vehicle that has been selected in this panel.
   *
   * @return The name of the selected vehicle or an empty {@link Optional}, if no vehicle has been
   * selected.
   */
  public Optional<String> getSelectedVehicle() {
    return vehicleComboBox.getSelectedItem() != null
        ? Optional.of((String) vehicleComboBox.getSelectedItem())
        : Optional.empty();
  }

  /**
   * Returns the {@link VehicleCommAdapterMessage} that has been configured in this panel.
   *
   * @return The {@link VehicleCommAdapterMessage} that has been configured in this panel.
   */
  public VehicleCommAdapterMessage getVehicleCommAdapterMessage() {
    return new VehicleCommAdapterMessage(
        messageTypeComboBox.getSelectedItem() != null
            ? (String) messageTypeComboBox.getSelectedItem()
            : "",
        extractMessageParametersFromTable()
    );
  }

  private void initTable() {
    tableModel = new ParametersTableModel();
    parametersTable.setModel(tableModel);

    parametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    TableRowSorter<ParametersTableModel> sorter = new TableRowSorter<>(tableModel);
    // Sort the table by the parameter key
    sorter.setSortKeys(
        Arrays.asList(
            new RowSorter.SortKey(ParametersTableModel.COLUMN_KEY, SortOrder.ASCENDING)
        )
    );
    sorter.setSortsOnUpdates(true);
    parametersTable.setRowSorter(sorter);
  }

  private Optional<CommAdapterMessageParameter> showAddParameterDialog() {
    ParameterEditorPanel parameterEditor = editorProvider.get();
    parameterEditor.setParameterSuggestions(
        messageSuggestions.getParameterSuggestionsFor(
            String.valueOf(messageTypeComboBox.getSelectedItem())
        )
    );
    StandardDialog dialog = new StandardDialog(
        this,
        true,
        parameterEditor,
        BUNDLE.getString("sendVehicleCommAdapterMessagePanel.dialog_addParameter.title")
    );
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    if (dialog.getReturnStatus() != StandardDialog.RET_OK) {
      return Optional.empty();
    }

    return Optional.of(parameterEditor.getParameter());
  }

  private Optional<CommAdapterMessageParameter> showEditParameterDialog(
      CommAdapterMessageParameter parameterToEdit
  ) {
    ParameterEditorPanel parameterEditor = editorProvider.get();
    parameterEditor.setParameterSuggestions(
        messageSuggestions.getParameterSuggestionsFor(
            String.valueOf(messageTypeComboBox.getSelectedItem())
        )
    );
    parameterEditor.setParameter(parameterToEdit);
    StandardDialog dialog = new StandardDialog(
        this,
        true,
        parameterEditor,
        BUNDLE.getString("sendVehicleCommAdapterMessagePanel.dialog_editParameter.title")
    );
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    if (dialog.getReturnStatus() != StandardDetailsDialog.RET_OK) {
      return Optional.empty();
    }

    return Optional.of(parameterEditor.getParameter());
  }

  private Map<String, String> extractMessageParametersFromTable() {
    return tableModel.getDataVector().stream()
        .collect(
            Collectors.toMap(
                rowVector -> (String) rowVector.get(ParametersTableModel.COLUMN_KEY),
                rowVector -> (String) rowVector.get(ParametersTableModel.COLUMN_VALUE)
            )
        );
  }

  private void validateInput() {
    boolean vehicleSelected = vehicleComboBox.getSelectedIndex() != -1;
    for (InputValidationListener validationListener : validationListeners) {
      validationListener.inputValidationSuccessful(vehicleSelected);
    }
  }

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    vehicleLabel = new javax.swing.JLabel();
    vehicleComboBox = new javax.swing.JComboBox<>();
    messageTypeLabel = new javax.swing.JLabel();
    messageTypeComboBox = new javax.swing.JComboBox<>();
    parametersPanel = new javax.swing.JPanel();
    parametersScrollPane = new javax.swing.JScrollPane();
    parametersTable = new javax.swing.JTable();
    addParameterButton = new javax.swing.JButton();
    editParameterButton = new javax.swing.JButton();
    removeParameterButton = new javax.swing.JButton();

    setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/operating/dialogs/sendVehicleCommAdapterMessage"); // NOI18N
    vehicleLabel.setText(bundle.getString("sendVehicleCommAdapterMessagePanel.label_vehicle.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
    add(vehicleLabel, gridBagConstraints);

    vehicleComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        selectedVehicleChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
    add(vehicleComboBox, gridBagConstraints);

    messageTypeLabel.setText(bundle.getString("sendVehicleCommAdapterMessagePanel.label_messageType.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
    add(messageTypeLabel, gridBagConstraints);

    messageTypeComboBox.setEditable(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
    add(messageTypeComboBox, gridBagConstraints);

    parametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("sendVehicleCommAdapterMessagePanel.panel_parameters.border.text"))); // NOI18N
    parametersPanel.setLayout(new java.awt.GridBagLayout());

    parametersScrollPane.setPreferredSize(new java.awt.Dimension(300, 250));

    parametersTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    parametersScrollPane.setViewportView(parametersTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 5;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
    parametersPanel.add(parametersScrollPane, gridBagConstraints);

    addParameterButton.setText("Add");
    addParameterButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addParameterButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 3, 3);
    parametersPanel.add(addParameterButton, gridBagConstraints);

    editParameterButton.setText("Edit");
    editParameterButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editParameterButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 3, 3);
    parametersPanel.add(editParameterButton, gridBagConstraints);

    removeParameterButton.setText("Remove");
    removeParameterButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeParameterButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 3, 3);
    parametersPanel.add(removeParameterButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.ipadx = 5;
    add(parametersPanel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // FORMATTER:ON
  // CHECKSTYLE:ON

  private void addParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParameterButtonActionPerformed
    showAddParameterDialog().ifPresent(parameter -> {
      if (tableModel.keyExists(parameter.key())) {
        JOptionPane.showMessageDialog(
            this,
            BUNDLE.getString(
                "sendVehicleCommAdapterMessagePanel.optionPane_keyAlreadyExists.message"
            ) + parameter.key()
        );
        return;
      }

      tableModel.addParameter(parameter.key(), parameter.value());
    });
  }//GEN-LAST:event_addParameterButtonActionPerformed

  private void editParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editParameterButtonActionPerformed
    int selectedRowInView = parametersTable.getSelectedRow();
    if (selectedRowInView == -1) {
      return;
    }
    int selectedRowInModel = parametersTable.convertRowIndexToModel(selectedRowInView);
    CommAdapterMessageParameter selectedParameter = new CommAdapterMessageParameter(
        (String) tableModel.getValueAt(selectedRowInModel, ParametersTableModel.COLUMN_KEY),
        (String) tableModel.getValueAt(selectedRowInModel, ParametersTableModel.COLUMN_VALUE)
    );

    showEditParameterDialog(selectedParameter).ifPresent(updatedParameter -> {
      if (tableModel.keyExistsExcludingRow(updatedParameter.key(), selectedRowInModel)) {
        JOptionPane.showMessageDialog(
            this,
            BUNDLE.getString(
                "sendVehicleCommAdapterMessagePanel.optionPane_keyAlreadyExists.message"
            ) + updatedParameter.key()
        );
        return;
      }

      tableModel.updateParameter(
          selectedRowInModel,
          updatedParameter.key(),
          updatedParameter.value()
      );
    });
  }//GEN-LAST:event_editParameterButtonActionPerformed

  private void removeParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeParameterButtonActionPerformed
    int selectedRowInView = parametersTable.getSelectedRow();
    if (selectedRowInView == -1) {
      return;
    }
    int selectedRowInModel = parametersTable.convertRowIndexToModel(selectedRowInView);

    tableModel.removeRow(selectedRowInModel);
  }//GEN-LAST:event_removeParameterButtonActionPerformed

  private void selectedVehicleChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectedVehicleChanged
    validateInput();
  }//GEN-LAST:event_selectedVehicleChanged

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addParameterButton;
  private javax.swing.JButton editParameterButton;
  private javax.swing.JComboBox<String> messageTypeComboBox;
  private javax.swing.JLabel messageTypeLabel;
  private javax.swing.JPanel parametersPanel;
  private javax.swing.JScrollPane parametersScrollPane;
  private javax.swing.JTable parametersTable;
  private javax.swing.JButton removeParameterButton;
  private javax.swing.JComboBox<String> vehicleComboBox;
  private javax.swing.JLabel vehicleLabel;
  // End of variables declaration//GEN-END:variables
  // FORMATTER:ON
  // CHECKSTYLE:ON

  private static class ParametersTableModel
      extends
        DefaultTableModel {

    private static final int COLUMN_KEY = 0;
    private static final int COLUMN_VALUE = 1;
    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[]{
        String.class, String.class
    };
    private static final String[] COLUMN_NAMES = new String[]{
        BUNDLE.getString("parametersTableModel.column_key.headerText"),
        BUNDLE.getString("parametersTableModel.column_value.headerText"),};

    ParametersTableModel() {
      super(
          new Object[][]{},
          COLUMN_NAMES
      );
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }

    public void addParameter(String key, String value) {
      addRow(new Object[]{key, value});
    }

    public void updateParameter(int row, String key, String value) {
      removeRow(row);
      addRow(new Object[]{key, value});
    }

    private boolean keyExists(String key) {
      for (int i = 0; i < getRowCount(); i++) {
        if (Objects.equals(getValueAt(i, ParametersTableModel.COLUMN_KEY), key)) {
          return true;
        }
      }
      return false;
    }

    private boolean keyExistsExcludingRow(String key, int row) {
      for (int i = 0; i < getRowCount(); i++) {
        if (i == row) {
          continue;
        }
        if (Objects.equals(getValueAt(i, ParametersTableModel.COLUMN_KEY), key)) {
          return true;
        }
      }
      return false;
    }
  }
}
