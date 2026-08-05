// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import static java.util.Objects.requireNonNull;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ProcessModelImpl;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.InitPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action.ActionConfigurationPanel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.NodeMapping;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the gui of the vehicle control and visualizes the status of protocol data.
 */
public class ControlPanel
    extends
      VehicleCommAdapterPanel {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ControlPanel.class);
  /**
   * Declares how many orders should be saved.
   */
  private static final int MAX_LAST_ORDERS = 20;
  /**
   * The action configuration panel for new orders.
   */
  private final ActionConfigurationPanel newOrderActionConfigurationPanel;
  /**
   * The action configuration panel for instant actions.
   */
  private final ActionConfigurationPanel instantActionConfigurationPanel;
  /**
   * Model for last order list.
   */
  private final DefaultListModel<Order> lastOrderListModel = new DefaultListModel<>();
  /**
   * Model for last instant action list.
   */
  private final DefaultListModel<InstantActions> lastInstantActionListsModel
      = new DefaultListModel<>();
  /**
   * The vehicle service used for interaction with the comm adapter.
   */
  private final VehicleService vehicleService;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * Maps points from movement commands to a VDA5050 node.
   */
  private final NodeMapping nodeMapping;
  /**
   * The comm adapter's process model.
   */
  private ProcessModelImplTO processModel;

  /**
   * Creates a new instance.
   *
   * @param newOrderActionConfigurationPanel action configuration panel for new orders
   * @param instantActionConfigurationPanel action configuration panel for instant actions
   * @param processModel The comm adapter's process model
   * @param vehicleService The vehicle service
   * @param callWrapper The call wrapper to use for service calls
   * @param nodeMapping Maps points from movement commands to a VDA5050 node.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public ControlPanel(
      ActionConfigurationPanel newOrderActionConfigurationPanel,
      ActionConfigurationPanel instantActionConfigurationPanel,
      @Assisted
      ProcessModelImplTO processModel,
      @Assisted
      VehicleService vehicleService,
      @ServiceCallWrapper
      CallWrapper callWrapper,
      NodeMapping nodeMapping
  ) {
    this.newOrderActionConfigurationPanel
        = requireNonNull(newOrderActionConfigurationPanel, "newOrderActionConfigurationPanel");
    this.instantActionConfigurationPanel
        = requireNonNull(instantActionConfigurationPanel, "instantActionConfigurationPanel");
    this.processModel = requireNonNull(processModel, "processModel");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.nodeMapping = requireNonNull(nodeMapping, "nodeMapping");
    initComponents();
    initComboBoxes();
    initGuiContent();
  }

  @Override
  public void processModelChange(String attributeChanged, VehicleProcessModelTO newProcessModel) {
    if (!(newProcessModel instanceof ProcessModelImplTO)) {
      return;
    }
    processModel = (ProcessModelImplTO) newProcessModel;

    if (Objects.equals(
        attributeChanged,
        VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED.name()
    )) {
      updateCommAdapterEnabled(processModel.isCommAdapterEnabled());
    }
    else if (Objects.equals(
        attributeChanged,
        VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name()
    )) {
      updateCommAdapterConnected(processModel.isCommAdapterConnected());
    }
    else if (Objects.equals(attributeChanged, ProcessModelImpl.Attribute.VEHICLE_IDLE.name())) {
      updateVehicleIdle(processModel.isVehicleIdle());
    }
    else if (Objects.equals(attributeChanged, ProcessModelImpl.Attribute.LAST_ORDER.name())) {
      updateLastOrder(processModel.getLastOrderSent());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.LAST_INSTANT_ACTIONS.name()
    )) {
      updateLastInstantActions(processModel.getLastInstantActionsSent());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.BROKER_CONNECTED.name()
    )) {
      updateCommAdapterConnectedToBroker(processModel.isBrokerConnected());
    }
    else if (Objects.equals(
        attributeChanged,
        ProcessModelImpl.Attribute.TOPIC_PREFIX.name()
    )) {
      updateTopicPrefix(processModel.getTopicPrefix());
    }
  }

  /**
   * Initializes combo boxes for destinations and actions.
   */
  private void initComboBoxes() {
    try {
      pathComboBox.removeAllItems();
      callWrapper.call(() -> vehicleService.fetch(Path.class)).stream()
          .sorted(Comparators.objectsByName())
          .forEach(path -> {
            pathComboBox.addItem(path);
          });
    }
    catch (Exception ex) {
      LOG.warn("Error fetching points", ex);
    }
  }

  /**
   * Updates all fields showing an attribute of the process model to the current state.
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

  /**
   * Updates the state of specific elements in the gui to let the user interact with them or not
   * depending on the enabled state of the comm adapter.
   *
   * @param enabled The enabled state of the comm adapter
   */
  private void updateCommAdapterEnabled(boolean enabled) {
    SwingUtilities.invokeLater(() -> {
      enableAdapterCheckBox.setSelected(enabled);
    });
  }

  /**
   * Updates buttons for interacting with the vehicle when the connection is established or not.
   *
   * @param connected Whether the connection to the vehicle is established
   */
  private void updateCommAdapterConnected(boolean connected) {
    SwingUtilities.invokeLater(() -> {
      vehicleConnectedButton.setSelected(connected);

      sendOrderButton.setEnabled(connected);
      sendInstantActionButton.setEnabled(connected);
    });
  }

  /**
   * Updates elements for when the adapter is connected to the broker or not.
   *
   * @param connected Whether the connection to the vehicle is established
   */
  private void updateCommAdapterConnectedToBroker(boolean connected) {
    SwingUtilities.invokeLater(() -> {
      brokerConnectedButton.setSelected(connected);
    });
  }

  private void updateTopicPrefix(String topicPrefix) {
    SwingUtilities.invokeLater(() -> {
      topicPrefixTextField.setText(topicPrefix);
    });
  }

  /**
   * Updates the idle state of the vehicle in the gui.
   *
   * @param idle The idle state
   */
  private void updateVehicleIdle(boolean idle) {
    // TODO Introduce a seperate element to indicate the vehicles idle state.
  }

  /**
   * Updates the list of last orders sent with the given one as last order.
   *
   * @param lastOrderSent The last order sent
   */
  private void updateLastOrder(Order lastOrderSent) {
    if (lastOrderSent == null) {
      return;
    }

    SwingUtilities.invokeLater(() -> {
      applyLastOrderButton.setEnabled(true);
      lastOrderListModel.add(0, lastOrderSent);
      lastOrdersList.setSelectedIndex(0);
      // Delete last element(s) if our list model contains too many elements.
      while (lastOrderListModel.getSize() > MAX_LAST_ORDERS) {
        lastOrderListModel.removeElement(lastOrderListModel.lastElement());
      }
    });
  }

  /**
   * Updates the list of last instant actions sent to the vehicle.
   *
   * @param lastInstantActionsSent The last instant action sent.
   */
  private void updateLastInstantActions(InstantActions lastInstantActionsSent) {
    if (lastInstantActionsSent == null) {
      return;
    }

    SwingUtilities.invokeLater(() -> {
      applyLastInstantActionButton.setEnabled(true);
      lastInstantActionListsModel.add(0, lastInstantActionsSent);
      lastInstantActionsList.setSelectedIndex(0);
      // Delete last element if our list model contains too many elements
      while (lastInstantActionListsModel.getSize() > MAX_LAST_ORDERS) {
        lastInstantActionListsModel.removeElement(lastInstantActionListsModel.lastElement());
      }
    });
  }

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

  /**
   * Enables or disables the comm adapter.
   *
   * @param enable Whether the comm adapter should be enabled or disabled
   */
  private void enableCommAdapter(boolean enable) {
    try {
      if (enable) {
        callWrapper.call(() -> vehicleService.enableCommAdapter(processModel.getVehicleRef()));
      }
      else {
        callWrapper.call(() -> vehicleService.disableCommAdapter(processModel.getVehicleRef()));
      }
    }
    catch (Exception ex) {
      LOG.warn("Error enabling/disabling comm adapter", ex);
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

    controlPanelScrollPane = new javax.swing.JScrollPane();
    scrollPaneContainerPanel = new javax.swing.JPanel();
    connectionSettingsPanel = new javax.swing.JPanel();
    enableAdapterCheckBox = new javax.swing.JCheckBox();
    brokerConnectedButton = new javax.swing.JButton();
    vehicleConnectedButton = new javax.swing.JButton();
    connectionSettingsPanelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    topicPrefixLabel = new javax.swing.JLabel();
    topicPrefixTextField = new javax.swing.JTextField();
    orderPanel = new javax.swing.JPanel();
    pathLabel = new javax.swing.JLabel();
    pathComboBox = new javax.swing.JComboBox<>();
    orderIdLabel = new javax.swing.JLabel();
    orderIdTextField = new javax.swing.JTextField();
    sendOrderButton = new javax.swing.JButton();
    orderUpdateIdLabel = new javax.swing.JLabel();
    orderUpdateIdTextField = new javax.swing.JTextField();
    applyLastOrderButton = new javax.swing.JButton();
    lastOrdersScrollPane = new javax.swing.JScrollPane();
    lastOrdersList = new javax.swing.JList<>();
    org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action.ActionConfigurationPanel newOrderActionConfigurationPanelLocal = newOrderActionConfigurationPanel;
    newOrderPanelSeparator = new javax.swing.JSeparator();
    instantActionPanel = new javax.swing.JPanel();
    sendInstantActionButton = new javax.swing.JButton();
    lastInstantActionsScrollPane = new javax.swing.JScrollPane();
    lastInstantActionsList = new javax.swing.JList<>();
    applyLastInstantActionButton = new javax.swing.JButton();
    org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action.ActionConfigurationPanel instantActionConfigurationPanelLocal = instantActionConfigurationPanel;
    instantActionPanelSeparator = new javax.swing.JSeparator();

    setLayout(new java.awt.GridBagLayout());

    controlPanelScrollPane.setBorder(null);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/vehicle/vda5050/v2_0/Bundle"); // NOI18N
    connectionSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("controlPanel.panel_connectionSettings.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    connectionSettingsPanel.setLayout(new java.awt.GridBagLayout());

    enableAdapterCheckBox.setText(bundle.getString("controlPanel.panel_connectionSettings.checkBox_enableAdapter.text")); // NOI18N
    enableAdapterCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        enableAdapterCheckBoxActionPerformed(evt);
      }
    });
    connectionSettingsPanel.add(enableAdapterCheckBox, new java.awt.GridBagConstraints());

    brokerConnectedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDGray.gif"))); // NOI18N
    brokerConnectedButton.setText(bundle.getString("controlPanel.panel_connectionSettings.button_brokerConnected.text")); // NOI18N
    brokerConnectedButton.setBorderPainted(false);
    brokerConnectedButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDRed.gif"))); // NOI18N
    brokerConnectedButton.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDGreen.gif"))); // NOI18N
    brokerConnectedButton.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    connectionSettingsPanel.add(brokerConnectedButton, gridBagConstraints);

    vehicleConnectedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDGray.gif"))); // NOI18N
    vehicleConnectedButton.setText(bundle.getString("controlPanel.panel_connectionSettings.button_vehicleConnected.text")); // NOI18N
    vehicleConnectedButton.setBorderPainted(false);
    vehicleConnectedButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDRed.gif"))); // NOI18N
    vehicleConnectedButton.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/LEDGreen.gif"))); // NOI18N
    vehicleConnectedButton.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    connectionSettingsPanel.add(vehicleConnectedButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    connectionSettingsPanel.add(connectionSettingsPanelFiller, gridBagConstraints);

    topicPrefixLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    topicPrefixLabel.setText(bundle.getString("controlPanel.panel_connectionSettings.label_topicPrefix.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    connectionSettingsPanel.add(topicPrefixLabel, gridBagConstraints);

    topicPrefixTextField.setEditable(false);
    topicPrefixTextField.setText("interface/v1/manufacturer/serialno");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    connectionSettingsPanel.add(topicPrefixTextField, gridBagConstraints);

    orderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("controlPanel.panel_sendOrder.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    orderPanel.setLayout(new java.awt.GridBagLayout());

    pathLabel.setText(bundle.getString("controlPanel.panel_sendOrder.label_path.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    orderPanel.add(pathLabel, gridBagConstraints);

    pathComboBox.setRenderer(new PathRenderer());
    pathComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        pathComboBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 3);
    orderPanel.add(pathComboBox, gridBagConstraints);

    orderIdLabel.setText(bundle.getString("controlPanel.panel_sendOrder.label_orderId.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    orderPanel.add(orderIdLabel, gridBagConstraints);

    orderIdTextField.setColumns(8);
    orderIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    orderIdTextField.setText("1");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    orderPanel.add(orderIdTextField, gridBagConstraints);

    sendOrderButton.setText(bundle.getString("controlPanel.panel_sendOrder.button_sendOrder.text")); // NOI18N
    sendOrderButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendOrderButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    orderPanel.add(sendOrderButton, gridBagConstraints);

    orderUpdateIdLabel.setText(bundle.getString("controlPanel.panel_sendOrder.label_orderUpdateId.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    orderPanel.add(orderUpdateIdLabel, gridBagConstraints);

    orderUpdateIdTextField.setColumns(6);
    orderUpdateIdTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    orderUpdateIdTextField.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 3);
    orderPanel.add(orderUpdateIdTextField, gridBagConstraints);

    applyLastOrderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/down-button.16.png"))); // NOI18N
    applyLastOrderButton.setText(bundle.getString("controlPanel.panel_sendOrder.button_applyLastOrderButton.text")); // NOI18N
    applyLastOrderButton.setEnabled(false);
    applyLastOrderButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    applyLastOrderButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        applyLastOrderButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    orderPanel.add(applyLastOrderButton, gridBagConstraints);

    lastOrdersScrollPane.setPreferredSize(new java.awt.Dimension(250, 100));

    lastOrdersList.setModel(lastOrderListModel);
    lastOrdersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    lastOrdersList.setCellRenderer(new OrderListCellRenderer());
    lastOrdersList.setPrototypeCellValue(OrderListCellRenderer.PROTOTYPE_ORDER);
    lastOrdersScrollPane.setViewportView(lastOrdersList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    orderPanel.add(lastOrdersScrollPane, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    orderPanel.add(newOrderActionConfigurationPanelLocal, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 3, 6, 3);
    orderPanel.add(newOrderPanelSeparator, gridBagConstraints);

    instantActionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Instant action", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
    instantActionPanel.setLayout(new java.awt.GridBagLayout());

    sendInstantActionButton.setText(bundle.getString("controlPanel.panel_instantActionPanel.button_sendInstantActionButton.text")); // NOI18N
    sendInstantActionButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendInstantActionButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    instantActionPanel.add(sendInstantActionButton, gridBagConstraints);

    lastInstantActionsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    lastInstantActionsScrollPane.setPreferredSize(new java.awt.Dimension(250, 100));

    lastInstantActionsList.setModel(lastInstantActionListsModel);
    lastInstantActionsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    lastInstantActionsList.setCellRenderer(new InstantActionsListCellRenderer());
    lastInstantActionsList.setPrototypeCellValue(InstantActionsListCellRenderer.PROTOTYPE_INSTANT_ACTIONS);
    lastInstantActionsScrollPane.setViewportView(lastInstantActionsList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    instantActionPanel.add(lastInstantActionsScrollPane, gridBagConstraints);

    applyLastInstantActionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/commadapter/vehicle/vda5050/v2_0/images/down-button.16.png"))); // NOI18N
    applyLastInstantActionButton.setText(bundle.getString("controlPanel.panel_sendOrder.button_applyLastOrderButton.text")); // NOI18N
    applyLastInstantActionButton.setEnabled(false);
    applyLastInstantActionButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    applyLastInstantActionButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        applyLastInstantActionButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    instantActionPanel.add(applyLastInstantActionButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
    instantActionPanel.add(instantActionConfigurationPanelLocal, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 3, 6, 3);
    instantActionPanel.add(instantActionPanelSeparator, gridBagConstraints);

    javax.swing.GroupLayout scrollPaneContainerPanelLayout = new javax.swing.GroupLayout(scrollPaneContainerPanel);
    scrollPaneContainerPanel.setLayout(scrollPaneContainerPanelLayout);
    scrollPaneContainerPanelLayout.setHorizontalGroup(
      scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scrollPaneContainerPanelLayout.createSequentialGroup()
        .addGroup(scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(connectionSettingsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(scrollPaneContainerPanelLayout.createSequentialGroup()
            .addComponent(orderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(instantActionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(0, 0, Short.MAX_VALUE))
    );
    scrollPaneContainerPanelLayout.setVerticalGroup(
      scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scrollPaneContainerPanelLayout.createSequentialGroup()
        .addComponent(connectionSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(scrollPaneContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(orderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(instantActionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, Short.MAX_VALUE))
    );

    controlPanelScrollPane.setViewportView(scrollPaneContainerPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(controlPanelScrollPane, gridBagConstraints);

    getAccessibleContext().setAccessibleName(bundle.getString("controlPanel.accessibleName")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON
  // FORMATTER:ON

  private void sendOrderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendOrderButtonActionPerformed
    long updateId;
    try {
      updateId = Long.parseLong(orderUpdateIdTextField.getText());
    }
    catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(
          null,
          "Cannot convert \"" + orderUpdateIdTextField.getText() + "\" to integer",
          "Cannot parse integer",
          JOptionPane.ERROR
      );
      return;
    }

    Path path = (Path) pathComboBox.getSelectedItem();
    TCSObjectReference<Point> sourcePoint = path.getSourcePoint();
    TCSObjectReference<Point> destinationPoint = path.getDestinationPoint();

    Map<String, String> messageParameters = new HashMap<>();
    messageParameters.put(
        CommAdapterMessages.SEND_ORDER_PARAM_ORDER_ID,
        orderIdTextField.getText()
    );
    messageParameters.put(
        CommAdapterMessages.SEND_ORDER_PARAM_ORDER_UPDATE_ID,
        String.valueOf(updateId)
    );
    messageParameters.put(
        CommAdapterMessages.SEND_ORDER_PARAM_SOURCE_NODE,
        sourcePoint.getName()
    );
    messageParameters.put(
        CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE,
        destinationPoint.getName()
    );
    messageParameters.put(
        CommAdapterMessages.SEND_ORDER_PARAM_EDGE,
        path.getName()
    );

    try {
      Optional<Action> maybeAction = newOrderActionConfigurationPanel.getAction();
      if (maybeAction.isPresent()) {
        Action action = maybeAction.get();

        validateParameters(action);

        messageParameters.put(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE,
            action.getActionType()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID,
            action.getActionId()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
            action.getBlockingType().name()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION,
            action.getActionDescription()
        );


        action.getActionParameters().forEach(
            actionParameter -> messageParameters.put(
                CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX
                    + actionParameter.getKey(),
                actionParameter.getValue().toString()
            )
        );
      }

      sendAdapterMessage(
          new VehicleCommAdapterMessage(CommAdapterMessages.SEND_ORDER_TYPE, messageParameters)
      );
    }
    catch (InvalidActionParameterException ex) {
      JOptionPane.showMessageDialog(
          this,
          ex.getMessage(),
          "Invalid parameter",
          JOptionPane.ERROR_MESSAGE
      );
    }
  }//GEN-LAST:event_sendOrderButtonActionPerformed

  private void enableAdapterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAdapterCheckBoxActionPerformed
    enableCommAdapter(enableAdapterCheckBox.isSelected());
  }//GEN-LAST:event_enableAdapterCheckBoxActionPerformed

  private void sendInstantActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendInstantActionButtonActionPerformed
    try {
      Optional<Action> maybeInstant = instantActionConfigurationPanel.getAction();
      if (maybeInstant.isPresent()) {
        Action action = maybeInstant.get();
        validateParameters(action);

        Map<String, String> messageParameters = new HashMap<>();

        messageParameters.put(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE,
            action.getActionType()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID,
            action.getActionId()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE,
            action.getBlockingType().name()
        );
        messageParameters.put(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION,
            action.getActionDescription()
        );

        action.getActionParameters()
            .forEach(
                actionParameter -> messageParameters.put(
                    CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX
                        + actionParameter.getKey(),
                    actionParameter.getValue().toString()
                )
            );

        sendAdapterMessage(
            new VehicleCommAdapterMessage(
                CommAdapterMessages.SEND_INSTANT_ACTION_TYPE, messageParameters
            )
        );
      }
    }
    catch (InvalidActionParameterException ex) {
      JOptionPane.showMessageDialog(
          this,
          ex.getMessage(),
          "Invalid action parameter",
          JOptionPane.ERROR_MESSAGE
      );
    }
  }//GEN-LAST:event_sendInstantActionButtonActionPerformed

  private void applyLastOrderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyLastOrderButtonActionPerformed
    fillNewOrderPanel(lastOrdersList.getSelectedValue());
  }//GEN-LAST:event_applyLastOrderButtonActionPerformed

  private void applyLastInstantActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyLastInstantActionButtonActionPerformed
    fillInstantActionsPanel(lastInstantActionsList.getSelectedValue());
  }//GEN-LAST:event_applyLastInstantActionButtonActionPerformed

  private void pathComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_pathComboBoxItemStateChanged
    if (pathComboBox.getSelectedItem() == null) {
      pathComboBox.setToolTipText(null);
      return;
    }

    pathComboBox.setToolTipText(((Path) pathComboBox.getSelectedItem()).getName());
  }//GEN-LAST:event_pathComboBoxItemStateChanged

  /**
   * Updates the order input fields to the values from the order.
   *
   * @param order The order.
   */
  private void fillNewOrderPanel(Order order) {
    if (order == null) {
      return;
    }

    orderIdTextField.setText(order.getOrderId());
    orderUpdateIdTextField.setText(String.valueOf(order.getOrderUpdateId()));
    if (!order.getEdges().isEmpty()) {
      String pathName = order.getEdges().get(0).getEdgeId();
      vehicleService.fetch(Path.class, pathName)
          .ifPresent(path -> pathComboBox.setSelectedItem(path));
    }

    Optional<Action> lastAction = getLastNodeAction(order);
    if (lastAction.isPresent()) {
      newOrderActionConfigurationPanel.setAction(lastAction.get());
    }
    else {
      newOrderActionConfigurationPanel.clear();
    }
  }

  private void fillInstantActionsPanel(InstantActions instantActions) {
    if (instantActions == null) {
      return;
    }
    if (instantActions.getActions().isEmpty()) {
      return;
    }
    instantActionConfigurationPanel.setAction(instantActions.getActions().get(0));
  }

  private Optional<Action> getLastNodeAction(Order order) {
    if (order.getNodes().isEmpty()) {
      return Optional.empty();
    }
    Node lastNode = order.getNodes().get(order.getNodes().size() - 1);
    if (lastNode.getActions().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(lastNode.getActions().get(0));
  }

  /**
   * Validates the parameters of the given action.
   *
   * @param action The action
   * @throws InvalidActionParameterException If any parameters of the given action are not valid.
   */
  private void validateParameters(Action action)
      throws InvalidActionParameterException {
    if (Objects.equals(action.getActionType(), InitPosition.ACTION_TYPE)) {
      validateInitPositionNumericParams(action);
    }
  }

  /**
   * Validates that x, y, and theta parameters of an initPosition action are valid finite numbers.
   *
   * @param action The action.
   * @throws InvalidActionParameterException If any parameter is not a valid finite number.
   */
  private void validateInitPositionNumericParams(Action action) {
    if (!Objects.equals(action.getActionType(), InitPosition.ACTION_TYPE)) {
      return;
    }

    Optional<ActionParameter> mistypedParameter = action.getActionParameters().stream()
        .filter(
            parameter -> Objects.equals(parameter.getKey(), InitPosition.PARAMKEY_X)
                || Objects.equals(parameter.getKey(), InitPosition.PARAMKEY_Y)
                || Objects.equals(parameter.getKey(), InitPosition.PARAMKEY_THETA)
        )
        .filter(parameter -> parameter.getValue() instanceof String)
        .filter(parameter -> {
          try {
            return !Double.isFinite(Double.parseDouble((String) parameter.getValue()));
          }
          catch (NumberFormatException e) {
            return true;
          }
        })
        .findAny();

    if (mistypedParameter.isPresent()) {
      ActionParameter parameter = mistypedParameter.get();
      throw new InvalidActionParameterException(
          "Parameter '%s': value '%s' is not a floating-point number"
              .formatted(
                  parameter.getKey(),
                  parameter.getValue()
              )
      );
    }
  }


  /**
   * Indicates an invalid action parameter.
   */
  private static class InvalidActionParameterException
      extends
        RuntimeException {
    InvalidActionParameterException(String message) {
      super(message);
    }
  }

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton applyLastInstantActionButton;
  private javax.swing.JButton applyLastOrderButton;
  private javax.swing.JButton brokerConnectedButton;
  private javax.swing.JPanel connectionSettingsPanel;
  private javax.swing.Box.Filler connectionSettingsPanelFiller;
  private javax.swing.JScrollPane controlPanelScrollPane;
  private javax.swing.JCheckBox enableAdapterCheckBox;
  private javax.swing.JPanel instantActionPanel;
  private javax.swing.JSeparator instantActionPanelSeparator;
  private javax.swing.JList<InstantActions> lastInstantActionsList;
  private javax.swing.JScrollPane lastInstantActionsScrollPane;
  private javax.swing.JList<Order> lastOrdersList;
  private javax.swing.JScrollPane lastOrdersScrollPane;
  private javax.swing.JSeparator newOrderPanelSeparator;
  private javax.swing.JLabel orderIdLabel;
  private javax.swing.JTextField orderIdTextField;
  private javax.swing.JPanel orderPanel;
  private javax.swing.JLabel orderUpdateIdLabel;
  private javax.swing.JTextField orderUpdateIdTextField;
  private javax.swing.JComboBox<Path> pathComboBox;
  private javax.swing.JLabel pathLabel;
  private javax.swing.JPanel scrollPaneContainerPanel;
  private javax.swing.JButton sendInstantActionButton;
  private javax.swing.JButton sendOrderButton;
  private javax.swing.JLabel topicPrefixLabel;
  private javax.swing.JTextField topicPrefixTextField;
  private javax.swing.JButton vehicleConnectedButton;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
