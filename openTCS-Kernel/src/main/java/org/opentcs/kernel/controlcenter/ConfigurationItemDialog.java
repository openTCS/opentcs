/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.Objects;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;
import org.opentcs.access.ConfigurationItemTO;
import org.opentcs.access.LocalKernel;
import org.opentcs.util.configuration.ConfigurationDataType;

/**
 * Dialog window for a configuration item.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ConfigurationItemDialog
    extends javax.swing.JDialog {

  /**
   * The proxy kernel this client has to communicate with.
   */
  private final LocalKernel kernel;
  /**
   * The configuration item object.
   */
  private final ConfigurationItemTO configItem;
  /**
   * The configuration value checkbox.
   */
  private JCheckBox booleanValueChkBox;
  /**
   * The list of Enum values.
   */
  private JComboBox<String> enumValueList;
  /**
   * The list of possible integer values for Integer type Items.
   */
  private JSpinner valueSpinner;
  /**
   * Holds the value in the spinner.
   */
  private JFormattedTextField spinnerTextField;

  /**
   * Creates a new ConfigurationItemDialog.If the data type of the 
   * configuration item is Boolean, then the value field is initialized 
   * as checkbox and a textfield otherwise.
   * 
   * @param kernel The proxy kernel this client has to communicate with.
   * @param configItem The current select configuration item.
   */
  @SuppressWarnings("unchecked")
  public ConfigurationItemDialog(LocalKernel kernel,
                                 ConfigurationItemTO configItem) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.configItem = Objects.requireNonNull(configItem, "configItem");
    initComponents();
    nameSpaceTextField.setText(configItem.getNamespace());
    keyTextField.setText(configItem.getKey());
    descriptionTextArea.setText(configItem.getDescription());
    ConfigurationDataType type = configItem.getConstraint().getType();
    dataTypeTextField.setText(type.toString());

    GridBagConstraints gridBagConstraints;

    if (type.equals(ConfigurationDataType.BOOLEAN)) {
      configurationItemPanel.remove(valueTextField);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 8;
      gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
      booleanValueChkBox = new JCheckBox();
      boolean checkboxStatus = Boolean.parseBoolean(configItem.getValue());
      booleanValueChkBox.setSelected(checkboxStatus);
      configurationItemPanel.add(booleanValueChkBox, gridBagConstraints);
    }
    else if (type.equals(ConfigurationDataType.ENUM)) {
      configurationItemPanel.remove(valueTextField);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 8;
      gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
      enumValueList = new JComboBox<>(configItem.getConstraint().getEnum().toArray(new String[0]));
      gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      enumValueList.setSelectedItem(configItem.getValue());
      enumValueList.setEditable(false);
      configurationItemPanel.add(enumValueList, gridBagConstraints);
    }
    else if (type.equals(ConfigurationDataType.BYTE)
        || type.equals(ConfigurationDataType.SHORT)
        || type.equals(ConfigurationDataType.LONG)
        || type.equals(ConfigurationDataType.INTEGER)) {
      configurationItemPanel.remove(valueTextField);
      gridBagConstraints = new GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 8;
      gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
      SpinnerModel model =
          new SpinnerNumberModel(Integer.parseInt(configItem.getValue()),
                                 ((Double) configItem.getConstraint().getMinVal()).intValue(),
                                 ((Double) configItem.getConstraint().getMaxVal()).intValue(), 1);
      valueSpinner = new JSpinner(model);
      valueSpinner.setName("valueSpinner");
      JSpinner.NumberEditor editor = 
          new JSpinner.NumberEditor(valueSpinner, "#");
      valueSpinner.setEditor(editor);
      JComponent comp = valueSpinner.getEditor();
      spinnerTextField = (JFormattedTextField) comp.getComponent(0);
      spinnerTextField.setHorizontalAlignment(JTextField.LEFT);
      DefaultFormatter formatter = 
          (DefaultFormatter) spinnerTextField.getFormatter();
      formatter.setAllowsInvalid(false);
      gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
      valueSpinner.setValue(Integer.parseInt(configItem.getValue()));
      configurationItemPanel.add(valueSpinner, gridBagConstraints);
    }
    else {
      valueTextField.setText(configItem.getValue());
    }
    pack();
    setLocationRelativeTo(null);
  }

  // CHECKSTYLE:OFF
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    configurationItemPanel = new javax.swing.JPanel();
    nameSpace = new javax.swing.JLabel();
    nameSpaceTextField = new javax.swing.JTextField();
    key = new javax.swing.JLabel();
    keyTextField = new javax.swing.JTextField();
    description = new javax.swing.JLabel();
    value = new javax.swing.JLabel();
    valueTextField = new javax.swing.JTextField();
    saveButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    descriptionTextArea = new javax.swing.JTextArea();
    dataType = new javax.swing.JLabel();
    dataTypeTextField = new javax.swing.JTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Configuration Item Dialog");
    setBounds(new java.awt.Rectangle(200, 200, 0, 0));
    setModal(true);
    setName("ConfigItemDialog"); // NOI18N
    setResizable(false);

    configurationItemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Configuration Item:"));
    configurationItemPanel.setName("configurationItemPanel"); // NOI18N
    java.awt.GridBagLayout configurationItemPanelLayout = new java.awt.GridBagLayout();
    configurationItemPanelLayout.columnWidths = new int[] {0, 0, 0, 0, 0};
    configurationItemPanelLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    configurationItemPanel.setLayout(configurationItemPanelLayout);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/Bundle"); // NOI18N
    nameSpace.setText(bundle.getString("SelectedItemNamespace")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    configurationItemPanel.add(nameSpace, gridBagConstraints);

    nameSpaceTextField.setEditable(false);
    nameSpaceTextField.setName("nameSpaceTextField"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    configurationItemPanel.add(nameSpaceTextField, gridBagConstraints);

    key.setText(bundle.getString("SelectedItemKey")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    configurationItemPanel.add(key, gridBagConstraints);

    keyTextField.setEditable(false);
    keyTextField.setName("keyTextField"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    configurationItemPanel.add(keyTextField, gridBagConstraints);

    description.setText(bundle.getString("SelectedItemDescription")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    configurationItemPanel.add(description, gridBagConstraints);

    value.setText(bundle.getString("SelectedItemValue")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    configurationItemPanel.add(value, gridBagConstraints);

    valueTextField.setInputVerifier(new PassVerifier());
    valueTextField.setName("valueTextField"); // NOI18N
    valueTextField.setVerifyInputWhenFocusTarget(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    configurationItemPanel.add(valueTextField, gridBagConstraints);

    saveButton.setText(bundle.getString("SetConfigurationItemValue")); // NOI18N
    saveButton.setName("saveButton"); // NOI18N
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    configurationItemPanel.add(saveButton, gridBagConstraints);

    cancelButton.setText(bundle.getString("Cancel")); // NOI18N
    cancelButton.setName("cancelButton"); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    configurationItemPanel.add(cancelButton, gridBagConstraints);

    descriptionTextArea.setColumns(20);
    descriptionTextArea.setFont(keyTextField.getFont());
    descriptionTextArea.setLineWrap(true);
    descriptionTextArea.setRows(5);
    descriptionTextArea.setWrapStyleWord(true);
    descriptionTextArea.setName("descriptionTextArea"); // NOI18N
    jScrollPane1.setViewportView(descriptionTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    configurationItemPanel.add(jScrollPane1, gridBagConstraints);

    dataType.setText(bundle.getString("SelectedItemDataType")); // NOI18N
    dataType.setMaximumSize(new java.awt.Dimension(61, 14));
    dataType.setMinimumSize(new java.awt.Dimension(61, 14));
    dataType.setPreferredSize(new java.awt.Dimension(28, 14));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    configurationItemPanel.add(dataType, gridBagConstraints);
    dataType.getAccessibleContext().setAccessibleName("dataType");

    dataTypeTextField.setEditable(false);
    dataTypeTextField.setName("datatypeTextField"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    configurationItemPanel.add(dataTypeTextField, gridBagConstraints);
    dataTypeTextField.getAccessibleContext().setAccessibleName("dataTypeTextField");

    getContentPane().add(configurationItemPanel, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    ConfigurationItemTO confItem = this.configItem;
    boolean flag = true;

    ConfigurationItemTO newItem = new ConfigurationItemTO();
    ConfigurationItemTO oldItem = this.configItem;
    ConfigurationDataType type = confItem.getConstraint().getType();
    newItem.setNamespace(nameSpaceTextField.getText());
    newItem.setKey(keyTextField.getText());
    newItem.setDescription(descriptionTextArea.getText());
    newItem.setConstraint(confItem.getConstraint());

    oldItem.setNamespace(nameSpaceTextField.getText());
    oldItem.setKey(keyTextField.getText());
    oldItem.setDescription(descriptionTextArea.getText());
    oldItem.setConstraint(confItem.getConstraint());

    if (type.equals(ConfigurationDataType.BOOLEAN)) {
      newItem.setValue(Boolean.toString(booleanValueChkBox.isSelected()));
      oldItem.setValue(Boolean.toString(booleanValueChkBox.isSelected()));
    }
    else if (type.equals(ConfigurationDataType.ENUM)) {
      newItem.setValue(enumValueList.getSelectedItem().toString());
      oldItem.setValue(enumValueList.getSelectedItem().toString());
    }
    else if (type.equals(ConfigurationDataType.BYTE)
      || type.equals(ConfigurationDataType.SHORT)
      || type.equals(ConfigurationDataType.LONG)
      || type.equals(ConfigurationDataType.INTEGER)) {
      valueSpinner.setValue(spinnerTextField.getValue());
      newItem.setValue(spinnerTextField.getValue().toString());
      oldItem.setValue(spinnerTextField.getValue().toString());
    }
    else {
      PassVerifier verifier = new PassVerifier();
      boolean verifyResult = verifier.verify(valueTextField);
      if (verifyResult) {
        newItem.setValue(valueTextField.getText());
        oldItem.setValue(valueTextField.getText());
      }
    }
    try {
      kernel.setConfigurationItem(newItem);
    }
    catch (IllegalArgumentException exp) {
      valueTextField.setBackground(Color.red);
      flag = false;
    }
    if (flag) {
      dispose();
    }
  }//GEN-LAST:event_saveButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel configurationItemPanel;
  private javax.swing.JLabel dataType;
  private javax.swing.JTextField dataTypeTextField;
  private javax.swing.JLabel description;
  private javax.swing.JTextArea descriptionTextArea;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel key;
  private javax.swing.JTextField keyTextField;
  private javax.swing.JLabel nameSpace;
  private javax.swing.JTextField nameSpaceTextField;
  private javax.swing.JButton saveButton;
  private javax.swing.JLabel value;
  private javax.swing.JTextField valueTextField;
  // End of variables declaration//GEN-END:variables
// CHECKSTYLE:ON

  /**
   * Verifies that the value entered by user matches the data type and 
   * sets the textfield backgroud to red if not.
   */
  private class PassVerifier
      extends InputVerifier {

    /**
     * PassVerifier class constructor.
     */
    public PassVerifier() {
    }

    @Override
    public boolean verify(JComponent input) {
      JTextField tf = (JTextField) input;
      ConfigurationDataType type = configItem.getConstraint().getType();
      boolean flag = true;
      try {
        switch (type) {
          case INTEGER:
            int typeInt = Integer.parseInt(tf.getText());
            break;
          case LONG:
            long typeLong = Long.parseLong(tf.getText());
            break;
          case BOOLEAN:
            boolean typeBool = Boolean.parseBoolean(tf.getText());
            break;
          case BYTE:
            byte typeByte = Byte.parseByte(tf.getText());
            break;
          case DOUBLE:
            double typeDouble = Double.parseDouble(tf.getText());
            break;
          case FLOAT:
            float typeFloat = Float.parseFloat(tf.getText());
            break;
          case SHORT:
            short typeShort = Short.parseShort(tf.getText());
            break;
          case STRING:
            // do nada
            break;
          case ENUM:
            // do nada
            break;
          default:
            throw new IllegalArgumentException();
        }
      }
      catch (NumberFormatException exp) {
        flag = false;
      }
      return flag;
    }

    @Override
    public boolean shouldYieldFocus(JComponent input) {
      if (!verify(input)) {
        valueTextField.setBackground(Color.red);
      }
      return verify(input);
    }
  }
}
