// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.I18nCommAdapter.BUNDLE_PATH;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.awt.Color;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ProcessModelImpl;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.CallWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class StatusPanel
    extends
      VehicleCommAdapterPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatusPanel.class);
  /**
   * The resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * Default color for text field background.
   */
  private static final Color TEXTFIELD_BACKGROUND_DEFAULT = new Color(255, 255, 204);
  /**
   * Warning color for text field background.
   */
  private static final Color TEXTFIELD_BACKGROUND_WARNING = new Color(255, 100, 100);
  /**
   * The vehicle service used for interaction with the comm adapter.
   */
  private final VehicleService vehicleService;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * The comm adapter's process model.
   */
  private ProcessModelImplTO processModel;

  /**
   * Creates a new instance.
   *
   * @param processModel The comm adapter's process model.
   * @param vehicleService The vehicle service.
   * @param callWrapper The call wrapper to use for service calls.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public StatusPanel(
      @Assisted
      ProcessModelImplTO processModel,
      @Assisted
      VehicleService vehicleService,
      @ServiceCallWrapper
      CallWrapper callWrapper
  ) {
    this.processModel = requireNonNull(processModel, "processModel");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");

    initComponents();
    initGuiContent();
  }

  /**
   * Sets the initial content for each attribute of the process model.
   */
  private void initGuiContent() {
    // Trigger an update for all attributes once first.
    for (VehicleProcessModel.Attribute attribute : VehicleProcessModel.Attribute.values()) {
      processModelChange(attribute.name(), processModel);
    }
    for (ProcessModelImpl.Attribute attribute : ProcessModelImpl.Attribute.values()) {
      processModelChange(attribute.name(), processModel);
    }
  }

  @Override
  public void processModelChange(String attributeChanged, VehicleProcessModelTO newProcessModel) {
    if (!(newProcessModel instanceof ProcessModelImplTO)) {
      return;
    }

    processModel = (ProcessModelImplTO) newProcessModel;

    if (Objects.equals(
        attributeChanged,
        VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name()
    )) {
      updateCommAdapterConnected(processModel.isCommAdapterConnected());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.CURRENT_STATE.name()
    )) {
      updateCurrentState(processModel.getCurrentState());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.CONNECTION_MESSAGE.name()
    )) {
      updateConnection(processModel.getCurrentConnection());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.VISUALIZATION_MESSAGE.name()
    )) {
      updateVisualization(processModel.getCurrentVisualization());
    }
  }

  /**
   * Updates the status panel when the connection to the vehicle changes.
   *
   * @param connected Whether a connection to the vehicle is established
   */
  private void updateCommAdapterConnected(boolean connected) {
    SwingUtilities.invokeLater(() -> {
      buttonGetState.setEnabled(connected);
      buttonShowLastReportedState.setEnabled(connected);
    });
  }

  /**
   * Updates the status panel with the given state response.
   *
   * @param stateMessage The state response
   */
  private void updateCurrentState(State stateMessage) {
    SwingUtilities.invokeLater(() -> updateStatusPanel(stateMessage));
  }

  /**
   * Updates the connection panel with the new connection information.
   *
   * @param connectionMessage the connection message to display.
   */
  private void updateConnection(Connection connectionMessage) {
    SwingUtilities.invokeLater(() -> updateConnectionPanel(connectionMessage));
  }

  /**
   * Updates the visualization panel with the new visualization information.
   *
   * @param visMessage the visualization message to display.
   */
  private void updateVisualization(Visualization visMessage) {
    SwingUtilities.invokeLater(() -> updateVisualizationPanel(visMessage));
  }

  /**
   * Updates the status panel with the content of a state response.
   *
   * @param stateMessage The state response
   */
  private void updateStatusPanel(final State stateMessage) {
    requireNonNull(stateMessage, "state");

    headerIdTextField.setText(String.valueOf(stateMessage.getHeaderId()));
    stateTimestampTextField.setText(stateMessage.getTimestamp().toString());
    versionTextField.setText(stateMessage.getVersion());
    manufacturerTextField.setText(stateMessage.getManufacturer());
    serialNoTextField.setText(stateMessage.getSerialNumber());

    orderIdTextField.setText(stateMessage.getOrderId());
    orderUpdateIdTextField.setText(String.valueOf(stateMessage.getOrderUpdateId()));
    lastNodeIdTextField.setText(stateMessage.getLastNodeId());
    zoneSetIdTextField.setText(stateMessage.getZoneSetId());
    lastNodeSeqIdTextField.setText(String.valueOf(stateMessage.getLastNodeSequenceId()));
    drivingTextField.setText(String.valueOf(stateMessage.isDriving()));
    pausedTextField.setText(String.valueOf(stateMessage.isPaused()));
    newBaseRequestTextField.setText(String.valueOf(stateMessage.isNewBaseRequest()));
    distSinceLastNodeTextField.setText(
        stateMessage.getDistanceSinceLastNode() == null
            ? "-"
            : String.format("%.2f", stateMessage.getDistanceSinceLastNode())
    );
    operatingModeTextField.setText(stateMessage.getOperatingMode().name());

    if (stateMessage.getAgvPosition() == null) {
      agvPosInitializedTextField.setText("-");
      agvPosMapIdTextField.setText("-");
      agvPosXTextField.setText("-");
      agvPosYTextField.setText("-");
      agvPosThetaTextField.setText("-");
    }
    else {
      agvPosInitializedTextField.setText(
          String.valueOf(stateMessage.getAgvPosition().isPositionInitialized())
      );
      agvPosMapIdTextField.setText(stateMessage.getAgvPosition().getMapId());
      agvPosXTextField.setText(String.format("%.2f", stateMessage.getAgvPosition().getX()));
      agvPosYTextField.setText(String.format("%.2f", stateMessage.getAgvPosition().getY()));
      agvPosThetaTextField.setText(String.format("%.2f", stateMessage.getAgvPosition().getTheta()));
    }

    if (stateMessage.getVelocity() == null) {
      velocityXTextField.setText("-");
      velocityYTextField.setText("-");
      velocityOmegaTextField.setText("-");
    }
    else {
      velocityXTextField.setText(String.format("%.2f", stateMessage.getVelocity().getVx()));
      velocityYTextField.setText(String.format("%.2f", stateMessage.getVelocity().getVy()));
      velocityOmegaTextField.setText(String.format("%.2f", stateMessage.getVelocity().getOmega()));
    }

    batteryChargeTextField.setText(
        String.format("%.1f", stateMessage.getBatteryState().getBatteryCharge())
    );
    chargingTextField.setText(String.valueOf(stateMessage.getBatteryState().isCharging()));

    eStopTextField.setText(stateMessage.getSafetyState().geteStop().name());
    fieldViolationTextField.setText(
        String.valueOf(stateMessage.getSafetyState().isFieldViolation())
    );

    updateErrorStatusPanel(stateMessage);
  }

  private void updateErrorStatusPanel(final State stateMessage) {
    int errorCount = 0;
    int warningCount = 0;
    for (ErrorEntry error : stateMessage.getErrors()) {
      if (error.getErrorLevel() == ErrorLevel.FATAL) {
        errorCount += 1;
      }
      else if (error.getErrorLevel() == ErrorLevel.WARNING) {
        warningCount += 1;
      }
    }

    warningCountTextField.setText(String.valueOf(warningCount));
    if (warningCount > 0) {
      warningCountTextField.setBackground(TEXTFIELD_BACKGROUND_WARNING);
    }
    else {
      warningCountTextField.setBackground(TEXTFIELD_BACKGROUND_DEFAULT);
    }
    fatalErrorCountTextField.setText(String.valueOf(errorCount));
    if (errorCount > 0) {
      fatalErrorCountTextField.setBackground(TEXTFIELD_BACKGROUND_WARNING);
    }
    else {
      fatalErrorCountTextField.setBackground(TEXTFIELD_BACKGROUND_DEFAULT);
    }
  }

  /**
   * Updates the connection panel.
   *
   * @param connectionMessage the connection message to display.
   */
  private void updateConnectionPanel(Connection connectionMessage) {
    requireNonNull(connectionMessage, "connectionMessage");

    connectionHeaderIdTextField.setText(String.valueOf(connectionMessage.getHeaderId()));
    connectionTimestampTextField.setText(connectionMessage.getTimestamp().toString());
    connectionTextField.setText(connectionMessage.getConnectionState().name());
  }

  /**
   * Updates the visualization panel.
   *
   * @param visMessage the visualization message to display.
   */
  private void updateVisualizationPanel(Visualization visMessage) {
    requireNonNull(visMessage, "visMessage");

    visualizationHeaderIdTextField.setText(String.valueOf(visMessage.getHeaderId()));
    visualizationTimestampTextField.setText(visMessage.getTimestamp().toString());

    if (visMessage.getVelocity() == null) {
      visualizationVelocityXTextField.setText("-");
      visualizationVelocityYTextField.setText("-");
      visualizationVelocityOmegaTextField.setText("-");
    }
    else {
      visualizationVelocityXTextField.setText(
          String.format("%.2f", visMessage.getVelocity().getVx())
      );
      visualizationVelocityYTextField.setText(
          String.format("%.2f", visMessage.getVelocity().getVy())
      );
      visualizationVelocityOmegaTextField.setText(
          String.format("%.2f", visMessage.getVelocity().getOmega())
      );
    }

    if (visMessage.getAgvPosition() == null) {
      visualizationAgvPosInitializedTextField.setText("-");
      visualizationAgvPosMapIdTextField.setText("-");
      visualizationAgvPosXTextField.setText("-");
      visualizationAgvPosYTextField.setText("-");
      visualizationAgvPosThetaTextField.setText("-");
    }
    else {
      visualizationAgvPosInitializedTextField.setText(
          String.valueOf(visMessage.getAgvPosition().isPositionInitialized())
      );
      visualizationAgvPosMapIdTextField.setText(visMessage.getAgvPosition().getMapId());
      visualizationAgvPosXTextField.setText(
          String.format("%.2f", visMessage.getAgvPosition().getX())
      );
      visualizationAgvPosYTextField.setText(
          String.format("%.2f", visMessage.getAgvPosition().getY())
      );
      visualizationAgvPosThetaTextField.setText(
          String.format("%.2f", visMessage.getAgvPosition().getTheta())
      );
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

    statusPanelScrollPane = new javax.swing.JScrollPane();
    scrollPaneContainerPanel = new javax.swing.JPanel();
    statePanel = new javax.swing.JPanel();
    headerPanel = new javax.swing.JPanel();
    stateTimestampLabel = new javax.swing.JLabel();
    stateTimestampTextField = new javax.swing.JTextField();
    headerIdLabel = new javax.swing.JLabel();
    headerIdTextField = new javax.swing.JTextField();
    versionLabel = new javax.swing.JLabel();
    versionTextField = new javax.swing.JTextField();
    manufacturerLabel = new javax.swing.JLabel();
    manufacturerTextField = new javax.swing.JTextField();
    serialNoLabel = new javax.swing.JLabel();
    serialNoTextField = new javax.swing.JTextField();
    headerPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    panelManualStateRequest = new javax.swing.JPanel();
    buttonGetState = new javax.swing.JButton();
    generalStatePanel = new javax.swing.JPanel();
    orderIdLabel = new javax.swing.JLabel();
    orderIdTextField = new javax.swing.JTextField();
    orderUpdateIdLabel = new javax.swing.JLabel();
    orderUpdateIdTextField = new javax.swing.JTextField();
    lastNodeIdLabel = new javax.swing.JLabel();
    lastNodeIdTextField = new javax.swing.JTextField();
    zoneSetIdLabel = new javax.swing.JLabel();
    zoneSetIdTextField = new javax.swing.JTextField();
    lastNodeSeqIdLabel = new javax.swing.JLabel();
    lastNodeSeqIdTextField = new javax.swing.JTextField();
    drivingLabel = new javax.swing.JLabel();
    drivingTextField = new javax.swing.JTextField();
    pausedLabel = new javax.swing.JLabel();
    pausedTextField = new javax.swing.JTextField();
    newBaseRequestLabel = new javax.swing.JLabel();
    newBaseRequestTextField = new javax.swing.JTextField();
    distSinceLastNodeLabel = new javax.swing.JLabel();
    distSinceLastNodeTextField = new javax.swing.JTextField();
    operatingModelLabel = new javax.swing.JLabel();
    operatingModeTextField = new javax.swing.JTextField();
    generalStatePanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    agvPositionPanel = new javax.swing.JPanel();
    agvPosInitializedLabel = new javax.swing.JLabel();
    agvPosInitializedTextField = new javax.swing.JTextField();
    agvPosMapIdLabel = new javax.swing.JLabel();
    agvPosMapIdTextField = new javax.swing.JTextField();
    agvPosXLabel = new javax.swing.JLabel();
    agvPosXTextField = new javax.swing.JTextField();
    agvPosYLabel = new javax.swing.JLabel();
    agvPosYTextField = new javax.swing.JTextField();
    agvPosThetaLabel = new javax.swing.JLabel();
    agvPosThetaTextField = new javax.swing.JTextField();
    velocityPanel = new javax.swing.JPanel();
    velocityXLabel = new javax.swing.JLabel();
    velocityXTextField = new javax.swing.JTextField();
    velocityYLabel = new javax.swing.JLabel();
    velocityYTextField = new javax.swing.JTextField();
    velocityOmegaLabel = new javax.swing.JLabel();
    velocityOmegaTextField = new javax.swing.JTextField();
    velocityPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    batteryStatePanel = new javax.swing.JPanel();
    batteryChargeLabel = new javax.swing.JLabel();
    batteryChargeTextField = new javax.swing.JTextField();
    chargingLabel = new javax.swing.JLabel();
    chargingTextField = new javax.swing.JTextField();
    safetyStatePanel = new javax.swing.JPanel();
    eStopLabel = new javax.swing.JLabel();
    eStopTextField = new javax.swing.JTextField();
    fieldViolationLabel = new javax.swing.JLabel();
    fieldViolationTextField = new javax.swing.JTextField();
    errorStatePanel = new javax.swing.JPanel();
    warningCountTextField = new javax.swing.JTextField();
    fatalErrorCountTextField = new javax.swing.JTextField();
    fatalErrorCountLabel = new javax.swing.JLabel();
    warningCountLabel = new javax.swing.JLabel();
    errorStatePanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    buttonShowLastReportedState = new javax.swing.JButton();
    connectionPanel = new javax.swing.JPanel();
    connectionHeaderIdLabel = new javax.swing.JLabel();
    connectionHeaderIdTextField = new javax.swing.JTextField();
    connectionTimestampLabel = new javax.swing.JLabel();
    connectionTimestampTextField = new javax.swing.JTextField();
    connectionLabel = new javax.swing.JLabel();
    connectionTextField = new javax.swing.JTextField();
    connectionPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    visualizationPanel = new javax.swing.JPanel();
    visualizationHeaderIdLabel = new javax.swing.JLabel();
    visualizationHeaderIdTextField = new javax.swing.JTextField();
    visualizationTimestampLabel = new javax.swing.JLabel();
    visualizationTimestampTextField = new javax.swing.JTextField();
    visualizationRowPanel = new javax.swing.JPanel();
    visualizationAgvPositionPanel = new javax.swing.JPanel();
    visualizationAgvPosInitializedLabel = new javax.swing.JLabel();
    visualizationAgvPosInitializedTextField = new javax.swing.JTextField();
    visualizationAgvPosMapIdLabel = new javax.swing.JLabel();
    visualizationAgvPosMapIdTextField = new javax.swing.JTextField();
    visualizationAgvPosXLabel = new javax.swing.JLabel();
    visualizationAgvPosXTextField = new javax.swing.JTextField();
    visualizationAgvPosYLabel = new javax.swing.JLabel();
    visualizationAgvPosYTextField = new javax.swing.JTextField();
    visualizationAgvPosThetaLabel = new javax.swing.JLabel();
    visualizationAgvPosThetaTextField = new javax.swing.JTextField();
    visualizationVelocityPanel = new javax.swing.JPanel();
    visualizationVelocityXLabel = new javax.swing.JLabel();
    visualizationVelocityXTextField = new javax.swing.JTextField();
    visualizationVelocityYLabel = new javax.swing.JLabel();
    visualizationVelocityYTextField = new javax.swing.JTextField();
    visualizationVelocityOmegaLabel = new javax.swing.JLabel();
    visualizationVelocityOmegaTextField = new javax.swing.JTextField();
    visualizationRowPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    visualizationPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

    setLayout(new java.awt.GridBagLayout());

    statusPanelScrollPane.setBorder(null);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/vehicle/vda5050/v2_0/Bundle"); // NOI18N
    statePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("statusPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    statePanel.setLayout(new java.awt.GridBagLayout());

    headerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_header.border.title"))); // NOI18N
    headerPanel.setLayout(new java.awt.GridBagLayout());

    stateTimestampLabel.setText("Timestamp:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    headerPanel.add(stateTimestampLabel, gridBagConstraints);

    stateTimestampTextField.setEditable(false);
    stateTimestampTextField.setBackground(new java.awt.Color(255, 255, 204));
    stateTimestampTextField.setColumns(35);
    stateTimestampTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    stateTimestampTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    stateTimestampTextField.setText("YYYY-MM-DDTHH:mm:ss.ssZ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
    headerPanel.add(stateTimestampTextField, gridBagConstraints);

    headerIdLabel.setText("Header ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    headerPanel.add(headerIdLabel, gridBagConstraints);

    headerIdTextField.setEditable(false);
    headerIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    headerIdTextField.setColumns(8);
    headerIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    headerIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    headerIdTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    headerPanel.add(headerIdTextField, gridBagConstraints);

    versionLabel.setText("Version:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    headerPanel.add(versionLabel, gridBagConstraints);

    versionTextField.setEditable(false);
    versionTextField.setBackground(new java.awt.Color(255, 255, 204));
    versionTextField.setColumns(8);
    versionTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    versionTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    versionTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    headerPanel.add(versionTextField, gridBagConstraints);

    manufacturerLabel.setText("Manufacturer:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    headerPanel.add(manufacturerLabel, gridBagConstraints);

    manufacturerTextField.setEditable(false);
    manufacturerTextField.setBackground(new java.awt.Color(255, 255, 204));
    manufacturerTextField.setColumns(30);
    manufacturerTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    manufacturerTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    manufacturerTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    headerPanel.add(manufacturerTextField, gridBagConstraints);

    serialNoLabel.setText("Serial number:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    headerPanel.add(serialNoLabel, gridBagConstraints);

    serialNoTextField.setEditable(false);
    serialNoTextField.setBackground(new java.awt.Color(255, 255, 204));
    serialNoTextField.setColumns(30);
    serialNoTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    serialNoTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    serialNoTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    headerPanel.add(serialNoTextField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    headerPanel.add(headerPanelFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
    statePanel.add(headerPanel, gridBagConstraints);

    panelManualStateRequest.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("statusPanel.panel_manualStateRequest.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11))); // NOI18N
    panelManualStateRequest.setLayout(new java.awt.CardLayout());

    buttonGetState.setText(bundle.getString("statusPanel.panel_manualStateRequest.button_getState.text")); // NOI18N
    buttonGetState.setEnabled(false);
    buttonGetState.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonGetStateActionPerformed(evt);
      }
    });
    panelManualStateRequest.add(buttonGetState, "card2");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    statePanel.add(panelManualStateRequest, gridBagConstraints);

    generalStatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_generalState.border.title"))); // NOI18N
    generalStatePanel.setLayout(new java.awt.GridBagLayout());

    orderIdLabel.setText("Order ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    generalStatePanel.add(orderIdLabel, gridBagConstraints);

    orderIdTextField.setEditable(false);
    orderIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    orderIdTextField.setColumns(10);
    orderIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    orderIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    orderIdTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    generalStatePanel.add(orderIdTextField, gridBagConstraints);

    orderUpdateIdLabel.setText("Order update ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(orderUpdateIdLabel, gridBagConstraints);

    orderUpdateIdTextField.setEditable(false);
    orderUpdateIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    orderUpdateIdTextField.setColumns(10);
    orderUpdateIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    orderUpdateIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    orderUpdateIdTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    generalStatePanel.add(orderUpdateIdTextField, gridBagConstraints);

    lastNodeIdLabel.setText("Last node ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    generalStatePanel.add(lastNodeIdLabel, gridBagConstraints);

    lastNodeIdTextField.setEditable(false);
    lastNodeIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    lastNodeIdTextField.setColumns(10);
    lastNodeIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    lastNodeIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    lastNodeIdTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(lastNodeIdTextField, gridBagConstraints);

    zoneSetIdLabel.setText("Zone set ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(zoneSetIdLabel, gridBagConstraints);

    zoneSetIdTextField.setEditable(false);
    zoneSetIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    zoneSetIdTextField.setColumns(10);
    zoneSetIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    zoneSetIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    zoneSetIdTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    generalStatePanel.add(zoneSetIdTextField, gridBagConstraints);

    lastNodeSeqIdLabel.setText("Last node sequence ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(lastNodeSeqIdLabel, gridBagConstraints);

    lastNodeSeqIdTextField.setEditable(false);
    lastNodeSeqIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    lastNodeSeqIdTextField.setColumns(10);
    lastNodeSeqIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    lastNodeSeqIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    lastNodeSeqIdTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(lastNodeSeqIdTextField, gridBagConstraints);

    drivingLabel.setText("Driving:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    generalStatePanel.add(drivingLabel, gridBagConstraints);

    drivingTextField.setEditable(false);
    drivingTextField.setBackground(new java.awt.Color(255, 255, 204));
    drivingTextField.setColumns(10);
    drivingTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    drivingTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    drivingTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(drivingTextField, gridBagConstraints);

    pausedLabel.setText("Paused:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(pausedLabel, gridBagConstraints);

    pausedTextField.setEditable(false);
    pausedTextField.setBackground(new java.awt.Color(255, 255, 204));
    pausedTextField.setColumns(10);
    pausedTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    pausedTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    pausedTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(pausedTextField, gridBagConstraints);

    newBaseRequestLabel.setText("New base request:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(newBaseRequestLabel, gridBagConstraints);

    newBaseRequestTextField.setEditable(false);
    newBaseRequestTextField.setBackground(new java.awt.Color(255, 255, 204));
    newBaseRequestTextField.setColumns(10);
    newBaseRequestTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    newBaseRequestTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    newBaseRequestTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(newBaseRequestTextField, gridBagConstraints);

    distSinceLastNodeLabel.setText("Distance since last node:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    generalStatePanel.add(distSinceLastNodeLabel, gridBagConstraints);

    distSinceLastNodeTextField.setEditable(false);
    distSinceLastNodeTextField.setBackground(new java.awt.Color(255, 255, 204));
    distSinceLastNodeTextField.setColumns(10);
    distSinceLastNodeTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    distSinceLastNodeTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    distSinceLastNodeTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(distSinceLastNodeTextField, gridBagConstraints);

    operatingModelLabel.setText("Operating mode:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    generalStatePanel.add(operatingModelLabel, gridBagConstraints);

    operatingModeTextField.setEditable(false);
    operatingModeTextField.setBackground(new java.awt.Color(255, 255, 204));
    operatingModeTextField.setColumns(10);
    operatingModeTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    operatingModeTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    operatingModeTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    generalStatePanel.add(operatingModeTextField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    generalStatePanel.add(generalStatePanelFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    statePanel.add(generalStatePanel, gridBagConstraints);

    agvPositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_agvPosition.border.title"))); // NOI18N
    agvPositionPanel.setLayout(new java.awt.GridBagLayout());

    agvPosInitializedLabel.setText("Initialized:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    agvPositionPanel.add(agvPosInitializedLabel, gridBagConstraints);

    agvPosInitializedTextField.setEditable(false);
    agvPosInitializedTextField.setBackground(new java.awt.Color(255, 255, 204));
    agvPosInitializedTextField.setColumns(10);
    agvPosInitializedTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    agvPosInitializedTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    agvPosInitializedTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    agvPositionPanel.add(agvPosInitializedTextField, gridBagConstraints);

    agvPosMapIdLabel.setText("Map ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    agvPositionPanel.add(agvPosMapIdLabel, gridBagConstraints);

    agvPosMapIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    agvPosMapIdTextField.setColumns(10);
    agvPosMapIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    agvPosMapIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    agvPosMapIdTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    agvPositionPanel.add(agvPosMapIdTextField, gridBagConstraints);

    agvPosXLabel.setText("x:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    agvPositionPanel.add(agvPosXLabel, gridBagConstraints);

    agvPosXTextField.setEditable(false);
    agvPosXTextField.setBackground(new java.awt.Color(255, 255, 204));
    agvPosXTextField.setColumns(10);
    agvPosXTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    agvPosXTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    agvPosXTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    agvPositionPanel.add(agvPosXTextField, gridBagConstraints);

    agvPosYLabel.setText("y:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    agvPositionPanel.add(agvPosYLabel, gridBagConstraints);

    agvPosYTextField.setEditable(false);
    agvPosYTextField.setBackground(new java.awt.Color(255, 255, 204));
    agvPosYTextField.setColumns(10);
    agvPosYTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    agvPosYTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    agvPosYTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    agvPositionPanel.add(agvPosYTextField, gridBagConstraints);

    agvPosThetaLabel.setText("theta:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    agvPositionPanel.add(agvPosThetaLabel, gridBagConstraints);

    agvPosThetaTextField.setEditable(false);
    agvPosThetaTextField.setBackground(new java.awt.Color(255, 255, 204));
    agvPosThetaTextField.setColumns(10);
    agvPosThetaTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    agvPosThetaTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    agvPosThetaTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    agvPositionPanel.add(agvPosThetaTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    statePanel.add(agvPositionPanel, gridBagConstraints);

    velocityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_velocity.border.title"))); // NOI18N
    velocityPanel.setLayout(new java.awt.GridBagLayout());

    velocityXLabel.setText("x:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    velocityPanel.add(velocityXLabel, gridBagConstraints);

    velocityXTextField.setEditable(false);
    velocityXTextField.setBackground(new java.awt.Color(255, 255, 204));
    velocityXTextField.setColumns(10);
    velocityXTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    velocityXTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    velocityXTextField.setText("double");
    velocityPanel.add(velocityXTextField, new java.awt.GridBagConstraints());

    velocityYLabel.setText("y:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    velocityPanel.add(velocityYLabel, gridBagConstraints);

    velocityYTextField.setEditable(false);
    velocityYTextField.setBackground(new java.awt.Color(255, 255, 204));
    velocityYTextField.setColumns(10);
    velocityYTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    velocityYTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    velocityYTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    velocityPanel.add(velocityYTextField, gridBagConstraints);

    velocityOmegaLabel.setText("omega:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    velocityPanel.add(velocityOmegaLabel, gridBagConstraints);

    velocityOmegaTextField.setEditable(false);
    velocityOmegaTextField.setBackground(new java.awt.Color(255, 255, 204));
    velocityOmegaTextField.setColumns(10);
    velocityOmegaTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    velocityOmegaTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    velocityOmegaTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    velocityPanel.add(velocityOmegaTextField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    velocityPanel.add(velocityPanelFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    statePanel.add(velocityPanel, gridBagConstraints);

    batteryStatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_batteryState.border.title"))); // NOI18N
    batteryStatePanel.setLayout(new java.awt.GridBagLayout());

    batteryChargeLabel.setText("Battery charge:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    batteryStatePanel.add(batteryChargeLabel, gridBagConstraints);

    batteryChargeTextField.setEditable(false);
    batteryChargeTextField.setBackground(new java.awt.Color(255, 255, 204));
    batteryChargeTextField.setColumns(10);
    batteryChargeTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    batteryChargeTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    batteryChargeTextField.setText("double");
    batteryStatePanel.add(batteryChargeTextField, new java.awt.GridBagConstraints());

    chargingLabel.setText("Charging:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    batteryStatePanel.add(chargingLabel, gridBagConstraints);

    chargingTextField.setEditable(false);
    chargingTextField.setBackground(new java.awt.Color(255, 255, 204));
    chargingTextField.setColumns(10);
    chargingTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    chargingTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    chargingTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    batteryStatePanel.add(chargingTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    statePanel.add(batteryStatePanel, gridBagConstraints);

    safetyStatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Safety state"));
    safetyStatePanel.setLayout(new java.awt.GridBagLayout());

    eStopLabel.setText("E-stop:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    safetyStatePanel.add(eStopLabel, gridBagConstraints);

    eStopTextField.setEditable(false);
    eStopTextField.setBackground(new java.awt.Color(255, 255, 204));
    eStopTextField.setColumns(10);
    eStopTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    eStopTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    eStopTextField.setText("String");
    safetyStatePanel.add(eStopTextField, new java.awt.GridBagConstraints());

    fieldViolationLabel.setText("Field violation:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    safetyStatePanel.add(fieldViolationLabel, gridBagConstraints);

    fieldViolationTextField.setEditable(false);
    fieldViolationTextField.setBackground(new java.awt.Color(255, 255, 204));
    fieldViolationTextField.setColumns(10);
    fieldViolationTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    fieldViolationTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    fieldViolationTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    safetyStatePanel.add(fieldViolationTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    statePanel.add(safetyStatePanel, gridBagConstraints);

    errorStatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_errorState.border.title"))); // NOI18N
    errorStatePanel.setLayout(new java.awt.GridBagLayout());

    warningCountTextField.setBackground(new java.awt.Color(255, 255, 204));
    warningCountTextField.setColumns(10);
    warningCountTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    warningCountTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    warningCountTextField.setText("int");
    warningCountTextField.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    errorStatePanel.add(warningCountTextField, gridBagConstraints);

    fatalErrorCountTextField.setBackground(new java.awt.Color(255, 255, 204));
    fatalErrorCountTextField.setColumns(10);
    fatalErrorCountTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    fatalErrorCountTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    fatalErrorCountTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    errorStatePanel.add(fatalErrorCountTextField, gridBagConstraints);

    fatalErrorCountLabel.setText(bundle.getString("statusPanel.panel_errorState.label_fatalErrorCount.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    errorStatePanel.add(fatalErrorCountLabel, gridBagConstraints);

    warningCountLabel.setText(bundle.getString("statusPanel.panel_errorState.label_warningCount.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    errorStatePanel.add(warningCountLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    errorStatePanel.add(errorStatePanelFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    statePanel.add(errorStatePanel, gridBagConstraints);

    buttonShowLastReportedState.setText(bundle.getString("statusPanel.panel_telegramContent.button_showLastReportedState")); // NOI18N
    buttonShowLastReportedState.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonShowLastReportedStateActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    statePanel.add(buttonShowLastReportedState, gridBagConstraints);

    connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("statusPanel.connectionPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    connectionPanel.setLayout(new java.awt.GridBagLayout());

    connectionHeaderIdLabel.setText("Header ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    connectionPanel.add(connectionHeaderIdLabel, gridBagConstraints);

    connectionHeaderIdTextField.setEditable(false);
    connectionHeaderIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    connectionHeaderIdTextField.setColumns(8);
    connectionHeaderIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    connectionHeaderIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    connectionHeaderIdTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    connectionPanel.add(connectionHeaderIdTextField, gridBagConstraints);

    connectionTimestampLabel.setText("Timestamp:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    connectionPanel.add(connectionTimestampLabel, gridBagConstraints);

    connectionTimestampTextField.setEditable(false);
    connectionTimestampTextField.setBackground(new java.awt.Color(255, 255, 204));
    connectionTimestampTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    connectionTimestampTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    connectionTimestampTextField.setText("YYYY-MM-DDTHH:mm:ss.ssZ");
    connectionTimestampTextField.setPreferredSize(new java.awt.Dimension(259, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    connectionPanel.add(connectionTimestampTextField, gridBagConstraints);

    connectionLabel.setText("Connection:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    connectionPanel.add(connectionLabel, gridBagConstraints);

    connectionTextField.setEditable(false);
    connectionTextField.setBackground(new java.awt.Color(255, 255, 204));
    connectionTextField.setColumns(20);
    connectionTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    connectionTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    connectionTextField.setText("CONNECTION_BROKEN");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    connectionPanel.add(connectionTextField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    connectionPanel.add(connectionPanelFiller, gridBagConstraints);

    visualizationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("statusPanel.visualizationPanel.title.border"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    visualizationPanel.setLayout(new java.awt.GridBagLayout());

    visualizationHeaderIdLabel.setText("Header ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    visualizationPanel.add(visualizationHeaderIdLabel, gridBagConstraints);

    visualizationHeaderIdTextField.setEditable(false);
    visualizationHeaderIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationHeaderIdTextField.setColumns(8);
    visualizationHeaderIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationHeaderIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationHeaderIdTextField.setText("int");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    visualizationPanel.add(visualizationHeaderIdTextField, gridBagConstraints);

    visualizationTimestampLabel.setText("Timestamp:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    visualizationPanel.add(visualizationTimestampLabel, gridBagConstraints);

    visualizationTimestampTextField.setEditable(false);
    visualizationTimestampTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationTimestampTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationTimestampTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationTimestampTextField.setText("YYYY-MM-DDTHH:mm:ss.ssZ");
    visualizationTimestampTextField.setPreferredSize(new java.awt.Dimension(259, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    visualizationPanel.add(visualizationTimestampTextField, gridBagConstraints);

    visualizationRowPanel.setLayout(new java.awt.GridBagLayout());

    visualizationAgvPositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_agvPosition.border.title"))); // NOI18N
    visualizationAgvPositionPanel.setLayout(new java.awt.GridBagLayout());

    visualizationAgvPosInitializedLabel.setText("Initialized:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationAgvPositionPanel.add(visualizationAgvPosInitializedLabel, gridBagConstraints);

    visualizationAgvPosInitializedTextField.setEditable(false);
    visualizationAgvPosInitializedTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationAgvPosInitializedTextField.setColumns(10);
    visualizationAgvPosInitializedTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationAgvPosInitializedTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationAgvPosInitializedTextField.setText("boolean");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    visualizationAgvPositionPanel.add(visualizationAgvPosInitializedTextField, gridBagConstraints);

    visualizationAgvPosMapIdLabel.setText("Map ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationAgvPositionPanel.add(visualizationAgvPosMapIdLabel, gridBagConstraints);

    visualizationAgvPosMapIdTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationAgvPosMapIdTextField.setColumns(10);
    visualizationAgvPosMapIdTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationAgvPosMapIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationAgvPosMapIdTextField.setText("String");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationAgvPositionPanel.add(visualizationAgvPosMapIdTextField, gridBagConstraints);

    visualizationAgvPosXLabel.setText("x:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationAgvPositionPanel.add(visualizationAgvPosXLabel, gridBagConstraints);

    visualizationAgvPosXTextField.setEditable(false);
    visualizationAgvPosXTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationAgvPosXTextField.setColumns(10);
    visualizationAgvPosXTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationAgvPosXTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationAgvPosXTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationAgvPositionPanel.add(visualizationAgvPosXTextField, gridBagConstraints);

    visualizationAgvPosYLabel.setText("y:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationAgvPositionPanel.add(visualizationAgvPosYLabel, gridBagConstraints);

    visualizationAgvPosYTextField.setEditable(false);
    visualizationAgvPosYTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationAgvPosYTextField.setColumns(10);
    visualizationAgvPosYTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationAgvPosYTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationAgvPosYTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationAgvPositionPanel.add(visualizationAgvPosYTextField, gridBagConstraints);

    visualizationAgvPosThetaLabel.setText("theta:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationAgvPositionPanel.add(visualizationAgvPosThetaLabel, gridBagConstraints);

    visualizationAgvPosThetaTextField.setEditable(false);
    visualizationAgvPosThetaTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationAgvPosThetaTextField.setColumns(10);
    visualizationAgvPosThetaTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationAgvPosThetaTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationAgvPosThetaTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationAgvPositionPanel.add(visualizationAgvPosThetaTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    visualizationRowPanel.add(visualizationAgvPositionPanel, gridBagConstraints);

    visualizationVelocityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("statusPanel.panel_velocity.border.title"))); // NOI18N
    visualizationVelocityPanel.setLayout(new java.awt.GridBagLayout());

    visualizationVelocityXLabel.setText("x:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationVelocityPanel.add(visualizationVelocityXLabel, gridBagConstraints);

    visualizationVelocityXTextField.setEditable(false);
    visualizationVelocityXTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationVelocityXTextField.setColumns(10);
    visualizationVelocityXTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationVelocityXTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationVelocityXTextField.setText("double");
    visualizationVelocityPanel.add(visualizationVelocityXTextField, new java.awt.GridBagConstraints());

    visualizationVelocityYLabel.setText("y:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationVelocityPanel.add(visualizationVelocityYLabel, gridBagConstraints);

    visualizationVelocityYTextField.setEditable(false);
    visualizationVelocityYTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationVelocityYTextField.setColumns(10);
    visualizationVelocityYTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationVelocityYTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationVelocityYTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationVelocityPanel.add(visualizationVelocityYTextField, gridBagConstraints);

    visualizationVelocityOmegaLabel.setText("omega:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    visualizationVelocityPanel.add(visualizationVelocityOmegaLabel, gridBagConstraints);

    visualizationVelocityOmegaTextField.setEditable(false);
    visualizationVelocityOmegaTextField.setBackground(new java.awt.Color(255, 255, 204));
    visualizationVelocityOmegaTextField.setColumns(10);
    visualizationVelocityOmegaTextField.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
    visualizationVelocityOmegaTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    visualizationVelocityOmegaTextField.setText("double");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    visualizationVelocityPanel.add(visualizationVelocityOmegaTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
    visualizationRowPanel.add(visualizationVelocityPanel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    visualizationRowPanel.add(visualizationRowPanelFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    visualizationPanel.add(visualizationRowPanel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    visualizationPanel.add(visualizationPanelFiller, gridBagConstraints);

    javax.swing.GroupLayout scrollPaneContainerPanelLayout = new javax.swing.GroupLayout(scrollPaneContainerPanel);
    scrollPaneContainerPanel.setLayout(scrollPaneContainerPanelLayout);
    scrollPaneContainerPanelLayout.setHorizontalGroup(
      scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scrollPaneContainerPanelLayout.createSequentialGroup()
        .addGroup(scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(statePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(visualizationPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(connectionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 0, Short.MAX_VALUE))
    );
    scrollPaneContainerPanelLayout.setVerticalGroup(
      scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scrollPaneContainerPanelLayout.createSequentialGroup()
        .addComponent(connectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(visualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 69, Short.MAX_VALUE))
    );

    statusPanelScrollPane.setViewportView(scrollPaneContainerPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(statusPanelScrollPane, gridBagConstraints);

    getAccessibleContext().setAccessibleName(bundle.getString("statusPanel.accessibleName")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON
  // FORMATTER:ON

  private void buttonShowLastReportedStateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowLastReportedStateActionPerformed
    if (processModel.getCurrentState() == null) {
      JOptionPane.showMessageDialog(
          this,
          BUNDLE.getString("statusPanel.optionPane_noStateToShow.message")
      );
      return;
    }

    StateMessageDialog dialog = new StateMessageDialog(this, processModel.getCurrentState());
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }//GEN-LAST:event_buttonShowLastReportedStateActionPerformed

  private void buttonGetStateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGetStateActionPerformed
    sendAdapterMessage(
        new VehicleCommAdapterMessage(
            CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
            Map.of(
                CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE,
                "stateRequest",
                CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID,
                UUID.randomUUID().toString(),
                CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE,
                BlockingType.NONE.name()
            )
        )
    );
  }//GEN-LAST:event_buttonGetStateActionPerformed

  /**
   * Sends a message to the comm adapter.
   *
   * @param message The message
   */
  private void sendAdapterMessage(VehicleCommAdapterMessage message) {
    try {
      callWrapper.call(
          () -> vehicleService.sendCommAdapterMessage(
              processModel.getVehicleRef(),
              message
          )
      );
    }
    catch (Exception ex) {
      LOG.warn("Error sending comm adapter message '{}'", message, ex);
    }
  }

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel agvPosInitializedLabel;
  private javax.swing.JTextField agvPosInitializedTextField;
  private javax.swing.JLabel agvPosMapIdLabel;
  private javax.swing.JTextField agvPosMapIdTextField;
  private javax.swing.JLabel agvPosThetaLabel;
  private javax.swing.JTextField agvPosThetaTextField;
  private javax.swing.JLabel agvPosXLabel;
  private javax.swing.JTextField agvPosXTextField;
  private javax.swing.JLabel agvPosYLabel;
  private javax.swing.JTextField agvPosYTextField;
  private javax.swing.JPanel agvPositionPanel;
  private javax.swing.JLabel batteryChargeLabel;
  private javax.swing.JTextField batteryChargeTextField;
  private javax.swing.JPanel batteryStatePanel;
  private javax.swing.JButton buttonGetState;
  private javax.swing.JButton buttonShowLastReportedState;
  private javax.swing.JLabel chargingLabel;
  private javax.swing.JTextField chargingTextField;
  private javax.swing.JLabel connectionHeaderIdLabel;
  private javax.swing.JTextField connectionHeaderIdTextField;
  private javax.swing.JLabel connectionLabel;
  private javax.swing.JPanel connectionPanel;
  private javax.swing.Box.Filler connectionPanelFiller;
  private javax.swing.JTextField connectionTextField;
  private javax.swing.JLabel connectionTimestampLabel;
  private javax.swing.JTextField connectionTimestampTextField;
  private javax.swing.JLabel distSinceLastNodeLabel;
  private javax.swing.JTextField distSinceLastNodeTextField;
  private javax.swing.JLabel drivingLabel;
  private javax.swing.JTextField drivingTextField;
  private javax.swing.JLabel eStopLabel;
  private javax.swing.JTextField eStopTextField;
  private javax.swing.JPanel errorStatePanel;
  private javax.swing.Box.Filler errorStatePanelFiller;
  private javax.swing.JLabel fatalErrorCountLabel;
  private javax.swing.JTextField fatalErrorCountTextField;
  private javax.swing.JLabel fieldViolationLabel;
  private javax.swing.JTextField fieldViolationTextField;
  private javax.swing.JPanel generalStatePanel;
  private javax.swing.Box.Filler generalStatePanelFiller;
  private javax.swing.JLabel headerIdLabel;
  private javax.swing.JTextField headerIdTextField;
  private javax.swing.JPanel headerPanel;
  private javax.swing.Box.Filler headerPanelFiller;
  private javax.swing.JLabel lastNodeIdLabel;
  private javax.swing.JTextField lastNodeIdTextField;
  private javax.swing.JLabel lastNodeSeqIdLabel;
  private javax.swing.JTextField lastNodeSeqIdTextField;
  private javax.swing.JLabel manufacturerLabel;
  private javax.swing.JTextField manufacturerTextField;
  private javax.swing.JLabel newBaseRequestLabel;
  private javax.swing.JTextField newBaseRequestTextField;
  private javax.swing.JTextField operatingModeTextField;
  private javax.swing.JLabel operatingModelLabel;
  private javax.swing.JLabel orderIdLabel;
  private javax.swing.JTextField orderIdTextField;
  private javax.swing.JLabel orderUpdateIdLabel;
  private javax.swing.JTextField orderUpdateIdTextField;
  private javax.swing.JPanel panelManualStateRequest;
  private javax.swing.JLabel pausedLabel;
  private javax.swing.JTextField pausedTextField;
  private javax.swing.JPanel safetyStatePanel;
  private javax.swing.JPanel scrollPaneContainerPanel;
  private javax.swing.JLabel serialNoLabel;
  private javax.swing.JTextField serialNoTextField;
  private javax.swing.JPanel statePanel;
  private javax.swing.JLabel stateTimestampLabel;
  private javax.swing.JTextField stateTimestampTextField;
  private javax.swing.JScrollPane statusPanelScrollPane;
  private javax.swing.JLabel velocityOmegaLabel;
  private javax.swing.JTextField velocityOmegaTextField;
  private javax.swing.JPanel velocityPanel;
  private javax.swing.Box.Filler velocityPanelFiller;
  private javax.swing.JLabel velocityXLabel;
  private javax.swing.JTextField velocityXTextField;
  private javax.swing.JLabel velocityYLabel;
  private javax.swing.JTextField velocityYTextField;
  private javax.swing.JLabel versionLabel;
  private javax.swing.JTextField versionTextField;
  private javax.swing.JLabel visualizationAgvPosInitializedLabel;
  private javax.swing.JTextField visualizationAgvPosInitializedTextField;
  private javax.swing.JLabel visualizationAgvPosMapIdLabel;
  private javax.swing.JTextField visualizationAgvPosMapIdTextField;
  private javax.swing.JLabel visualizationAgvPosThetaLabel;
  private javax.swing.JTextField visualizationAgvPosThetaTextField;
  private javax.swing.JLabel visualizationAgvPosXLabel;
  private javax.swing.JTextField visualizationAgvPosXTextField;
  private javax.swing.JLabel visualizationAgvPosYLabel;
  private javax.swing.JTextField visualizationAgvPosYTextField;
  private javax.swing.JPanel visualizationAgvPositionPanel;
  private javax.swing.JLabel visualizationHeaderIdLabel;
  private javax.swing.JTextField visualizationHeaderIdTextField;
  private javax.swing.JPanel visualizationPanel;
  private javax.swing.Box.Filler visualizationPanelFiller;
  private javax.swing.JPanel visualizationRowPanel;
  private javax.swing.Box.Filler visualizationRowPanelFiller;
  private javax.swing.JLabel visualizationTimestampLabel;
  private javax.swing.JTextField visualizationTimestampTextField;
  private javax.swing.JLabel visualizationVelocityOmegaLabel;
  private javax.swing.JTextField visualizationVelocityOmegaTextField;
  private javax.swing.JPanel visualizationVelocityPanel;
  private javax.swing.JLabel visualizationVelocityXLabel;
  private javax.swing.JTextField visualizationVelocityXTextField;
  private javax.swing.JLabel visualizationVelocityYLabel;
  private javax.swing.JTextField visualizationVelocityYTextField;
  private javax.swing.JLabel warningCountLabel;
  private javax.swing.JTextField warningCountTextField;
  private javax.swing.JLabel zoneSetIdLabel;
  private javax.swing.JTextField zoneSetIdTextField;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
