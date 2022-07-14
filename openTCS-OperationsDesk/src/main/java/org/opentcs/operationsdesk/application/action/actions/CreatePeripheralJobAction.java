/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.AbstractAction;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.operationsdesk.transport.CreatePeripheralJobPanel;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action to trigger the creation of a peripheral job.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class CreatePeripheralJobAction
    extends AbstractAction {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CreatePeripheralJobAction.class);
  /**
   * This action class's ID.
   */
  public static final String ID = "actions.createPeripheralJob";
  /**
   * Access to the resource bundle.
   */
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * The parent component for dialogs show by this action.
   */
  private final Component dialogParent;
  /**
   * Provides panels for entering a new peripheral job.
   */
  private final Provider<CreatePeripheralJobPanel> jobPanelProvider;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param dialogParent The parent for dialogs shown by this action.
   * @param peripheralJobPanel Provides panels for entering new peripheral jobs.
   * @param portalProvider Provides access to the kernel service portal.
   */
  @Inject
  public CreatePeripheralJobAction(@ApplicationFrame Component dialogParent,
                                   Provider<CreatePeripheralJobPanel> peripheralJobPanel,
                                   SharedKernelServicePortalProvider portalProvider) {
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.jobPanelProvider = requireNonNull(peripheralJobPanel, "peripheralJobPanel");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    putValue(NAME, BUNDLE.getString("createPeripheralJobAction.name"));
    putValue(MNEMONIC_KEY, Integer.valueOf('P'));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CreatePeripheralJobPanel contentPanel = jobPanelProvider.get();
    StandardContentDialog dialog = new StandardContentDialog(dialogParent, contentPanel);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
      return;
    }

    PeripheralJobCreationTO job
        = new PeripheralJobCreationTO("Job-",
                                      contentPanel.getReservationToken(),
                                      contentPanel.getPeripheralOperation())
            .withIncompleteName(true);

    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      sharedPortal.getPortal().getPeripheralJobService().createPeripheralJob(job);
      sharedPortal.getPortal().getPeripheralDispatcherService().dispatch();
    }
    catch (KernelRuntimeException exception) {
      LOG.warn("Unexpected exception", exception);
    }
  }
}
