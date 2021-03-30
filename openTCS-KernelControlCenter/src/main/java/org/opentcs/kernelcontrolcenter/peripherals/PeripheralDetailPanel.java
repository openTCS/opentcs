/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.opentcs.components.Lifecycle;
import org.opentcs.drivers.peripherals.management.PeripheralCommAdapterPanel;
import org.opentcs.drivers.peripherals.management.PeripheralCommAdapterPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the comm adapter panels for a given peripheral device.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralDetailPanel
    extends JPanel
    implements PropertyChangeListener,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralDetailPanel.class);
  /**
   * A panel's default border title.
   */
  private static final String DEFAULT_BORDER_TITLE = "";
  /**
   * The adapter specific list of panels.
   */
  private final List<PeripheralCommAdapterPanel> customPanelList = new LinkedList<>();
  /**
   * The set of factories to create adapter specific panels with.
   */
  private final Set<PeripheralCommAdapterPanelFactory> panelFactories;
  /**
   * The peripheral device that is currently associated with/being displayed in this panel.
   */
  private LocalPeripheralEntry peripheralEntry;
  /**
   * Whether this panel is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param panelFactories The factories to create adapter specific panels with.
   */
  @Inject
  public PeripheralDetailPanel(Set<PeripheralCommAdapterPanelFactory> panelFactories) {
    this.panelFactories = requireNonNull(panelFactories, "panelFactories");

    initComponents();

    // Make sure we start with an empty panel.
    detachFromEntry();
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    for (PeripheralCommAdapterPanelFactory panelFactory : panelFactories) {
      panelFactory.initialize();
    }

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

    detachFromEntry();

    for (PeripheralCommAdapterPanelFactory panelFactory : panelFactories) {
      panelFactory.terminate();
    }

    initialized = false;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof LocalPeripheralEntry)) {
      return;
    }

    LocalPeripheralEntry entry = (LocalPeripheralEntry) evt.getSource();
    if (!Objects.equals(entry, peripheralEntry)) {
      // Since we're registered only to the currently selected/attached entry, this should never 
      // happen.
      return;
    }

    if (Objects.equals(evt.getPropertyName(), LocalPeripheralEntry.Attribute.PROCESS_MODEL.name())) {
      for (PeripheralCommAdapterPanel panel : customPanelList) {
        panel.processModelChanged(entry.getProcessModel());
      }
    }
  }

  /**
   * Attaches this panel to a peripheral device.
   *
   * @param newPeripheralEntry The peripheral entry to attach to.
   */
  public void attachToEntry(LocalPeripheralEntry newPeripheralEntry) {
    requireNonNull(newPeripheralEntry, "newPeripheralEntry");

    // Clean up first - but only if we're not reattaching to the same entry.
    if (peripheralEntry != newPeripheralEntry) {
      detachFromEntry();
    }
    peripheralEntry = newPeripheralEntry;

    setBorderTitle(peripheralEntry.getProcessModel().getLocation().getName());

    // Ensure the tabbed pane containing peripheral information is shown.
    removeAll();
    add(tabbedPane);

    updateCustomPanels();

    // Update panel contents.
    validate();
    if (!customPanelList.isEmpty()) {
      tabbedPane.setSelectedIndex(0);
    }

    peripheralEntry.addPropertyChangeListener(this);
  }

  /**
   * Detaches this panel from a peripheral device (if it is currently attached to any).
   */
  private void detachFromEntry() {
    if (peripheralEntry != null) {
      peripheralEntry.removePropertyChangeListener(this);
      removeAndClearCustomPanels();
      peripheralEntry = null;
    }
    setBorderTitle(DEFAULT_BORDER_TITLE);
    // Remove the contents of this panel.
    removeAll();
    add(noPeripheralDevicePanel);
    // Update panel contents.
    validate();
  }

  /**
   * Removes the custom panels from this panel's tabbed pane.
   */
  private void removeAndClearCustomPanels() {
    for (PeripheralCommAdapterPanel panel : customPanelList) {
      LOG.debug("Removing {} from tabbedPane.", panel);
      tabbedPane.remove(panel);
    }

    customPanelList.clear();
  }

  /**
   * Update the list of custom panels in the tabbed pane.
   */
  private void updateCustomPanels() {
    removeAndClearCustomPanels();

    if (peripheralEntry == null) {
      return;
    }

    for (PeripheralCommAdapterPanelFactory panelFactory : panelFactories) {
      customPanelList.addAll(panelFactory.getPanelsFor(peripheralEntry.getAttachedCommAdapter(),
                                                       peripheralEntry.getLocation(),
                                                       peripheralEntry.getProcessModel()));
    }

    for (PeripheralCommAdapterPanel curPanel : customPanelList) {
      LOG.debug("Adding {} with title {} to tabbedPane.", curPanel, curPanel.getTitle());
      tabbedPane.addTab(curPanel.getTitle(), curPanel);
    }
  }

  /**
   * Sets this panel's border title.
   *
   * @param title This panel's new border title.
   */
  private void setBorderTitle(String title) {
    requireNonNull(title, "title");
    ((TitledBorder) getBorder()).setTitle(title);
    // Trigger a repaint - the title sometimes looks strange otherwise.
    repaint();
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

    noPeripheralDevicePanel = new javax.swing.JPanel();
    noPeripheralDeviceLabel = new javax.swing.JLabel();
    tabbedPane = new javax.swing.JTabbedPane();

    noPeripheralDevicePanel.setLayout(new java.awt.BorderLayout());

    noPeripheralDeviceLabel.setFont(noPeripheralDeviceLabel.getFont().deriveFont(noPeripheralDeviceLabel.getFont().getSize()+3f));
    noPeripheralDeviceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/kernelcontrolcenter/Bundle"); // NOI18N
    noPeripheralDeviceLabel.setText(bundle.getString("peripheralDetailPanel.label_noPeripheralDeviceAttached.text")); // NOI18N
    noPeripheralDevicePanel.add(noPeripheralDeviceLabel, java.awt.BorderLayout.CENTER);

    setBorder(javax.swing.BorderFactory.createTitledBorder(DEFAULT_BORDER_TITLE));
    setLayout(new java.awt.BorderLayout());

    tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    add(tabbedPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel noPeripheralDeviceLabel;
  private javax.swing.JPanel noPeripheralDevicePanel;
  private javax.swing.JTabbedPane tabbedPane;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

}
