/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Dialog for adding operating time and load handling devices to an operation.
 * An <code>OperationSpec</code> instance will be created as a result.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class EditOperationSpecDialog
    extends JDialog {

  /**
   * <code>OperationSpec</code> instance as a result ofthe user's input. Will be created
   * when ok-button is pressed.
   */
  private OperationSpec opSpec;
  /**
   * The communication adapter this dialog belongs to.
   */
  private final LoopbackCommunicationAdapter commAdapter;

  /**
   * Creates new EditOperationSpecDialog.
   *
   * @param adapter Communication adapter of the vehicle this dialog belongs to.
   */
  public EditOperationSpecDialog(LoopbackCommunicationAdapter adapter) {
    this.commAdapter = requireNonNull(adapter, "adapter");
    initComponents();
    setLocationRelativeTo(null);
    // Add some listeners to control button states according to user input
    opNameTextField.getDocument().addDocumentListener(new RequiredTextFieldListener());
    opTimeTextField.getDocument().addDocumentListener(new RequiredTextFieldListener());
    deviceTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    deviceTable.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        deleteDeviceButton.setEnabled(!deviceTable.getSelectionModel().isSelectionEmpty());
      }
    });
    // Add input verifier to avoid invalid input
    opTimeTextField.setInputVerifier(new DecimalInputVerifier());
  }

  /**
   * Creates a dialog and presets it's input fields according to the
   * given operationSpec.
   *
   * @param adapter CommunicationAdapter of the vehicle the created
   * <code>OperationSpec</code> will belong to.
   * @param operationSpec OperationSpec used to preset the values in the dialog.
   */
  public EditOperationSpecDialog(LoopbackCommunicationAdapter adapter,
                                 OperationSpec operationSpec) {
    this(adapter);
    opNameTextField.setText(operationSpec.getOperationName());
    opTimeTextField.setText(new Integer(operationSpec.getOperatingTime()).toString());
    if (operationSpec.changesLoadCondition()) {
      deviceCheckBox.setSelected(true);
      LoadHandlingDeviceTableModel model
          = (LoadHandlingDeviceTableModel) deviceTable.getModel();
      model.updateLoadHandlingDevices(
          new LinkedList<>(operationSpec.getLoadCondition()));
    }
  }

  /**
   * Returns an <code>OperationSpec</code> instance as the result of this dialog.
   * Will be <code>null</code> if the user has not finished the dialog by
   * clicking the ok-button or if the user has cancelled the dialog.
   *
   * @return <code>OperationSpec</code> instance
   */
  public OperationSpec getOperationSpec() {
    return opSpec;
  }

  /**
   * Load the current load handling devices into the table.
   */
  private void loadDeviceTable() {
    TableModel tableModel = deviceTable.getModel();
    if (tableModel instanceof LoadHandlingDeviceTableModel) {
      LoadHandlingDeviceTableModel deviceTableModel
          = (LoadHandlingDeviceTableModel) tableModel;
      // Get a deep copy of the current device list of the vehicle
      List<LoadHandlingDevice> currentDevices
          = commAdapter.getProcessModel().getVehicleLoadHandlingDevices();
      List<LoadHandlingDevice> currentDevicesClone = new LinkedList<>();
      for (LoadHandlingDevice d : currentDevices) {
        currentDevicesClone.add(d);
      }
      // Fill table with the current devices
      deviceTableModel.updateLoadHandlingDevices(currentDevicesClone);
    }
  }

  /**
   * Empty the device table.
   */
  private void clearDeviceTable() {
    TableModel tableModel = deviceTable.getModel();
    if (tableModel instanceof LoadHandlingDeviceTableModel) {
      LoadHandlingDeviceTableModel deviceTableModel
          = (LoadHandlingDeviceTableModel) tableModel;
      // Set device list to an empty list
      deviceTableModel.updateLoadHandlingDevices(new LinkedList<LoadHandlingDevice>());
    }
  }

  /**
   * Checks if the required text fields are empty and enables/disables
   * the ok-Button accordingly.
   */
  private void requiredFieldsChanged() {
    boolean inputComplete;
    inputComplete = !opNameTextField.getText().trim().isEmpty()
        && !opTimeTextField.getText().trim().isEmpty();
    okButton.setEnabled(inputComplete);
  }

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

    inputPanel = new javax.swing.JPanel();
    opNameLabel = new javax.swing.JLabel();
    opTimeLabel = new javax.swing.JLabel();
    opNameTextField = new javax.swing.JTextField();
    opTimeTextField = new javax.swing.JTextField();
    opTimeUnitTextField = new javax.swing.JLabel();
    deviceCheckBox = new javax.swing.JCheckBox();
    deviceTableScrollPane = new javax.swing.JScrollPane();
    deviceTable = new javax.swing.JTable();
    deviceButtonsPanel1 = new javax.swing.JPanel();
    addDeviceButton = new javax.swing.JButton();
    deleteDeviceButton = new javax.swing.JButton();
    deviceExplanationLabel = new javax.swing.JLabel();
    buttonPanel = new javax.swing.JPanel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle"); // NOI18N
    setTitle(bundle.getString("addOpSpecDialog.title")); // NOI18N
    setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    setLocationByPlatform(true);
    setMinimumSize(new java.awt.Dimension(10, 10));
    setModal(true);
    setResizable(false);
    getContentPane().setLayout(new java.awt.GridBagLayout());

    inputPanel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    inputPanel.setPreferredSize(new java.awt.Dimension(280, 340));
    inputPanel.setLayout(new java.awt.GridBagLayout());

    opNameLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    opNameLabel.setText(bundle.getString("addOpSpecDialog.opNameLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
    inputPanel.add(opNameLabel, gridBagConstraints);

    opTimeLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    opTimeLabel.setText(bundle.getString("addOpSpecDialog.opTimeLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
    inputPanel.add(opTimeLabel, gridBagConstraints);

    opNameTextField.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    opNameTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    opNameTextField.setMinimumSize(new java.awt.Dimension(90, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 12, 5);
    inputPanel.add(opNameTextField, gridBagConstraints);

    opTimeTextField.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    opTimeTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    opTimeTextField.setMinimumSize(new java.awt.Dimension(90, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 12, 5);
    inputPanel.add(opTimeTextField, gridBagConstraints);

    opTimeUnitTextField.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    opTimeUnitTextField.setText(bundle.getString("addOpSpecDialog.opTimeUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
    inputPanel.add(opTimeUnitTextField, gridBagConstraints);

    deviceCheckBox.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    deviceCheckBox.setText(bundle.getString("addOpSpecDialog.devicesCheckBox")); // NOI18N
    deviceCheckBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
    deviceCheckBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        deviceCheckBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    inputPanel.add(deviceCheckBox, gridBagConstraints);

    deviceTableScrollPane.setMinimumSize(new java.awt.Dimension(280, 200));
    deviceTableScrollPane.setPreferredSize(new java.awt.Dimension(280, 200));

    deviceTable.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    deviceTable.setModel(new LoadHandlingDeviceTableModel());
    deviceTable.setMinimumSize(new java.awt.Dimension(22, 20));
    deviceTableScrollPane.setViewportView(deviceTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
    inputPanel.add(deviceTableScrollPane, gridBagConstraints);

    addDeviceButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    addDeviceButton.setText(bundle.getString("addOpSpecDialog.addDeviceButton")); // NOI18N
    addDeviceButton.setEnabled(false);
    addDeviceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addDeviceButtonActionPerformed(evt);
      }
    });
    deviceButtonsPanel1.add(addDeviceButton);

    deleteDeviceButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    deleteDeviceButton.setText(bundle.getString("addOpSpecDialog.deleteDeviceButton")); // NOI18N
    deleteDeviceButton.setEnabled(false);
    deleteDeviceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteDeviceButtonActionPerformed(evt);
      }
    });
    deviceButtonsPanel1.add(deleteDeviceButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    inputPanel.add(deviceButtonsPanel1, gridBagConstraints);

    deviceExplanationLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    deviceExplanationLabel.setText(bundle.getString("addOpSpecDialog.addDeviceExplanation")); // NOI18N
    deviceExplanationLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    deviceExplanationLabel.setEnabled(false);
    deviceExplanationLabel.setMinimumSize(new java.awt.Dimension(490, 42));
    deviceExplanationLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 3;
    inputPanel.add(deviceExplanationLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
    getContentPane().add(inputPanel, gridBagConstraints);

    buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    okButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    okButton.setText(bundle.getString("addOpSpecDialog.okButton")); // NOI18N
    okButton.setEnabled(false);
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(okButton);

    cancelButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    cancelButton.setText(bundle.getString("addOpSpecDialog.cancelButton")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(cancelButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
    getContentPane().add(buttonPanel, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    String operation = opNameTextField.getText();
    int operatingTime;
    try {
      operatingTime = Integer.parseInt(opTimeTextField.getText());
    }
    catch (NumberFormatException e) {
      // Do nothing as the text field does not yet contain a valid input  
      return;
    }
    boolean devicesSpecified = false;
    List<LoadHandlingDevice> devices = new LinkedList<>();
    if (deviceCheckBox.isSelected()) {
      devicesSpecified = true;
      if (deviceTable.getModel() instanceof LoadHandlingDeviceTableModel) {
        LoadHandlingDeviceTableModel model
            = (LoadHandlingDeviceTableModel) deviceTable.getModel();
        devices = model.getLoadHandlingDevices();
      }
    }
    // Create OperationSpec instance as the result of the dialog
    if (devicesSpecified) {
      opSpec = new OperationSpec(operation, operatingTime, devices);
    }
    else {
      opSpec = new OperationSpec(operation, operatingTime);
    }
    dispose();
  }//GEN-LAST:event_okButtonActionPerformed

  private void deviceCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_deviceCheckBoxItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      loadDeviceTable();
      deviceExplanationLabel.setEnabled(true);
      deviceTable.setEnabled(true);
      addDeviceButton.setEnabled(true);
      // Note: deleteDeviceButton is enabled in ListSelectionListener
    }
    else {
      deviceExplanationLabel.setEnabled(false);
      deviceTable.setEnabled(false);
      addDeviceButton.setEnabled(false);
      deleteDeviceButton.setEnabled(false);
      clearDeviceTable();
    }
  }//GEN-LAST:event_deviceCheckBoxItemStateChanged

  private void deleteDeviceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDeviceButtonActionPerformed
    List<LoadHandlingDevice> newList = new LinkedList<>();
    LoadHandlingDeviceTableModel model = (LoadHandlingDeviceTableModel) deviceTable.getModel();
    List<LoadHandlingDevice> oldList = model.getLoadHandlingDevices();
    int j = 0;
    for (int i : deviceTable.getSelectedRows()) {
      while (j < i) {
        newList.add(oldList.get(j));
        j++;
      }
      j++;
    }
    while (j < oldList.size()) {
      newList.add(oldList.get(j));
      j++;
    }
    model.updateLoadHandlingDevices(newList);
  }//GEN-LAST:event_deleteDeviceButtonActionPerformed

  private void addDeviceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDeviceButtonActionPerformed
    LoadHandlingDeviceTableModel model = (LoadHandlingDeviceTableModel) deviceTable.getModel();
    model.getLoadHandlingDevices().add(new LoadHandlingDevice("", false));
    int newIndex = model.getLoadHandlingDevices().size() - 1;
    model.fireTableRowsInserted(newIndex, newIndex);
  }//GEN-LAST:event_addDeviceButtonActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addDeviceButton;
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton cancelButton;
  private javax.swing.JButton deleteDeviceButton;
  private javax.swing.JPanel deviceButtonsPanel1;
  private javax.swing.JCheckBox deviceCheckBox;
  private javax.swing.JLabel deviceExplanationLabel;
  private javax.swing.JTable deviceTable;
  private javax.swing.JScrollPane deviceTableScrollPane;
  private javax.swing.JPanel inputPanel;
  private javax.swing.JButton okButton;
  private javax.swing.JLabel opNameLabel;
  private javax.swing.JTextField opNameTextField;
  private javax.swing.JLabel opTimeLabel;
  private javax.swing.JTextField opTimeTextField;
  private javax.swing.JLabel opTimeUnitTextField;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * A <code>DocumentListener</code> for the text fields that need to be filled.
   */
  private class RequiredTextFieldListener
      implements DocumentListener {

    /**
     * Create an instance.
     */
    public RequiredTextFieldListener() {
      // Do nothing 
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      requiredFieldsChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      requiredFieldsChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
  }

  /**
   * An <code>InputVerifier</code> for <code>JTextField</code> that accepts
   * only decimal numbers (0-9).
   * If the text field contains invalid input, the background color is changed
   * and the focus is hold.
   */
  private class DecimalInputVerifier
      extends InputVerifier {

    /**
     * Create an instance.
     */
    public DecimalInputVerifier() {
      //Do nothing
    }

    @Override
    public boolean verify(JComponent input) {
      JTextField textField = (JTextField) input;
      String text = textField.getText();
      // Does the text contains only digits?
      return text.matches("[0-9]*");
    }

    @Override
    public boolean shouldYieldFocus(JComponent input) {
      JTextField textField = (JTextField) input;
      if (verify(input)) {
        textField.setBackground(Color.WHITE);
        return true;
      }
      else {
        textField.setBackground(new Color(255, 92, 92));
        return false;
      }
    }
  }
}
