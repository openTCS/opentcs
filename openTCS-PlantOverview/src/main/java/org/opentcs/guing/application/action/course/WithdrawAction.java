/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class WithdrawAction
    extends AbstractAction {

  /**
   * Withdraws the current transport order from a vehicle.
   */
  public static final String ID = "course.vehicle.withdrawTransportOrder";
  public static final String IMMEDIATELY_ID = "course.vehicle.withdrawTransportOrderImmediately";

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WithdrawAction.class);
  /**
   * The vehicles.
   */
  private final Collection<VehicleModel> vehicles;
  /**
   * Resource path to the correct lables.
   */
  private final boolean immediateAbort;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;

  /**
   * Creates a new instance.
   *
   * @param vehicles The selected vehicles.
   * @param immediateAbort Whether or not to abort immediately
   * @param portalProvider Provides access to a shared portal.
   * @param dialogParent The parent component for dialogs shown by this action.
   */
  @Inject
  public WithdrawAction(@Assisted Collection<VehicleModel> vehicles,
                        @Assisted boolean immediateAbort,
                        SharedKernelServicePortalProvider portalProvider,
                        @ApplicationFrame Component dialogParent) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    this.immediateAbort = requireNonNull(immediateAbort, "immediateAbort");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (immediateAbort) {
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      int dialogResult
          = JOptionPane.showConfirmDialog(dialogParent,
                                          bundle.getString("course.vehicle.withdrawTransportOrderImmediately.confirmation.text"),
                                          bundle.getString("course.vehicle.withdrawTransportOrderImmediately.confirmation.title"),
                                          JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.WARNING_MESSAGE);

      if (dialogResult != JOptionPane.OK_OPTION) {
        return;
      }
    }

    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {

      for (VehicleModel vehicle : vehicles) {
        sharedPortal.getPortal().getDispatcherService().withdrawByVehicle(
            vehicle.getVehicle().getReference(), immediateAbort);
      }

    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }

}
