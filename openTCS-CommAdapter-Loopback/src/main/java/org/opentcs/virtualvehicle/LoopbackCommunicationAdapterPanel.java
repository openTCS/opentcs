/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import com.google.inject.assistedinject.Assisted;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.util.Comparators;
import org.opentcs.virtualvehicle.inputcomponents.DropdownListInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.InputDialog;
import org.opentcs.virtualvehicle.inputcomponents.InputPanel;
import org.opentcs.virtualvehicle.inputcomponents.SingleTextInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.TextInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.TextListInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.TripleTextInputPanel;
import org.opentcs.util.gui.TCSObjectNameListCellRenderer;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import static java.util.Objects.requireNonNull;

/**
 * The LoopbackCommunicationAdapterPanel corresponding to the
 * LoopbackCommunicationAdapter.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommunicationAdapterPanel
    extends VehicleCommAdapterPanel {

  /**
   * A resource bundle for internationalization.
   */
  private static final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle");
  /**
   * The Kernel.
   */
  private final LocalKernel kernel;
  /**
   * This panel's communication adapter.
   */
  private final LoopbackCommunicationAdapter commAdapter;
  /**
   * The comm adapter's model.
   */
  private final LoopbackVehicleModel vehicleModel;

  /**
   * Creates new LoopbackCommunicationAdapterPanel.
   *
   * @param adapter The loopback communication adapter.
   */
  @Inject
  LoopbackCommunicationAdapterPanel(LocalKernel kernel,
                                    @Assisted LoopbackCommunicationAdapter adapter) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.commAdapter = requireNonNull(adapter, "adapter");
    this.vehicleModel = adapter.getProcessModel();
    initComponents();

    devicesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    fileChooser.addChoosableFileFilter(new XmlFileFilter());
    OpSpecListListener listListener = new OpSpecListListener();
    operationSpecList.addListSelectionListener(listListener);
    operationSpecList.getModel().addListDataListener(listListener);
    loadOperationSpecList();
    ((DefaultListModel<String>) opSpecDeviceList.getModel()).addElement("-");
    // Make the list of devices non-selectable by overriding it's cell renderer
    opSpecDeviceList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value,
                                                    int index,
                                                    boolean isSelected,
                                                    boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, value, index,
                                                  false, false);
      }
    });

    /* // Load vehicle profile 
     VehicleProfile profile = null;
     String selectedProfileName = VehicleProfiles.getSelectedProfile();
     if (selectedProfileName != null) {
     profile = VehicleProfiles.getProfile(selectedProfileName);
     }
     if (profile == null) {
     profile = new VehicleProfile();
     }
     loadPropertiesFromProfile(profile); */
    profilesPanel.setVisible(false);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof LoopbackVehicleModel) {
      updateLoopbackVehicleModelData(evt);
      updateVehicleProcessModelData(evt);
    }
  }

  private void updateLoopbackVehicleModelData(PropertyChangeEvent evt) {
    LoopbackVehicleModel vm = (LoopbackVehicleModel) evt.getSource();
    if (Objects.equals(evt.getPropertyName(),
                       LoopbackVehicleModel.Attribute.DEFAULT_OPERATING_TIME.name())) {
      updateDefaultOperatingTime(vm.getDefaultOperatingTime());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.IDLE_POWER.name())) {
      updateIdlePower(vm.getIdlePower());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.MAX_ACCELERATION.name())) {
      updateMaxAcceleration(vm.getMaxAcceleration());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.MAX_DECELERATION.name())) {
      updateMaxDeceleration(vm.getMaxDeceleration());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.MAX_FORWARD_VELOCITY.name())) {
      updateMaxForwardVelocity(vm.getMaxFwdVelocity());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.MAX_REVERSE_VELOCITY.name())) {
      updateMaxReverseVelocity(vm.getMaxRevVelocity());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.MOVEMENT_POWER.name())) {
      updateMovementPower(vm.getMovementPower());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.OPERATION_POWER.name())) {
      updateOperationPower(vm.getOperationPower());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.OPERATION_SPECS.name())) {
      updateOperationSpecs(vm.getOperationSpecs());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.SINGLE_STEP_MODE.name())) {
      updateSingleStepMode(vm.isSingleStepModeEnabled());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            LoopbackVehicleModel.Attribute.VEHICLE_PAUSED.name())) {
      updateVehiclePaused(vm.isVehiclePaused());
    }
    // XXX This should be moved from the comm adapter itself to the process model.
    updateCapacity(commAdapter.getEnergyCapacity());
  }

  private void updateVehicleProcessModelData(PropertyChangeEvent evt) {
    VehicleProcessModel vpm = (VehicleProcessModel) evt.getSource();
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED.name())) {
      updateCommAdapterEnabled(vpm.isCommAdapterEnabled());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.POSITION.name())) {
      updatePosition(vpm.getVehiclePosition());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState(vpm.getVehicleState());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.PRECISE_POSITION.name())) {
      updatePrecisePosition(vpm.getVehiclePrecisePosition());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ORIENTATION_ANGLE.name())) {
      updateOrientationAngle(vpm.getVehicleOrientationAngle());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name())) {
      updateVehicleLoadHandlingDevices(vpm.getVehicleLoadHandlingDevices());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ENERGY_LEVEL.name())) {
      updateEnergyLevel(vpm.getVehicleEnergyLevel());
    }
  }

  private void updateEnergyLevel(int energy) {
    SwingUtilities.invokeLater(() -> energyLevelTxt.setText(Integer.toString(energy)));
  }

  private void updateCapacity(double capacity) {
    SwingUtilities.invokeLater(() -> {
      DecimalFormat format = new DecimalFormat("0.0");
      String capacityString = format.format(capacity);
      energyCapacityText.setText(capacityString);
    });
  }

  private void updateCommAdapterEnabled(boolean isEnabled) {
    SwingUtilities.invokeLater(() -> {
      setStatePanelEnabled(isEnabled);
      chkBoxEnable.setSelected(isEnabled);
    });
  }

  private void updatePosition(String vehiclePosition) {
    SwingUtilities.invokeLater(() -> {
      if (vehiclePosition == null) {
        positionTxt.setText("");
      }
      else {
        for (Point curPoint : kernel.getTCSObjects(Point.class)) {
          if (curPoint.getName().equals(vehiclePosition)) {
            positionTxt.setText(curPoint.getName());
            break;
          }
        }
      }
    });
  }

  private void updateVehicleState(Vehicle.State vehicleState) {
    SwingUtilities.invokeLater(() -> stateTxt.setText(vehicleState.toString()));
  }

  private void updatePrecisePosition(Triple precisePos) {
    SwingUtilities.invokeLater(() -> {
      if (precisePos == null) {
        setPrecisePosText(null, null, null);
      }
      else {
        setPrecisePosText(precisePos.getX(), precisePos.getY(), precisePos.getZ());
      }
    });
  }

  private void updateOrientationAngle(Double orientation) {
    SwingUtilities.invokeLater(() -> {
      if (Double.isNaN(orientation)) {
        orientationAngleTxt.setText(bundle.getString("OrientationAngleNotSet"));
      }
      else {
        orientationAngleTxt.setText(Double.toString(orientation));
      }
    });
  }

  private void updateVehicleLoadHandlingDevices(List<LoadHandlingDevice> loadHandlingDevices) {
    SwingUtilities.invokeLater(()
        -> ((LoadHandlingDeviceTableModel) devicesTable.getModel())
        .updateLoadHandlingDevices(loadHandlingDevices));
  }

  private void updateDefaultOperatingTime(int defaultOperatingTime) {
    SwingUtilities.invokeLater(
        () -> defaultOpTimeTxt.setText(Integer.toString(defaultOperatingTime)));
  }

  private void updateIdlePower(double idlePower) {
    SwingUtilities.invokeLater(() -> idlePowerTxt.setText(Double.toString(idlePower)));
  }

  private void updateMaxAcceleration(int maxAcceleration) {
    SwingUtilities.invokeLater(() -> maxAccelTxt.setText(Integer.toString(maxAcceleration)));
  }

  private void updateMaxDeceleration(int maxDeceleration) {
    SwingUtilities.invokeLater(() -> maxDecelTxt.setText(Integer.toString(maxDeceleration)));
  }

  private void updateMaxForwardVelocity(int maxFwdVelocity) {
    SwingUtilities.invokeLater(() -> maxFwdVeloTxt.setText(Integer.toString(maxFwdVelocity)));
  }

  private void updateMaxReverseVelocity(int maxRevVelocity) {
    SwingUtilities.invokeLater(() -> maxRevVeloTxt.setText(Integer.toString(maxRevVelocity)));
  }

  private void updateMovementPower(double movementPower) {
    SwingUtilities.invokeLater(()
        -> movementPowerTxt.setText(Double.toString(movementPower)));
  }

  private void updateOperationPower(double operationPower) {
    SwingUtilities.invokeLater(() -> operationPowerTxt.setText(Double.toString(operationPower)));
  }

  private void updateOperationSpecs(Map<String, OperationSpec> operationSpecs) {
    SwingUtilities.invokeLater(() -> {
      DefaultListModel<OperationSpec> newModel
          = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
      newModel.clear();
      for (OperationSpec os : operationSpecs.values()) {
        newModel.addElement(os);
      }
      setOpSpecDetailPanel(operationSpecList.getSelectedValue());
    });
  }

  private void updateSingleStepMode(boolean singleStepMode) {
    SwingUtilities.invokeLater(() -> {
      triggerButton.setEnabled(singleStepMode);
      singleModeRadioButton.setSelected(singleStepMode);
      flowModeRadioButton.setSelected(!singleStepMode);
    });
  }

  private void updateVehiclePaused(boolean isVehiclePaused) {
    SwingUtilities.invokeLater(() -> pauseVehicleCheckBox.setSelected(isVehiclePaused));
  }

  /**
   * Loads the OpeationSpecs saved in <code>LoopbackCommunicationAdapter</code>
   * into the operation spec list. Can be used to initialize the list or to
   * update it's content after external changes to the operation specs in
   * <code>LoopbackCommunicationAdapter</code>.
   */
  private void loadOperationSpecList() {
    DefaultListModel<OperationSpec> opSpecs
        = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
    opSpecs.clear();
    for (OperationSpec opSpec : vehicleModel.getOperationSpecs().values()) {
      opSpecs.addElement(new OperationSpec(opSpec));
    }
  }

  /**
   * Apply the operation specs from the list of operations to the communication
   * adapter.
   */
  private void applyOperationSpecs() {
    Map<String, OperationSpec> opSpecsMap = new HashMap<>();
    DefaultListModel<OperationSpec> model
        = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
    for (int i = 0; i < model.size(); i++) {
      OperationSpec opSpec = model.get(i);
      opSpecsMap.put(opSpec.getOperationName(), opSpec);
    }
    vehicleModel.setOperationSpecs(opSpecsMap);
  }

  /**
   * Enable/disable the input fields and buttons in the "Current position/state"
   * panel. If disabled the user can not change any values or modify the
   * vehicles state.
   *
   * @param enabled boolean indicating if the panel should be enabled
   */
  private void setStatePanelEnabled(boolean enabled) {
    positionTxt.setEnabled(enabled);
    stateTxt.setEnabled(enabled);
    energyLevelTxt.setEnabled(enabled);
    precisePosTextArea.setEnabled(enabled);
    orientationAngleTxt.setEnabled(enabled);
    pauseVehicleCheckBox.setEnabled(enabled);
  }

  /**
   * Enable/disable the OperationSpecDetailPanel.
   *
   * @param enabled true, if panel should be enabled
   */
  private void setOpSpecDetailPanelEnabled(boolean enabled) {
    operationNameLabel.setEnabled(enabled);
    operationNameValue.setEnabled(enabled);
    operatingTimeLabel.setEnabled(enabled);
    operatingTimeValue.setEnabled(enabled);
    changesDevicesLabel.setEnabled(enabled);
    changesDevicesValue.setEnabled(enabled);
    opSpecDeviceList.setEnabled(enabled);
    loadHandlingDevicesLabel.setEnabled(enabled);
  }

  /**
   * Make the operationSpecDetailPanel view the data of the specified
   * operationSpec.
   * If opSpec is <code>null</code> the panel will be cleared.
   *
   * @param opSpec the OperationSpec to display.
   */
  private void setOpSpecDetailPanel(OperationSpec opSpec) {
    if (opSpec == null) {
      operationNameValue.setText("-");
      operatingTimeValue.setText("-");
      changesDevicesValue.setText("-");
      ((DefaultListModel<String>) opSpecDeviceList.getModel()).clear();
      setOpSpecDetailPanelEnabled(false);
    }
    else {
      operationNameValue.setText(opSpec.getOperationName());
      operatingTimeValue.setText(String.valueOf(opSpec.getOperatingTime())
          + " " + bundle.getString("opSpecPanel.ms"));
      changesDevicesValue.setText(opSpec.changesLoadCondition()
          ? bundle.getString("opSpecPanel.yes")
          : bundle.getString("opSpecPanel.no"));
      setOpSpecDetailPanelEnabled(true);
      DefaultListModel<String> list
          = (DefaultListModel<String>) opSpecDeviceList.getModel();
      list.clear();
      if (opSpec.changesLoadCondition()) {
        for (LoadHandlingDevice device : opSpec.getLoadCondition()) {
          list.addElement(device.getLabel() + (device.isFull() ? " (full)" : ""));
        }
      }
      else {
        list.addElement("-");
      }
    }
  }

  /**
   * Clear the OperationSpecDetailPanel.
   */
  private void clearOpSpecDetailPanel() {
    setOpSpecDetailPanel(null);
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

    modeButtonGroup = new javax.swing.ButtonGroup();
    fileChooser = new javax.swing.JFileChooser();
    vehicleBahaviourPanel = new javax.swing.JPanel();
    PropsPowerOuterContainerPanel = new javax.swing.JPanel();
    PropsPowerInnerContainerPanel = new javax.swing.JPanel();
    vehiclePropsPanel = new javax.swing.JPanel();
    maxFwdVeloLbl = new javax.swing.JLabel();
    maxFwdVeloTxt = new javax.swing.JTextField();
    maxFwdVeloUnitLbl = new javax.swing.JLabel();
    maxRevVeloLbl = new javax.swing.JLabel();
    maxRevVeloTxt = new javax.swing.JTextField();
    maxRevVeloUnitLbl = new javax.swing.JLabel();
    maxAccelLbl = new javax.swing.JLabel();
    maxAccelTxt = new javax.swing.JTextField();
    maxAccelUnitLbl = new javax.swing.JLabel();
    maxDecelTxt = new javax.swing.JTextField();
    maxDecelLbl = new javax.swing.JLabel();
    maxDecelUnitLbl = new javax.swing.JLabel();
    defaultOpTimeLbl = new javax.swing.JLabel();
    defaultOpTimeUntiLbl = new javax.swing.JLabel();
    defaultOpTimeTxt = new javax.swing.JTextField();
    vehiclePowerPanel = new javax.swing.JPanel();
    movementPowerLbl = new javax.swing.JLabel();
    operationPowerLbl = new javax.swing.JLabel();
    capacityLabel = new javax.swing.JLabel();
    idlePowerLbl = new javax.swing.JLabel();
    energyCapacityText = new javax.swing.JTextField();
    movementPowerTxt = new javax.swing.JTextField();
    operationPowerTxt = new javax.swing.JTextField();
    idlePowerTxt = new javax.swing.JTextField();
    idlePowerUnitLbl = new javax.swing.JLabel();
    operationPowerUnitLbl = new javax.swing.JLabel();
    movementPowerUnitLbl = new javax.swing.JLabel();
    capacityDimensionLabel = new javax.swing.JLabel();
    fillingPanel = new javax.swing.JPanel();
    opSpecPanel = new javax.swing.JPanel();
    opSpecActionPanel = new javax.swing.JPanel();
    addOpSpecButton = new javax.swing.JButton();
    editOpSpecButton = new javax.swing.JButton();
    rmOpSpecButton = new javax.swing.JButton();
    opSpecListScrollPane = new javax.swing.JScrollPane();
    operationSpecList = new javax.swing.JList<>();
    opSpecDetailPanel = new javax.swing.JPanel();
    opSpecDetailContainerPanel = new javax.swing.JPanel();
    operationNameLabel = new javax.swing.JLabel();
    operatingTimeLabel = new javax.swing.JLabel();
    changesDevicesLabel = new javax.swing.JLabel();
    loadHandlingDevicesLabel = new javax.swing.JLabel();
    changesDevicesValue = new javax.swing.JLabel();
    operatingTimeValue = new javax.swing.JLabel();
    operationNameValue = new javax.swing.JLabel();
    opSpecDeviceScrollPane = new javax.swing.JScrollPane();
    opSpecDeviceList = new javax.swing.JList<>();
    profilesContainerPanel = new javax.swing.JPanel();
    profilesPanel = new javax.swing.JPanel();
    saveProfileButton = new javax.swing.JButton();
    loadProfilesButton = new javax.swing.JButton();
    deleteProfilesButton = new javax.swing.JButton();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
    vehicleStatePanel = new javax.swing.JPanel();
    stateContainerPanel = new javax.swing.JPanel();
    connectionPanel = new javax.swing.JPanel();
    chkBoxEnable = new javax.swing.JCheckBox();
    curPosPanel = new javax.swing.JPanel();
    energyLevelTxt = new javax.swing.JTextField();
    energyLevelLbl = new javax.swing.JLabel();
    pauseVehicleCheckBox = new javax.swing.JCheckBox();
    orientationAngleLbl = new javax.swing.JLabel();
    precisePosUnitLabel = new javax.swing.JLabel();
    orientationAngleTxt = new javax.swing.JTextField();
    energyLevelLabel = new javax.swing.JLabel();
    orientationLabel = new javax.swing.JLabel();
    positionTxt = new javax.swing.JTextField();
    positionLabel = new javax.swing.JLabel();
    pauseVehicleLabel = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    stateTxt = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    precisePosTextArea = new javax.swing.JTextArea();
    eventPanel = new javax.swing.JPanel();
    includeAppendixCheckBox = new javax.swing.JCheckBox();
    appendixTxt = new javax.swing.JTextField();
    dispatchEventButton = new javax.swing.JButton();
    dispatchCommandFailedButton = new javax.swing.JButton();
    controlTabPanel = new javax.swing.JPanel();
    singleModeRadioButton = new javax.swing.JRadioButton();
    flowModeRadioButton = new javax.swing.JRadioButton();
    triggerButton = new javax.swing.JButton();
    loadDevicePanel = new javax.swing.JPanel();
    jScrollPane3 = new javax.swing.JScrollPane();
    devicesTable = new javax.swing.JTable();
    jPanel1 = new javax.swing.JPanel();
    addDevicesButton = new javax.swing.JButton();
    deleteDeviceButton = new javax.swing.JButton();
    saveDeviceButton = new javax.swing.JButton();

    setName("LoopbackCommunicationAdapterPanel"); // NOI18N
    setLayout(new java.awt.BorderLayout());

    vehicleBahaviourPanel.setLayout(new java.awt.BorderLayout());

    PropsPowerOuterContainerPanel.setLayout(new java.awt.BorderLayout());

    PropsPowerInnerContainerPanel.setLayout(new javax.swing.BoxLayout(PropsPowerInnerContainerPanel, javax.swing.BoxLayout.X_AXIS));

    vehiclePropsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Vehicle_properties"))); // NOI18N
    vehiclePropsPanel.setLayout(new java.awt.GridBagLayout());

    maxFwdVeloLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    maxFwdVeloLbl.setText(bundle.getString("maxFwdVelocityLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxFwdVeloLbl, gridBagConstraints);

    maxFwdVeloTxt.setEditable(false);
    maxFwdVeloTxt.setBackground(new java.awt.Color(255, 255, 255));
    maxFwdVeloTxt.setColumns(5);
    maxFwdVeloTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maxFwdVeloTxt.setText("1000");
    maxFwdVeloTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    maxFwdVeloTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        maxFwdVeloTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxFwdVeloTxt, gridBagConstraints);

    maxFwdVeloUnitLbl.setText("mm/s");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxFwdVeloUnitLbl, gridBagConstraints);

    maxRevVeloLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    maxRevVeloLbl.setText(bundle.getString("maxRevVelocityLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxRevVeloLbl, gridBagConstraints);

    maxRevVeloTxt.setEditable(false);
    maxRevVeloTxt.setBackground(new java.awt.Color(255, 255, 255));
    maxRevVeloTxt.setColumns(5);
    maxRevVeloTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maxRevVeloTxt.setText("1000");
    maxRevVeloTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    maxRevVeloTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        maxRevVeloTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxRevVeloTxt, gridBagConstraints);

    maxRevVeloUnitLbl.setText("mm/s");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxRevVeloUnitLbl, gridBagConstraints);

    maxAccelLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    maxAccelLbl.setText(bundle.getString("maxAccelerationLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxAccelLbl, gridBagConstraints);

    maxAccelTxt.setEditable(false);
    maxAccelTxt.setBackground(new java.awt.Color(255, 255, 255));
    maxAccelTxt.setColumns(5);
    maxAccelTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maxAccelTxt.setText("1000");
    maxAccelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    maxAccelTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        maxAccelTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxAccelTxt, gridBagConstraints);

    maxAccelUnitLbl.setText("<html>mm/s<sup>2</sup>");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxAccelUnitLbl, gridBagConstraints);

    maxDecelTxt.setEditable(false);
    maxDecelTxt.setBackground(new java.awt.Color(255, 255, 255));
    maxDecelTxt.setColumns(5);
    maxDecelTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maxDecelTxt.setText("1000");
    maxDecelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    maxDecelTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        maxDecelTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxDecelTxt, gridBagConstraints);

    maxDecelLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    maxDecelLbl.setText(bundle.getString("maxDecelerationLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxDecelLbl, gridBagConstraints);

    maxDecelUnitLbl.setText("<html>mm/s<sup>2</sup>");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(maxDecelUnitLbl, gridBagConstraints);

    defaultOpTimeLbl.setText(bundle.getString("defaultOperatingTime")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(defaultOpTimeLbl, gridBagConstraints);

    defaultOpTimeUntiLbl.setText("ms");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(defaultOpTimeUntiLbl, gridBagConstraints);

    defaultOpTimeTxt.setEditable(false);
    defaultOpTimeTxt.setBackground(new java.awt.Color(255, 255, 255));
    defaultOpTimeTxt.setColumns(5);
    defaultOpTimeTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    defaultOpTimeTxt.setText("1000");
    defaultOpTimeTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    defaultOpTimeTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        defaultOpTimeTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePropsPanel.add(defaultOpTimeTxt, gridBagConstraints);

    PropsPowerInnerContainerPanel.add(vehiclePropsPanel);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle"); // NOI18N
    vehiclePowerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("vehiclePowerPanelTitle"))); // NOI18N
    vehiclePowerPanel.setLayout(new java.awt.GridBagLayout());

    movementPowerLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    movementPowerLbl.setText(bundle.getString("movementPowerLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePowerPanel.add(movementPowerLbl, gridBagConstraints);

    operationPowerLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    operationPowerLbl.setText(bundle.getString("operationPowerLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePowerPanel.add(operationPowerLbl, gridBagConstraints);

    capacityLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    capacityLabel.setText(bundle.getString("capacityLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePowerPanel.add(capacityLabel, gridBagConstraints);

    idlePowerLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    idlePowerLbl.setText(bundle.getString("idlePowerLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePowerPanel.add(idlePowerLbl, gridBagConstraints);

    energyCapacityText.setEditable(false);
    energyCapacityText.setBackground(new java.awt.Color(255, 255, 255));
    energyCapacityText.setColumns(5);
    energyCapacityText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    energyCapacityText.setText("1000");
    energyCapacityText.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    energyCapacityText.setEnabled(false);
    energyCapacityText.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        energyCapacityTextMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    vehiclePowerPanel.add(energyCapacityText, gridBagConstraints);

    movementPowerTxt.setEditable(false);
    movementPowerTxt.setBackground(new java.awt.Color(255, 255, 255));
    movementPowerTxt.setColumns(5);
    movementPowerTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    movementPowerTxt.setText("1000");
    movementPowerTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    movementPowerTxt.setEnabled(false);
    movementPowerTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        movementPowerTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    vehiclePowerPanel.add(movementPowerTxt, gridBagConstraints);

    operationPowerTxt.setEditable(false);
    operationPowerTxt.setBackground(new java.awt.Color(255, 255, 255));
    operationPowerTxt.setColumns(5);
    operationPowerTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    operationPowerTxt.setText("1000");
    operationPowerTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    operationPowerTxt.setEnabled(false);
    operationPowerTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        operationPowerTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    vehiclePowerPanel.add(operationPowerTxt, gridBagConstraints);

    idlePowerTxt.setEditable(false);
    idlePowerTxt.setBackground(new java.awt.Color(255, 255, 255));
    idlePowerTxt.setColumns(5);
    idlePowerTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    idlePowerTxt.setText("1000");
    idlePowerTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    idlePowerTxt.setEnabled(false);
    idlePowerTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        idlePowerTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    vehiclePowerPanel.add(idlePowerTxt, gridBagConstraints);

    idlePowerUnitLbl.setText(bundle.getString("idlePowerUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    vehiclePowerPanel.add(idlePowerUnitLbl, gridBagConstraints);

    operationPowerUnitLbl.setText(bundle.getString("operationPowerUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    vehiclePowerPanel.add(operationPowerUnitLbl, gridBagConstraints);

    movementPowerUnitLbl.setText(bundle.getString("movementPowerUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    vehiclePowerPanel.add(movementPowerUnitLbl, gridBagConstraints);

    capacityDimensionLabel.setText(bundle.getString("capacityUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    vehiclePowerPanel.add(capacityDimensionLabel, gridBagConstraints);

    fillingPanel.setPreferredSize(new java.awt.Dimension(10, 14));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    vehiclePowerPanel.add(fillingPanel, gridBagConstraints);

    PropsPowerInnerContainerPanel.add(vehiclePowerPanel);

    PropsPowerOuterContainerPanel.add(PropsPowerInnerContainerPanel, java.awt.BorderLayout.WEST);

    vehicleBahaviourPanel.add(PropsPowerOuterContainerPanel, java.awt.BorderLayout.NORTH);

    opSpecPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Operation_specs"))); // NOI18N
    opSpecPanel.setMinimumSize(new java.awt.Dimension(334, 300));
    opSpecPanel.setLayout(new java.awt.BorderLayout());

    opSpecActionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    addOpSpecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/add_icon.png"))); // NOI18N
    addOpSpecButton.setToolTipText(bundle.getString("Add_entry")); // NOI18N
    addOpSpecButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    addOpSpecButton.setContentAreaFilled(false);
    addOpSpecButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addOpSpecButtonActionPerformed(evt);
      }
    });
    opSpecActionPanel.add(addOpSpecButton);

    editOpSpecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/edit_icon.png"))); // NOI18N
    editOpSpecButton.setToolTipText(bundle.getString("Edit_entry")); // NOI18N
    editOpSpecButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    editOpSpecButton.setContentAreaFilled(false);
    editOpSpecButton.setEnabled(false);
    editOpSpecButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editOpSpecButtonActionPerformed(evt);
      }
    });
    opSpecActionPanel.add(editOpSpecButton);

    rmOpSpecButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/remove_icon.png"))); // NOI18N
    rmOpSpecButton.setToolTipText(bundle.getString("Remove_entry")); // NOI18N
    rmOpSpecButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    rmOpSpecButton.setContentAreaFilled(false);
    rmOpSpecButton.setEnabled(false);
    rmOpSpecButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rmOpSpecButtonActionPerformed(evt);
      }
    });
    opSpecActionPanel.add(rmOpSpecButton);

    opSpecPanel.add(opSpecActionPanel, java.awt.BorderLayout.NORTH);

    opSpecListScrollPane.setMaximumSize(new java.awt.Dimension(170, 32767));
    opSpecListScrollPane.setMinimumSize(new java.awt.Dimension(170, 23));
    opSpecListScrollPane.setPreferredSize(new java.awt.Dimension(170, 130));

    operationSpecList.setModel(new DefaultListModel<OperationSpec>()
    );
    operationSpecList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    opSpecListScrollPane.setViewportView(operationSpecList);

    opSpecPanel.add(opSpecListScrollPane, java.awt.BorderLayout.WEST);

    opSpecDetailPanel.setLayout(new java.awt.BorderLayout());

    opSpecDetailContainerPanel.setLayout(new java.awt.GridBagLayout());

    operationNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    operationNameLabel.setText(bundle.getString("opSpecPanel.name")); // NOI18N
    operationNameLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(operationNameLabel, gridBagConstraints);

    operatingTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    operatingTimeLabel.setText(bundle.getString("opSpecPanel.time")); // NOI18N
    operatingTimeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(operatingTimeLabel, gridBagConstraints);

    changesDevicesLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    changesDevicesLabel.setText(bundle.getString("opSpecPanel.deviceChange")); // NOI18N
    changesDevicesLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(changesDevicesLabel, gridBagConstraints);

    loadHandlingDevicesLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    loadHandlingDevicesLabel.setText(bundle.getString("opSpecPanel.devicesLabel")); // NOI18N
    loadHandlingDevicesLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(loadHandlingDevicesLabel, gridBagConstraints);

    changesDevicesValue.setText("-");
    changesDevicesValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(changesDevicesValue, gridBagConstraints);

    operatingTimeValue.setText("-");
    operatingTimeValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(operatingTimeValue, gridBagConstraints);

    operationNameValue.setText("-");
    operationNameValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    opSpecDetailContainerPanel.add(operationNameValue, gridBagConstraints);

    opSpecDetailPanel.add(opSpecDetailContainerPanel, java.awt.BorderLayout.NORTH);

    opSpecDeviceScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    opSpecDeviceScrollPane.setViewportBorder(null);

    opSpecDeviceList.setBackground(new Color(opSpecDetailPanel.getBackground().getRed(), opSpecDetailPanel.getBackground().getGreen(), opSpecDetailPanel.getBackground().getBlue()));
    opSpecDeviceList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    opSpecDeviceList.setModel(new DefaultListModel<String>()
    );
    opSpecDeviceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    opSpecDeviceScrollPane.setViewportView(opSpecDeviceList);

    opSpecDetailPanel.add(opSpecDeviceScrollPane, java.awt.BorderLayout.CENTER);

    opSpecPanel.add(opSpecDetailPanel, java.awt.BorderLayout.CENTER);

    vehicleBahaviourPanel.add(opSpecPanel, java.awt.BorderLayout.CENTER);
    opSpecPanel.getAccessibleContext().setAccessibleName("OperationSpecifications");

    profilesContainerPanel.setLayout(new java.awt.BorderLayout());

    profilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("vehicleProfilesLabel"))); // NOI18N

    saveProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/save_icon.png"))); // NOI18N
    saveProfileButton.setToolTipText(bundle.getString("saveParameters")); // NOI18N
    saveProfileButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    saveProfileButton.setContentAreaFilled(false);
    saveProfileButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    saveProfileButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveProfileButtonActionPerformed(evt);
      }
    });
    profilesPanel.add(saveProfileButton);

    loadProfilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/load_icon.png"))); // NOI18N
    loadProfilesButton.setToolTipText(bundle.getString("loadParameters")); // NOI18N
    loadProfilesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    loadProfilesButton.setContentAreaFilled(false);
    loadProfilesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    loadProfilesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        loadProfileButtonActionPerformed(evt);
      }
    });
    profilesPanel.add(loadProfilesButton);

    deleteProfilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/virtualvehicle/images/delete_icon.png"))); // NOI18N
    deleteProfilesButton.setToolTipText(bundle.getString("deleteProfile")); // NOI18N
    deleteProfilesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    deleteProfilesButton.setContentAreaFilled(false);
    deleteProfilesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    deleteProfilesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteProfilesButtonActionPerformed(evt);
      }
    });
    profilesPanel.add(deleteProfilesButton);

    profilesContainerPanel.add(profilesPanel, java.awt.BorderLayout.WEST);
    profilesContainerPanel.add(filler1, java.awt.BorderLayout.CENTER);

    vehicleBahaviourPanel.add(profilesContainerPanel, java.awt.BorderLayout.SOUTH);

    add(vehicleBahaviourPanel, java.awt.BorderLayout.CENTER);

    vehicleStatePanel.setLayout(new java.awt.BorderLayout());

    stateContainerPanel.setLayout(new javax.swing.BoxLayout(stateContainerPanel, javax.swing.BoxLayout.Y_AXIS));

    connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Adapter_status"))); // NOI18N
    connectionPanel.setName("connectionPanel"); // NOI18N
    connectionPanel.setLayout(new java.awt.GridBagLayout());

    chkBoxEnable.setText(bundle.getString("Enable_communication_adapter")); // NOI18N
    chkBoxEnable.setName("chkBoxEnable"); // NOI18N
    chkBoxEnable.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        chkBoxEnableActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    connectionPanel.add(chkBoxEnable, gridBagConstraints);

    stateContainerPanel.add(connectionPanel);

    curPosPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("CurrentPositionSettings"))); // NOI18N
    curPosPanel.setName("curPosPanel"); // NOI18N
    curPosPanel.setLayout(new java.awt.GridBagLayout());

    energyLevelTxt.setEditable(false);
    energyLevelTxt.setBackground(new java.awt.Color(255, 255, 255));
    energyLevelTxt.setText("100");
    energyLevelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    energyLevelTxt.setName("energyLevelTxt"); // NOI18N
    energyLevelTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        energyLevelTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    curPosPanel.add(energyLevelTxt, gridBagConstraints);

    energyLevelLbl.setText("%");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    curPosPanel.add(energyLevelLbl, gridBagConstraints);

    pauseVehicleCheckBox.setEnabled(false);
    pauseVehicleCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    pauseVehicleCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    pauseVehicleCheckBox.setName("pauseVehicleCheckBox"); // NOI18N
    pauseVehicleCheckBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        pauseVehicleCheckBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    curPosPanel.add(pauseVehicleCheckBox, gridBagConstraints);

    orientationAngleLbl.setText(bundle.getString("AngleUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    curPosPanel.add(orientationAngleLbl, gridBagConstraints);

    precisePosUnitLabel.setText(bundle.getString("precisePosUnit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    curPosPanel.add(precisePosUnitLabel, gridBagConstraints);

    orientationAngleTxt.setEditable(false);
    orientationAngleTxt.setBackground(new java.awt.Color(255, 255, 255));
    orientationAngleTxt.setText(bundle.getString("OrientationAngleNotSet")); // NOI18N
    orientationAngleTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    orientationAngleTxt.setName("orientationAngleTxt"); // NOI18N
    orientationAngleTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        orientationAngleTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    curPosPanel.add(orientationAngleTxt, gridBagConstraints);

    energyLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    energyLevelLabel.setText(bundle.getString("energyLevelLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(energyLevelLabel, gridBagConstraints);

    orientationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    orientationLabel.setText(bundle.getString("orientationLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(orientationLabel, gridBagConstraints);

    positionTxt.setEditable(false);
    positionTxt.setBackground(new java.awt.Color(255, 255, 255));
    positionTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    positionTxt.setName("positionTxt"); // NOI18N
    positionTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        positionTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    curPosPanel.add(positionTxt, gridBagConstraints);

    positionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    positionLabel.setText(bundle.getString("positionLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(positionLabel, gridBagConstraints);

    pauseVehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    pauseVehicleLabel.setText(bundle.getString("pauseVehicle")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(pauseVehicleLabel, gridBagConstraints);

    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel2.setText(bundle.getString("stateLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(jLabel2, gridBagConstraints);

    stateTxt.setEditable(false);
    stateTxt.setBackground(new java.awt.Color(255, 255, 255));
    stateTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    stateTxt.setName("stateTxt"); // NOI18N
    stateTxt.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        stateTxtMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    curPosPanel.add(stateTxt, gridBagConstraints);

    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel3.setText(bundle.getString("precisePosLabel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    curPosPanel.add(jLabel3, gridBagConstraints);

    precisePosTextArea.setEditable(false);
    precisePosTextArea.setFont(positionTxt.getFont());
    precisePosTextArea.setRows(3);
    precisePosTextArea.setText("X:\nY:\nZ:");
    precisePosTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    precisePosTextArea.setName("precisePosTextArea"); // NOI18N
    precisePosTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        precisePosTextAreaMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    curPosPanel.add(precisePosTextArea, gridBagConstraints);

    stateContainerPanel.add(curPosPanel);
    curPosPanel.getAccessibleContext().setAccessibleName("Change");

    eventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Event_dispatching"))); // NOI18N
    eventPanel.setLayout(new java.awt.GridBagLayout());

    includeAppendixCheckBox.setText(bundle.getString("Include_appendix")); // NOI18N
    includeAppendixCheckBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        includeAppendixCheckBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    eventPanel.add(includeAppendixCheckBox, gridBagConstraints);

    appendixTxt.setEditable(false);
    appendixTxt.setColumns(10);
    appendixTxt.setText("XYZ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    eventPanel.add(appendixTxt, gridBagConstraints);

    dispatchEventButton.setText(bundle.getString("Dispatch_event")); // NOI18N
    dispatchEventButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dispatchEventButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    eventPanel.add(dispatchEventButton, gridBagConstraints);

    dispatchCommandFailedButton.setText(bundle.getString("dispatchCommandFailed.button.text")); // NOI18N
    dispatchCommandFailedButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dispatchCommandFailedButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    eventPanel.add(dispatchCommandFailedButton, gridBagConstraints);

    stateContainerPanel.add(eventPanel);

    controlTabPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Command_processing"))); // NOI18N
    controlTabPanel.setLayout(new java.awt.GridBagLayout());

    modeButtonGroup.add(singleModeRadioButton);
    singleModeRadioButton.setText(bundle.getString("SingleStepMode")); // NOI18N
    singleModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    singleModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    singleModeRadioButton.setName("singleModeRadioButton"); // NOI18N
    singleModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        singleModeRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    controlTabPanel.add(singleModeRadioButton, gridBagConstraints);

    modeButtonGroup.add(flowModeRadioButton);
    flowModeRadioButton.setSelected(true);
    flowModeRadioButton.setText(bundle.getString("FlowModus")); // NOI18N
    flowModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    flowModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    flowModeRadioButton.setName("flowModeRadioButton"); // NOI18N
    flowModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        flowModeRadioButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    controlTabPanel.add(flowModeRadioButton, gridBagConstraints);

    triggerButton.setText(bundle.getString("Next_step")); // NOI18N
    triggerButton.setEnabled(false);
    triggerButton.setName("triggerButton"); // NOI18N
    triggerButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        triggerButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    controlTabPanel.add(triggerButton, gridBagConstraints);

    stateContainerPanel.add(controlTabPanel);

    vehicleStatePanel.add(stateContainerPanel, java.awt.BorderLayout.NORTH);

    loadDevicePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LoadHandlingDevices"))); // NOI18N
    loadDevicePanel.setLayout(new java.awt.BorderLayout());

    jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 402));

    devicesTable.setModel(new LoadHandlingDeviceTableModel());
    jScrollPane3.setViewportView(devicesTable);

    loadDevicePanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    addDevicesButton.setText(bundle.getString("AddNewDevice")); // NOI18N
    addDevicesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addDevicesButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    jPanel1.add(addDevicesButton, gridBagConstraints);

    deleteDeviceButton.setText(bundle.getString("DeleteSelectedDevice")); // NOI18N
    deleteDeviceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteDeviceButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    jPanel1.add(deleteDeviceButton, gridBagConstraints);

    saveDeviceButton.setText(bundle.getString("applyDevices")); // NOI18N
    saveDeviceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveDeviceButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    jPanel1.add(saveDeviceButton, gridBagConstraints);

    loadDevicePanel.add(jPanel1, java.awt.BorderLayout.SOUTH);

    vehicleStatePanel.add(loadDevicePanel, java.awt.BorderLayout.CENTER);

    add(vehicleStatePanel, java.awt.BorderLayout.WEST);

    getAccessibleContext().setAccessibleName(bundle.getString("LoopbackOptions")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

  private void singleModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleModeRadioButtonActionPerformed
    if (singleModeRadioButton.isSelected()) {
      triggerButton.setEnabled(true);
      vehicleModel.setSingleStepModeEnabled(true);
    }
  }//GEN-LAST:event_singleModeRadioButtonActionPerformed

  private void flowModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flowModeRadioButtonActionPerformed
    if (flowModeRadioButton.isSelected()) {
      triggerButton.setEnabled(false);
      vehicleModel.setSingleStepModeEnabled(false);
      commAdapter.trigger();
    }
  }//GEN-LAST:event_flowModeRadioButtonActionPerformed

  private void triggerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_triggerButtonActionPerformed
    commAdapter.trigger();
  }//GEN-LAST:event_triggerButtonActionPerformed

private void chkBoxEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBoxEnableActionPerformed
  if (chkBoxEnable.isSelected()) {
    commAdapter.enable();
    setStatePanelEnabled(true);
  }
  else {
    commAdapter.disable();
    setStatePanelEnabled(false);
  }
}//GEN-LAST:event_chkBoxEnableActionPerformed

private void addOpSpecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOpSpecButtonActionPerformed
  // Get input from dialog
  EditOperationSpecDialog dialog = new EditOperationSpecDialog(commAdapter);
  dialog.setLocationRelativeTo(this);
  dialog.setVisible(true);
  OperationSpec newOpSpec = dialog.getOperationSpec();
  if (newOpSpec != null) {
    // Add new OperationSec instance to the list model
    DefaultListModel<OperationSpec> model
        = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
    model.addElement(newOpSpec);
    // Apply new operation specs to commAdapter
    applyOperationSpecs();
  }
}//GEN-LAST:event_addOpSpecButtonActionPerformed

private void rmOpSpecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmOpSpecButtonActionPerformed
  int selectedIndex = operationSpecList.getSelectedIndex();
  if (selectedIndex != -1) {
    operationSpecList.clearSelection();
    // Delete selected OperationSpec from model
    DefaultListModel<OperationSpec> model
        = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
    model.remove(selectedIndex);
    applyOperationSpecs();
  }
}//GEN-LAST:event_rmOpSpecButtonActionPerformed

  private void editOpSpecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editOpSpecButtonActionPerformed
    DefaultListModel<OperationSpec> model
        = (DefaultListModel<OperationSpec>) operationSpecList.getModel();
    int index = operationSpecList.getSelectedIndex();
    if (index != -1) {
      // Open dialog to edit the data 
      OperationSpec opSpec = model.get(index);
      EditOperationSpecDialog dialog = new EditOperationSpecDialog(commAdapter, opSpec);
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
      // Update table data
      OperationSpec editedOpSpec = dialog.getOperationSpec();
      if (editedOpSpec != null) {
        model.set(index, editedOpSpec);
      }
      applyOperationSpecs();
    }
  }//GEN-LAST:event_editOpSpecButtonActionPerformed

  private void saveProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProfileButtonActionPerformed
    // Create dialog
    List<String> profiles = VehicleProfiles.getProfileNames();
    String currentProfileName = VehicleProfiles.getSelectedProfile();
    InputPanel panel = new TextListInputPanel.Builder(
        bundle.getString("saveVehicleProfileTitle"),
        profiles)
        .setMessage(bundle.getString("saveVehicleProfileMessage"))
        .setInitialSelection(currentProfileName)
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_NOT_EMPTY)
        .build();
    InputDialog dialog = new InputDialog(panel);
    dialog.setVisible(true);
    // Get input from dialog
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      String newProfileName = (String) dialog.getInput();
      // Trim and remove redundant white space
      newProfileName = newProfileName.trim().replaceAll("\\s+", " ");

      VehicleProfile profile = storePropertiesInProfile(newProfileName);
      VehicleProfiles.saveProfile(profile);
      VehicleProfiles.setSelectedProfile(newProfileName);
    }
  }//GEN-LAST:event_saveProfileButtonActionPerformed

  private void loadProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadProfileButtonActionPerformed
    // Create dialog
    List<String> profiles = VehicleProfiles.getProfileNames();
    String currentProfileName = VehicleProfiles.getSelectedProfile();
    InputPanel panel = new DropdownListInputPanel.Builder<>(
        bundle.getString("loadVehicleProfileTitle"),
        profiles)
        .setMessage(bundle.getString("loadVehicleProfileMessage"))
        .setInitialSelection(currentProfileName)
        .build();
    InputDialog dialog = new InputDialog(panel);
    dialog.setVisible(true);
    // Get input from dialog
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED
        && dialog.getInput() != null) {
      VehicleProfile profile
          = VehicleProfiles.getProfile((String) dialog.getInput());
      loadPropertiesFromProfile(profile);
    }
  }//GEN-LAST:event_loadProfileButtonActionPerformed

  /**
   * Load the properties from the specified profile.
   *
   * @param profile the profile
   */
  private void loadPropertiesFromProfile(VehicleProfile profile) {
    EnergyStorage currentStorage = commAdapter.getEnergyStorage();
    if (currentStorage.getCapacity() != profile.getCapacity()) {
      EnergyStorage newStorage
          = EnergyStorage.createInstance(profile.getCapacity());
      newStorage.setEnergyLevel(currentStorage.getEnergyLevel());
      commAdapter.setEnergyStorage(newStorage);
    }
    vehicleModel.setIdlePower(profile.getIdlePower());
    vehicleModel.setMovementPower(profile.getMovementPower());
    vehicleModel.setOperationPower(profile.getOperationPower());
    vehicleModel.setMaxFwdVelocity(profile.getFwdVelocity());
    vehicleModel.setMaxRevVelocity(profile.getRevVelocity());
    vehicleModel.setMaxAcceleration(profile.getAcceleration());
    vehicleModel.setMaxDeceleration(profile.getDeceleration());
    vehicleModel.setDefaultOperatingTime(profile.getDefaultOpTime());
    // Create a copy of the list of OperationSpecs
    List<OperationSpec> opSpecList = new LinkedList<>();
    List<OperationSpec> profOpSpecList = profile.getOpSpecs();
    for (OperationSpec spec : profOpSpecList) {
      opSpecList.add(new OperationSpec(spec));
    }
    // Create HashMap of OperationSpecs required by comm adapter
    Map<String, OperationSpec> opSpecs = new HashMap<>();
    for (OperationSpec spec : opSpecList) {
      opSpecs.put(spec.getOperationName(), spec);
    }
    vehicleModel.setOperationSpecs(opSpecs);
    // Load operation specs into the list and update gui accordingly
    loadOperationSpecList();
    VehicleProfiles.setSelectedProfile(profile.getName());
    // updateGui();
  }

  /**
   * Store the current properties of the vehicle in new
   * <code>VehicleProfile</code>. The returned profile is not saved yet. So you
   * probably want to call
   * {@link org.opentcs.virtualvehicle.VehicleProfiles.saveProfile() VehicleProfiles.saveProfile(VehicleProfile)}.
   *
   * @param name Name of the new vehicle profile.
   * @return The new VehicleProfile.
   */
  private VehicleProfile storePropertiesInProfile(String name) {
    VehicleProfile profile = new VehicleProfile(name);
    profile.setCapacity(commAdapter.getEnergyStorage().getCapacity());
    profile.setIdlePower(vehicleModel.getIdlePower());
    profile.setMovementPower(vehicleModel.getMovementPower());
    profile.setOperationPower(vehicleModel.getOperationPower());
    profile.setFwdVelocity(vehicleModel.getMaxFwdVelocity());
    profile.setRevVelocity(vehicleModel.getMaxRevVelocity());
    profile.setAcceleration(vehicleModel.getMaxAcceleration());
    profile.setDeceleration(vehicleModel.getMaxDeceleration());
    profile.setDefaultOpTime(vehicleModel.getDefaultOperatingTime());
    // Store a copy of the OperationSpec-list
    List<OperationSpec> opSpecList
        = new LinkedList<>(vehicleModel.getOperationSpecs().values());
    if (!opSpecList.isEmpty()) {
      List<OperationSpec> newOpSpecList = new LinkedList<>();
      for (OperationSpec spec : opSpecList) {
        newOpSpecList.add(new OperationSpec(spec));
      }
      profile.setOpSpecs(newOpSpecList);
    }
    return profile;
  }

  private void maxFwdVeloTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxFwdVeloTxtMouseClicked
    InputDialog dialog = new InputDialog(
        new SingleTextInputPanel.Builder(
            bundle.getString("maxFwdVelocityTitle"))
        .setInitialValue(maxFwdVeloTxt.getText())
        .setLabel(bundle.getString("maxFwdVelocityLabel"))
        .setUnitLabel(bundle.getString("maxFwdVelocityUnit"))
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_POS)
        .build());
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      int velocity = Integer.parseInt((String) dialog.getInput());
      vehicleModel.setMaxFwdVelocity(velocity);
    }
  }//GEN-LAST:event_maxFwdVeloTxtMouseClicked

  private void maxRevVeloTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxRevVeloTxtMouseClicked
    InputDialog dialog = new InputDialog(
        new SingleTextInputPanel.Builder(
            bundle.getString("maxRevVelocityTitle"))
        .setInitialValue(maxRevVeloTxt.getText())
        .setLabel(bundle.getString("maxRevVelocityLabel"))
        .setUnitLabel(bundle.getString("maxRevVelocityUnit"))
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_NEG)
        .build());
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      int velocity = Integer.parseInt((String) dialog.getInput());
      vehicleModel.setMaxRevVelocity(velocity);
    }
  }//GEN-LAST:event_maxRevVeloTxtMouseClicked

  private void maxAccelTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxAccelTxtMouseClicked
    InputDialog dialog = new InputDialog(
        new SingleTextInputPanel.Builder(
            bundle.getString("maxAccelerationTitle"))
        .setInitialValue(maxAccelTxt.getText())
        .setLabel(bundle.getString("maxAccelerationLabel"))
        .setUnitLabel(bundle.getString("maxAccelerationUnit"))
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_POS)
        .build());
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      int acceleration = Integer.parseInt((String) dialog.getInput());
      vehicleModel.setMaxAcceleration(acceleration);
    }
  }//GEN-LAST:event_maxAccelTxtMouseClicked

  private void maxDecelTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxDecelTxtMouseClicked
    InputDialog dialog = new InputDialog(
        new SingleTextInputPanel.Builder(
            bundle.getString("maxDecelerationTitle"))
        .setInitialValue(maxDecelTxt.getText())
        .setLabel(bundle.getString("maxDecelerationLabel"))
        .setUnitLabel(bundle.getString("maxDecelerationUnit"))
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_NEG)
        .build());
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      int deceleration = Integer.parseInt((String) dialog.getInput());
      vehicleModel.setMaxDeceleration(deceleration);
    }
  }//GEN-LAST:event_maxDecelTxtMouseClicked

  private void defaultOpTimeTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultOpTimeTxtMouseClicked
    InputDialog dialog = new InputDialog(
        new SingleTextInputPanel.Builder(
            bundle.getString("operationTimeTitle"))
        .setInitialValue(defaultOpTimeTxt.getText())
        .setUnitLabel(bundle.getString("operationTimeUnit"))
        .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_POS)
        .setMessage(bundle.getString("operationTimeMessage"))
        .build());
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
      int opTime = Integer.parseInt((String) dialog.getInput());
      vehicleModel.setDefaultOperatingTime(opTime);
    }
  }//GEN-LAST:event_defaultOpTimeTxtMouseClicked

  private void movementPowerTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_movementPowerTxtMouseClicked
    if (movementPowerTxt.isEnabled()) {
      InputDialog dialog = new InputDialog(
          new SingleTextInputPanel.Builder(
              bundle.getString("movementPowerTitle"))
          .setInitialValue(movementPowerTxt.getText())
          .setUnitLabel(bundle.getString("movementPowerUnit"))
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT_POS)
          .build());
      dialog.setVisible(true);
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        double power = Double.parseDouble((String) dialog.getInput());
        vehicleModel.setMovementPower(power);
      }
    }
  }//GEN-LAST:event_movementPowerTxtMouseClicked

  private void operationPowerTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_operationPowerTxtMouseClicked
    if (operationPowerTxt.isEnabled()) {
      InputDialog dialog = new InputDialog(
          new SingleTextInputPanel.Builder(
              bundle.getString("operationPowerTitle"))
          .setInitialValue(operationPowerTxt.getText())
          .setUnitLabel(bundle.getString("operationPowerUnit"))
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT_POS)
          .build());
      dialog.setVisible(true);
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        double power = Double.parseDouble((String) dialog.getInput());
        vehicleModel.setOperationPower(power);
      }
    }
  }//GEN-LAST:event_operationPowerTxtMouseClicked

  private void idlePowerTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_idlePowerTxtMouseClicked
    if (idlePowerTxt.isEnabled()) {
      InputDialog dialog = new InputDialog(
          new SingleTextInputPanel.Builder(
              bundle.getString("idlePowerTitle"))
          .setInitialValue(idlePowerTxt.getText())
          .setUnitLabel(bundle.getString("idlePowerUnit"))
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT_POS)
          .build());
      dialog.setVisible(true);
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        double power = Double.parseDouble((String) dialog.getInput());
        vehicleModel.setIdlePower(power);
      }
    }
  }//GEN-LAST:event_idlePowerTxtMouseClicked

  private void energyCapacityTextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_energyCapacityTextMouseClicked
    if (energyCapacityText.isEnabled()) {
      InputDialog dialog = new InputDialog(
          new SingleTextInputPanel.Builder(
              bundle.getString("capacityTitle"))
          .setInitialValue(energyCapacityText.getText())
          .setUnitLabel(bundle.getString("capacityUnit"))
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT_POS)
          .build());
      dialog.setVisible(true);
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        double capacity = Double.parseDouble((String) dialog.getInput());
        EnergyStorage currentStorage = commAdapter.getEnergyStorage();
        if (capacity != currentStorage.getCapacity()) {
          EnergyStorage newStorage = EnergyStorage.createInstance(capacity);
          // Capacity changed, but energy level should remain the same.
          // => Absolute energy value changes!
          newStorage.setEnergyLevel(currentStorage.getEnergyLevel());
          commAdapter.setEnergyStorage(newStorage);
        }
        updateCapacity(capacity);
      }
    }
  }//GEN-LAST:event_energyCapacityTextMouseClicked

  private void deleteProfilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteProfilesButtonActionPerformed
    // Create dialog
    List<String> profiles = VehicleProfiles.getProfileNames();
    String currentProfileName = VehicleProfiles.getSelectedProfile();
    InputPanel panel = new DropdownListInputPanel.Builder<>(
        bundle.getString("deleteVehicleProfileTitle"),
        profiles)
        .setMessage(bundle.getString("deleteVehicleProfileMessage"))
        .setInitialSelection(currentProfileName)
        .build();
    InputDialog dialog = new InputDialog(panel);
    dialog.setVisible(true);
    // Get input from dialog
    if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED
        && dialog.getInput() != null) {
      VehicleProfiles.remove((String) dialog.getInput());
    }
  }//GEN-LAST:event_deleteProfilesButtonActionPerformed

  private void precisePosTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_precisePosTextAreaMouseClicked
    if (precisePosTextArea.isEnabled()) {
      Triple pos = vehicleModel.getVehiclePrecisePosition();
      // Create panel and dialog
      TripleTextInputPanel.Builder builder
          = new TripleTextInputPanel.Builder(bundle.getString("precisePosTitle"));
      builder.setUnitLabels(bundle.getString("precisePosUnit"));
      builder.setLabels(bundle.getString("precisePosXLabel"),
                        bundle.getString("precisePosYLabel"),
                        bundle.getString("precisePosZLabel"));
      builder.enableResetButton(null);
      builder.enableValidation(TextInputPanel.TextInputValidator.REGEX_INT);
      if (pos != null) {
        builder.setInitialValues(Long.toString(pos.getX()),
                                 Long.toString(pos.getY()),
                                 Long.toString(pos.getZ()));
      }
      InputPanel panel = builder.build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get dialog result and set vehicle precise position
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        if (dialog.getInput() == null) {
          // Clear precise position
          vehicleModel.setVehiclePrecisePosition(null);
        }
        else {
          // Set new precise position
          long x, y, z;
          String[] newPos = (String[]) dialog.getInput();
          try {
            x = Long.parseLong(newPos[0]);
            y = Long.parseLong(newPos[1]);
            z = Long.parseLong(newPos[2]);
          }
          catch (NumberFormatException | NullPointerException e) {
            return;
          }
          vehicleModel.setVehiclePrecisePosition(new Triple(x, y, z));
        }
      }
    }
  }//GEN-LAST:event_precisePosTextAreaMouseClicked

  private void stateTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stateTxtMouseClicked
    if (stateTxt.isEnabled()) {
      List<Vehicle.State> states
          = new ArrayList<>(Arrays.asList(Vehicle.State.values()));
      Vehicle.State currentState = vehicleModel.getVehicleState();
      // Create panel and dialog
      InputPanel panel = new DropdownListInputPanel.Builder<>(
          bundle.getString("stateTitle"),
          states)
          .setLabel(bundle.getString("stateLabel"))
          .setInitialSelection(currentState)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get dialog results and set vahicle stare
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        Vehicle.State newState = (Vehicle.State) dialog.getInput();
        if (newState != currentState) {
          vehicleModel.setVehicleState(newState);
        }
      }
    }
  }//GEN-LAST:event_stateTxtMouseClicked

  private void positionTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_positionTxtMouseClicked
    if (positionTxt.isEnabled()) {
      // Prepare list of model points
      Set<Point> pointSet = kernel.getTCSObjects(Point.class);
      List<Point> pointList = new ArrayList<>(pointSet);
      Collections.sort(pointList, Comparators.objectsByName());
      pointList.add(0, null);
      // Get currently selected point
      // TODO is there a better way to do this?
      Point currentPoint = null;
      String currentPointName = vehicleModel.getVehiclePosition();
      for (Point p : pointList) {
        if (p != null && p.getName().equals(currentPointName)) {
          currentPoint = p;
          break;
        }
      }
      // Create panel and dialog
      InputPanel panel = new DropdownListInputPanel.Builder<>(
          bundle.getString("positionTitle"),
          pointList)
          .setLabel(bundle.getString("positionLabel"))
          .setInitialSelection(currentPoint)
          .setRenderer(new TCSObjectNameListCellRenderer())
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get result from dialog and set vehicle position
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        Object item = dialog.getInput();
        if (item == null) {
          vehicleModel.setVehiclePosition(null);
        }
        else {
          vehicleModel.setVehiclePosition(((Point) item).getName());
        }
      }
    }
  }//GEN-LAST:event_positionTxtMouseClicked

  private void orientationAngleTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_orientationAngleTxtMouseClicked
    if (orientationAngleTxt.isEnabled()) {
      double currentAngle = vehicleModel.getVehicleOrientationAngle();
      String initialValue
          = (Double.isNaN(currentAngle) ? "" : Double.toString(currentAngle));
      // Create dialog and panel
      InputPanel panel = new SingleTextInputPanel.Builder(
          bundle.getString("orientationTitle"))
          .setLabel(bundle.getString("orientationLabel"))
          .setUnitLabel(bundle.getString("AngleUnit"))
          .setInitialValue(initialValue)
          .enableResetButton(null)
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get input from dialog
      InputDialog.ReturnStatus returnStatus = dialog.getReturnStatus();
      if (returnStatus == InputDialog.ReturnStatus.ACCEPTED) {
        String input = (String) dialog.getInput();
        if (input == null) { // The reset button was pressed
          if (!Double.isNaN(vehicleModel.getVehicleOrientationAngle())) {
            vehicleModel.setVehicleOrientationAngle(Double.NaN);
          }
        }
        else {
          // Set orientation provided by the user
          double angle;
          try {
            angle = Double.parseDouble(input);
          }
          catch (NumberFormatException e) {
            //TODO log message?
            return;
          }
          vehicleModel.setVehicleOrientationAngle(angle);
        }
      }
    }
  }//GEN-LAST:event_orientationAngleTxtMouseClicked

  private void pauseVehicleCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_pauseVehicleCheckBoxItemStateChanged
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
      vehicleModel.setVehiclePaused(true);
    }
    else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
      vehicleModel.setVehiclePaused(false);
    }
  }//GEN-LAST:event_pauseVehicleCheckBoxItemStateChanged

  private void energyLevelTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_energyLevelTxtMouseClicked
    if (energyLevelTxt.isEnabled()) {
      // Create panel and dialog
      InputPanel panel = new SingleTextInputPanel.Builder(
          bundle.getString("energyLevelTitle"))
          .setLabel(bundle.getString("energyLevelLabel"))
          .setUnitLabel("%")
          .setInitialValue(energyLevelTxt.getText())
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_RANGE_0_100)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get result from dialog and set energy level
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        String input = (String) dialog.getInput();
        int energy;
        try {
          energy = Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
          return;
        }
        commAdapter.setEnergyLevel(energy);
      }
    }
  }//GEN-LAST:event_energyLevelTxtMouseClicked

  private void saveDeviceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDeviceButtonActionPerformed
    LoadHandlingDeviceTableModel model = (LoadHandlingDeviceTableModel) devicesTable.getModel();
    vehicleModel.setVehicleLoadHandlingDevices(model.getLoadHandlingDevices());
  }//GEN-LAST:event_saveDeviceButtonActionPerformed

  private void deleteDeviceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDeviceButtonActionPerformed
    List<LoadHandlingDevice> newList = new LinkedList<>();
    LoadHandlingDeviceTableModel model = (LoadHandlingDeviceTableModel) devicesTable.getModel();
    List<LoadHandlingDevice> oldList = model.getLoadHandlingDevices();
    int[] selectedRows = devicesTable.getSelectedRows();
    int j = 0;
    for (int i : selectedRows) {
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

  private void addDevicesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDevicesButtonActionPerformed
    LoadHandlingDeviceTableModel model = (LoadHandlingDeviceTableModel) devicesTable.getModel();
    model.getLoadHandlingDevices().add(new LoadHandlingDevice("", false));
    int newIndex = model.getLoadHandlingDevices().size() - 1;
    model.fireTableRowsInserted(newIndex, newIndex);
  }//GEN-LAST:event_addDevicesButtonActionPerformed

  private void includeAppendixCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_includeAppendixCheckBoxItemStateChanged
    appendixTxt.setEditable(includeAppendixCheckBox.isSelected());
  }//GEN-LAST:event_includeAppendixCheckBoxItemStateChanged

  private void dispatchEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dispatchEventButtonActionPerformed
    String appendix
        = includeAppendixCheckBox.isSelected() ? appendixTxt.getText() : null;
    vehicleModel.publishEvent(new VehicleCommAdapterEvent(commAdapter.getName(), appendix));
  }//GEN-LAST:event_dispatchEventButtonActionPerformed

  private void dispatchCommandFailedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dispatchCommandFailedButtonActionPerformed
    MovementCommand failedCommand = commAdapter.getSentQueue().peek();
    if (failedCommand != null) {
      vehicleModel.commandFailed(commAdapter.getSentQueue().peek());
    }
  }//GEN-LAST:event_dispatchCommandFailedButtonActionPerformed

  /**
   * Set the specified precise position to the text area. The method takes care
   * of the formatting. If any of the parameters is null all values will be set
   * to the "clear"-value.
   *
   * @param x x-position
   * @param y y-position
   * @param z z-poition
   */
  private void setPrecisePosText(Long x, Long y, Long z) {
    // Convert values to srings
    String xS, yS, zS;
    try {
      xS = x.toString();
      yS = y.toString();
      zS = z.toString();
    }
    catch (NullPointerException e) {
      xS = yS = zS = bundle.getString("PrecisePosNotSet");
    }
    // Clip extremely long string values
    xS = (xS.length() > 20) ? (xS.substring(0, 20) + "...") : xS;
    yS = (yS.length() > 20) ? (yS.substring(0, 20) + "...") : yS;
    zS = (zS.length() > 20) ? (zS.substring(0, 20) + "...") : zS;
    // Build formatted text
    StringBuilder text = new StringBuilder("");
    text.append("X: ").append(xS).append("\n")
        .append("Y: ").append(yS).append("\n")
        .append("Z: ").append(zS);
    precisePosTextArea.setText(text.toString());
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel PropsPowerInnerContainerPanel;
  private javax.swing.JPanel PropsPowerOuterContainerPanel;
  private javax.swing.JButton addDevicesButton;
  private javax.swing.JButton addOpSpecButton;
  private javax.swing.JTextField appendixTxt;
  private javax.swing.JLabel capacityDimensionLabel;
  private javax.swing.JLabel capacityLabel;
  private javax.swing.JLabel changesDevicesLabel;
  private javax.swing.JLabel changesDevicesValue;
  private javax.swing.JCheckBox chkBoxEnable;
  private javax.swing.JPanel connectionPanel;
  private javax.swing.JPanel controlTabPanel;
  private javax.swing.JPanel curPosPanel;
  private javax.swing.JLabel defaultOpTimeLbl;
  private javax.swing.JTextField defaultOpTimeTxt;
  private javax.swing.JLabel defaultOpTimeUntiLbl;
  private javax.swing.JButton deleteDeviceButton;
  private javax.swing.JButton deleteProfilesButton;
  private javax.swing.JTable devicesTable;
  private javax.swing.JButton dispatchCommandFailedButton;
  private javax.swing.JButton dispatchEventButton;
  private javax.swing.JButton editOpSpecButton;
  private javax.swing.JTextField energyCapacityText;
  private javax.swing.JLabel energyLevelLabel;
  private javax.swing.JLabel energyLevelLbl;
  private javax.swing.JTextField energyLevelTxt;
  private javax.swing.JPanel eventPanel;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JPanel fillingPanel;
  private javax.swing.JRadioButton flowModeRadioButton;
  private javax.swing.JLabel idlePowerLbl;
  private javax.swing.JTextField idlePowerTxt;
  private javax.swing.JLabel idlePowerUnitLbl;
  private javax.swing.JCheckBox includeAppendixCheckBox;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JPanel loadDevicePanel;
  private javax.swing.JLabel loadHandlingDevicesLabel;
  private javax.swing.JButton loadProfilesButton;
  private javax.swing.JLabel maxAccelLbl;
  private javax.swing.JTextField maxAccelTxt;
  private javax.swing.JLabel maxAccelUnitLbl;
  private javax.swing.JLabel maxDecelLbl;
  private javax.swing.JTextField maxDecelTxt;
  private javax.swing.JLabel maxDecelUnitLbl;
  private javax.swing.JLabel maxFwdVeloLbl;
  private javax.swing.JTextField maxFwdVeloTxt;
  private javax.swing.JLabel maxFwdVeloUnitLbl;
  private javax.swing.JLabel maxRevVeloLbl;
  private javax.swing.JTextField maxRevVeloTxt;
  private javax.swing.JLabel maxRevVeloUnitLbl;
  private javax.swing.ButtonGroup modeButtonGroup;
  private javax.swing.JLabel movementPowerLbl;
  private javax.swing.JTextField movementPowerTxt;
  private javax.swing.JLabel movementPowerUnitLbl;
  private javax.swing.JPanel opSpecActionPanel;
  private javax.swing.JPanel opSpecDetailContainerPanel;
  private javax.swing.JPanel opSpecDetailPanel;
  private javax.swing.JList<String> opSpecDeviceList;
  private javax.swing.JScrollPane opSpecDeviceScrollPane;
  private javax.swing.JScrollPane opSpecListScrollPane;
  private javax.swing.JPanel opSpecPanel;
  private javax.swing.JLabel operatingTimeLabel;
  private javax.swing.JLabel operatingTimeValue;
  private javax.swing.JLabel operationNameLabel;
  private javax.swing.JLabel operationNameValue;
  private javax.swing.JLabel operationPowerLbl;
  private javax.swing.JTextField operationPowerTxt;
  private javax.swing.JLabel operationPowerUnitLbl;
  private javax.swing.JList<OperationSpec> operationSpecList;
  private javax.swing.JLabel orientationAngleLbl;
  private javax.swing.JTextField orientationAngleTxt;
  private javax.swing.JLabel orientationLabel;
  private javax.swing.JCheckBox pauseVehicleCheckBox;
  private javax.swing.JLabel pauseVehicleLabel;
  private javax.swing.JLabel positionLabel;
  private javax.swing.JTextField positionTxt;
  private javax.swing.JTextArea precisePosTextArea;
  private javax.swing.JLabel precisePosUnitLabel;
  private javax.swing.JPanel profilesContainerPanel;
  private javax.swing.JPanel profilesPanel;
  private javax.swing.JButton rmOpSpecButton;
  private javax.swing.JButton saveDeviceButton;
  private javax.swing.JButton saveProfileButton;
  private javax.swing.JRadioButton singleModeRadioButton;
  private javax.swing.JPanel stateContainerPanel;
  private javax.swing.JTextField stateTxt;
  private javax.swing.JButton triggerButton;
  private javax.swing.JPanel vehicleBahaviourPanel;
  private javax.swing.JPanel vehiclePowerPanel;
  private javax.swing.JPanel vehiclePropsPanel;
  private javax.swing.JPanel vehicleStatePanel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * An XML file filter.
   */
  private static class XmlFileFilter
      extends FileFilter {

    /**
     * Creates a new XmlFileFilter.
     */
    public XmlFileFilter() {
      // Do nada.
    }

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public String getDescription() {
      return "*.xml";
    }
  }

  /**
   * Listener that changes the OpSpecDetailPanel according to changes in the
   * OpSpecList.
   */
  private class OpSpecListListener
      implements ListSelectionListener,
                 ListDataListener {

    /**
     * Create a new instance.
     */
    public OpSpecListListener() {
      // do nothing
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      update();
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
      update();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
      /* The JList is also just a ListDataListener of it's ListModel.There 
       * is no guarantee that the JList's intervalRemoved() method is called
       * before this one. The result is, that the elements are already removed
       * from the list model, but the JList's selectedIndex value is not yet
       * updated. So before updating the DetailPanel to show the current 
       * selection, we need to make sure the selectedIndex still exists in
       * the ListModel. If not, the DetailPanel is cleared.
       */
      int selectedIndex = operationSpecList.getSelectedIndex();
      int numberOfListModelItems
          = ((DefaultListModel) operationSpecList.getModel()).size();
      if (selectedIndex >= numberOfListModelItems) {
        clearOpSpecDetailPanel();
        rmOpSpecButton.setEnabled(false);
        editOpSpecButton.setEnabled(false);
      }
      else {
        update();
      }
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      update();
    }

    /**
     * Update the OpSpecDetailPanel.
     */
    private void update() {
      OperationSpec selection = operationSpecList.getSelectedValue();
      setOpSpecDetailPanel(selection);
      if (selection == null) {
        rmOpSpecButton.setEnabled(false);
        editOpSpecButton.setEnabled(false);
      }
      else {
        rmOpSpecButton.setEnabled(true);
        editOpSpecButton.setEnabled(true);
      }
    }
  }
}
