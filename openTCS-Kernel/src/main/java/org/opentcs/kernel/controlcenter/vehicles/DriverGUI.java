/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import static com.google.common.base.Preconditions.checkState;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.SimVehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.util.Comparators;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.TCSObjectNameListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

/**
 * A Frame containing all vehicles and detailed information.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DriverGUI
    extends ControlCenterPanel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DriverGUI.class);
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore CONFIG_STORE
      = ConfigurationStore.getStore(DriverGUI.class.getName());
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle");
  /**
   * Proxy kernel to communicate with.
   */
  private final LocalKernel kernel;
  /**
   * The comm adapter registry.
   */
  private final VehicleCommAdapterRegistry commAdapterRegistry;
  /**
   * Manages attachment of drivers with vehicles.
   */
  private final AttachmentManager attachManager;
  /**
   * The list of vehicle entries.
   */
  private final List<VehicleEntry> vehicleEntries = new LinkedList<>();
  /**
   * A flag indicating whether this KernelExtension has been plugged in already.
   */
  private boolean initialized;
  /**
   * A flag indicating whether to attach adapters automatically on startup.
   */
  private boolean autoAttachOnStartup;
  /**
   * A flag indicating whether to enable adapters automatically on startup.
   */
  private boolean autoEnableOnStartup;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param attachManager The attachment manager.
   * @param commAdapterRegistry The comm adapter registry.
   */
  @Inject
  public DriverGUI(@Nonnull LocalKernel kernel,
                   @Nonnull AttachmentManager attachManager,
                   @Nonnull VehicleCommAdapterRegistry commAdapterRegistry) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.attachManager = requireNonNull(attachManager, "attachManager");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");

    initComponents();

    vehicleTable.setDefaultRenderer(VehicleCommAdapterFactory.class,
                                    new VehicleCommAdapterFactoryTableCellRenderer());
    // Initialize detail panels.
    vehicleDetailPanel.add(new DetailPanel());

    // Auto-attach vehicles (if we should)
    autoAttachOnStartup = CONFIG_STORE.getBoolean("autoAttachOnStartup", false);
    autoEnableOnStartup = CONFIG_STORE.getBoolean("autoEnableOnStartup", false);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }

    // Verify that the kernel is in a state in which controlling vehicles is
    // possible.
    Kernel.State kernelState = kernel.getState();
    checkState(Kernel.State.OPERATING.equals(kernelState),
               "Cannot work in kernel state %s",
               kernelState);

    commAdapterRegistry.initialize();

    EventQueue.invokeLater(() -> {
      initVehicleModels();
      initVehicleList();
      initAutoAttach();
    });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }

    // Detach all attached drivers to clean up.
    detachOnExit();

    commAdapterRegistry.terminate();

    initialized = false;
  }

  /**
   * Disables all adapters when exiting.
   */
  private void detachOnExit() {
    LOG.info("Detaching vehicle communication adapters...");
    for (VehicleEntry entry : vehicleEntries) {
      attachManager.detachAdapterFromVehicle(entry, false);
    }
    LOG.info("Detached vehicle communication adapters");
  }

  /**
   * Auto attaches adapters to every vehicle.
   */
  private void autoAttachAll() {
    for (VehicleEntry entry : vehicleEntries) {
      attachManager.autoAttachAdapterToVehicle(entry);
    }
  }

  private void autoAttachSelected() {
    for (int selectedRowIndex : vehicleTable.getSelectedRows()) {
      attachManager.autoAttachAdapterToVehicle(vehicleEntries.get(selectedRowIndex));
    }
  }

  /**
   * Enables an attached adapter for all vehicles in the list.
   */
  private void enableCommAdaptersForAll() {
    enableCommAdapters(vehicleEntries);
  }

  /**
   * Enables an attached adapter for all selected vehicles in the list.
   */
  private void enableCommAdaptersForSelection() {
    List<VehicleEntry> selectedEntries = new LinkedList<>();
    for (int selectedIndex : vehicleTable.getSelectedRows()) {
      selectedEntries.add(vehicleEntries.get(selectedIndex));
    }

    enableCommAdapters(selectedEntries);
  }

  private void enableCommAdapters(List<VehicleEntry> selectedEntries) {
    selectedEntries.stream()
        .map(entry -> entry.getCommAdapter())
        .filter(adapter -> adapter != null)
        .filter(adapter -> !adapter.isEnabled())
        .forEach(adapter -> adapter.enable());
  }

  /**
   * Disables an attached adapter for all vehicles in the list.
   */
  private void disableCommAdaptersForAll() {
    disableCommAdapters(vehicleEntries);
  }

  /**
   * Disables an attached adapter for all selected vehicles in the list.
   */
  private void disableCommAdaptersForSelection() {
    List<VehicleEntry> selectedEntries = new LinkedList<>();
    for (int selectedIndex : vehicleTable.getSelectedRows()) {
      selectedEntries.add(vehicleEntries.get(selectedIndex));
    }

    disableCommAdapters(selectedEntries);
  }

  private void disableCommAdapters(List<VehicleEntry> selectedEntries) {
    selectedEntries.stream()
        .filter(entry -> entry.getCommAdapter() != null)
        .filter(entry -> entry.getCommAdapter().isEnabled())
        .forEach(entry -> entry.getCommAdapter().disable());
  }

  /**
   * Initializes the combo boxes with available adapters for every vehicle.
   */
  private void initAdapterComboBoxes() {
    SingleCellEditor adapterCellEditor = new SingleCellEditor(vehicleTable);
    SingleCellEditor pointsCellEditor = new SingleCellEditor(vehicleTable);

    for (int rowIndex = 0; rowIndex < vehicleEntries.size(); rowIndex++) {
      initCommAdaptersComboBox(rowIndex, adapterCellEditor);
      initPointsComboBox(rowIndex, pointsCellEditor);
    }

    vehicleTable.getColumn("Adapter").setCellEditor(adapterCellEditor);
    vehicleTable.getColumn("Position").setCellEditor(pointsCellEditor);
  }

  private void initCommAdaptersComboBox(int rowIndex, SingleCellEditor adapterCellEditor) {
    VehicleEntry currentEntry = vehicleEntries.get(rowIndex);

    final CommAdapterComboBox comboBox = new CommAdapterComboBox();
    comboBox.addItem(new NullVehicleCommAdapterFactory());
    commAdapterRegistry.findFactoriesFor(currentEntry.getVehicle())
        .forEach(factory -> comboBox.addItem(factory));
    comboBox.setSelectedIndex(0);
    currentEntry.addPropertyChangeListener(comboBox);
    comboBox.setRenderer(new AdapterFactoryCellRenderer());

    comboBox.addItemListener((ItemEvent evt) -> {
      if (evt.getStateChange() == ItemEvent.DESELECTED) {
        return;
      }
      // XXX We currently have to check if any row is selected because this action listener can be
      // triggered be the "auto-attach all" function, too, which would cause an exception in
      // vehicleTable.getSelectedRow().
      // Since this effectively detaches a newly-created comm adapter and then attaches a new one of
      // the same type, a better solution should be found.
      if (vehicleTable.getSelectedRowCount() > 0) {
        VehicleCommAdapterFactory factory = (VehicleCommAdapterFactory) comboBox.getSelectedItem();
        if (factory instanceof NullVehicleCommAdapterFactory) {
          // If the user has selected the empty entry from the combo box, just detach any comm
          // adapter from the vehicle.
          attachManager.detachAdapterFromVehicle(currentEntry, true);
        }
        else {
          // If the user has actually selected a new adapter to be attached, do it.
          attachManager.attachAdapterToVehicle(vehicleEntries.get(vehicleTable.getSelectedRow()),
                                               factory);
        }
      }
    });
    adapterCellEditor.setEditorAt(rowIndex, new DefaultCellEditor(comboBox));
  }

  /**
   * If a loopback adapter was chosen, this method initializes the combo boxes
   * with positions the user can set the vehicle to.
   *
   * @param rowIndex An index indicating which row this combo box belongs to
   * @param pointsCellEditor The <code>SingleCellEditor</code> containing
   * the combo boxes.
   */
  private void initPointsComboBox(int rowIndex, SingleCellEditor pointsCellEditor) {
    final JComboBox<Point> pointComboBox = new JComboBox<>();

    kernel.getTCSObjects(Point.class).stream()
        .sorted(Comparators.objectsByName())
        .forEach(point -> pointComboBox.addItem(point));
    pointComboBox.setSelectedIndex(-1);
    pointComboBox.setRenderer(new TCSObjectNameListCellRenderer());

    pointComboBox.addItemListener((ItemEvent e) -> {
      Point newPoint = (Point) e.getItem();
      VehicleEntry entry = vehicleEntries.get(vehicleTable.getSelectedRow());
      if (entry.getCommAdapter() instanceof SimVehicleCommAdapter) {
        SimVehicleCommAdapter adapter = (SimVehicleCommAdapter) entry.getCommAdapter();
        adapter.initVehiclePosition(newPoint.getName());
      }
      else {
        LOG.debug("Vehicle {}: Not a simulation adapter -> not setting initial position.",
                  entry.getVehicle().getName());
      }
    });
    pointsCellEditor.setEditorAt(rowIndex, new DefaultCellEditor(pointComboBox));
  }

  /**
   * Enables or disables auto attaching adapters on startup.
   *
   * @param autoAttachOnStartup <code>true</code> to enable, <code>false</code>
   * to disable
   */
  private void setAutoAttachOnStartup(boolean autoAttachOnStartup) {
    this.autoAttachOnStartup = autoAttachOnStartup;
    CONFIG_STORE.setBoolean("autoAttachOnStartup", autoAttachOnStartup);
  }

  /**
   * Enables or disables auto enabling adapters on startup.
   *
   * @param autoEnableOnStartup <code>true</code> to enable, <code>false</code>
   * to disable
   */
  private void setAutoEnableOnStartup(boolean autoEnableOnStartup) {
    this.autoEnableOnStartup = autoEnableOnStartup;
    CONFIG_STORE.setBoolean("autoEnableOnStartup", autoEnableOnStartup);
  }

  /**
   * Resets selected vehicles' positions to null.
   */
  private void resetSelectedVehiclePositions() {
    for (int selectedRowIndex : vehicleTable.getSelectedRows()) {
      vehicleEntries.get(selectedRowIndex).getProcessModel().setVehiclePosition(null);
    }
  }

  private void initVehicleModels() {
    vehicleEntries.clear();
    kernel.getTCSObjects(Vehicle.class).stream()
        .sorted(Comparators.objectsByName())
        .forEach(vehicle -> vehicleEntries.add(new VehicleEntry(vehicle)));
  }

  private void initVehicleList() {
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (VehicleEntry curEntry : vehicleEntries) {
      model.addData(curEntry);
      curEntry.addPropertyChangeListener(model);
    }

    vehicleTable.getComponentPopupMenu().setEnabled(!model.getVehicleEntries().isEmpty());

    initAdapterComboBoxes();
  }

  private void initAutoAttach() {
    if (autoAttachOnStartup) {
      autoAttachAll();
      // Auto-enable vehicles (if we should)
      if (autoEnableOnStartup) {
        enableCommAdaptersForAll();
      }
    }
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
    List<VehicleEntry> selectedEntries = new LinkedList<>();
    for (int selectedRowIndex : vehicleTable.getSelectedRows()) {
      selectedEntries.add(vehicleEntries.get(selectedRowIndex));
    }
    driverMenu.removeAll();
    for (VehicleCommAdapterFactory factory : commAdapterRegistry.getFactories()) {
      boolean enabled = true;
      List<VehicleEntry> vehiclesToAttach = new LinkedList<>();
      for (VehicleEntry selectedEntry : selectedEntries) {
        if (!factory.providesAdapterFor(selectedEntry.getVehicle())) {
          enabled = false;
          vehiclesToAttach.clear();
          break;
        }
        else if (selectedEntry.getCommAdapter() == null) {
          vehiclesToAttach.add(selectedEntry);
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
    autoAttachSelected();
  }//GEN-LAST:event_autoAttachSelectedMenuItemActionPerformed

  private void detachAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detachAllMenuItemActionPerformed
    for (VehicleEntry entry : vehicleEntries) {
      attachManager.detachAdapterFromVehicle(entry, true);
    }
  }//GEN-LAST:event_detachAllMenuItemActionPerformed

  private void detachAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detachAllSelectedMenuItemActionPerformed
    for (int selectedRowIndex : vehicleTable.getSelectedRows()) {
      attachManager.detachAdapterFromVehicle(vehicleEntries.get(selectedRowIndex), true);
    }
  }//GEN-LAST:event_detachAllSelectedMenuItemActionPerformed

  private void enableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllMenuItemActionPerformed
    enableCommAdaptersForAll();
  }//GEN-LAST:event_enableAllMenuItemActionPerformed

  private void enableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllSelectedMenuItemActionPerformed
    enableCommAdaptersForSelection();
  }//GEN-LAST:event_enableAllSelectedMenuItemActionPerformed

  private void disableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllMenuItemActionPerformed
    disableCommAdaptersForAll();
  }//GEN-LAST:event_disableAllMenuItemActionPerformed

  private void disableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllSelectedMenuItemActionPerformed
    disableCommAdaptersForSelection();
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
    for (VehicleEntry entry : vehicleEntries) {
      VehicleCommAdapter commAdapter = entry.getCommAdapter();
      if (commAdapter == null) {
        detachedCount++;
      }
      else {
        attachedCount++;
        if (commAdapter.isEnabled()) {
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
    List<VehicleEntry> selectedEntries = new LinkedList<>();
    for (int selectedRowIndex : vehicleTable.getSelectedRows()) {
      selectedEntries.add(vehicleEntries.get(selectedRowIndex));
    }
    for (VehicleEntry entry : selectedEntries) {
      VehicleCommAdapter commAdapter = entry.getCommAdapter();
      if (commAdapter == null) {
        detachedCount++;
      }
      else {
        attachedCount++;
        if (commAdapter.isEnabled()) {
          enabledCount++;
        }
        else {
          disabledCount++;
        }
      }
    }
    for (VehicleEntry entry : selectedEntries) {
      VehicleCommAdapter commAdapter = entry.getCommAdapter();
      if (commAdapter != null) {
        if (commAdapter.isEnabled()) {
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
        VehicleEntry clickedEntry = model.getDataAt(index);
        DetailPanel detailPanel = (DetailPanel) vehicleDetailPanel.getComponent(0);
        detailPanel.attachToVehicle(clickedEntry);
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
   * Attaches adapters produced by a given factory to a set of vehicles when performed.
   */
  private class AttachCommAdapterAction
      extends AbstractAction {

    /**
     * The affected vehicles' entries.
     */
    private final List<VehicleEntry> vehicleEntries;
    /**
     * The factory providing the communication adapter.
     */
    private final VehicleCommAdapterFactory factory;

    /**
     * Creates a new AttachCommAdapterAction.
     *
     * @param description A string describing the factory.
     * @param vehicleEntries The affected vehicles' entries.
     * @param factory The factory providing the communication adapter.
     */
    private AttachCommAdapterAction(String description,
                                    List<VehicleEntry> vehicleEntries,
                                    VehicleCommAdapterFactory factory) {
      super(description);
      this.vehicleEntries = requireNonNull(vehicleEntries, "vehicleEntries");
      this.factory = requireNonNull(factory, "factory");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
      for (VehicleEntry entry : vehicleEntries) {
        attachManager.attachAdapterToVehicle(entry, factory);
      }
    }
  }
}
