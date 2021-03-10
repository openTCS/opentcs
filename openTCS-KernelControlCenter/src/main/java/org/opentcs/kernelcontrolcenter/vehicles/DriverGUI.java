/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernelcontrolcenter.ControlCenterPanel;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.model.Point;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.commands.InitPositionCommand;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import static org.opentcs.kernelcontrolcenter.I18nKernelControlCenter.BUNDLE_PATH;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.Comparators;
import org.opentcs.util.gui.BoundsPopupMenuListener;
import org.opentcs.util.gui.StringListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel containing all vehicles and detailed information.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DriverGUI
    extends ControlCenterPanel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DriverGUI.class);
  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The service portal to use for kernel interaction.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * The pool of vehicle entries.
   */
  private final LocalVehicleEntryPool vehicleEntryPool;
  /**
   * The detail panel to dispay when selecting a vehicle.
   */
  private final DetailPanel detailPanel;
  /**
   * Whether this panel is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param servicePortal The service portal to use for kernel interaction.
   * @param callWrapper The call wrapper to use for service calls.
   * @param vehicleEntryPool The pool of vehicle entries.
   * @param detailPanel The detail panel to display.
   */
  @Inject
  public DriverGUI(@Nonnull KernelServicePortal servicePortal,
                   @Nonnull @ServiceCallWrapper CallWrapper callWrapper,
                   @Nonnull LocalVehicleEntryPool vehicleEntryPool,
                   @Nonnull DetailPanel detailPanel) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.vehicleEntryPool = requireNonNull(vehicleEntryPool, "vehicleEntryPool");
    this.detailPanel = requireNonNull(detailPanel, "detailPanel");

    initComponents();

    vehicleTable.setDefaultRenderer(VehicleCommAdapterDescription.class,
                                    new VehicleCommAdapterFactoryTableCellRenderer());
    vehicleDetailPanel.add(detailPanel);
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

    // Verify that the kernel is in a state in which controlling vehicles is possible.
    Kernel.State kernelState;
    try {
      kernelState = callWrapper.call(() -> servicePortal.getState());
    }
    catch (Exception ex) {
      LOG.warn("Error getting the kernel state", ex);
      return;
    }
    checkState(Kernel.State.OPERATING.equals(kernelState),
               "Cannot work in kernel state %s",
               kernelState);

    vehicleEntryPool.initialize();
    detailPanel.initialize();

    EventQueue.invokeLater(() -> {
      initVehicleList();
    });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }

    detailPanel.terminate();
    vehicleEntryPool.terminate();
    initialized = false;
  }

  private void initVehicleList() {
    vehicleTable.setModel(new VehicleTableModel(servicePortal.getVehicleService(), callWrapper));
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();

    vehicleEntryPool.getEntries().forEach((vehicleName, entry) -> {
      model.addData(entry);
      entry.addPropertyChangeListener(model);
    });

    vehicleTable.getComponentPopupMenu().setEnabled(!model.getVehicleEntries().isEmpty());

    initAdapterComboBoxes();
  }

  /**
   * Initializes the combo boxes with available adapters for every vehicle.
   */
  private void initAdapterComboBoxes() {
    SingleCellEditor adapterCellEditor = new SingleCellEditor(vehicleTable);
    SingleCellEditor pointsCellEditor = new SingleCellEditor(vehicleTable);

    int index = 0;
    for (LocalVehicleEntry entry : vehicleEntryPool.getEntries().values()) {
      initCommAdaptersComboBox(entry, index, adapterCellEditor);
      initPointsComboBox(index, pointsCellEditor);
      index++;
    }

    vehicleTable.getColumn(VehicleTableModel.ADAPTER_COLUMN_IDENTIFIER)
        .setCellEditor(adapterCellEditor);
    vehicleTable.getColumn(VehicleTableModel.POSITION_COLUMN_IDENTIFIER)
        .setCellEditor(pointsCellEditor);
  }

  private void initCommAdaptersComboBox(LocalVehicleEntry vehicleEntry,
                                        int rowIndex,
                                        SingleCellEditor adapterCellEditor) {
    final CommAdapterComboBox comboBox = new CommAdapterComboBox();
    AttachmentInformation ai;
    try {
      ai = callWrapper.call(() -> servicePortal.getVehicleService().fetchAttachmentInformation(
          vehicleEntry.getAttachmentInformation().getVehicleReference()));
    }
    catch (Exception ex) {
      LOG.warn("Error fetching attachment information for {}", vehicleEntry.getVehicleName(), ex);
      return;
    }
    ai.getAvailableCommAdapters().forEach(factory -> comboBox.addItem(factory));

    // Set the selection to the attached comm adapter, (The vehicle is already attached to a comm
    // adapter due to auto attachment on startup.)
    comboBox.setSelectedItem(vehicleEntry.getAttachmentInformation().getAttachedCommAdapter());

    comboBox.setRenderer(new AdapterFactoryCellRenderer());
    comboBox.addPopupMenuListener(new BoundsPopupMenuListener());
    comboBox.addItemListener((ItemEvent evt) -> {
      if (evt.getStateChange() == ItemEvent.DESELECTED) {
        return;
      }

      // If we selected a comm adapter that's already attached, do nothing.
      if (Objects.equals(evt.getItem(), vehicleEntry.getAttachedCommAdapterDescription())) {
        LOG.debug("{} is already attached to: {}", vehicleEntry.getVehicleName(), evt.getItem());
        return;
      }

      int reply = JOptionPane.showConfirmDialog(
          null,
          bundle.getString("driverGui.optionPane_driverChangeConfirmation.message"),
          bundle.getString("driverGui.optionPane_driverChangeConfirmation.title"),
          JOptionPane.YES_NO_OPTION);
      if (reply == JOptionPane.NO_OPTION) {
        return;
      }

      VehicleCommAdapterDescription factory = comboBox.getSelectedItem();
      try {
        callWrapper.call(() -> servicePortal.getVehicleService().attachCommAdapter(
            vehicleEntry.getAttachmentInformation().getVehicleReference(), factory));
      }
      catch (Exception ex) {
        LOG.warn("Error attaching adapter {} to vehicle {}",
                 factory,
                 vehicleEntry.getVehicleName(),
                 ex);
        return;
      }
      LOG.info("Attaching comm adapter {} to {}", factory, vehicleEntry.getVehicleName());
    });
    adapterCellEditor.setEditorAt(rowIndex, new DefaultCellEditor(comboBox));

    vehicleEntry.addPropertyChangeListener(comboBox);
  }

  /**
   * If a loopback adapter was chosen, this method initializes the combo boxes with positions the
   * user can set the vehicle to.
   *
   * @param rowIndex An index indicating which row this combo box belongs to
   * @param pointsCellEditor The <code>SingleCellEditor</code> containing the combo boxes.
   */
  private void initPointsComboBox(int rowIndex, SingleCellEditor pointsCellEditor) {
    final JComboBox<Point> pointComboBox = new JComboBox<>();

    Set<Point> points;
    try {
      points = callWrapper.call(() -> servicePortal.getVehicleService().fetchObjects(Point.class));
    }
    catch (Exception ex) {
      LOG.warn("Error fetching points", ex);
      return;
    }

    points.stream().sorted(Comparators.objectsByName())
        .forEach(point -> pointComboBox.addItem(point));
    pointComboBox.setSelectedIndex(-1);
    pointComboBox.setRenderer(new StringListCellRenderer<>(x -> x == null ? "" : x.getName()));

    pointComboBox.addItemListener((ItemEvent e) -> {
      try {
        Point newPoint = (Point) e.getItem();
        LocalVehicleEntry vehicleEntry = vehicleEntryPool.getEntryFor(getSelectedVehicleName());
        if (vehicleEntry.getAttachedCommAdapterDescription().isSimVehicleCommAdapter()) {
          callWrapper.call(() -> servicePortal.getVehicleService().sendCommAdapterCommand(
              vehicleEntry.getAttachmentInformation().getVehicleReference(),
              new InitPositionCommand(newPoint.getName())));
        }
        else {
          LOG.debug("Vehicle {}: Not a simulation adapter -> not setting initial position.",
                    vehicleEntry.getVehicleName());
        }
      }
      catch (Exception ex) {
        LOG.warn("Error sending init position command", ex);
      }
    });
    pointsCellEditor.setEditorAt(rowIndex, new DefaultCellEditor(pointComboBox));
  }

  private void enableAllCommAdapters() {
    enableCommAdapters(vehicleEntryPool.getEntries().values());
  }

  private void enableSelectedCommAdapters() {
    enableCommAdapters(getSelectedVehicleEntries());
  }

  private void enableCommAdapters(Collection<LocalVehicleEntry> selectedEntries) {
    Collection<LocalVehicleEntry> entries = selectedEntries.stream()
        .filter(entry -> !entry.getProcessModel().isCommAdapterEnabled())
        .collect(Collectors.toList());

    try {
      for (LocalVehicleEntry entry : entries) {
        callWrapper.call(() -> servicePortal.getVehicleService().enableCommAdapter(
            entry.getAttachmentInformation().getVehicleReference()));
      }
    }
    catch (Exception ex) {
      LOG.warn("Error enabling comm adapter, canceling", ex);
    }
  }

  private void disableAllCommAdapters() {
    disableCommAdapters(vehicleEntryPool.getEntries().values());
  }

  private void disableSelectedCommAdapters() {
    disableCommAdapters(getSelectedVehicleEntries());
  }

  private void disableCommAdapters(Collection<LocalVehicleEntry> selectedEntries) {
    Collection<LocalVehicleEntry> entries = selectedEntries.stream()
        .filter(entry -> entry.getProcessModel().isCommAdapterEnabled())
        .collect(Collectors.toList());

    try {
      for (LocalVehicleEntry entry : entries) {
        callWrapper.call(() -> servicePortal.getVehicleService().disableCommAdapter(
            entry.getAttachmentInformation().getVehicleReference()));
      }
    }
    catch (Exception ex) {
      LOG.warn("Error disabling comm adapter, canceling", ex);
    }
  }

  private String getSelectedVehicleName() {
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    return model.getDataAt(vehicleTable.getSelectedRow()).getVehicleName();
  }

  private List<String> getSelectedVehicleNames() {
    List<String> selectedVehicleNames = new ArrayList<>();
    VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
    for (int selectedRow : vehicleTable.getSelectedRows()) {
      String selectedVehicleName = model.getDataAt(selectedRow).getVehicleName();
      selectedVehicleNames.add(selectedVehicleName);
    }
    return selectedVehicleNames;
  }

  private List<LocalVehicleEntry> getSelectedVehicleEntries() {
    List<LocalVehicleEntry> selectedEntries = new LinkedList<>();
    for (String selectedVehicleName : getSelectedVehicleNames()) {
      selectedEntries.add(vehicleEntryPool.getEntryFor(selectedVehicleName));
    }
    return selectedEntries;
  }

  private void createDriverMenu() {
    driverMenu.removeAll();

    // Collect all available comm adapters/factories
    Set<VehicleCommAdapterDescription> availableDescriptions = new HashSet<>();
    vehicleEntryPool.getEntries().forEach((vehicleName, entry) -> {
      availableDescriptions.addAll(entry.getAttachmentInformation().getAvailableCommAdapters());
    });

    for (VehicleCommAdapterDescription description : availableDescriptions) {
      // If there's one vehicle not supported by this factory the selection can't be attached to it
      boolean factorySupportsSelectedVehicles = getSelectedVehicleEntries().stream()
          .map(entry -> entry.getAttachmentInformation().getAvailableCommAdapters())
          .allMatch(descriptions -> !Collections.disjoint(descriptions, availableDescriptions));

      List<String> vehiclesToAttach = new ArrayList<>();
      if (factorySupportsSelectedVehicles) {
        vehiclesToAttach = getSelectedVehicleNames();
      }

      Action action = new AttachCommAdapterAction(vehiclesToAttach, description);
      JMenuItem menuItem = driverMenu.add(action);
      menuItem.setEnabled(!vehiclesToAttach.isEmpty());
    }
  }

  private void createPopupMenu() {
    // Find out how many vehicles (don't) have a driver attached.
    StatesCounts stateCounts = getCommAdapterStateCountsFor(vehicleEntryPool.getEntries().values());
    enableAllMenuItem.setEnabled(stateCounts.disabledCount > 0);
    disableAllMenuItem.setEnabled(stateCounts.enabledCount > 0);

    // Now do the same for those that are selected.
    stateCounts = getCommAdapterStateCountsFor(getSelectedVehicleEntries());
    enableAllSelectedMenuItem.setEnabled(stateCounts.disabledCount > 0);
    disableAllSelectedMenuItem.setEnabled(stateCounts.enabledCount > 0);
  }

  private StatesCounts getCommAdapterStateCountsFor(Collection<LocalVehicleEntry> entries) {
    StatesCounts stateCounts = new StatesCounts();
    for (LocalVehicleEntry entry : entries) {
      VehicleProcessModelTO processModel = entry.getProcessModel();
      if (processModel.isCommAdapterEnabled()) {
        stateCounts.enabledCount++;
      }
      else {
        stateCounts.disabledCount++;
      }
    }
    return stateCounts;
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
        enableAllMenuItem = new javax.swing.JMenuItem();
        enableAllSelectedMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        disableAllMenuItem = new javax.swing.JMenuItem();
        disableAllSelectedMenuItem = new javax.swing.JMenuItem();
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

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/kernelcontrolcenter/Bundle"); // NOI18N
        driverMenu.setText(bundle.getString("driverGui.popupMenu_vehicles.subMenu_driver.text")); // NOI18N
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

        enableAllMenuItem.setText(bundle.getString("driverGui.popupMenu_vehicles.menuItem_enableAll.text")); // NOI18N
        enableAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAllMenuItemActionPerformed(evt);
            }
        });
        vehicleListPopupMenu.add(enableAllMenuItem);

        enableAllSelectedMenuItem.setText(bundle.getString("driverGui.popupMenu_vehicles.menuItem_enableSelected.text")); // NOI18N
        enableAllSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAllSelectedMenuItemActionPerformed(evt);
            }
        });
        vehicleListPopupMenu.add(enableAllSelectedMenuItem);
        vehicleListPopupMenu.add(jSeparator4);

        disableAllMenuItem.setText(bundle.getString("driverGui.popupMenu_vehicles.menuItem_disableAll.text")); // NOI18N
        disableAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableAllMenuItemActionPerformed(evt);
            }
        });
        vehicleListPopupMenu.add(disableAllMenuItem);

        disableAllSelectedMenuItem.setText(bundle.getString("driverGui.popupMenu_vehicles.menuItem_disableSelected.text")); // NOI18N
        disableAllSelectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableAllSelectedMenuItemActionPerformed(evt);
            }
        });
        vehicleListPopupMenu.add(disableAllSelectedMenuItem);

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        listDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("driverGui.panel_vehicles.border.title"))); // NOI18N
        listDisplayPanel.setMaximumSize(new java.awt.Dimension(464, 2147483647));
        listDisplayPanel.setMinimumSize(new java.awt.Dimension(464, 425));
        listDisplayPanel.setLayout(new java.awt.BorderLayout());

        vehicleTable.setComponentPopupMenu(vehicleListPopupMenu);
        vehicleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vehicleTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(vehicleTable);

        listDisplayPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(listDisplayPanel);

        vehicleDetailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("driverGui.panel_vehicleDetails.border.title"))); // NOI18N
        vehicleDetailPanel.setPreferredSize(new java.awt.Dimension(800, 23));
        vehicleDetailPanel.setLayout(new java.awt.BorderLayout());
        add(vehicleDetailPanel);
        vehicleDetailPanel.getAccessibleContext().setAccessibleName(bundle.getString("driverGui.panel_vehicleDetails.accessibleName")); // NOI18N

        getAccessibleContext().setAccessibleName(bundle.getString("driverGui.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

  private void driverMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_driverMenuMenuSelected
    createDriverMenu();
  }//GEN-LAST:event_driverMenuMenuSelected

  private void enableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllMenuItemActionPerformed
    enableAllCommAdapters();
  }//GEN-LAST:event_enableAllMenuItemActionPerformed

  private void enableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllSelectedMenuItemActionPerformed
    enableSelectedCommAdapters();
  }//GEN-LAST:event_enableAllSelectedMenuItemActionPerformed

  private void disableAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllMenuItemActionPerformed
    disableAllCommAdapters();
  }//GEN-LAST:event_disableAllMenuItemActionPerformed

  private void disableAllSelectedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllSelectedMenuItemActionPerformed
    disableSelectedCommAdapters();
  }//GEN-LAST:event_disableAllSelectedMenuItemActionPerformed

  private void vehicleListPopupMenuPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_vehicleListPopupMenuPopupMenuWillBecomeVisible
    createPopupMenu();
  }//GEN-LAST:event_vehicleListPopupMenuPopupMenuWillBecomeVisible

  private void vehicleTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleTableMouseClicked
    if (evt.getClickCount() == 2) {
      int index = vehicleTable.getSelectedRow();
      if (index >= 0) {
        VehicleTableModel model = (VehicleTableModel) vehicleTable.getModel();
        LocalVehicleEntry clickedEntry = model.getDataAt(index);
        DetailPanel detailPanel = (DetailPanel) vehicleDetailPanel.getComponent(0);
        detailPanel.attachToVehicle(clickedEntry);
      }
    }
  }//GEN-LAST:event_vehicleTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem disableAllMenuItem;
    private javax.swing.JMenuItem disableAllSelectedMenuItem;
    private javax.swing.JMenu driverMenu;
    private javax.swing.JMenuItem enableAllMenuItem;
    private javax.swing.JMenuItem enableAllSelectedMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPanel listDisplayPanel;
    private javax.swing.JMenuItem noDriversMenuItem;
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
    private final List<String> vehicleNames;
    /**
     * The factory providing the communication adapter.
     */
    private final VehicleCommAdapterDescription commAdapterDescription;

    /**
     * Creates a new AttachCommAdapterAction.
     *
     * @param vehicleNames The affected vehicles' entries.
     * @param commAdapterDescription The factory providing the communication adapter.
     */
    private AttachCommAdapterAction(List<String> vehicleNames,
                                    VehicleCommAdapterDescription commAdapterDescription) {
      super(commAdapterDescription.getDescription());
      this.vehicleNames = requireNonNull(vehicleNames, "vehicleNames");
      this.commAdapterDescription = requireNonNull(commAdapterDescription, "factory");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
      for (String vehicleName : vehicleNames) {
        servicePortal.getVehicleService().attachCommAdapter(
            vehicleEntryPool.getEntryFor(vehicleName).getAttachmentInformation().getVehicleReference(),
            commAdapterDescription);
      }
    }
  }

  private class StatesCounts {

    private int enabledCount;
    private int disabledCount;
  }
}
