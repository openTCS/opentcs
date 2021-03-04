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
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.PointPanel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SendVehicleToPointAction
    extends AbstractAction {

  /**
   * Sends a vehicle directly to a point.
   */
  public static final String ID = "course.vehicle.sendToPoint";
  /**
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * A helper for creating transport orders with the kernel.
   */
  private final TransportOrderUtil orderUtil;

  /**
   * Creates a new instance.
   *
   * @param vehicle The selected vehicle.
   * @param applicationFrame The application's main view.
   * @param modelManager Provides the current system model.
   * @param orderUtil A helper for creating transport orders with the kernel.
   */
  @Inject
  public SendVehicleToPointAction(@Assisted VehicleModel vehicle,
                                  @ApplicationFrame JFrame applicationFrame,
                                  ModelManager modelManager,
                                  TransportOrderUtil orderUtil) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.applicationFrame = requireNonNull(applicationFrame, "applicationFrame");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    List<PointModel> pointModels = pointModels();

    if (!pointModels.isEmpty()) {
      PointPanel contentPanel = new PointPanel(pointModels);
      StandardContentDialog fDialog = new StandardContentDialog(applicationFrame, contentPanel);
      contentPanel.addInputValidationListener(fDialog);
      fDialog.setTitle(evt.getActionCommand());
      fDialog.setVisible(true);

      if (fDialog.getReturnStatus() == StandardContentDialog.RET_OK) {
        PointModel point = (PointModel) contentPanel.getSelectedItem();
        orderUtil.createTransportOrder(point, vehicleModel);
      }
    }
  }

  private List<PointModel> pointModels() {
    return modelManager.getModel().getPointModels();
  }
}
