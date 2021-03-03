/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.drivers.BasicCommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterFactory;
import org.opentcs.drivers.CommunicationAdapterRegistry;
import org.opentcs.drivers.SimCommunicationAdapter;
import org.opentcs.drivers.VehicleManager;
import org.opentcs.drivers.VehicleModel;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * A Frame containing all vehicles and detailed information.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DriverGUI
    extends JPanel
    implements KernelExtension {

  /**
   * This class's Logger.
   */
  private static final Logger log = Logger.getLogger(DriverGUI.class.getName());
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore =
      ConfigurationStore.getStore(DriverGUI.class.getName());
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle =
      ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");
  /**
   * Proxy kernel to communicate with.
   */
  private final LocalKernel kernel;
  /**
   * The list of vehicle models.
   */
  private final List<VehicleModel> vehicleModels = new LinkedList<>();
  /**
   * A flag indicating whether this KernelExtension has been plugged in already.
   */
  private boolean pluggedIn;
  /**
   * A flag indicating whether to attach adapters automatically on startup.
   */
  private boolean autoAttachOnStartup;
  /**
   * A flag indicating whether to enable adapters automatically on startup.
   */
  private boolean autoEnableOnStartup;

  /**
   * Creates a new DriverGUI.
   * 
   * @param kernel The kernel.
   */
  public DriverGUI(LocalKernel kernel) {
    if (kernel == null) {
      throw new NullPointerException("kernel is null");
    }
    this.kernel = kernel;
    initComponents();

    // Initialize detail panels.
    vehicleDetailPanel.add(new DetailPanel(""));

    // Auto-attach vehicles (if we should)
    autoAttachOnStartup = configStore.getBoolean("autoAttachOnStartup", false);
    autoEnableOnStartup = configStore.getBoolean("autoEnableOnStartup", false);
  }

  @Override
  public boolean isPluggedIn() {
    return pluggedIn;
  }

  @Override
  public void plugIn() {
    if (pluggedIn) {
      throw new IllegalStateException("Already plugged in.");
    }

    // Verify that the kernel is in a state in which controlling vehicles is
    // possible.
    Kernel.State kernelState = kernel.getState();
    if (!Kernel.State.OPERATING.equals(kernelState)) {
      throw new IllegalStateException("Cannot work in kernel state "
          + kernelState.name());
    }

    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        // Initialize list of vehicles.
        initVehicleModels();
        VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
        for (VehicleModel curModel : vehicleModels) {
          model.addData(curModel);
          curModel.addObserver(model);
        }

        if (model.getVehicleModels().isEmpty()) {
          vehicleTable.getComponentPopupMenu().setEnabled(false);
        }

        initAdapterComboBoxes();
        vehicleTable.setDragEnabled(true);

        if (autoAttachOnStartup) {
          autoAttachAll();
          // Auto-enable vehicles (if we should)
          if (autoEnableOnStartup) {
            enableAll();
          }
        }
        JFrame rootFrame = (JFrame) SwingUtilities.getRoot(DriverGUI.this);
        if (rootFrame.getExtendedState() == JFrame.NORMAL) {
          rootFrame.pack();
        }
      }
    });

    pluggedIn = true;
  }

  @Override
  public void plugOut() {
    if (!pluggedIn) {
      throw new IllegalStateException("Not plugged in.");
    }

    // Detach all attached drivers to clean up.
    detachOnExit();

    pluggedIn = false;
  }

  /**
   * Attaches an adapter to a vehicle.
   * 
   * @param vehicleModel The vehicleModel
   * @param factory The factory that shall be assigned
   */
  private void attachAdapterToVehicle(VehicleModel vehicleModel,
                                      CommunicationAdapterFactory factory) {
    if (vehicleModel == null) {
      return;
    }
    if (vehicleModel.hasCommunicationAdapter()) {
      vehicleModel.getCommunicationAdapter().disable();
      vehicleModel.removeCommunicationAdapter();
      if (vehicleModel.hasVehicleManager()) {
        try {
          kernel.getVehicleManagerPool().detachVehicleManager(
              vehicleModel.getName());
        }
        catch (IllegalArgumentException e) {
          // Don't do anything. As vehicleModel.setVehicleManager(null);
          // is not called an exception will be thrown here.
        }
        // Don't do this for now, though it would be correct. If we set the
        // manager to null here, we cannot reset the vehicle's position any
        // more.
//          vehicleModel.setVehicleManager(null);
      }
    }
    if (factory == null) {
      return;
    }

    BasicCommunicationAdapter commAdapter;
    VehicleManager vehicleManager;
    if (vehicleModel.hasCommunicationFactory()) {
      if (factory.getAdapterDescription().equals(
          vehicleModel.getCommunicationFactory().getAdapterDescription())) {
        return;
      }
    }
    commAdapter = factory.getAdapterFor(vehicleModel.getVehicle());
    if (commAdapter == null) {
      return;
    }

    commAdapter.setVehicleModel(vehicleModel);
    vehicleManager = kernel.getVehicleManagerPool().getVehicleManager(
        vehicleModel.getName(), commAdapter);
    vehicleModel.setVehicleManager(vehicleManager);
    vehicleModel.setCommunicationAdapter(commAdapter);
    vehicleModel.setCommunicationFactory(factory);
    kernel.setTCSObjectProperty(vehicleModel.getVehicle().getReference(),
                                Vehicle.PREFERRED_ADAPTER,
                                factory.getClass().getName());

    // Set initial vehicle position if related property is set
    if (commAdapter instanceof SimCommunicationAdapter) {
      SimCommunicationAdapter simCommAdapter;
      simCommAdapter = (SimCommunicationAdapter) commAdapter;
      Vehicle vehicle = vehicleModel.getVehicle();
      String initialPos = vehicle.getProperties().get(
          ObjectPropConstants.VEHICLE_INITIAL_POSITION);
      if (initialPos != null) {
        simCommAdapter.initVehiclePosition(initialPos);
      }
    }
  }

  /**
   * Disables all adapters when exiting.
   */
  private void detachOnExit() {
    log.info("Detaching communication adapters...");
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> listModel = model.getVehicleModels();
    for (int i = 0; i < listModel.size(); i++) {
      VehicleModel vehicleModel = listModel.get(i);
      if (vehicleModel.hasCommunicationAdapter()) {
        vehicleModel.getCommunicationAdapter().disable();
      }
    }
    log.info("Detached communication adapters");
  }

  /**
   * Auto attaches adapters to every vehicle.
   */
  private void autoAttachAll() {
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> listModel = model.getVehicleModels();
    CommunicationAdapterRegistry reg = kernel.getCommAdapterRegistry();
    for (int i = 0; i < listModel.size(); i++) {
      VehicleModel vehicleModel = listModel.get(i);
      if (!vehicleModel.hasCommunicationAdapter()) {
        Vehicle veh = getUpdatedVehicle(vehicleModel.getVehicle());
        String preferedAdapter = null;
        if (veh != null) {
          preferedAdapter = veh.getProperties().get(Vehicle.PREFERRED_ADAPTER);
        }
        boolean foundFactory = false;
        if (preferedAdapter != null) {
          for (CommunicationAdapterFactory factory : reg.getFactories()) {
            if (preferedAdapter.equals(factory.getClass().getName())) {
              attachAdapterToVehicle(vehicleModel, factory);
              foundFactory = true;
              break;
            }
          }
          if (!foundFactory) {
            log.info("Couldn't autoattach prefered adapter "
                + preferedAdapter + " to " + vehicleModel.getName()
                + ". Such an adapter doesn't exist.");
          }
        }
        if (!foundFactory) {
          List<CommunicationAdapterFactory> factories =
              reg.findFactoriesFor(vehicleModel.getVehicle());
          // Attach the first adapter that is available.
          if (!factories.isEmpty()) {
            attachAdapterToVehicle(vehicleModel, factories.get(0));
          }
        }
      }
    }
  }

  /**
   * Returns an updated version of a vehicle.
   * 
   * @param oldVehicle The old vehicle.
   * @return The updated vehicle.
   */
  private Vehicle getUpdatedVehicle(Vehicle oldVehicle) {
    Set<Vehicle> vehicles = new TreeSet<>(TCSObject.nameComparator);
    vehicles.addAll(kernel.getTCSObjects(Vehicle.class));
    for (Vehicle curVeh : vehicles) {
      if (curVeh.getName().equals(oldVehicle.getName())) {
        return curVeh;
      }
    }
    return null;
  }

  /**
   * Enables an attached adapter for all
   * vehicles in the gui list.
   */
  private void enableAll() {
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> listModel = model.getVehicleModels();
    for (int i = 0; i < listModel.size(); i++) {
      VehicleModel vehicleModel = listModel.get(i);
      if (vehicleModel.hasCommunicationAdapter()) {
        if (!vehicleModel.getCommunicationAdapter().isEnabled()) {
          vehicleModel.getCommunicationAdapter().enable();
        }
      }
    }
  }

  /**
   * Enables an attached adapter for all
   * selected vehicles in the gui list.
   */
  private void enableAllSelected() {
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }

    for (VehicleModel vehicleModel : selectedVehicleModels) {
      if (vehicleModel.hasCommunicationAdapter()) {
        if (!vehicleModel.getCommunicationAdapter().isEnabled()) {
          vehicleModel.getCommunicationAdapter().enable();
        }
      }
    }
  }

  /**
   * Disables an attached adapter for all
   * vehicles in the gui list.
   */
  private void disableAll() {
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> listModel = model.getVehicleModels();
    for (int i = 0; i < listModel.size(); i++) {
      VehicleModel vehicleModel = listModel.get(i);
      if (vehicleModel.hasCommunicationAdapter()) {
        if (vehicleModel.getCommunicationAdapter().isEnabled()) {
          vehicleModel.getCommunicationAdapter().disable();
        }
      }
    }
  }

  /**
   * Disables an attached adapter for all
   * selected vehicles in the gui list.
   */
  private void disableAllSelected() {
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }
    for (VehicleModel vehicleModel : selectedVehicleModels) {
      if (vehicleModel.hasCommunicationAdapter()) {
        if (vehicleModel.getCommunicationAdapter().isEnabled()) {
          vehicleModel.getCommunicationAdapter().disable();
        }
      }
    }
  }

  /**
   * Initializes the combo boxes with available adapters for every vehicle.
   */
  private void initAdapterComboBoxes() {
    SingleCellEditor adapterCellEditor = new SingleCellEditor(vehicleTable);
    final SingleCellEditor pointsCellEditor = new SingleCellEditor(vehicleTable);

    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> vehicles = model.getVehicleModels();

    for (int i = 0; i < vehicles.size(); i++) {
      VehicleModel currentVehicle = vehicles.get(i);
      List<CommunicationAdapterFactory> factories = new LinkedList<>();
      factories.add(null);
      factories.addAll(kernel.getCommAdapterRegistry().findFactoriesFor(currentVehicle.getVehicle()));

      final WideComboBox<CommunicationAdapterFactory> comboBox =
          new WideComboBox<>();
      for (CommunicationAdapterFactory currentFactory : factories) {
        comboBox.addItem(currentFactory);
      }
      comboBox.setSelectedIndex(0);
      currentVehicle.addObserver(comboBox);
      comboBox.setRenderer(new AdapterFactoryCellRenderer());

      comboBox.addItemListener(new java.awt.event.ItemListener() {
        @Override
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
          CommunicationAdapterFactory factory = (CommunicationAdapterFactory) comboBox.getSelectedItem();
          VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
          final VehicleModel selectedVehicle = model.getDataAt(vehicleTable.getSelectedRow());
          attachAdapterToVehicle(selectedVehicle, factory);
        }
      });
      initPointsComboBox(i, pointsCellEditor);
      adapterCellEditor.setEditorAt(i, new DefaultCellEditor(comboBox));
    }

    vehicleTable.getColumn("Adapter").setCellEditor(adapterCellEditor);
    vehicleTable.getColumn("Position").setCellEditor(pointsCellEditor);
  }

  /**
   * If a loopback adapter was chosen this method initializes the combo boxes
   * with positions the user can set the vehicle to.
   *
   * @param i An index indicating which row this combo box belongs to
   * @param pointsCellEditor The <code>SingleCellEditor</code> containing
   * the combo boxes.
   */
  private void initPointsComboBox(int i, SingleCellEditor pointsCellEditor) {
    Set<Point> points = new TreeSet<>(TCSObject.nameComparator);
    points.addAll(kernel.getTCSObjects(Point.class));
    final JComboBox<Point> pointComboBox = new JComboBox<>();

    for (Point currentPoint : points) {
      pointComboBox.addItem(currentPoint);
    }
    pointComboBox.setSelectedIndex(-1);

    pointComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        Point newPoint = (Point) e.getItem();
        VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
        VehicleModel vehicle = model.getDataAt(vehicleTable.getSelectedRow());
        if (vehicle.getCommunicationAdapter() instanceof SimCommunicationAdapter) {
          SimCommunicationAdapter adapter =
              (SimCommunicationAdapter) vehicle.getCommunicationAdapter();
          adapter.initVehiclePosition(newPoint.getName());
        }
        else {
          log.warning("Communication adapter of vehicle " + vehicle.getName()
              + " does not implement SimCommunicationAdapter.");
        }
      }
    });
    pointsCellEditor.setEditorAt(i, new DefaultCellEditor(pointComboBox));
  }

  /**
   * Enables or disables auto attaching adapters on startup.
   * 
   * @param autoAttachOnStartup <code>true</code> to enable, <code>false</code>
   * to disable
   */
  private void setAutoAttachOnStartup(boolean autoAttachOnStartup) {
    this.autoAttachOnStartup = autoAttachOnStartup;
    configStore.setBoolean("autoAttachOnStartup", autoAttachOnStartup);
  }

  /**
   * Enables or disables auto enabling adapters on startup.
   * 
   * @param autoEnableOnStartup <code>true</code> to enable, <code>false</code>
   * to disable
   */
  private void setAutoEnableOnStartup(boolean autoEnableOnStartup) {
    this.autoEnableOnStartup = autoEnableOnStartup;
    configStore.setBoolean("autoEnableOnStartup", autoEnableOnStartup);
  }

  /**
   * Resets selected vehicles' positions to null.
   */
  private void resetSelectedVehiclePositions() {
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }
    for (VehicleModel vehicleModel : selectedVehicleModels) {
      // If the vehicle has a communication adapter and it's disabled, reset the
      // position with it.
      if (vehicleModel.hasCommunicationAdapter()) {
        vehicleModel.getCommunicationAdapter().setVehiclePosition(null);
      }
      // If the vehicle doesn't have a communication adapter, reset the position
      // directly with the vehicle manager.
      else if (vehicleModel.hasVehicleManager()) {
        vehicleModel.getVehicleManager().setVehiclePosition(null);
      }
      else {
        log.warning("Cannot reset position without comm adapter or manager");
      }
    }
  }

  /**
   * Initializes all available vehicle models.
   */
  private void initVehicleModels() {
    vehicleModels.clear();
    Set<Vehicle> vehicles = new TreeSet<>(TCSObject.nameComparator);
    vehicles.addAll(kernel.getTCSObjects(Vehicle.class));
    for (Vehicle i : vehicles) {
      vehicleModels.add(new VehicleModel(i));
    }
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

    vehicleListPopupMenu = new javax.swing.JPopupMenu();
    driverMenu = new javax.swing.JMenu();
    noDriversMenuItem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    autoAttachAllMenuItem = new javax.swing.JMenuItem();
    autoAttachSelectedMenuItem = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JSeparator();
    detachAllMenuItem = new javax.swing.JMenuItem();
    detachAllSelectedMenuItem = new javax.swing.JMenuItem();
    jSeparator3 = new javax.swing.JSeparator();
    enableAllMenuItem = new javax.swing.JMenuItem();
    enableAllSelectedMenuItem = new javax.swing.JMenuItem();
    jSeparator4 = new javax.swing.JSeparator();
    disableAllMenuItem = new javax.swing.JMenuItem();
    disableAllSelectedMenuItem = new javax.swing.JMenuItem();
    jSeparator5 = new javax.swing.JSeparator();
    resetVehiclePositionMenuItem = new javax.swing.JMenuItem();
    jSeparator6 = new javax.swing.JSeparator();
    startupMenu = new javax.swing.JMenu();
    startupAutoAttachMenuItem = new javax.swing.JCheckBoxMenuItem();
    startupAutoEnableMenuItem = new javax.swing.JCheckBoxMenuItem();
    listDisplayPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    vehicleTable = new javax.swing.JTable();
    vehicleDetailPanel = new javax.swing.JPanel();

    vehicleListPopupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
      public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
      }
      public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
      }
      public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
        vehicleListPopupMenuPopupMenuWillBecomeVisible(evt);
      }
    });

    driverMenu.setText(bundle.getString("Driver")); // NOI18N
    driverMenu.addMenuListener(new javax.swing.event.MenuListener() {
      public void menuCanceled(javax.swing.event.MenuEvent evt) {
      }
      public void menuDeselected(javax.swing.event.MenuEvent evt) {
      }
      public void menuSelected(javax.swing.event.MenuEvent evt) {
        driverMenuMenuSelected(evt);
      }
    });

    noDriversMenuItem.setText("No drivers available.");
    noDriversMenuItem.setEnabled(false);
    driverMenu.add(noDriversMenuItem);

    vehicleListPopupMenu.add(driverMenu);
    vehicleListPopupMenu.add(jSeparator1);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle"); // NOI18N
    autoAttachAllMenuItem.setText(bundle.getString("AutoAttachAll")); // NOI18N
    autoAttachAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        autoAttachAllMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(autoAttachAllMenuItem);

    autoAttachSelectedMenuItem.setText(bundle.getString("AutoAttachSelected")); // NOI18N
    autoAttachSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        autoAttachSelectedMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(autoAttachSelectedMenuItem);
    vehicleListPopupMenu.add(jSeparator2);

    detachAllMenuItem.setText(bundle.getString("DetachAll")); // NOI18N
    detachAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        detachAllMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(detachAllMenuItem);

    detachAllSelectedMenuItem.setText(bundle.getString("DetachSelected")); // NOI18N
    detachAllSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        detachAllSelectedMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(detachAllSelectedMenuItem);
    vehicleListPopupMenu.add(jSeparator3);

    enableAllMenuItem.setText(bundle.getString("EnableAll")); // NOI18N
    enableAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        enableAllMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(enableAllMenuItem);

    enableAllSelectedMenuItem.setText(bundle.getString("EnableSelected")); // NOI18N
    enableAllSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        enableAllSelectedMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(enableAllSelectedMenuItem);
    vehicleListPopupMenu.add(jSeparator4);

    disableAllMenuItem.setText(bundle.getString("DisableAll")); // NOI18N
    disableAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        disableAllMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(disableAllMenuItem);

    disableAllSelectedMenuItem.setText(bundle.getString("DisableSelected")); // NOI18N
    disableAllSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        disableAllSelectedMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(disableAllSelectedMenuItem);
    vehicleListPopupMenu.add(jSeparator5);

    resetVehiclePositionMenuItem.setText(bundle.getString("ResetVehiclePosition")); // NOI18N
    resetVehiclePositionMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetVehiclePositionMenuItemActionPerformed(evt);
      }
    });
    vehicleListPopupMenu.add(resetVehiclePositionMenuItem);
    vehicleListPopupMenu.add(jSeparator6);

    startupMenu.setText(bundle.getString("StartupOptions")); // NOI18N
    startupMenu.addMenuListener(new javax.swing.event.MenuListener() {
      public void menuCanceled(javax.swing.event.MenuEvent evt) {
      }
      public void menuDeselected(javax.swing.event.MenuEvent evt) {
      }
      public void menuSelected(javax.swing.event.MenuEvent evt) {
        startupMenuMenuSelected(evt);
      }
    });

    startupAutoAttachMenuItem.setSelected(true);
    startupAutoAttachMenuItem.setText(bundle.getString("AutoAttachAll")); // NOI18N
    startupAutoAttachMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        startupAutoAttachMenuItemActionPerformed(evt);
      }
    });
    startupMenu.add(startupAutoAttachMenuItem);

    startupAutoEnableMenuItem.setSelected(true);
    startupAutoEnableMenuItem.setText(bundle.getString("EnableAll")); // NOI18N
    startupAutoEnableMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        startupAutoEnableMenuItemActionPerformed(evt);
      }
    });
    startupMenu.add(startupAutoEnableMenuItem);

    vehicleListPopupMenu.add(startupMenu);

    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

    listDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ModelView"))); // NOI18N
    listDisplayPanel.setMaximumSize(new java.awt.Dimension(464, 2147483647));
    listDisplayPanel.setMinimumSize(new java.awt.Dimension(464, 425));
    listDisplayPanel.setLayout(new java.awt.BorderLayout());

    vehicleTable.setModel(new VehicleTableModel());
    vehicleTable.setComponentPopupMenu(vehicleListPopupMenu);
    vehicleTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        vehicleTableMouseClicked(evt);
      }
    });
    jScrollPane1.setViewportView(vehicleTable);

    listDisplayPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    add(listDisplayPanel);

    vehicleDetailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DetailView"))); // NOI18N
    vehicleDetailPanel.setPreferredSize(new java.awt.Dimension(800, 23));
    vehicleDetailPanel.setLayout(new java.awt.BorderLayout());
    add(vehicleDetailPanel);
    vehicleDetailPanel.getAccessibleContext().setAccessibleName(bundle.getString("DetailView")); // NOI18N

    getAccessibleContext().setAccessibleName(bundle.getString("driverGUI")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

  private void driverMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_driverMenuMenuSelected
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }
    driverMenu.removeAll();
    CommunicationAdapterRegistry reg = kernel.getCommAdapterRegistry();
    List<CommunicationAdapterFactory> factories = reg.getFactories();
    for (CommunicationAdapterFactory factory : factories) {
      boolean enabled = true;
      List<VehicleModel> vehiclesToAttach = new LinkedList<>();
      for (VehicleModel selectedVehicleModel : selectedVehicleModels) {
        if (!factory.providesAdapterFor(selectedVehicleModel.getVehicle())) {
          enabled = false;
          vehiclesToAttach.clear();
          break;
        }
        else if (selectedVehicleModel.getCommunicationAdapter() == null) {
          vehiclesToAttach.add(selectedVehicleModel);
        }
      }
      Action action = new AttachCommAdapterAction(factory.getAdapterDescription(),
                                                  vehiclesToAttach,
                                                  factory);
      JMenuItem menuItem = driverMenu.add(action);
      menuItem.setEnabled(enabled && !vehiclesToAttach.isEmpty());
    }
  }//GEN-LAST:event_driverMenuMenuSelected

  private void autoAttachAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoAttachAllMenuItemActionPerformed
    autoAttachAll();
  }//GEN-LAST:event_autoAttachAllMenuItemActionPerformed

  private void autoAttachSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoAttachSelectedMenuItemActionPerformed
    List<VehicleModel> selectedValues = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedValues.add(model.getDataAt(selectedTableValues[i]));
    }

    CommunicationAdapterRegistry reg = kernel.getCommAdapterRegistry();
    for (VehicleModel vehicleModel : selectedValues) {
      if (!vehicleModel.hasCommunicationAdapter()) {
        Vehicle veh = getUpdatedVehicle(vehicleModel.getVehicle());
        String preferedAdapter = null;
        if (veh != null) {
          preferedAdapter = veh.getProperties().get(Vehicle.PREFERRED_ADAPTER);
        }
        boolean foundFactory = false;
        if (preferedAdapter != null) {
          for (CommunicationAdapterFactory factory : reg.getFactories()) {
            if (preferedAdapter.equals(factory.getClass().getName())) {
              attachAdapterToVehicle(vehicleModel, factory);
              foundFactory = true;
              model.update(vehicleModel, null);
              break;
            }
          }
          if (!foundFactory) {
            log.info("Couldn't autoattach prefered adapter "
                + preferedAdapter + " to " + vehicleModel.getName()
                + ". Such an adapter doesn't exist.");
          }
        }
        if (!foundFactory) {
          List<CommunicationAdapterFactory> factories =
              reg.findFactoriesFor(vehicleModel.getVehicle());
          // Attach the first adapter that is available.
          if (!factories.isEmpty()) {
            attachAdapterToVehicle(vehicleModel, factories.get(0));
          }
        }
      }
    }
  }//GEN-LAST:event_autoAttachSelectedMenuItemActionPerformed

  private void detachAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detachAllMenuItemActionPerformed
    log.finer("method entry");
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    List<VehicleModel> listModel = model.getVehicleModels();
    for (int i = 0; i < listModel.size(); i++) {
      VehicleModel vehicleModel = listModel.get(i);
      if (vehicleModel.hasCommunicationAdapter()) {
        vehicleModel.getCommunicationAdapter().disable();
        vehicleModel.removeCommunicationAdapter();
      }
      if (vehicleModel.hasVehicleManager()) {
        try {
          kernel.getVehicleManagerPool().detachVehicleManager(
              vehicleModel.getName());
        }
        catch (IllegalArgumentException e) {
          // Don't do anything. As vehicleModel.setVehicleManager(null);
          // is not called an exception will be thrown here.
        }
        // Don't do this for now, though it would be correct. If we set the
        // manager to null here, we cannot reset the vehicle's position any
        // more.
//          vehicleModel.setVehicleManager(null);
      }
    }
    log.fine("method exit");
  }//GEN-LAST:event_detachAllMenuItemActionPerformed

  private void detachAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detachAllSelectedMenuItemActionPerformed
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }
    for (VehicleModel vehicleModel : selectedVehicleModels) {
      if (vehicleModel.hasCommunicationAdapter()) {
        vehicleModel.getCommunicationAdapter().disable();
        vehicleModel.removeCommunicationAdapter();
      }
      if (vehicleModel.hasVehicleManager()) {
        try {
          kernel.getVehicleManagerPool().detachVehicleManager(
              vehicleModel.getName());
        }
        catch (IllegalArgumentException e) {
          // Don't do anything. As vehicleModel.setVehicleManager(null);
          // is not called an exception will be thrown here.
        }
        // Don't do this for now, though it would be correct. If we set the
        // manager to null here, we cannot reset the vehicle's position any
        // more.
//          vehicleModel.setVehicleManager(null);
      }
    }
  }//GEN-LAST:event_detachAllSelectedMenuItemActionPerformed

  private void enableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllMenuItemActionPerformed
    enableAll();
  }//GEN-LAST:event_enableAllMenuItemActionPerformed

  private void enableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllSelectedMenuItemActionPerformed
    enableAllSelected();
  }//GEN-LAST:event_enableAllSelectedMenuItemActionPerformed

  private void disableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllMenuItemActionPerformed
    disableAll();
  }//GEN-LAST:event_disableAllMenuItemActionPerformed

  private void disableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllSelectedMenuItemActionPerformed
    disableAllSelected();
  }//GEN-LAST:event_disableAllSelectedMenuItemActionPerformed

  private void resetVehiclePositionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetVehiclePositionMenuItemActionPerformed
    resetSelectedVehiclePositions();
  }//GEN-LAST:event_resetVehiclePositionMenuItemActionPerformed

  private void startupAutoAttachMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupAutoAttachMenuItemActionPerformed
    setAutoAttachOnStartup(startupAutoAttachMenuItem.isSelected());
  }//GEN-LAST:event_startupAutoAttachMenuItemActionPerformed

  private void startupAutoEnableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupAutoEnableMenuItemActionPerformed
    setAutoEnableOnStartup(startupAutoEnableMenuItem.isSelected());
  }//GEN-LAST:event_startupAutoEnableMenuItemActionPerformed

  private void startupMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_startupMenuMenuSelected
    startupAutoAttachMenuItem.setSelected(autoAttachOnStartup);
    startupAutoEnableMenuItem.setSelected(autoEnableOnStartup);
    startupAutoEnableMenuItem.setEnabled(autoAttachOnStartup);
  }//GEN-LAST:event_startupMenuMenuSelected

  private void vehicleListPopupMenuPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_vehicleListPopupMenuPopupMenuWillBecomeVisible
    int attachedCount = 0;
    int detachedCount = 0;
    int enabledCount = 0;
    int disabledCount = 0;
    boolean resetAll = false;
    // Find out how many vehicles (don't) have a driver attached.
    for (VehicleModel vehicleModel : vehicleModels) {
      if (vehicleModel.getCommunicationAdapter() == null) {
        detachedCount++;
      }
      else {
        attachedCount++;
        if (vehicleModel.getCommunicationAdapter().isEnabled()) {
          enabledCount++;
        }
        else {
          disabledCount++;
        }
      }
    }
    detachAllMenuItem.setEnabled(attachedCount > 0);
    autoAttachAllMenuItem.setEnabled(detachedCount > 0);
    enableAllMenuItem.setEnabled(disabledCount > 0);
    disableAllMenuItem.setEnabled(enabledCount > 0);
    // Now do the same for those that are selected.
    attachedCount = 0;
    detachedCount = 0;
    enabledCount = 0;
    disabledCount = 0;
    List<VehicleModel> selectedVehicleModels = new LinkedList<>();
    int[] selectedTableValues = vehicleTable.getSelectedRows();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int i = 0; i < selectedTableValues.length; i++) {
      selectedVehicleModels.add(model.getDataAt(selectedTableValues[i]));
    }
    for (VehicleModel vehicleModel : selectedVehicleModels) {
      if (!vehicleModel.hasCommunicationAdapter()) {
        detachedCount++;
      }
      else {
        attachedCount++;
        if (vehicleModel.getCommunicationAdapter().isEnabled()) {
          enabledCount++;
        }
        else {
          disabledCount++;
        }
      }
    }
    for (VehicleModel vehicleModel : selectedVehicleModels) {
      if (vehicleModel.hasCommunicationAdapter()) {
        if (vehicleModel.getCommunicationAdapter().isEnabled()) {
          resetAll = false;
          break;
        }
        else {
          resetAll = true;
        }
      }
      else {
        resetAll = true;
      }
    }
    detachAllSelectedMenuItem.setEnabled(attachedCount > 0);
    autoAttachSelectedMenuItem.setEnabled(detachedCount > 0);
    enableAllSelectedMenuItem.setEnabled(disabledCount > 0);
    disableAllSelectedMenuItem.setEnabled(enabledCount > 0);
    resetVehiclePositionMenuItem.setEnabled(resetAll);
  }//GEN-LAST:event_vehicleListPopupMenuPopupMenuWillBecomeVisible

  private void vehicleTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleTableMouseClicked
    if (evt.getClickCount() == 2) {
      int index = vehicleTable.getSelectedRow();
      if (index >= 0) {
        VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
        VehicleModel clickedModel = model.getDataAt(index);
        DetailPanel detailPanel = (DetailPanel) vehicleDetailPanel.getComponent(0);
        detailPanel.attachToVehicle(clickedModel);
      }
    }
  }//GEN-LAST:event_vehicleTableMouseClicked
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem autoAttachAllMenuItem;
  private javax.swing.JMenuItem autoAttachSelectedMenuItem;
  private javax.swing.JMenuItem detachAllMenuItem;
  private javax.swing.JMenuItem detachAllSelectedMenuItem;
  private javax.swing.JMenuItem disableAllMenuItem;
  private javax.swing.JMenuItem disableAllSelectedMenuItem;
  private javax.swing.JMenu driverMenu;
  private javax.swing.JMenuItem enableAllMenuItem;
  private javax.swing.JMenuItem enableAllSelectedMenuItem;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JSeparator jSeparator4;
  private javax.swing.JSeparator jSeparator5;
  private javax.swing.JSeparator jSeparator6;
  private javax.swing.JPanel listDisplayPanel;
  private javax.swing.JMenuItem noDriversMenuItem;
  private javax.swing.JMenuItem resetVehiclePositionMenuItem;
  private javax.swing.JCheckBoxMenuItem startupAutoAttachMenuItem;
  private javax.swing.JCheckBoxMenuItem startupAutoEnableMenuItem;
  private javax.swing.JMenu startupMenu;
  private javax.swing.JPanel vehicleDetailPanel;
  private javax.swing.JPopupMenu vehicleListPopupMenu;
  private javax.swing.JTable vehicleTable;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * Attaches an adapter to a vehicle when performed.
   */
  private final class AttachCommAdapterAction
      extends AbstractAction {

    /**
     * The vehicle's model.
     */
    private final List<VehicleModel> vehicleModelList;
    /**
     * The factory providing the communication adapter.
     */
    private final CommunicationAdapterFactory factory;

    /**
     * Creates a new AttachCommAdapterAction.
     *
     * @param description A string describing the factory.
     * @param vehicleModelList The vehicle's model list.
     * @param factory The factory providing the communication adapter.
     */
    private AttachCommAdapterAction(String description,
                                    List<VehicleModel> vehicleModelList,
                                    CommunicationAdapterFactory factory) {
      super(description);
      assert vehicleModelList != null;
      assert factory != null;
      this.vehicleModelList = vehicleModelList;
      this.factory = factory;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
      for (VehicleModel vehicleModel : vehicleModelList) {
        attachAdapterToVehicle(vehicleModel, factory);
      }
    }
  }
}
