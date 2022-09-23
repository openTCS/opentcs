/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.commadapter.peripheral.loopback.commands.EnableManualModeCommand;
import org.opentcs.commadapter.peripheral.loopback.commands.FinishJobProcessingCommand;
import org.opentcs.commadapter.peripheral.loopback.commands.SetStateCommand;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralCommAdapterPanel;
import org.opentcs.util.CallWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The panel for the loopback peripheral communication adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralCommAdapterPanel
    extends PeripheralCommAdapterPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoopbackPeripheralCommAdapterPanel.class);
  /**
   * The service portal to use.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * The comm adapter's process model.
   */
  private LoopbackPeripheralProcessModel processModel;

  @Inject
  public LoopbackPeripheralCommAdapterPanel(@Assisted LoopbackPeripheralProcessModel processModel,
                                            KernelServicePortal servicePortal,
                                            @ServiceCallWrapper CallWrapper callWrapper) {
    this.processModel = requireNonNull(processModel, "processModel");
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");

    initComponents();
    updateComponentsEnabled();
    updateComponentContents();
  }

  @Override
  public void processModelChanged(PeripheralProcessModel processModel) {
    requireNonNull(processModel, "processModel");
    if (!(processModel instanceof LoopbackPeripheralProcessModel)) {
      return;
    }

    SwingUtilities.invokeLater(() -> {
      this.processModel = (LoopbackPeripheralProcessModel) processModel;
      updateComponentsEnabled();
      updateComponentContents();
    });
  }

  private void updateComponentsEnabled() {
    boolean enabled = processModel.isCommAdapterEnabled();
    stateComboBox.setEnabled(enabled);
    finishCurrentJobButton.setEnabled(processModel.isManualModeEnabled());
    failCurrentJobButton.setEnabled(processModel.isManualModeEnabled());
  }

  private void updateComponentContents() {
    stateComboBox.setSelectedItem(processModel.getState());
    manualModeRadioButton.setSelected(processModel.isManualModeEnabled());
    automaticModeRadioButton.setSelected(!processModel.isManualModeEnabled());
  }

  private void sendCommAdapterCommand(PeripheralAdapterCommand command) {
    try {
      callWrapper.call(() -> servicePortal.getPeripheralService()
          .sendCommAdapterCommand(processModel.getLocation(), command));
    }
    catch (Exception ex) {
      LOG.warn("Error sending comm adapter command '{}'", command, ex);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always regenerated
   * by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    statePanel = new javax.swing.JPanel();
    stateLabel = new javax.swing.JLabel();
    stateComboBox = new javax.swing.JComboBox<>();
    jobProcessingPanel = new javax.swing.JPanel();
    automaticModeRadioButton = new javax.swing.JRadioButton();
    manualModeRadioButton = new javax.swing.JRadioButton();
    finishCurrentJobButton = new javax.swing.JButton();
    failCurrentJobButton = new javax.swing.JButton();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/peripheral/loopback/Bundle"); // NOI18N
    statePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackPeripheralCommAdapterPanel.panel_state.border.title"))); // NOI18N
    statePanel.setName("statePanel"); // NOI18N
    statePanel.setLayout(new java.awt.GridBagLayout());

    stateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    stateLabel.setText(bundle.getString("loopbackPeripheralCommAdapterPanel.label_state.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
    statePanel.add(stateLabel, gridBagConstraints);

    stateComboBox.setModel(new DefaultComboBoxModel<>(Arrays.asList(PeripheralInformation.State.values()).stream().filter(state -> state != PeripheralInformation.State.NO_PERIPHERAL).toArray(PeripheralInformation.State[]::new)));
    stateComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        stateComboBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    statePanel.add(stateComboBox, gridBagConstraints);

    jobProcessingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackPeripheralCommAdapterPanel.panel_jobProcessing.border.title"))); // NOI18N
    jobProcessingPanel.setLayout(new java.awt.GridBagLayout());

    automaticModeRadioButton.setSelected(true);
    automaticModeRadioButton.setText(bundle.getString("loopbackPeripheralCommAdapterPanel.radioButton_jobProcessingAutomatic.text")); // NOI18N
    automaticModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    automaticModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    automaticModeRadioButton.setName("automaticModeRadioButton"); // NOI18N
    automaticModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        automaticModeRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    jobProcessingPanel.add(automaticModeRadioButton, gridBagConstraints);

    manualModeRadioButton.setText(bundle.getString("loopbackPeripheralCommAdapterPanel.radioButton_jobProcessingManual.text")); // NOI18N
    manualModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    manualModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    manualModeRadioButton.setName("manualModeRadioButton"); // NOI18N
    manualModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        manualModeRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    jobProcessingPanel.add(manualModeRadioButton, gridBagConstraints);

    finishCurrentJobButton.setText(bundle.getString("loopbackPeripheralCommAdapterPanel.button_finishCurrentJob.text")); // NOI18N
    finishCurrentJobButton.setEnabled(false);
    finishCurrentJobButton.setName("finishCurrentJobButton"); // NOI18N
    finishCurrentJobButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        finishCurrentJobButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    jobProcessingPanel.add(finishCurrentJobButton, gridBagConstraints);

    failCurrentJobButton.setText(bundle.getString("loopbackPeripheralCommAdapterPanel.button_failCurrentJob.text")); // NOI18N
    failCurrentJobButton.setEnabled(false);
    failCurrentJobButton.setName("processCurrentJobButton"); // NOI18N
    failCurrentJobButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        failCurrentJobButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    jobProcessingPanel.add(failCurrentJobButton, gridBagConstraints);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(statePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
          .addComponent(jobProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap(170, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(statePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jobProcessingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(165, Short.MAX_VALUE))
    );

    getAccessibleContext().setAccessibleName(bundle.getString("loopbackPeripheralCommAdapterPanel.accessibleName")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

  private void manualModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualModeRadioButtonActionPerformed
    sendCommAdapterCommand(new EnableManualModeCommand(manualModeRadioButton.isSelected()));
  }//GEN-LAST:event_manualModeRadioButtonActionPerformed

  private void automaticModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticModeRadioButtonActionPerformed
    sendCommAdapterCommand(new EnableManualModeCommand(!manualModeRadioButton.isSelected()));
  }//GEN-LAST:event_automaticModeRadioButtonActionPerformed

  private void finishCurrentJobButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishCurrentJobButtonActionPerformed
    sendCommAdapterCommand(new FinishJobProcessingCommand(false));
  }//GEN-LAST:event_finishCurrentJobButtonActionPerformed

  private void stateComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stateComboBoxItemStateChanged
    if (evt.getStateChange() != ItemEvent.SELECTED) {
      return;
    }

    PeripheralInformation.State selectedState
        = (PeripheralInformation.State) stateComboBox.getSelectedItem();
    if (selectedState == processModel.getState()) {
      // If the selection has changed due to an update in the process model (i.e. the user has not
      // selected an item in this panel's combo box), we don't want to send a set state command.
      return;
    }

    sendCommAdapterCommand(
        new SetStateCommand((PeripheralInformation.State) stateComboBox.getSelectedItem())
    );
  }//GEN-LAST:event_stateComboBoxItemStateChanged

  private void failCurrentJobButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_failCurrentJobButtonActionPerformed
    sendCommAdapterCommand(new FinishJobProcessingCommand(true));
  }//GEN-LAST:event_failCurrentJobButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton automaticModeRadioButton;
  private javax.swing.JButton failCurrentJobButton;
  private javax.swing.JButton finishCurrentJobButton;
  private javax.swing.JPanel jobProcessingPanel;
  private javax.swing.JRadioButton manualModeRadioButton;
  private javax.swing.JComboBox<PeripheralInformation.State> stateComboBox;
  private javax.swing.JLabel stateLabel;
  private javax.swing.JPanel statePanel;
  // End of variables declaration//GEN-END:variables
}
