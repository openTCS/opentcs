/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelClient;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ReleaseVehicleAction
    extends AbstractAction {

  /**
   * Withdraws the current transport order from a vehicle, disables it and resets its position.
   */
  public static final String ID = "course.vehicle.releaseVehicle";
  /**
   * Property key of the vehicle release confirmation text.
   */
  private static final String MESSAGE_CONFIRM_RELEASE_TEXT = "message.confirmVehicleRelease.text";
  /**
   * Property key of the vehicle release confirmation title.
   */
  private static final String MESSAGE_CONFIRM_RELEASE_TITLE = "message.confirmVehicleRelease.title";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleaseVehicleAction.class);
  /**
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;

  /**
   * Creates a new instance.
   *
   * @param vehicle The selected vehicle.
   * @param applicationFrame The application's main view.
   * @param kernelProvider Provides access to a shared kernel.
   */
  @Inject
  public ReleaseVehicleAction(@Assisted VehicleModel vehicle,
                              @ApplicationFrame JFrame applicationFrame,
                              SharedKernelProvider kernelProvider) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.applicationFrame = requireNonNull(applicationFrame, "applicationFrame");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    try (SharedKernelClient kernelClient = kernelProvider.register()) {
      if (JOptionPane.showConfirmDialog(
          applicationFrame,
          labels.getString(MESSAGE_CONFIRM_RELEASE_TEXT) + " " + vehicleReference(kernelClient.getKernel()).getName(),
          labels.getString(MESSAGE_CONFIRM_RELEASE_TITLE),
          JOptionPane.YES_NO_OPTION)
          == JOptionPane.YES_OPTION) {
        kernelClient.getKernel().releaseVehicle(vehicleReference(kernelClient.getKernel()));
      }
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }

  private TCSObjectReference<Vehicle> vehicleReference(Kernel kernel) {
    return kernel.getTCSObject(Vehicle.class, vehicleModel.getName()).getReference();
  }

}
