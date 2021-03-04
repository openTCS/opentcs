/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.SchedulerAllocationState;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to display the allocated resources of each vehicle with atleast one allocation.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ResourceAllocationPanel
    extends PluggablePanel
    implements EventHandler {

  /**
   * This class' logger:
   */
  private static final Logger LOG = LoggerFactory.getLogger(ResourceAllocationPanel.class);
  /**
   * The kernel to query allocations from.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * Where we register for events.
   */
  private final EventSource eventSource;
  /**
   * The client that is registered with the kernel provider.
   */
  private SharedKernelServicePortal sharedPortal;
  /**
   * Whether this panel was initialized.
   */
  private boolean initialized;
  /**
   * If the table model should update its contents if an event arrives.
   */
  private boolean enableUpdates = true;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider The kernel provider.
   * @param eventSource Where this instance registers for events.
   */
  @Inject
  public ResourceAllocationPanel(SharedKernelServicePortalProvider kernelProvider,
                                 @ApplicationEventBus EventSource eventSource) {
    this.portalProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    initComponents();
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized - skipping.");
      return;
    }
    // Register event listener in the kernel.
    try {
      sharedPortal = portalProvider.register();
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Kernel unavailable", exc);
      return;
    }

    eventSource.subscribe(this);

    // Trigger an update to the table model.
    handleVehicleStateChange(sharedPortal.getPortal().getSchedulerService().fetchSchedulerAllocations());

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Already terminated - skipping.");
      return;
    }
    // Remove event listener in the kernel.
    eventSource.subscribe(this);
    sharedPortal.close();

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    requireNonNull(event, "event");

    //Skip event if we dont want any updates
    if (!enableUpdates) {
      return;
    }
    //Skip non object events as were only interested in vehicle updates
    if (!(event instanceof TCSObjectEvent)) {
      LOG.debug("Event is not a TCSObjectEvent, ignoring.");
      return;
    }
    //Skip non vehicle events
    TCSObjectEvent tcsObjectEvent = (TCSObjectEvent) event;
    if (!(tcsObjectEvent.getCurrentOrPreviousObjectState() instanceof Vehicle)) {
      LOG.debug("TCSObjectEvent is not about a Vehicle, ignoring.");
      return;
    }
    //Check if we have access to the kernel
    if (portalProvider == null || !portalProvider.portalShared()) {
      LOG.debug("No connection to the kernel but received an event.");
      return;
    }

    // Ignore events if we're not operating or connected. (Vehicle objects may change a lot in modelling mode.)
    if (sharedPortal.getPortal().getState() != Kernel.State.OPERATING) {
      LOG.debug("Kernel is not in operating mode - skipping.");
      return;
    }
    handleVehicleStateChange(sharedPortal.getPortal().getSchedulerService().fetchSchedulerAllocations());
  }

  /**
   * Handles a vehicle update.
   * Queries the kernel for the resource allocations of all vehicles and updates the table model.
   *
   * @param vehicle The vehicle which changed
   */
  private void handleVehicleStateChange(SchedulerAllocationState allocationState) {
    if (allocationState == null) {
      LOG.debug("Kernel did not answer to the scheduled allocations query.");
      return;
    }

    ((AllocationTreeModel) allocationTable.getModel()).updateAllocations(allocationState.getAllocationStates());
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionsPanel = new javax.swing.JPanel();
        enableUpdatesCheckbox = new javax.swing.JCheckBox();
        allocationScrollPane = new javax.swing.JScrollPane();
        allocationTable = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        enableUpdatesCheckbox.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels"); // NOI18N
        enableUpdatesCheckbox.setText(bundle.getString("ResourceAllocationPanel.disableUpdates.text")); // NOI18N
        enableUpdatesCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableUpdatesCheckboxActionPerformed(evt);
            }
        });
        optionsPanel.add(enableUpdatesCheckbox, new java.awt.GridBagConstraints());

        add(optionsPanel, java.awt.BorderLayout.PAGE_START);

        allocationTable.setModel(new AllocationTreeModel());
        allocationTable.setCellRenderer(new AllocationTreeCellRenderer());
        allocationScrollPane.setViewportView(allocationTable);

        add(allocationScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  private void enableUpdatesCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableUpdatesCheckboxActionPerformed
    enableUpdates = enableUpdatesCheckbox.isSelected();
  }//GEN-LAST:event_enableUpdatesCheckboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane allocationScrollPane;
    protected javax.swing.JTree allocationTable;
    private javax.swing.JCheckBox enableUpdatesCheckbox;
    private javax.swing.JPanel optionsPanel;
    // End of variables declaration//GEN-END:variables
}
