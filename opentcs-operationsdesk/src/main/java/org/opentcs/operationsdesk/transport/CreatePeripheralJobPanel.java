// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.awt.event.ItemEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.util.gui.StringListCellRenderer;

/**
 */
public class CreatePeripheralJobPanel
    extends
      DialogContent {

  private static final Comparator<LocationModel> BY_NAME
      = (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.CREATE_PERIPHERAL_JOB_PATH);
  /**
   * List of locations to choose from.
   */
  private final List<LocationModel> locations;

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public CreatePeripheralJobPanel(ModelManager modelManager) {
    requireNonNull(modelManager, "modelManager");
    locations = modelManager.getModel().getLocationModels().stream()
        .filter(
            location -> !Objects.equals(
                location.getPropertyPeripheralState().getText(),
                PeripheralInformation.State.NO_PERIPHERAL.name()
            )
        )
        .sorted(BY_NAME)
        .collect(Collectors.toList());

    initComponents();
    setDialogTitle(bundle.getString("createPeripheralJobPanel.title"));
  }

  @Override
  public void initFields() {
    locations.stream().forEach(locationCombobox::addItem);
    loadOperations();
  }

  @Override
  public void update() {
    updateFailed = false;
    if (reservationTokenTextField.getText().isEmpty()) {
      updateFailed = true;
      JOptionPane.showMessageDialog(
          this,
          bundle.getString("createPeripheralJobPanel.optionPane_reserveTokenEmpty.message"),
          bundle.getString("createPeripheralJobPanel.optionPane_reserveTokenEmpty.title"),
          JOptionPane.ERROR_MESSAGE
      );

    }
    if (locationCombobox.getSelectedItem() == null
        || operationCombobox.getSelectedItem() == null) {
      updateFailed = true;
      JOptionPane.showMessageDialog(
          this,
          bundle.getString("createPeripheralJobPanel.optionPane_invalidOperation.message"),
          bundle.getString("createPeripheralJobPanel.optionPane_invalidOperation.title"),
          JOptionPane.ERROR_MESSAGE
      );
    }
  }

  private void loadOperations() {
    LocationModel location = (LocationModel) locationCombobox.getSelectedItem();
    if (location == null) {
      return;
    }

    operationCombobox.removeAllItems();
    for (String op : location.getLocationType().getPropertyAllowedPeripheralOperations()
        .getItems()) {
      operationCombobox.addItem(op);
    }
  }

  public String getReservationToken() {
    return reservationTokenTextField.getText();
  }

  public PeripheralOperationCreationTO getPeripheralOperation() {
    return new PeripheralOperationCreationTO(
        (String) operationCombobox.getSelectedItem(),
        ((LocationModel) locationCombobox.getSelectedItem()).getName()
    );
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

        reservationTokenLabel = new javax.swing.JLabel();
        reservationTokenTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCombobox = new javax.swing.JComboBox<>();
        operationLabel = new javax.swing.JLabel();
        operationCombobox = new javax.swing.JComboBox<>();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/operating/dialogs/createPeripheralJob"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("createPeripheralJobPanel.border.title"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        reservationTokenLabel.setText(bundle.getString("createPeripheralJobPanel.label_reservationToken.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(reservationTokenLabel, gridBagConstraints);

        reservationTokenTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(reservationTokenTextField, gridBagConstraints);

        locationLabel.setText(bundle.getString("createPeripheralJobPanel.label_location.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(locationLabel, gridBagConstraints);

        locationCombobox.setPreferredSize(new java.awt.Dimension(150, 20));
        locationCombobox.setRenderer(new StringListCellRenderer<LocationModel>(location -> (location!=null)?location.getName():""));
        locationCombobox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                locationComboboxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(locationCombobox, gridBagConstraints);

        operationLabel.setText(bundle.getString("createPeripheralJobPanel.label_operation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(operationLabel, gridBagConstraints);

        operationCombobox.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(operationCombobox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON
  // FORMATTER:ON

  private void locationComboboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_locationComboboxItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      loadOperations();
    }
  }//GEN-LAST:event_locationComboboxItemStateChanged

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<LocationModel> locationCombobox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox<String> operationCombobox;
    private javax.swing.JLabel operationLabel;
    private javax.swing.JLabel reservationTokenLabel;
    private javax.swing.JTextField reservationTokenTextField;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
