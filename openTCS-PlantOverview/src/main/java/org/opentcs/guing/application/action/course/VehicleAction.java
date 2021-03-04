/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.LocationActionPanel;
import org.opentcs.guing.transport.PointPanel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Different actions are unified here. One can call this class to get
 * a popup menu containing several vehicle action.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleAction
    extends AbstractAction {

  /**
   * Scrolls to a vehicle in the drawing.
   */
  public static final String SCROLL_TO = "course.vehicle.scrollTo";
  /**
   * Automatically moves the drawing so a vehicle is always visible.
   */
  public static final String FOLLOW = "course.vehicle.follow";
  /**
   * Sends a vehicle directly to a point.
   */
  public static final String SEND_TO_POINT = "course.vehicle.sendToPoint";
  /**
   * Sends a vehicle directly to a location.
   */
  public static final String SEND_TO_LOCATION = "course.vehicle.sendToLocation";
  /**
   * Withdraws the current transport order from a vehicle.
   */
  public static final String WITHDRAW_TRANSPORT_ORDER = "course.vehicle.withdrawTransportOrder";
  /**
   * Withdraws the current transport order from a vehicle immediately and sets its ProcState to
   * UNAVAILABLE.
   */
  public static final String WITHDRAW_TRANSPORT_ORDER_IMMEDIATELY = "course.vehicle.withdrawTransportOrderImmediately";
  /**
   * Withdraws the current transport order from a vehicle and sets its
   * ProcState to UNAVAILABLE.
   */
  public static final String WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE = "course.vehicle.withdrawTransportOrderDisableVehicle";
  /**
   * Withdraws the current transport order from a vehicle, disables it and resets its position.
   */
  public static final String RELEASE_VEHICLE = "course.vehicle.releaseVehicle";
  /**
   * Dispatches the vehicle.
   */
  public static final String DISPATCH_VEHICLE = "course.vehicle.dispatchVehicle";
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
  private static final Logger LOG = LoggerFactory.getLogger(VehicleAction.class);
  /**
   * The vehicle.
   */
  private final VehicleModel fVehicle;
  /**
   * The application's main view.
   */
  private final OpenTCSView view;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;
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
   * @param actionId
   * @param vehicle
   * @param view The application's main view.
   * @param kernelProvider Provides access to a kernel.
   * @param drawingEditor The drawing editor.
   * @param modelManager Provides the current system model.
   * @param orderUtil A helper for creating transport orders with the kernel.
   */
  @Inject
  public VehicleAction(@Assisted String actionId,
                       @Assisted VehicleModel vehicle,
                       OpenTCSView view,
                       SharedKernelProvider kernelProvider,
                       OpenTCSDrawingEditor drawingEditor,
                       ModelManager modelManager,
                       TransportOrderUtil orderUtil) {
    this.fVehicle = requireNonNull(vehicle, "vehicle");
    this.view = requireNonNull(view, "view");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");

    ResourceBundleUtil.getBundle().configureAction(this, actionId);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    if (evt.getActionCommand().equals(labels.getString(SCROLL_TO + ".text"))) {
      Figure figure = fVehicle.getFigure();
      OpenTCSDrawingView drawingView = drawingEditor.getActiveView();

      if (drawingView != null && figure != null) {
        drawingView.clearSelection();
        drawingView.addToSelection(figure);
        drawingView.scrollTo(figure);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(FOLLOW + ".text"))) {
      JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) evt.getSource();
      OpenTCSDrawingView drawingView = drawingEditor.getActiveView();

      if (drawingView != null) {
        if (checkBox.isSelected()) {
          drawingView.followVehicle(fVehicle);
        }
        else {
          drawingView.stopFollowVehicle();
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(SEND_TO_POINT + ".text"))) {
      List<PointModel> pointModels = pointModels();

      if (!pointModels.isEmpty()) {
        PointPanel contentPanel = new PointPanel(pointModels);
        StandardContentDialog fDialog = new StandardContentDialog(view, contentPanel);
        contentPanel.addInputValidationListener(fDialog);
        fDialog.setTitle(evt.getActionCommand());
        fDialog.setVisible(true);

        if (fDialog.getReturnStatus() == StandardContentDialog.RET_OK) {
          PointModel point = (PointModel) contentPanel.getSelectedItem();
          orderUtil.createTransportOrder(point, fVehicle);
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(SEND_TO_LOCATION + ".text"))) {
      List<LocationModel> locModels = locationModels();

      if (!locModels.isEmpty()) {
        LocationActionPanel contentPanel = new LocationActionPanel(locModels);
        StandardContentDialog fDialog = new StandardContentDialog(view, contentPanel);
        fDialog.setTitle(evt.getActionCommand());
        fDialog.setVisible(true);

        if (fDialog.getReturnStatus() == StandardContentDialog.RET_OK) {
          LocationModel location = contentPanel.getSelectedLocation();
          List<AbstractFigureComponent> destinationModels = new ArrayList<>();
          destinationModels.add(location);
          List<String> actions = new ArrayList<>();
          actions.add(contentPanel.getSelectedAction());
          orderUtil.createTransportOrder(destinationModels,
                                         actions,
                                         System.currentTimeMillis(),
                                         fVehicle);
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(WITHDRAW_TRANSPORT_ORDER + ".text"))) {
      try {
        kernel().withdrawTransportOrderByVehicle(vehicleReference(), false, false);
      }
      catch (KernelRuntimeException e) {
        LOG.warn("Unexpected exception", e);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(WITHDRAW_TRANSPORT_ORDER_IMMEDIATELY + ".text"))) {
      try {
        kernel().withdrawTransportOrderByVehicle(vehicleReference(), true, true);
      }
      catch (KernelRuntimeException e) {
        LOG.warn("Unexpected exception", e);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE + ".text"))) {
      try {
        kernel().withdrawTransportOrderByVehicle(vehicleReference(), false, true);
      }
      catch (KernelRuntimeException e) {
        LOG.warn("Unexpected exception", e);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(RELEASE_VEHICLE + ".text"))) {
      if (JOptionPane.showConfirmDialog(
          view,
          labels.getString(MESSAGE_CONFIRM_RELEASE_TEXT) + " " + vehicleReference().getName(),
          labels.getString(MESSAGE_CONFIRM_RELEASE_TITLE),
          JOptionPane.YES_NO_OPTION)
          == JOptionPane.YES_OPTION) {
        try {
          kernel().releaseVehicle(vehicleReference());
        }
        catch (KernelRuntimeException e) {
          LOG.warn("Unexpected exception", e);
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(DISPATCH_VEHICLE + ".text"))) {
      try {
        kernel().dispatchVehicle(vehicleReference(), true);
      }
      catch (KernelRuntimeException e) {
        LOG.warn("Unexpected exception", e);
      }
    }
  }

  private Kernel kernel() {
    return kernelProvider.getKernel();
  }

  private TCSObjectReference<Vehicle> vehicleReference() {
    return kernel().getTCSObject(Vehicle.class, fVehicle.getName()).getReference();
  }

  private List<LocationModel> locationModels() {
    return modelManager.getModel().getLocationModels();
  }

  private List<PointModel> pointModels() {
    return modelManager.getModel().getPointModels();
  }
}
