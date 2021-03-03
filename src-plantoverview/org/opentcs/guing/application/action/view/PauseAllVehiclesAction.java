/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.drivers.messages.LimitSpeed;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * Action for pausing all vehicles.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PauseAllVehiclesAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "openTCS.pauseAllVehicles";
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;
  /**
   * Whether the vehicles are currently paused or not.
   */
  private boolean paused;

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides the current system model.
   * @param kernelProvider Provides access to a kernel.
   */
  @Inject
  public PauseAllVehiclesAction(ModelManager modelManager,
                                SharedKernelProvider kernelProvider) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    final Object kernelClient = new Object();
    try {
      kernelProvider.register(kernelClient);

      paused = !paused;
      limitVehicleSpeed(paused ? 0 : 100, kernelProvider.getKernel());
    }
    finally {
      kernelProvider.unregister(kernelClient);
    }
  }

  private void limitVehicleSpeed(int speed, Kernel kernel) {
    if (kernel == null) {
      return;
    }
    Logger.getLogger(PauseAllVehiclesAction.class.getName()).warning("Limiting to " + speed);
    ModelComponent folder
        = modelManager.getModel().getMainFolder(SystemModel.FolderKey.VEHICLES);

    if (kernelProvider.kernelShared()) {
      for (ModelComponent component : folder.getChildComponents()) {
        VehicleModel vModel = (VehicleModel) component;
        kernel.sendCommAdapterMessage(vModel.getVehicle().getReference(),
                                      new LimitSpeed(LimitSpeed.Type.RELATIVE_VEHICLE,
                                                     speed));
      }
    }
  }
}
