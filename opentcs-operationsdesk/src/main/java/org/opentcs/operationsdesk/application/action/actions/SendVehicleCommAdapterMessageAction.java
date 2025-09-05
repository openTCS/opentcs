// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action.actions;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.operationsdesk.vehicles.SendVehicleCommAdapterMessagePanel;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An action to send a {@link VehicleCommAdapterMessage}.
 */
public class SendVehicleCommAdapterMessageAction
    extends
      AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "actions.sendVehicleCommAdapterMessage";
  private static final Logger LOG
      = LoggerFactory.getLogger(SendVehicleCommAdapterMessageAction.class);
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  private final Component dialogParent;
  private final Provider<SendVehicleCommAdapterMessagePanel> panelProvider;
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param dialogParent The parent component for dialogs shown by this action.
   * @param panelProvider Provides panels for sending {@link VehicleCommAdapterMessage}s.
   * @param portalProvider Provides access to a portal.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public SendVehicleCommAdapterMessageAction(
      @ApplicationFrame
      Component dialogParent,
      Provider<SendVehicleCommAdapterMessagePanel> panelProvider,
      SharedKernelServicePortalProvider portalProvider
  ) {
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    putValue(NAME, BUNDLE.getString("sendVehicleCommAdapterMessageAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    sendVehicleCommAdapterMessage();
  }

  public void sendVehicleCommAdapterMessage() {
    SendVehicleCommAdapterMessagePanel contentPanel = panelProvider.get();
    StandardContentDialog dialog = new StandardContentDialog(dialogParent, contentPanel);
    contentPanel.addInputValidationListener(dialog);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
      return;
    }

    contentPanel.getSelectedVehicle().ifPresent(vehicleName -> {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        Vehicle vehicle = sharedPortal.getPortal().getVehicleService().fetchObject(
            Vehicle.class, vehicleName
        );
        sharedPortal.getPortal().getVehicleService().sendCommAdapterMessage(
            vehicle.getReference(),
            contentPanel.getVehicleCommAdapterMessage()
        );
      }
      catch (ServiceUnavailableException exc) {
        LOG.warn("Could not connect to kernel", exc);
      }
    });
  }
}
