/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A helper class for creating transport orders with the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderUtil {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(TransportOrderDispatcher.class.getName());
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider Provides a access to a kernel.
   */
  @Inject
  public TransportOrderUtil(SharedKernelProvider kernelProvider) {
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
  }

  /**
   * Creates a new transport order.
   *
   * @param locModels The locations to visit.
   * @param actions The actions to execute.
   * @param deadline The deadline.
   * @param vModel The vehicle that shall execute this order. Pass
   * <code>null</code> to let the kernel determine one.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(List<LocationModel> locModels,
                                   List<String> actions,
                                   long deadline,
                                   VehicleModel vModel) {
    requireNonNull(locModels, "locations");
    requireNonNull(actions, "actions");

    List<DriveOrder.Destination> destinations = new ArrayList<>();
    for (int i = 0; i < locModels.size(); i++) {
      LocationModel locModel = locModels.get(i);
      String action = actions.get(i);
      Location location
          = getKernel().getTCSObject(Location.class, locModel.getName());
      destinations.add(new DriveOrder.Destination(location.getReference(), action));
    }

    try {
      TransportOrder tOrder = getKernel().createTransportOrder(destinations);
      getKernel().setTransportOrderDeadline(tOrder.getReference(), deadline);

      if (vModel != null) {
        Vehicle vehicle = getKernel().getTCSObject(Vehicle.class, vModel.getName());
        getKernel().setTransportOrderIntendedVehicle(tOrder.getReference(),
                                                     vehicle.getReference());
      }

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Creates a new transport order by copying the given one.
   *
   * @param pattern The transport order that server as a pattern.
   */
  public void createTransportOrder(TransportOrder pattern) {
    List<DriveOrder.Destination> destinations = new ArrayList<>();
    List<DriveOrder> driveOrders = new ArrayList<>();
    driveOrders.addAll(pattern.getPastDriveOrders());

    if (pattern.getCurrentDriveOrder() != null) {
      driveOrders.add(pattern.getCurrentDriveOrder());
    }

    driveOrders.addAll(pattern.getFutureDriveOrders());

    for (DriveOrder dOrder : driveOrders) {
      destinations.add(dOrder.getDestination());
    }

    try {
      TransportOrder tOrder = getKernel().createTransportOrder(destinations);
      getKernel().setTransportOrderDeadline(tOrder.getReference(),
                                            pattern.getDeadline());

      if (pattern.getIntendedVehicle() != null) {
        getKernel().setTransportOrderIntendedVehicle(tOrder.getReference(),
                                                     pattern.getIntendedVehicle());
      }

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Creates a new transport order for the purpose to drive to a point.
   *
   * @param pointModel The point that shall be driven to.
   * @param vModel The vehicle to execute this order.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(PointModel pointModel, VehicleModel vModel) {
    requireNonNull(pointModel, "point");
    requireNonNull(vModel, "vehicle");

    // This is only allowed in operating mode.
    if (getKernel().getState() != Kernel.State.OPERATING) {
      return;
    }

    TCSObjectReference<Location> locRef
        = TCSObjectReference.getDummyReference(Location.class, pointModel.getName());
    List<DriveOrder.Destination> dest
        = Collections.singletonList(new DriveOrder.Destination(locRef, DriveOrder.Destination.OP_MOVE));

    try {
      TransportOrder t = getKernel().createTransportOrder(dest);
      getKernel().setTransportOrderDeadline(t.getReference(),
                                            System.currentTimeMillis());

      Vehicle vehicle = getKernel().getTCSObject(Vehicle.class, vModel.getName());
      getKernel().setTransportOrderIntendedVehicle(t.getReference(),
                                                   vehicle.getReference());

      getKernel().activateTransportOrder(t.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Returns the kernel.
   *
   * @return The kernel.
   */
  private Kernel getKernel() {
    return kernelProvider.getKernel();
  }

}
