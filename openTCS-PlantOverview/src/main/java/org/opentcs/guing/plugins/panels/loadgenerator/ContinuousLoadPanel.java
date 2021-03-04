/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.plugins.panels.loadgenerator.PropertyTableModel.PropEntry;
import org.opentcs.util.Comparators;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.gui.StringListCellRenderer;
import org.opentcs.util.gui.StringTableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel for continously creating transport orders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ContinuousLoadPanel
    extends PluggablePanel {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ContinuousLoadPanel.class);
  /**
   * This classe's bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle");
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * Where we get events from.
   */
  private final EventSource eventSource;
  /**
   * The client that is registered at the kernel.
   */
  private SharedKernelServicePortal sharedPortal;
  /**
   * The object service.
   */
  private TCSObjectService objectService;
  /**
   * The instance trigger creation of new orders.
   */
  private volatile OrderGenerationTrigger orderGenTrigger;
  /**
   * The currently selected transport order data.
   */
  private TransportOrderData selectedTrOrder;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param portalProvider The application's portal provider.
   * @param eventSource Where components can register for events.
   */
  @Inject
  public ContinuousLoadPanel(SharedKernelServicePortalProvider portalProvider,
                             @ApplicationEventBus EventSource eventSource) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.eventSource = requireNonNull(eventSource, "eventSource");

    initComponents();

    JComboBox<TransportOrderData.Deadline> deadlineComboBox = new JComboBox<>();
    deadlineComboBox.addItem(null);
    for (TransportOrderData.Deadline i : TransportOrderData.Deadline.values()) {
      deadlineComboBox.addItem(i);
    }
    DefaultCellEditor deadlineEditor = new DefaultCellEditor(deadlineComboBox);
    toTable.setDefaultEditor(TransportOrderData.Deadline.class, deadlineEditor);

    toTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    doTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    // Get a kernel reference.
    try {
      sharedPortal = portalProvider.register();
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Kernel unavailable", exc);
      return;
    }

    objectService = (TCSObjectService) sharedPortal.getPortal().getPlantModelService();

    Set<Vehicle> vehicles = new TreeSet<>(Comparators.objectsByName());
    vehicles.addAll(objectService.fetchObjects(Vehicle.class));
    JComboBox<TCSObjectReference<Vehicle>> vehiclesComboBox = new JComboBox<>();
    vehiclesComboBox.addItem(null);
    vehiclesComboBox.setRenderer(new StringListCellRenderer<>(x -> x == null ? "" : x.getName()));
    for (Vehicle curVehicle : vehicles) {
      vehiclesComboBox.addItem(curVehicle.getReference());
    }
    DefaultCellEditor vehicleEditor = new DefaultCellEditor(vehiclesComboBox);
    toTable.setDefaultEditor(TCSObjectReference.class, vehicleEditor);
    toTable.setDefaultRenderer(
        TCSObjectReference.class,
        new StringTableCellRenderer<TCSObjectReference<?>>(x -> x == null ? "" : x.getName()));

    updateElementStates();

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    // Disable order generation
    orderGenChkBox.setSelected(false);

    sharedPortal.close();
    initialized = false;
  }

  private void updateElementStates() {
    updateTriggerElementStates();
    updateOrderSpecElementStates();

    updateRandomOrderElementStates();
    updateExplicitOrderElementStates();
  }

  private void updateTriggerElementStates() {
    boolean enabled = !orderGenChkBox.isSelected();

    singleTriggerRadioButton.setEnabled(enabled);
    thresholdTriggerRadioButton.setEnabled(enabled);
    thresholdSpinner.setEnabled(enabled);
    thresholdOrdersLbl.setEnabled(enabled);
    timerTriggerRadioButton.setEnabled(enabled);
    timerSpinner.setEnabled(enabled);
    timerSecondsLbl.setEnabled(enabled);
  }

  private void updateOrderSpecElementStates() {
    boolean enabled = !orderGenChkBox.isSelected();

    randomOrderSpecButton.setEnabled(enabled);
    explicitOrderSpecButton.setEnabled(enabled);
  }

  private void updateRandomOrderElementStates() {
    boolean enabled = randomOrderSpecButton.isSelected() && !orderGenChkBox.isSelected();

    randomOrderCountSpinner.setEnabled(enabled);
    randomOrderCountLbl.setEnabled(enabled);
    randomOrderSizeSpinner.setEnabled(enabled);
    randomOrderSizeLbl.setEnabled(enabled);
  }

  private void updateExplicitOrderElementStates() {
    boolean enabled = explicitOrderSpecButton.isSelected() && !orderGenChkBox.isSelected();

    transportOrderGenPanel.setEnabled(enabled);
    transportOrdersPanel.setEnabled(enabled);
    addToTOTableButton.setEnabled(enabled);
    jTabbedPane1.setEnabled(enabled);
    driveOrdersPanel.setEnabled(enabled);
    propertyPanel.setEnabled(enabled);

    doTable.setEnabled(enabled);
    toTable.setEnabled(enabled);
    openButton.setEnabled(enabled);
    saveButton.setEnabled(enabled);

    boolean transportOrderSelected = enabled && toTable.getSelectedRow() >= 0;
    boolean driveOrderSelected = transportOrderSelected && doTable.getSelectedRow() >= 0;
    boolean propertySelected = transportOrderSelected && propertyTable.getSelectedRow() >= 0;
    // These buttons should only be enabled if an order is selected
    deleteFromTOTableButton.setEnabled(transportOrderSelected);
    addDOButton.setEnabled(transportOrderSelected);
    addPropertyButton.setEnabled(transportOrderSelected);
    deleteFromDOTableButton.setEnabled(driveOrderSelected);
    removePropertyButton.setEnabled(propertySelected);
  }

  /**
   * Creates a suitable OrderBatchCreator.
   *
   * @return A suitable OrderBatchCreator.
   */
  private OrderBatchCreator createOrderBatchCreator() {
    if (randomOrderSpecButton.isSelected()) {
      int orderCount = (Integer) randomOrderCountSpinner.getValue();
      int orderSize = (Integer) randomOrderSizeSpinner.getValue();
      return new RandomOrderBatchCreator(sharedPortal.getPortal().getTransportOrderService(),
                                         sharedPortal.getPortal().getDispatcherService(),
                                         orderCount,
                                         orderSize);
    }
    else if (explicitOrderSpecButton.isSelected()) {
      saveCurrentTableData();
      TransportOrderTableModel tableModel
          = (TransportOrderTableModel) toTable.getModel();
      for (TransportOrderData curData : tableModel.getList()) {
        if (curData.getDriveOrders().isEmpty()) {
          JOptionPane.showMessageDialog(this,
                                        bundle.getString("error.driveOrderEmpty.text"),
                                        bundle.getString("error.driveOrderEmpty.title"),
                                        JOptionPane.ERROR_MESSAGE);
          return null;
        }
        else {
          for (DriveOrderStructure curDOS : curData.getDriveOrders()) {
            if (curDOS.getDriveOrderLocation() == null
                || curDOS.getDriveOrderVehicleOperation() == null) {
              JOptionPane.showMessageDialog(this,
                                            bundle.getString("error.driveOrderIncorrect.text"),
                                            bundle.getString("error.driveOrderIncorrect.title"),
                                            JOptionPane.ERROR_MESSAGE);
              return null;
            }
          }
        }
      }
      return new ExplicitOrderBatchGenerator(sharedPortal.getPortal().getTransportOrderService(),
                                             sharedPortal.getPortal().getDispatcherService(),
                                             tableModel.getList());
    }
    else {
      throw new UnsupportedOperationException("Unsupported order spec.");
    }
  }

  /**
   * Creates a new order generation trigger.
   *
   * @return A new order generation trigger
   */
  private OrderGenerationTrigger createOrderGenTrigger() {
    OrderBatchCreator batchCreator = createOrderBatchCreator();
    if (batchCreator == null) {
      return null;
    }
    if (thresholdTriggerRadioButton.isSelected()) {
      return new ThresholdOrderGenTrigger(eventSource,
                                          objectService,
                                          (Integer) thresholdSpinner.getValue(),
                                          batchCreator);
    }
    else if (timerTriggerRadioButton.isSelected()) {
      return new TimeoutOrderGenTrigger((Integer) timerSpinner.getValue() * 1000,
                                        batchCreator);
    }
    else if (singleTriggerRadioButton.isSelected()) {
      return new SingleOrderGenTrigger(batchCreator);
    }
    else {
      LOG.warn("No trigger selected");
      return null;
    }
  }

  /**
   * Saves the data in the table models to the actual TransportOrderData.
   */
  private void saveCurrentTableData() {
    if (selectedTrOrder == null) {
      return;
    }

    // Save the local properties before clearing the table
    PropertyTableModel propTableModel = (PropertyTableModel) propertyTable.getModel();
    selectedTrOrder.getProperties().clear();
    for (PropEntry propEntry : propTableModel.getList()) {
      selectedTrOrder.addProperty(propEntry.getKey(), propEntry.getValue());
    }
    // Save the drive orders
    DriveOrderTableModel doTableModel = (DriveOrderTableModel) doTable.getModel();
    selectedTrOrder.getDriveOrders().clear();
    for (DriveOrderStructure curDO : doTableModel.getContent()) {
      selectedTrOrder.addDriveOrder(curDO);
    }
  }

  /**
   * Builds the tables when a transport order was selected.
   *
   * @param selectedRow Indicating which transport order was selected.
   */
  private void buildTableModels(int selectedRow) {
    if (selectedRow < 0) {
      return;
    }

    saveCurrentTableData();

    selectedTrOrder = ((TransportOrderTableModel) toTable.getModel()).getDataAt(selectedRow);
    if (selectedTrOrder != null) {
      buildTableModels(selectedTrOrder);
    }
  }

  private void buildTableModels(TransportOrderData transportOrder) {
    requireNonNull(transportOrder, "transportOrder");

    // Drive orders
    locationsComboBox.removeAllItems();
    operationTypesComboBox.removeAllItems();
    SortedSet<Location> sortedLocationSet = new TreeSet<>(Comparators.objectsByName());
    sortedLocationSet.addAll(objectService.fetchObjects(Location.class));
    for (Location i : sortedLocationSet) {
      locationsComboBox.addItem(i.getReference());
    }
    locationsComboBox.addItemListener((ItemEvent e) -> locationsComboBoxItemStateChanged(e));
    doTable.setModel(new DriveOrderTableModel(transportOrder.getDriveOrders()));
    doTable.setDefaultEditor(TCSObjectReference.class, new DefaultCellEditor(locationsComboBox));
    doTable.setDefaultEditor(String.class, new DefaultCellEditor(operationTypesComboBox));

    // Properties
    propertyTable.setModel(new PropertyTableModel(transportOrder.getProperties()));
    propertyTable.getColumn(bundle.getString("key"))
        .setCellEditor(new DefaultCellEditor(propertyComboBox));
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

    orderSpecButtonGroup = new javax.swing.ButtonGroup();
    triggerButtonGroup = new javax.swing.ButtonGroup();
    operationTypesComboBox = new javax.swing.JComboBox<>();
    locationsComboBox = new javax.swing.JComboBox<>();
    locationsComboBox.setRenderer(new LocationComboBoxRenderer());
    propertyComboBox = new javax.swing.JComboBox<>();
    propertyComboBox.setSelectedIndex(-1);
    fileChooser = new javax.swing.JFileChooser();
    triggerPanel = new javax.swing.JPanel();
    thresholdTriggerRadioButton = new javax.swing.JRadioButton();
    thresholdSpinner = new javax.swing.JSpinner();
    thresholdOrdersLbl = new javax.swing.JLabel();
    fillingLbl = new javax.swing.JLabel();
    timerTriggerRadioButton = new javax.swing.JRadioButton();
    timerSpinner = new javax.swing.JSpinner();
    timerSecondsLbl = new javax.swing.JLabel();
    singleTriggerRadioButton = new javax.swing.JRadioButton();
    orderProfilePanel = new javax.swing.JPanel();
    randomOrderSpecPanel = new javax.swing.JPanel();
    randomOrderSpecButton = new javax.swing.JRadioButton();
    randomOrderCountSpinner = new javax.swing.JSpinner();
    randomOrderCountLbl = new javax.swing.JLabel();
    fillingLbl3 = new javax.swing.JLabel();
    randomOrderSizeSpinner = new javax.swing.JSpinner();
    randomOrderSizeLbl = new javax.swing.JLabel();
    explicitOrderSpecPanel = new javax.swing.JPanel();
    explicitOrderSpecButton = new javax.swing.JRadioButton();
    fillingLbl4 = new javax.swing.JLabel();
    orderGenPanel = new javax.swing.JPanel();
    orderGenChkBox = new javax.swing.JCheckBox();
    fillingLbl5 = new javax.swing.JLabel();
    transportOrderGenPanel = new javax.swing.JPanel();
    transportOrdersPanel = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    toTable = new javax.swing.JTable();
    TOTableSelectionListener listener = new TOTableSelectionListener(toTable);
toTable.getSelectionModel().addListSelectionListener(listener);
    transportOrdersActionPanel = new javax.swing.JPanel();
    addToTOTableButton = new javax.swing.JButton();
    deleteFromTOTableButton = new javax.swing.JButton();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    driveOrdersPanel = new javax.swing.JPanel();
    driveOrdersScrollPane = new javax.swing.JScrollPane();
    doTable = new javax.swing.JTable();
    deleteFromDOTableButton = new javax.swing.JButton();
    addDOButton = new javax.swing.JButton();
    propertyPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    propertyTable = new javax.swing.JTable();
    addPropertyButton = new javax.swing.JButton();
    removePropertyButton = new javax.swing.JButton();
    openSavePanel = new javax.swing.JPanel();
    openButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle"); // NOI18N
    operationTypesComboBox.setToolTipText(bundle.getString("Allowed_operations")); // NOI18N

    locationsComboBox.setToolTipText(bundle.getString("Available_locations")); // NOI18N
    locationsComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        locationsComboBoxItemStateChanged(evt);
      }
    });

    propertyComboBox.setEditable(true);
    propertyComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "keyX" }));

    setPreferredSize(new java.awt.Dimension(520, 700));
    setLayout(new java.awt.GridBagLayout());

    triggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("GeneratorTriggerTitle"))); // NOI18N
    triggerPanel.setLayout(new java.awt.GridBagLayout());

    triggerButtonGroup.add(thresholdTriggerRadioButton);
    thresholdTriggerRadioButton.setText(bundle.getString("IfThereAreNoMoreThan")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    triggerPanel.add(thresholdTriggerRadioButton, gridBagConstraints);

    thresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 100, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    triggerPanel.add(thresholdSpinner, gridBagConstraints);

    thresholdOrdersLbl.setText(bundle.getString("ThresholdOrders")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    triggerPanel.add(thresholdOrdersLbl, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    triggerPanel.add(fillingLbl, gridBagConstraints);

    triggerButtonGroup.add(timerTriggerRadioButton);
    timerTriggerRadioButton.setText(bundle.getString("AfterATimeoutOf")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    triggerPanel.add(timerTriggerRadioButton, gridBagConstraints);

    timerSpinner.setModel(new javax.swing.SpinnerNumberModel(60, 1, 3600, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    triggerPanel.add(timerSpinner, gridBagConstraints);

    timerSecondsLbl.setText(bundle.getString("Seconds")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    triggerPanel.add(timerSecondsLbl, gridBagConstraints);

    triggerButtonGroup.add(singleTriggerRadioButton);
    singleTriggerRadioButton.setSelected(true);
    singleTriggerRadioButton.setText(bundle.getString("singleTriggerRadioButton")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    triggerPanel.add(singleTriggerRadioButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    add(triggerPanel, gridBagConstraints);

    orderProfilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OrderProfileTitle"))); // NOI18N
    orderProfilePanel.setLayout(new java.awt.GridBagLayout());

    randomOrderSpecPanel.setLayout(new java.awt.GridBagLayout());

    orderSpecButtonGroup.add(randomOrderSpecButton);
    randomOrderSpecButton.setSelected(true);
    randomOrderSpecButton.setText(bundle.getString("CreateOrdersRandomly")); // NOI18N
    randomOrderSpecButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        randomOrderSpecButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    randomOrderSpecPanel.add(randomOrderSpecButton, gridBagConstraints);

    randomOrderCountSpinner.setModel(new javax.swing.SpinnerNumberModel(7, 1, 100, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    randomOrderSpecPanel.add(randomOrderCountSpinner, gridBagConstraints);

    randomOrderCountLbl.setText(bundle.getString("OrdersAtATime")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    randomOrderSpecPanel.add(randomOrderCountLbl, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    randomOrderSpecPanel.add(fillingLbl3, gridBagConstraints);

    randomOrderSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, 10, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    randomOrderSpecPanel.add(randomOrderSizeSpinner, gridBagConstraints);

    randomOrderSizeLbl.setText(bundle.getString("DriveOrdersPerTransportOrder")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    randomOrderSpecPanel.add(randomOrderSizeLbl, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    orderProfilePanel.add(randomOrderSpecPanel, gridBagConstraints);

    explicitOrderSpecPanel.setLayout(new java.awt.GridBagLayout());

    orderSpecButtonGroup.add(explicitOrderSpecButton);
    explicitOrderSpecButton.setText(bundle.getString("CreateOrdersByDefinition")); // NOI18N
    explicitOrderSpecButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        explicitOrderSpecButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    explicitOrderSpecPanel.add(explicitOrderSpecButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    explicitOrderSpecPanel.add(fillingLbl4, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    orderProfilePanel.add(explicitOrderSpecPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(orderProfilePanel, gridBagConstraints);

    orderGenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("OrderGenerationTitle"))); // NOI18N
    orderGenPanel.setLayout(new java.awt.GridBagLayout());

    orderGenChkBox.setText(bundle.getString("EnableOrderGeneration")); // NOI18N
    orderGenChkBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        orderGenChkBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    orderGenPanel.add(orderGenChkBox, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    orderGenPanel.add(fillingLbl5, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(orderGenPanel, gridBagConstraints);

    transportOrderGenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Transport_orders_modelling"))); // NOI18N
    transportOrderGenPanel.setEnabled(false);
    transportOrderGenPanel.setPreferredSize(new java.awt.Dimension(1057, 800));
    transportOrderGenPanel.setLayout(new java.awt.GridBagLayout());

    transportOrdersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Transport_orders"))); // NOI18N
    transportOrdersPanel.setEnabled(false);
    transportOrdersPanel.setPreferredSize(new java.awt.Dimension(568, 452));
    transportOrdersPanel.setLayout(new java.awt.GridBagLayout());

    jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane2.setPreferredSize(new java.awt.Dimension(100, 500));

    toTable.setModel(new TransportOrderTableModel());
    toTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane2.setViewportView(toTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    transportOrdersPanel.add(jScrollPane2, gridBagConstraints);

    transportOrdersActionPanel.setLayout(new java.awt.GridBagLayout());

    addToTOTableButton.setText(bundle.getString("Add_new_order")); // NOI18N
    addToTOTableButton.setToolTipText(bundle.getString("Add_empty_transport_order")); // NOI18N
    addToTOTableButton.setEnabled(false);
    addToTOTableButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addToTOTableButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    transportOrdersActionPanel.add(addToTOTableButton, gridBagConstraints);

    deleteFromTOTableButton.setText(bundle.getString("Delete_selected")); // NOI18N
    deleteFromTOTableButton.setToolTipText(bundle.getString("Remove_selected_transport_order")); // NOI18N
    deleteFromTOTableButton.setEnabled(false);
    deleteFromTOTableButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteFromTOTableButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    transportOrdersActionPanel.add(deleteFromTOTableButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.weightx = 1.0;
    transportOrdersPanel.add(transportOrdersActionPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    transportOrderGenPanel.add(transportOrdersPanel, gridBagConstraints);

    jTabbedPane1.setEnabled(false);

    driveOrdersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Drive_orders"))); // NOI18N
    driveOrdersPanel.setEnabled(false);
    driveOrdersPanel.setLayout(new java.awt.GridBagLayout());

    doTable.setModel(new DriveOrderTableModel());
    doTable.setToolTipText(bundle.getString("Drive_orders_in_selected_transport_order")); // NOI18N
    doTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    driveOrdersScrollPane.setViewportView(doTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    driveOrdersPanel.add(driveOrdersScrollPane, gridBagConstraints);

    deleteFromDOTableButton.setText(bundle.getString("Delete_selected")); // NOI18N
    deleteFromDOTableButton.setToolTipText(bundle.getString("Remove_selected_drive_order")); // NOI18N
    deleteFromDOTableButton.setEnabled(false);
    deleteFromDOTableButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteFromDOTableButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    driveOrdersPanel.add(deleteFromDOTableButton, gridBagConstraints);

    addDOButton.setText(bundle.getString("addDO")); // NOI18N
    addDOButton.setEnabled(false);
    addDOButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addDOButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    driveOrdersPanel.add(addDOButton, gridBagConstraints);

    jTabbedPane1.addTab(bundle.getString("DriveOrders"), driveOrdersPanel); // NOI18N

    propertyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Properties"))); // NOI18N
    propertyPanel.setEnabled(false);
    propertyPanel.setLayout(new java.awt.GridBagLayout());

    jScrollPane1.setViewportView(propertyTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    propertyPanel.add(jScrollPane1, gridBagConstraints);

    addPropertyButton.setText(bundle.getString("addProperty")); // NOI18N
    addPropertyButton.setEnabled(false);
    addPropertyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addPropertyButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    propertyPanel.add(addPropertyButton, gridBagConstraints);

    removePropertyButton.setText(bundle.getString("removeProperty")); // NOI18N
    removePropertyButton.setEnabled(false);
    removePropertyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removePropertyButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    propertyPanel.add(removePropertyButton, gridBagConstraints);

    jTabbedPane1.addTab(bundle.getString("Properties"), propertyPanel); // NOI18N

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    transportOrderGenPanel.add(jTabbedPane1, gridBagConstraints);
    jTabbedPane1.getAccessibleContext().setAccessibleName("Drive orders");

    openButton.setText(bundle.getString("open")); // NOI18N
    openButton.setEnabled(false);
    openButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openButtonActionPerformed(evt);
      }
    });
    openSavePanel.add(openButton);

    saveButton.setText(bundle.getString("save")); // NOI18N
    saveButton.setEnabled(false);
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveButtonActionPerformed(evt);
      }
    });
    openSavePanel.add(saveButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    transportOrderGenPanel.add(openSavePanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(transportOrderGenPanel, gridBagConstraints);

    getAccessibleContext().setAccessibleName(bundle.getString("Continuous_load")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

  private void randomOrderSpecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomOrderSpecButtonActionPerformed
    updateElementStates();
  }//GEN-LAST:event_randomOrderSpecButtonActionPerformed

  private void explicitOrderSpecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_explicitOrderSpecButtonActionPerformed
    updateElementStates();
  }//GEN-LAST:event_explicitOrderSpecButtonActionPerformed

  private void orderGenChkBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_orderGenChkBoxItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      if (sharedPortal.getPortal().getState().equals(Kernel.State.OPERATING)) {
        // Start order generation.
        orderGenTrigger = createOrderGenTrigger();
        if (orderGenTrigger == null) {
          return;
        }
        orderGenTrigger.setTriggeringEnabled(true);
      }
    }
    else // Stop order generation.
    if (orderGenTrigger != null) {
      orderGenTrigger.setTriggeringEnabled(false);
      orderGenTrigger = null;
    }

    updateElementStates();
  }//GEN-LAST:event_orderGenChkBoxItemStateChanged

  private void addToTOTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToTOTableButtonActionPerformed
    TransportOrderTableModel model = (TransportOrderTableModel) toTable.getModel();
    model.addData(new TransportOrderData());

    updateElementStates();
  }//GEN-LAST:event_addToTOTableButtonActionPerformed

  private void deleteFromTOTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFromTOTableButtonActionPerformed
    if (toTable.getSelectedRowCount() == 0) {
      return;
    }

    TransportOrderTableModel model = (TransportOrderTableModel) toTable.getModel();
    // Removes the selected row from the table.
    int selectedIndex = toTable.getSelectedRow();
    model.removeData(selectedIndex);
    if (model.getRowCount() > 0) {
      int indexToBeSelected = Math.min(selectedIndex, model.getRowCount() - 1);
      // Update the drive orders and properties tables before updating the selection.
      // XXX This is not the cleanest way to do it and should be fixed.
      TransportOrderData selectedOrder = model.getDataAt(indexToBeSelected);
      buildTableModels(selectedOrder);
      toTable.changeSelection(indexToBeSelected, 0, false, false);
    }
    else {
      doTable.setModel(new DriveOrderTableModel());
      propertyTable.setModel(new PropertyTableModel());
    }

    updateElementStates();
  }//GEN-LAST:event_deleteFromTOTableButtonActionPerformed

  private void deleteFromDOTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFromDOTableButtonActionPerformed
    if (doTable.getSelectedRow() == -1) {
      return;
    }

    int selectedRow = doTable.getSelectedRow();
    DriveOrderTableModel doTableModel = (DriveOrderTableModel) doTable.getModel();
    doTableModel.removeData(selectedRow);
    int indexToBeSelected = Math.min(selectedRow, doTableModel.getRowCount() - 1);
    doTable.changeSelection(indexToBeSelected, 0, true, false);

    updateElementStates();
  }//GEN-LAST:event_deleteFromDOTableButtonActionPerformed

  private void addDOButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDOButtonActionPerformed
    DriveOrderTableModel model = (DriveOrderTableModel) doTable.getModel();
    model.addData(new DriveOrderStructure());

    updateElementStates();
  }//GEN-LAST:event_addDOButtonActionPerformed

  @SuppressWarnings("unchecked")
  private void locationsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_locationsComboBoxItemStateChanged
    operationTypesComboBox.removeAllItems();
    if (locationsComboBox.getSelectedItem() == null) {
      return;
    }

    TCSObjectReference<Location> loc
        = (TCSObjectReference<Location>) locationsComboBox.getSelectedItem();
    Location location = objectService.fetchObject(Location.class, loc);
    TCSObjectReference<LocationType> locationRef = location.getType();
    LocationType locationType = objectService.fetchObject(LocationType.class, locationRef);
    Set<String> operationTypes = new TreeSet<>(locationType.getAllowedOperations());
    for (String j : operationTypes) {
      operationTypesComboBox.addItem(j);
    }

    // When selecting an item in the locationsComboBox we have
    // to update the vehicle operation in the DriveOrderTable manually,
    // otherwise the old value will persist and that could be a value
    // the new location doesn't support
    int selectedRow = doTable.getSelectedRow();
    if (selectedRow >= 0) {
      DriveOrderTableModel model = (DriveOrderTableModel) doTable.getModel();
      DriveOrderStructure dos = model.getDataAt(selectedRow);
      if (dos != null) {
        if (!operationTypes.isEmpty()) {
          dos.setDriveOrderVehicleOperation(operationTypes.iterator().next());
        }
        else {
          dos.setDriveOrderVehicleOperation(null);
        }
      }
      ((AbstractTableModel) doTable.getModel()).fireTableDataChanged();
    }
  }//GEN-LAST:event_locationsComboBoxItemStateChanged

  private void addPropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPropertyButtonActionPerformed
    PropertyTableModel model = (PropertyTableModel) propertyTable.getModel();
    model.addData(new PropEntry());

    updateElementStates();
  }//GEN-LAST:event_addPropertyButtonActionPerformed

  private void removePropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePropertyButtonActionPerformed
    if (propertyTable.getSelectedRow() == -1) {
      return;
    }

    PropertyTableModel model = (PropertyTableModel) propertyTable.getModel();
    model.removeData(propertyTable.getSelectedRow());

    updateElementStates();
  }//GEN-LAST:event_removePropertyButtonActionPerformed

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    saveCurrentTableData();
    int dialogResult = fileChooser.showSaveDialog(this);
    if (dialogResult != JFileChooser.APPROVE_OPTION) {
      return;
    }

    TransportOrderTableModel model = (TransportOrderTableModel) toTable.getModel();
    File targetFile = fileChooser.getSelectedFile();
    // XXX Check that the file name ends with '.xml'
    if (targetFile.exists()) {
      dialogResult = JOptionPane.showConfirmDialog(
          this,
          bundle.getString("A_file_with_the_chosen_name_already_exists")
          + bundle.getString("do_you_want_to_overwrite_it?"),
          bundle.getString("File_exists"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (dialogResult != JOptionPane.YES_OPTION) {
        return;
      }
    }
    try {
      model.toFile(targetFile);
    }
    catch (IOException exc) {
      LOG.warn("Exception saving property set to " + targetFile.getPath(), exc);
      JOptionPane.showMessageDialog(this,
                                    "Exception saving property set: " + exc.getMessage(),
                                    "Exception saving property set", JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_saveButtonActionPerformed

  private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
    int dialogResult = fileChooser.showOpenDialog(this);
    if (dialogResult != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File targetFile = fileChooser.getSelectedFile();
    if (!targetFile.exists()) {
      JOptionPane.showMessageDialog(this,
                                    bundle.getString("The_chosen_input_file_does_not_exist."),
                                    bundle.getString("File_does_not_exist"),
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      // unmarshal
      List<TransportOrderXMLStructure> xmlData = TransportOrderTableModel.fromFile(targetFile);
      List<TransportOrderData> newOrders = new ArrayList<>();
      for (TransportOrderXMLStructure curStruc : xmlData) {
        TransportOrderData data = new TransportOrderData();
        data.setName(curStruc.getName());
        switch (curStruc.getDeadline().toString()) {
          case "-5 min.":
            data.setDeadline(TransportOrderData.Deadline.MINUS_FIVE_MINUTES);
            break;
          case "5 min.":
            data.setDeadline(TransportOrderData.Deadline.PLUS_FIVE_MINUTES);
            break;
          case "-30 min.":
            data.setDeadline(TransportOrderData.Deadline.MINUS_HALF_HOUR);
            break;
          case "30 min.":
            data.setDeadline(TransportOrderData.Deadline.PLUS_HALF_HOUR);
            break;
          case " 1 h.":
            data.setDeadline(TransportOrderData.Deadline.PLUS_ONE_HOUR);
            break;
          case " 2 h.":
            data.setDeadline(TransportOrderData.Deadline.PLUS_TWO_HOURS);
            break;
          default:
            data.setDeadline(TransportOrderData.Deadline.PLUS_ONE_HOUR);
            break;
        }
        data.setIntendedVehicle(curStruc.getIntendedVehicle() == null ? null
            : objectService.fetchObject(Vehicle.class,
                                        curStruc.getIntendedVehicle()).getReference());
        for (TransportOrderXMLStructure.XMLMapEntry curEntry : curStruc.getProperties()) {
          data.addProperty(curEntry.getKey(), curEntry.getValue());
        }
        for (DriveOrderXMLStructure curDOXMLS : curStruc.getDriveOrders()) {
          DriveOrderStructure newDOS
              = new DriveOrderStructure(objectService.fetchObject(
                  Location.class, curDOXMLS.getDriveOrderLocation()).getReference(),
                                        curDOXMLS.getDriveOrderVehicleOperation());
          data.addDriveOrder(newDOS);
        }
        newOrders.add(data);
      }
      // clear tables
      doTable.setModel(new DriveOrderTableModel());
      propertyTable.setModel(new PropertyTableModel());
      TransportOrderTableModel model = new TransportOrderTableModel();
      for (TransportOrderData curData : newOrders) {
        model.addData(curData);
      }
      toTable.setModel(model);
    }
    catch (IOException | CredentialsException exc) {
      LOG.warn("Exception reading property set from " + targetFile.getPath(), exc);
      JOptionPane.showMessageDialog(this,
                                    "Exception reading property set:\n" + exc.getMessage(),
                                    "Exception reading property set",
                                    JOptionPane.ERROR_MESSAGE);
    }
    catch (NullPointerException e) {
      JOptionPane.showMessageDialog(this,
                                    "Objects in this file seem not to "
                                    + "appear in this model, please check your model.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }

    updateElementStates();
  }//GEN-LAST:event_openButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addDOButton;
  private javax.swing.JButton addPropertyButton;
  private javax.swing.JButton addToTOTableButton;
  private javax.swing.JButton deleteFromDOTableButton;
  private javax.swing.JButton deleteFromTOTableButton;
  private javax.swing.JTable doTable;
  private javax.swing.JPanel driveOrdersPanel;
  private javax.swing.JScrollPane driveOrdersScrollPane;
  private javax.swing.JRadioButton explicitOrderSpecButton;
  private javax.swing.JPanel explicitOrderSpecPanel;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.JLabel fillingLbl;
  private javax.swing.JLabel fillingLbl3;
  private javax.swing.JLabel fillingLbl4;
  private javax.swing.JLabel fillingLbl5;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JComboBox<TCSObjectReference<Location>> locationsComboBox;
  private javax.swing.JButton openButton;
  private javax.swing.JPanel openSavePanel;
  private javax.swing.JComboBox<String> operationTypesComboBox;
  private javax.swing.JCheckBox orderGenChkBox;
  private javax.swing.JPanel orderGenPanel;
  private javax.swing.JPanel orderProfilePanel;
  private javax.swing.ButtonGroup orderSpecButtonGroup;
  private javax.swing.JComboBox<String> propertyComboBox;
  private javax.swing.JPanel propertyPanel;
  private javax.swing.JTable propertyTable;
  private javax.swing.JLabel randomOrderCountLbl;
  private javax.swing.JSpinner randomOrderCountSpinner;
  private javax.swing.JLabel randomOrderSizeLbl;
  private javax.swing.JSpinner randomOrderSizeSpinner;
  private javax.swing.JRadioButton randomOrderSpecButton;
  private javax.swing.JPanel randomOrderSpecPanel;
  private javax.swing.JButton removePropertyButton;
  private javax.swing.JButton saveButton;
  private javax.swing.JRadioButton singleTriggerRadioButton;
  private javax.swing.JLabel thresholdOrdersLbl;
  private javax.swing.JSpinner thresholdSpinner;
  private javax.swing.JRadioButton thresholdTriggerRadioButton;
  private javax.swing.JLabel timerSecondsLbl;
  private javax.swing.JSpinner timerSpinner;
  private javax.swing.JRadioButton timerTriggerRadioButton;
  private javax.swing.JTable toTable;
  private javax.swing.JPanel transportOrderGenPanel;
  private javax.swing.JPanel transportOrdersActionPanel;
  private javax.swing.JPanel transportOrdersPanel;
  private javax.swing.ButtonGroup triggerButtonGroup;
  private javax.swing.JPanel triggerPanel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * Creates a new selection listener for the transport order table.
   */
  private class TOTableSelectionListener
      implements ListSelectionListener {

    /**
     * The transport order table.
     */
    private final JTable table;

    /**
     * Creates a new TOTableSelectionListener.
     *
     * @param table The transport order table
     */
    public TOTableSelectionListener(JTable table) {
      this.table = requireNonNull(table, "table");
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
        ListSelectionModel model = (ListSelectionModel) e.getSource();
        int row = model.getMinSelectionIndex();
        buildTableModels(row);
        updateElementStates();
      }
    }
  }
}
