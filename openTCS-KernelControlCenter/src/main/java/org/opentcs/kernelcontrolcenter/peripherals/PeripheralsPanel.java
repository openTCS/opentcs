/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.common.peripherals.NullPeripheralCommAdapterDescription;
import org.opentcs.components.kernelcontrolcenter.ControlCenterPanel;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import static org.opentcs.kernelcontrolcenter.I18nKernelControlCenter.BUNDLE_PATH;
import org.opentcs.kernelcontrolcenter.util.SingleCellEditor;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.gui.BoundsPopupMenuListener;
import org.opentcs.util.gui.StringTableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel containing all locations representing peripheral devices.
 */
public class PeripheralsPanel
    extends ControlCenterPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralsPanel.class);
  /**
   * The service portal to use for kernel interaction.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The call wrapper to use.
   */
  private final CallWrapper callWrapper;
  /**
   * The pool of peripheral devices.
   */
  private final LocalPeripheralEntryPool peripheralEntryPool;
  /**
   * The details panel.
   */
  private final PeripheralDetailPanel detailPanel;
  /**
   * The table row sorter to use.
   */
  private TableRowSorter<PeripheralTableModel> sorter;
  /**
   * This instance's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates new instance.
   *
   * @param servicePortal The service portal to use for kernel interaction.
   * @param callWrapper The call wrapper to use for publishing events to the kernel.
   * @param peripheralEntryPool The pool of peripheral devices.
   * @param detailPanel The details panel.
   */
  @Inject
  public PeripheralsPanel(@Nonnull KernelServicePortal servicePortal,
                          @Nonnull @ServiceCallWrapper CallWrapper callWrapper,
                          @Nonnull LocalPeripheralEntryPool peripheralEntryPool,
                          @Nonnull PeripheralDetailPanel detailPanel) {
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.peripheralEntryPool = requireNonNull(peripheralEntryPool, "peripheralEntryPool");
    this.detailPanel = requireNonNull(detailPanel, "detailPanel");

    initComponents();

    peripheralTable.setDefaultRenderer(PeripheralCommAdapterDescription.class,
                                       new CommAdapterFactoryTableCellRenderer());

    peripheralDetailsPanel.add(detailPanel);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    // Verify that the kernel is in a state in which controlling peripherals is possible.
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

    peripheralEntryPool.initialize();
    detailPanel.initialize();

    EventQueue.invokeLater(() -> {
      initPeripheralTable();
    });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    detailPanel.terminate();
    peripheralEntryPool.terminate();

    initialized = false;
  }

  private void initPeripheralTable() {
    PeripheralTableModel model = new PeripheralTableModel(servicePortal, callWrapper);
    peripheralTable.setModel(model);
    peripheralTable.getColumnModel().getColumn(PeripheralTableModel.COLUMN_ADAPTER).setCellRenderer(
        new StringTableCellRenderer<PeripheralCommAdapterDescription>(
            description -> description.getDescription()
        )
    );
    sorter = new TableRowSorter<>(model);
    peripheralTable.setRowSorter(sorter);

    peripheralEntryPool.getEntries().forEach((location, entry) -> {
      model.addData(entry);
      entry.addPropertyChangeListener(model);
    });

    initComboBoxes();
    updateRowFilter();
  }

  private void initComboBoxes() {
    SingleCellEditor adpaterCellEditor = new SingleCellEditor(peripheralTable);

    int index = 0;
    for (LocalPeripheralEntry entry : peripheralEntryPool.getEntries().values()) {
      initCommAdaptersComboBox(entry, index, adpaterCellEditor);
      index++;
    }

    peripheralTable.getColumn(PeripheralTableModel.adapterColumnIdentifier())
        .setCellEditor(adpaterCellEditor);
  }

  private void initCommAdaptersComboBox(LocalPeripheralEntry peripheralEntry,
                                        int rowIndex,
                                        SingleCellEditor adapterCellEditor) {
    final PeripheralAdapterComboBox comboBox = new PeripheralAdapterComboBox();
    PeripheralAttachmentInformation ai;
    try {
      ai = callWrapper.call(() -> servicePortal.getPeripheralService().fetchAttachmentInformation(
          peripheralEntry.getLocation()
      ));
    }
    catch (Exception ex) {
      LOG.warn("Error fetching attachment information for {}",
               peripheralEntry.getLocation().getName(),
               ex
      );
      return;
    }

    for (PeripheralCommAdapterDescription factory : ai.getAvailableCommAdapters()) {
      comboBox.addItem(factory);
    }
    if (ai.getAvailableCommAdapters().isEmpty()) {
      comboBox.addItem(new NullPeripheralCommAdapterDescription());
    }

    // Set the selection to the attached comm adapter. (The peripheral is already attached to a comm
    // adapter due to auto attachment on startup.)
    comboBox.setSelectedItem(ai.getAttachedCommAdapter());

    comboBox.setRenderer(new AdapterFactoryCellRenderer());
    comboBox.addPopupMenuListener(new BoundsPopupMenuListener());
    comboBox.addItemListener((ItemEvent evt) -> {
      if (evt.getStateChange() == ItemEvent.DESELECTED) {
        return;
      }

      // If we selected a comm adapter that's already attached, do nothing.
      if (Objects.equals(evt.getItem(), peripheralEntry.getAttachedCommAdapter())) {
        LOG.debug("{} is already attached to: {}",
                  peripheralEntry.getLocation().getName(),
                  evt.getItem()
        );
        return;
      }

      int reply = JOptionPane.showConfirmDialog(
          null,
          bundle.getString("peripheralsPanel.optionPane_driverChangeConfirmation.message"),
          bundle.getString("peripheralsPanel.optionPane_driverChangeConfirmation.title"),
          JOptionPane.YES_NO_OPTION);
      if (reply == JOptionPane.NO_OPTION) {
        return;
      }

      PeripheralCommAdapterDescription factory = comboBox.getSelectedItem();
      try {
        callWrapper.call(() -> servicePortal.getPeripheralService().attachCommAdapter(
            peripheralEntry.getLocation(),
            factory
        ));
      }
      catch (Exception ex) {
        LOG.warn("Error attaching adapter {} to vehicle {}",
                 factory,
                 peripheralEntry.getLocation().getName(),
                 ex);
        return;
      }
      LOG.info("Attaching comm adapter {} to {}", factory, peripheralEntry.getLocation().getName());
    });
    adapterCellEditor.setEditorAt(rowIndex, new DefaultCellEditor(comboBox));

    peripheralEntry.addPropertyChangeListener(comboBox);
  }

  private void updateRowFilter() {
    sorter.setRowFilter(RowFilter.andFilter(toRegexFilters()));
  }

  private List<RowFilter<PeripheralTableModel, Integer>> toRegexFilters() {
    List<RowFilter<PeripheralTableModel, Integer>> result = new ArrayList<>();
    result.add(RowFilter.regexFilter(".*", PeripheralTableModel.COLUMN_ADAPTER));
    if (hideDetachedLocationsCheckBox.isSelected()) {
      result.add(RowFilter.notFilter(
          RowFilter.regexFilter(NullPeripheralCommAdapterDescription.class.getSimpleName(),
                                PeripheralTableModel.COLUMN_ADAPTER))
      );
    }

    return result;
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

    listPanel = new javax.swing.JPanel();
    filterPanel = new javax.swing.JPanel();
    hideDetachedLocationsCheckBox = new javax.swing.JCheckBox();
    jScrollPane1 = new javax.swing.JScrollPane();
    peripheralTable = new javax.swing.JTable();
    peripheralDetailsPanel = new javax.swing.JPanel();

    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

    listPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peripheral devices in model"));
    listPanel.setMaximumSize(new java.awt.Dimension(464, 2147483647));
    listPanel.setMinimumSize(new java.awt.Dimension(464, 425));
    listPanel.setPreferredSize(new java.awt.Dimension(464, 424));
    listPanel.setLayout(new java.awt.BorderLayout());

    filterPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    hideDetachedLocationsCheckBox.setSelected(true);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/kernelcontrolcenter/Bundle"); // NOI18N
    hideDetachedLocationsCheckBox.setText(bundle.getString("peripheralsPanel.checkBox_hideDetachedLocations.text")); // NOI18N
    hideDetachedLocationsCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        hideDetachedLocationsCheckBoxActionPerformed(evt);
      }
    });
    filterPanel.add(hideDetachedLocationsCheckBox);

    listPanel.add(filterPanel, java.awt.BorderLayout.NORTH);

    peripheralTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        peripheralTableMouseClicked(evt);
      }
    });
    jScrollPane1.setViewportView(peripheralTable);

    listPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    add(listPanel);

    peripheralDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peripheral details"));
    peripheralDetailsPanel.setPreferredSize(new java.awt.Dimension(800, 23));
    peripheralDetailsPanel.setLayout(new java.awt.BorderLayout());
    add(peripheralDetailsPanel);

    getAccessibleContext().setAccessibleName(bundle.getString("peripheralsPanel.accessibleName")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON

  private void peripheralTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peripheralTableMouseClicked
    if (evt.getClickCount() == 2) {
      int index = peripheralTable.getSelectedRow();
      if (index >= 0) {
        PeripheralTableModel model = (PeripheralTableModel) peripheralTable.getModel();
        LocalPeripheralEntry selectedEntry
            = model.getDataAt(peripheralTable.convertRowIndexToModel(index));
        detailPanel.attachToEntry(selectedEntry);
      }
    }
  }//GEN-LAST:event_peripheralTableMouseClicked

  private void hideDetachedLocationsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideDetachedLocationsCheckBoxActionPerformed
    updateRowFilter();
  }//GEN-LAST:event_hideDetachedLocationsCheckBoxActionPerformed

  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel filterPanel;
  private javax.swing.JCheckBox hideDetachedLocationsCheckBox;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JPanel listPanel;
  private javax.swing.JPanel peripheralDetailsPanel;
  private javax.swing.JTable peripheralTable;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
