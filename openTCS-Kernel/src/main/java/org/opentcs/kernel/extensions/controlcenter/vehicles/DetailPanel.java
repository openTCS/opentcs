/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays information about a vehicle (VehicleModel) graphically.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
final class DetailPanel
    extends JPanel
    implements PropertyChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DetailPanel.class);
  /**
   * A panel's default border title.
   */
  private static final String DEFAULT_BORDER_TITLE = "";
  /**
   * A <code>DateFormat</code> instance for formatting message's time stamps.
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());
  /**
   * The adapter specific list of JPanels.
   */
  @SuppressWarnings("deprecation")
  private final List<org.opentcs.drivers.vehicle.VehicleCommAdapterPanel> customPanelList
      = new LinkedList<>();
  /**
   * The logging table model to use.
   */
  private final LogTableModel loggingTableModel = new LogTableModel();
  /**
   * The vehicle model of the vehicle current associated with this window.
   */
  private VehicleEntry vehicleEntry;
  /**
   * The comm adapter currently attached to the vehicle (model).
   */
  private VehicleCommAdapter commAdapter;

  /**
   * Creates a new instance.
   */
  DetailPanel() {
    initComponents();

    loggingTable.setModel(loggingTableModel);
    loggingTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    loggingTable.getColumnModel().getColumn(1).setPreferredWidth(110);
    loggingTable.getSelectionModel().addListSelectionListener(new RowListener());
    // Make sure we start with an empty panel.
    detachFromVehicle();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof VehicleEntry) {
      updateFromVehicleEntry(evt);
    }
    else if (evt.getSource() instanceof VehicleProcessModel) {
      updateFromVehicleProcessModel(evt);
    }
  }

  // Methods not declared in any interface start here.
  /**
   * Attaches this panel to a vehicle.
   *
   * @param newVehicleEntry The vehicle entry to attach to.
   */
  void attachToVehicle(VehicleEntry newVehicleEntry) {
    requireNonNull(newVehicleEntry, "newVehicleEntry");

    // Clean up first - but only if we're not reattaching the vehicle model
    // which is already attached to this panel.
    if (vehicleEntry != null && vehicleEntry != newVehicleEntry) {
      detachFromVehicle();
    }
    vehicleEntry = newVehicleEntry;
    vehicleEntry.addPropertyChangeListener(this);
    vehicleEntry.getProcessModel().addPropertyChangeListener(this);
    setBorderTitle(vehicleEntry.getProcessModel().getName());
    // Ensure the tabbed pane containing vehicle information is shown.
    removeAll();
    add(tabbedPane);

    // Init vehicle status.
    loggingTableModel.clear();
    for (UserNotification curMessage : vehicleEntry.getProcessModel().getNotifications()) {
      loggingTableModel.addRow(curMessage);
    }
    initVehicleEntryAttributes();
    // Update panel contents.
    validate();
//    int newIndex = vehicleEntry.getSelectedTabIndex();
    int newIndex = 0;
    LOG.debug("Setting tabbedPane.selectedTabIndex to {}", newIndex);
    tabbedPane.setSelectedIndex(newIndex);
  }

  /**
   * Detaches this panel from a vehicle (if it is currently attached to any).
   */
  private void detachFromVehicle() {
    if (vehicleEntry != null) {
      vehicleEntry.removePropertyChangeListener(this);
      vehicleEntry.getProcessModel().removePropertyChangeListener(this);
      // Remove all custom panels and let the comm adapter know we don't need
      // them any more.
      removeCustomPanels();
      if (commAdapter != null) {
        commAdapter = null;
      }
      customPanelList.clear();
      vehicleEntry = null;
    }
    // Clear the log message table.
    loggingTableModel.clear();
    setBorderTitle(DEFAULT_BORDER_TITLE);
    // Remove the contents of this panel.
    removeAll();
    add(noVehiclePanel);
    // Update panel contents.
    validate();
  }

  private void initVehicleEntryAttributes() {
    updateCommAdapter(vehicleEntry.getCommAdapter());

    updateCommAdapterEnabled(vehicleEntry.getProcessModel().isCommAdapterEnabled());
    updateVehiclePosition(vehicleEntry.getProcessModel().getVehiclePosition());
    updateVehicleState(vehicleEntry.getProcessModel().getVehicleState());
  }

  private void updateFromVehicleEntry(PropertyChangeEvent evt) {
    if (Objects.equals(evt.getPropertyName(), VehicleEntry.Attribute.COMM_ADAPTER.name())) {
      updateCommAdapter((VehicleCommAdapter) evt.getNewValue());
    }
  }

  private void updateFromVehicleProcessModel(PropertyChangeEvent evt) {
    if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED)) {
      updateCommAdapterEnabled((Boolean) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.POSITION.name())) {
      updateVehiclePosition((String) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState((Vehicle.State) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.USER_NOTIFICATION.name())) {
      updateUserNotification((UserNotification) evt.getNewValue());
    }
  }

  private void updateCommAdapter(VehicleCommAdapter newCommAdapter) {
    SwingUtilities.invokeLater(() -> {
      // If there was a comm adapter and it changed, we need to clean up a few
      // things first.
      if (commAdapter != null) {
        // Detach all custom panels of the old comm adapter.
        removeCustomPanels();
        customPanelList.clear();
      }
      // Update the comm adapter reference.
      commAdapter = newCommAdapter;
      // If we have a new comm adapter, set up a few things.
      if (commAdapter != null) {
        // Update the custom panels displayed.
        updateCustomPanels();
      }
      chkBoxEnable.setEnabled(commAdapter != null);
      chkBoxEnable.setSelected(commAdapter != null && commAdapter.isEnabled());
    });
  }

  private void updateCommAdapterEnabled(boolean enabled) {
    SwingUtilities.invokeLater(() -> chkBoxEnable.setSelected(enabled));
  }

  private void updateVehiclePosition(String position) {
    SwingUtilities.invokeLater(() -> curPosTxt.setText(position));
  }

  private void updateVehicleState(Vehicle.State state) {
    SwingUtilities.invokeLater(() -> curStateTxt.setText(state.toString()));
  }

  private void updateUserNotification(UserNotification notification) {
    SwingUtilities.invokeLater(() -> loggingTableModel.addRow(notification));
  }

  /**
   * Update the list of custom panels in the tabbed pane.
   */
  @SuppressWarnings("deprecation")
  private void updateCustomPanels() {
    for (org.opentcs.drivers.vehicle.VehicleCommAdapterPanel curPanel : customPanelList) {
      LOG.debug("Removing {} from tabbedPane.", curPanel);
      tabbedPane.remove(curPanel);
    }
    customPanelList.clear();
    if (commAdapter != null) {
      customPanelList.addAll(commAdapter.getAdapterPanels());
      for (org.opentcs.drivers.vehicle.VehicleCommAdapterPanel curPanel : customPanelList) {
        LOG.debug("Adding {} with title {} to tabbedPane.", curPanel, curPanel.getTitle());
        tabbedPane.addTab(curPanel.getTitle(), curPanel);
      }
    }
  }

  /**
   * Removes the custom panels from this panel's tabbed pane.
   */
  @SuppressWarnings("deprecation")
  private void removeCustomPanels() {
    LOG.debug("Setting selected component of tabbedPane to overviewTabPanel.");
    tabbedPane.setSelectedComponent(overviewTabPanel);
    for (org.opentcs.drivers.vehicle.VehicleCommAdapterPanel panel : customPanelList) {
      LOG.debug("Removing {} from tabbedPane.", panel);
      tabbedPane.remove(panel);
    }
  }

  /**
   * Sets this panel's border title.
   *
   * @param newTitle This panel's new border title.
   */
  private void setBorderTitle(String newTitle) {
    requireNonNull(newTitle, "newTitle");
    ((TitledBorder) getBorder()).setTitle(newTitle);
    // Trigger a repaint - the title sometimes looks strange otherwise.
    repaint();
  }

  /**
   * This method appends the selected notification to the text area in the log tab.
   *
   * @param row The selected row in the table.
   */
  private void outputLogNotification(int row) {
    UserNotification message = loggingTableModel.getRow(row);
    String timestamp = DATE_FORMAT.format(message.getTimestamp());
    String output = timestamp + " (" + message.getLevel() + "):\n" + message.getText();
    loggingTextArea.setText(output);
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    logPopupMenu = new javax.swing.JPopupMenu();
    clearMenuItem = new javax.swing.JMenuItem();
    loggingTablePopupMenu = new javax.swing.JPopupMenu();
    filterMenu = new javax.swing.JMenu();
    everythingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    warningsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    errorsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    noVehiclePanel = new javax.swing.JPanel();
    noVehicleLabel = new javax.swing.JLabel();
    tabbedPane = new javax.swing.JTabbedPane();
    overviewTabPanel = new javax.swing.JPanel();
    headPanel = new javax.swing.JPanel();
    statusPanel = new javax.swing.JPanel();
    adapterStatusPanel = new javax.swing.JPanel();
    chkBoxEnable = new javax.swing.JCheckBox();
    statusFiguresPanel = new javax.swing.JPanel();
    curPosLbl = new javax.swing.JLabel();
    curPosTxt = new javax.swing.JTextField();
    curStateLbl = new javax.swing.JLabel();
    curStateTxt = new javax.swing.JTextField();
    fillingLbl = new javax.swing.JLabel();
    logoPanel = new javax.swing.JPanel();
    logoLbl = new javax.swing.JLabel();
    logPanel = new javax.swing.JPanel();
    logTableScrollPane = new javax.swing.JScrollPane();
    loggingTable = new javax.swing.JTable();
    logTextScrollPane = new javax.swing.JScrollPane();
    loggingTextArea = new javax.swing.JTextArea();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle"); // NOI18N
    clearMenuItem.setText(bundle.getString("ClearLogMessageText")); // NOI18N
    clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearMenuItemActionPerformed(evt);
      }
    });
    logPopupMenu.add(clearMenuItem);

    filterMenu.setText(bundle.getString("FilterMessages")); // NOI18N
    filterMenu.setActionCommand(" message filtering");

    everythingCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowAll")); // NOI18N
    everythingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        everythingCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(everythingCheckBoxMenuItem);

    warningsCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowErrorsAndWarnings")); // NOI18N
    warningsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        warningsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(warningsCheckBoxMenuItem);

    errorsCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowErrors")); // NOI18N
    errorsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        errorsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(errorsCheckBoxMenuItem);

    loggingTablePopupMenu.add(filterMenu);

    noVehiclePanel.setLayout(new java.awt.BorderLayout());

    noVehicleLabel.setFont(noVehicleLabel.getFont().deriveFont(noVehicleLabel.getFont().getSize()+3f));
    noVehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    noVehicleLabel.setText(bundle.getString("NoVehicleAttached")); // NOI18N
    noVehiclePanel.add(noVehicleLabel, java.awt.BorderLayout.CENTER);

    setBorder(javax.swing.BorderFactory.createTitledBorder(DEFAULT_BORDER_TITLE));
    setLayout(new java.awt.BorderLayout());

    tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

    overviewTabPanel.setLayout(new java.awt.GridBagLayout());

    headPanel.setLayout(new java.awt.BorderLayout());

    statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.LINE_AXIS));

    adapterStatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("AdapterStatus"))); // NOI18N
    adapterStatusPanel.setLayout(new java.awt.BorderLayout());

    chkBoxEnable.setText(bundle.getString("EnableAdapter")); // NOI18N
    chkBoxEnable.setEnabled(false);
    chkBoxEnable.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        chkBoxEnableActionPerformed(evt);
      }
    });
    adapterStatusPanel.add(chkBoxEnable, java.awt.BorderLayout.CENTER);

    statusPanel.add(adapterStatusPanel);

    statusFiguresPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("VehicleStatus"))); // NOI18N
    statusFiguresPanel.setLayout(new java.awt.GridBagLayout());

    curPosLbl.setText(bundle.getString("CurrentPosition")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    statusFiguresPanel.add(curPosLbl, gridBagConstraints);

    curPosTxt.setEditable(false);
    curPosTxt.setColumns(9);
    curPosTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    curPosTxt.setText("Point-0001");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    statusFiguresPanel.add(curPosTxt, gridBagConstraints);

    curStateLbl.setText(bundle.getString("CurrentState")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    statusFiguresPanel.add(curStateLbl, gridBagConstraints);

    curStateTxt.setEditable(false);
    curStateTxt.setColumns(9);
    curStateTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    curStateTxt.setText(Vehicle.State.UNKNOWN.name());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    statusFiguresPanel.add(curStateTxt, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    statusFiguresPanel.add(fillingLbl, gridBagConstraints);

    statusPanel.add(statusFiguresPanel);

    headPanel.add(statusPanel, java.awt.BorderLayout.WEST);

    logoPanel.setBackground(new java.awt.Color(255, 255, 255));
    logoPanel.setLayout(new java.awt.BorderLayout());

    logoLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    logoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/kernel/controlcenter/res/logos/opentcs_logo.gif"))); // NOI18N
    logoPanel.add(logoLbl, java.awt.BorderLayout.CENTER);

    headPanel.add(logoPanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    overviewTabPanel.add(headPanel, gridBagConstraints);

    logPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Messages"))); // NOI18N
    logPanel.setPreferredSize(new java.awt.Dimension(468, 200));
    logPanel.setLayout(new java.awt.BorderLayout());

    logTableScrollPane.setComponentPopupMenu(loggingTablePopupMenu);

    loggingTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Time stamp", "Message"
      }
    ) {
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    loggingTable.setComponentPopupMenu(loggingTablePopupMenu);
    loggingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    logTableScrollPane.setViewportView(loggingTable);

    logPanel.add(logTableScrollPane, java.awt.BorderLayout.CENTER);

    loggingTextArea.setEditable(false);
    loggingTextArea.setColumns(20);
    loggingTextArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
    loggingTextArea.setLineWrap(true);
    loggingTextArea.setRows(3);
    loggingTextArea.setComponentPopupMenu(logPopupMenu);
    logTextScrollPane.setViewportView(loggingTextArea);

    logPanel.add(logTextScrollPane, java.awt.BorderLayout.SOUTH);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    overviewTabPanel.add(logPanel, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("GeneralStatus"), overviewTabPanel); // NOI18N

    add(tabbedPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void warningsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningsCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages((notification)
        -> notification.getLevel().equals(UserNotification.Level.IMPORTANT)
        || notification.getLevel().equals(UserNotification.Level.NOTEWORTHY));
    warningsCheckBoxMenuItem.setSelected(true);
    errorsCheckBoxMenuItem.setSelected(false);
    everythingCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_warningsCheckBoxMenuItemActionPerformed

  private void everythingCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_everythingCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages((notification) -> true);
    everythingCheckBoxMenuItem.setSelected(true);
    errorsCheckBoxMenuItem.setSelected(false);
    warningsCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_everythingCheckBoxMenuItemActionPerformed

  private void errorsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorsCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages(
        (notification) -> notification.getLevel().equals(UserNotification.Level.IMPORTANT));
    errorsCheckBoxMenuItem.setSelected(true);
    everythingCheckBoxMenuItem.setSelected(false);
    warningsCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_errorsCheckBoxMenuItemActionPerformed

  private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemActionPerformed
    loggingTextArea.setText("");
  }//GEN-LAST:event_clearMenuItemActionPerformed

  private void chkBoxEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBoxEnableActionPerformed
    if (chkBoxEnable.isSelected()) {
      commAdapter.enable();
    }
    else {
      commAdapter.disable();
    }
  }//GEN-LAST:event_chkBoxEnableActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel adapterStatusPanel;
  private javax.swing.JCheckBox chkBoxEnable;
  private javax.swing.JMenuItem clearMenuItem;
  private javax.swing.JLabel curPosLbl;
  private javax.swing.JTextField curPosTxt;
  private javax.swing.JLabel curStateLbl;
  private javax.swing.JTextField curStateTxt;
  private javax.swing.JCheckBoxMenuItem errorsCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem everythingCheckBoxMenuItem;
  private javax.swing.JLabel fillingLbl;
  private javax.swing.JMenu filterMenu;
  private javax.swing.JPanel headPanel;
  private javax.swing.JPanel logPanel;
  private javax.swing.JPopupMenu logPopupMenu;
  private javax.swing.JScrollPane logTableScrollPane;
  private javax.swing.JScrollPane logTextScrollPane;
  private javax.swing.JTable loggingTable;
  private javax.swing.JPopupMenu loggingTablePopupMenu;
  private javax.swing.JTextArea loggingTextArea;
  private javax.swing.JLabel logoLbl;
  private javax.swing.JPanel logoPanel;
  private javax.swing.JLabel noVehicleLabel;
  private javax.swing.JPanel noVehiclePanel;
  private javax.swing.JPanel overviewTabPanel;
  private javax.swing.JPanel statusFiguresPanel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JCheckBoxMenuItem warningsCheckBoxMenuItem;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * A <code>ListSelectionListener</code> for handling the logging table
   * selection events.
   */
  private final class RowListener
      implements ListSelectionListener {

    /**
     * Creates a new instance.
     */
    private RowListener() {
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }
      if (loggingTable.getSelectedRow() >= 0) {
        outputLogNotification(loggingTable.getSelectedRow());
      }
      else {
        loggingTextArea.setText("");
      }
    }
  }
}
