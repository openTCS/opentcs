/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.vehicles;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.AttachmentEvent;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.ProcessModelEvent;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanelFactory;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays information about a vehicle (VehicleModel) graphically.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DetailPanel
    extends JPanel
    implements EventHandler,
               Lifecycle {

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
  private final List<VehicleCommAdapterPanel> customPanelList = new LinkedList<>();
  /**
   * The set of factories to create adapter specific panels.
   */
  private final Set<VehicleCommAdapterPanelFactory> panelFactories;
  /**
   * The logging table model to use.
   */
  private final LogTableModel loggingTableModel = new LogTableModel();
  /**
   * Where this instance registers for application events.
   */
  private final EventSource eventSource;
  /**
   * The service portal to use for kernel interaction.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * The vehicle model of the vehicle current associated with this window.
   */
  private LocalVehicleEntry vehicleEntry;
  /**
   * The comm adapter currently attached to the vehicle (model).
   */
  private AttachmentInformation attachmentInfo;
  /**
   * Whether this panel is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param servicePortal The service portal to use for kernel interaction.
   * @param callWrapper The call wrapper to use for service calls.
   * @param eventSource Where this instance registers for application events.
   * @param panelFactories The factories to create adapter specific panels.
   */
  @Inject
  public DetailPanel(KernelServicePortal servicePortal,
                     @ServiceCallWrapper CallWrapper callWrapper,
                     @ApplicationEventBus EventSource eventSource,
                     Set<VehicleCommAdapterPanelFactory> panelFactories) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.panelFactories = requireNonNull(panelFactories, "panelFactories");

    initComponents();

    loggingTable.setModel(loggingTableModel);
    loggingTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    loggingTable.getColumnModel().getColumn(1).setPreferredWidth(110);
    loggingTable.getSelectionModel().addListSelectionListener(new RowListener());
    // Make sure we start with an empty panel.
    detachFromVehicle();
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    for (VehicleCommAdapterPanelFactory fatory : panelFactories) {
      fatory.initialize();
    }

    eventSource.subscribe(this);

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

    detachFromVehicle();

    eventSource.unsubscribe(this);

    for (VehicleCommAdapterPanelFactory fatory : panelFactories) {
      fatory.terminate();
    }

    initialized = false;
  }

  @Override
  public void onEvent(Object e) {
    // Ignore events if no vehicle entry is associated with this panel.
    if (vehicleEntry == null) {
      return;
    }

    if (e instanceof AttachmentEvent) {
      AttachmentEvent event = (AttachmentEvent) e;
      if (Objects.equals(vehicleEntry.getVehicleName(),
                         event.getVehicleName())) {
        updateFromVehicleEntry(event);
      }
    }
    if (e instanceof ProcessModelEvent) {
      ProcessModelEvent event = (ProcessModelEvent) e;
      if (Objects.equals(vehicleEntry.getVehicleName(),
                         event.getUpdatedProcessModel().getVehicleName())) {
        updateFromVehicleProcessModel(event);

        // Forward event to the comm adapter panels
        customPanelList.forEach(panel -> panel.processModelChange(event.getAttributeChanged(),
                                                                  event.getUpdatedProcessModel()));
      }
    }
  }

  /**
   * Attaches this panel to a vehicle.
   *
   * @param newVehicleEntry The vehicle entry to attach to.
   */
  void attachToVehicle(LocalVehicleEntry newVehicleEntry) {
    requireNonNull(newVehicleEntry, "newVehicleEntry");

    // Clean up first - but only if we're not reattaching the vehicle model which is already 
    // attached to this panel.
    if (vehicleEntry != newVehicleEntry) {
      detachFromVehicle();
    }
    vehicleEntry = newVehicleEntry;

    setBorderTitle(vehicleEntry.getVehicleName());

    // Ensure the tabbed pane containing vehicle information is shown.
    removeAll();
    add(tabbedPane);

    // Init vehicle status.
    loggingTableModel.clear();
    for (UserNotification notification : vehicleEntry.getProcessModel().getNotifications()) {
      loggingTableModel.addRow(notification);
    }
    initVehicleEntryAttributes();

    // Update panel contents.
    validate();
    tabbedPane.setSelectedIndex(0);
  }

  /**
   * Detaches this panel from a vehicle (if it is currently attached to any).
   */
  private void detachFromVehicle() {
    if (vehicleEntry != null) {
      // Remove all custom panels and let the comm adapter know we don't need them any more.
      removeCustomPanels();
      if (attachmentInfo != null) {
        attachmentInfo = null;
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
    updateCommAdapter(vehicleEntry.getAttachmentInformation());
    updateCommAdapterEnabled(vehicleEntry.getProcessModel().isCommAdapterEnabled());
    updateVehiclePosition(vehicleEntry.getProcessModel().getVehiclePosition());
    updateVehicleState(vehicleEntry.getProcessModel().getVehicleState());
  }

  private void updateFromVehicleEntry(AttachmentEvent evt) {
    updateCommAdapter(evt.getUpdatedAttachmentInformation());
  }

  private void updateFromVehicleProcessModel(ProcessModelEvent evt) {
    if (Objects.equals(evt.getAttributeChanged(), VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED.name())) {
      updateCommAdapterEnabled(evt.getUpdatedProcessModel().isCommAdapterEnabled());
    }
    else if (Objects.equals(evt.getAttributeChanged(), VehicleProcessModel.Attribute.POSITION.name())) {
      updateVehiclePosition(evt.getUpdatedProcessModel().getVehiclePosition());
    }
    else if (Objects.equals(evt.getAttributeChanged(), VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState(evt.getUpdatedProcessModel().getVehicleState());
    }
    else if (Objects.equals(evt.getAttributeChanged(),
                            VehicleProcessModel.Attribute.USER_NOTIFICATION.name())) {
      updateUserNotification(evt.getUpdatedProcessModel().getNotifications());
    }
  }

  private void updateCommAdapter(AttachmentInformation newAttachmentInfo) {
    SwingUtilities.invokeLater(() -> {
      // If there was a comm adapter and it changed, we need to clean up a few things first.
      if (attachmentInfo != null) {
        // Detach all custom panels of the old comm adapter.
        removeCustomPanels();
        customPanelList.clear();
      }
      // Update the comm adapter reference.
      attachmentInfo = newAttachmentInfo;
      // If we have a new comm adapter, set up a few things.
      if (attachmentInfo != null) {
        // Update the custom panels displayed.
        updateCustomPanels();
      }
      chkBoxEnable.setEnabled(attachmentInfo != null);
      chkBoxEnable.setSelected(attachmentInfo != null
          && vehicleEntry.getProcessModel().isCommAdapterEnabled());
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

  private void updateUserNotification(Queue<UserNotification> notifications) {
    SwingUtilities.invokeLater(() -> {
      loggingTableModel.clear();
      notifications.forEach(notification -> loggingTableModel.addRow(notification));
    });
  }

  /**
   * Update the list of custom panels in the tabbed pane.
   */
  private void updateCustomPanels() {
    for (VehicleCommAdapterPanel curPanel : customPanelList) {
      LOG.debug("Removing {} from tabbedPane.", curPanel);
      tabbedPane.remove(curPanel);
    }
    customPanelList.clear();
    if (attachmentInfo != null) {
      for (VehicleCommAdapterPanelFactory panelFactory : panelFactories) {
        customPanelList.addAll(panelFactory.getPanelsFor(vehicleEntry.getAttachedCommAdapterDescription(),
                                                         vehicleEntry.getAttachmentInformation().getVehicleReference(),
                                                         vehicleEntry.getProcessModel()));
        for (VehicleCommAdapterPanel curPanel : customPanelList) {
          LOG.debug("Adding {} with title {} to tabbedPane.", curPanel, curPanel.getTitle());
          tabbedPane.addTab(curPanel.getTitle(), curPanel);
        }
      }
    }
  }

  /**
   * Removes the custom panels from this panel's tabbed pane.
   */
  private void removeCustomPanels() {
    LOG.debug("Setting selected component of tabbedPane to overviewTabPanel.");
    tabbedPane.setSelectedComponent(overviewTabPanel);
    for (VehicleCommAdapterPanel panel : customPanelList) {
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

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/kernelcontrolcenter/Bundle"); // NOI18N
        clearMenuItem.setText(bundle.getString("detailPanel.popupMenu_messageDetails.menuItem_clear.text")); // NOI18N
        clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMenuItemActionPerformed(evt);
            }
        });
        logPopupMenu.add(clearMenuItem);

        filterMenu.setText(bundle.getString("detailPanel.popupMenu_messagesTable.subMenu_filter.text")); // NOI18N
        filterMenu.setActionCommand(" message filtering");

        everythingCheckBoxMenuItem.setText(bundle.getString("detailPanel.popupMenu_messagesTable.subMenu_filter.menuItem_showAll.text")); // NOI18N
        everythingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                everythingCheckBoxMenuItemActionPerformed(evt);
            }
        });
        filterMenu.add(everythingCheckBoxMenuItem);

        warningsCheckBoxMenuItem.setText(bundle.getString("detailPanel.popupMenu_messagesTable.subMenu_filter.menuItem_showErrorsAndWarnings.text")); // NOI18N
        warningsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningsCheckBoxMenuItemActionPerformed(evt);
            }
        });
        filterMenu.add(warningsCheckBoxMenuItem);

        errorsCheckBoxMenuItem.setText(bundle.getString("detailPanel.popupMenu_messagesTable.subMenu_filter.menuItem_showErrors.text")); // NOI18N
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
        noVehicleLabel.setText(bundle.getString("detailPanel.label_noVehicleAttached.text")); // NOI18N
        noVehiclePanel.add(noVehicleLabel, java.awt.BorderLayout.CENTER);

        setBorder(javax.swing.BorderFactory.createTitledBorder(DEFAULT_BORDER_TITLE));
        setLayout(new java.awt.BorderLayout());

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        overviewTabPanel.setLayout(new java.awt.GridBagLayout());

        headPanel.setLayout(new java.awt.BorderLayout());

        statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.LINE_AXIS));

        adapterStatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("detailPanel.panel_adapterStatus.border.title"))); // NOI18N
        adapterStatusPanel.setLayout(new java.awt.BorderLayout());

        chkBoxEnable.setText(bundle.getString("detailPanel.checkBox_enableAdapter.text")); // NOI18N
        chkBoxEnable.setEnabled(false);
        chkBoxEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBoxEnableActionPerformed(evt);
            }
        });
        adapterStatusPanel.add(chkBoxEnable, java.awt.BorderLayout.CENTER);

        statusPanel.add(adapterStatusPanel);

        statusFiguresPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("detailPanel.panel_vehicleStatus.border.title"))); // NOI18N
        statusFiguresPanel.setLayout(new java.awt.GridBagLayout());

        curPosLbl.setText(bundle.getString("detailPanel.label_currentPosition.text")); // NOI18N
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

        curStateLbl.setText(bundle.getString("detailPanel.label_currentState.text")); // NOI18N
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
    logoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/kernelcontrolcenter/res/logos/opentcs_logo.gif"))); // NOI18N
        logoPanel.add(logoLbl, java.awt.BorderLayout.CENTER);

        headPanel.add(logoPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        overviewTabPanel.add(headPanel, gridBagConstraints);

        logPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("detailPanel.panel_messages.border.title"))); // NOI18N
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

        tabbedPane.addTab(bundle.getString("detailPanel.tab_generalStatus.text"), overviewTabPanel); // NOI18N

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
    try {
      if (chkBoxEnable.isSelected()) {
        callWrapper.call(() -> servicePortal.getVehicleService()
            .enableCommAdapter(vehicleEntry.getAttachmentInformation().getVehicleReference()));
      }
      else {
        callWrapper.call(() -> servicePortal.getVehicleService()
            .disableCommAdapter(vehicleEntry.getAttachmentInformation().getVehicleReference()));
      }
    }
    catch (Exception ex) {
      LOG.warn("Error enabling/disabling comm adapter for {}", vehicleEntry.getVehicleName(), ex);
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
   * A <code>ListSelectionListener</code> for handling the logging table selection events.
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
