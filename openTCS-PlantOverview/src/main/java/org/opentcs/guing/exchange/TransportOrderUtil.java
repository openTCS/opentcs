/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static com.google.common.base.Preconditions.checkArgument;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for creating transport orders with the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderUtil {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderUtil.class);
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
   * @param destModels The locations or points to visit.
   * @param actions The actions to execute.
   * @param deadline The deadline.
   * @param vModel The vehicle that shall execute this order. Pass
   * <code>null</code> to let the kernel determine one.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(List<AbstractFigureComponent> destModels,
                                   List<String> actions,
                                   long deadline,
                                   VehicleModel vModel) {
    createTransportOrder(destModels, actions, new ArrayList<>(), deadline, vModel);
  }

  /**
   * Creates a new transport order.
   *
   * @param destModels The locations or points to visit.
   * @param actions The actions to execute.
   * @param propertiesList The properties for each destination.
   * @param deadline The deadline.
   * @param vModel The vehicle that shall execute this order. Pass
   * <code>null</code> to let the kernel determine one.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(List<AbstractFigureComponent> destModels,
                                   List<String> actions,
                                   List<Map<String, String>> propertiesList,
                                   long deadline,
                                   VehicleModel vModel) {
    requireNonNull(destModels, "locations");
    requireNonNull(actions, "actions");
    requireNonNull(propertiesList, "propertiesList");
    checkArgument(!destModels.stream()
        .anyMatch(o -> !(o instanceof PointModel || o instanceof LocationModel)),
                  "destModels have to be a PointModel or a Locationmodel");

    List<DestinationCreationTO> destinations = new ArrayList<>();
    for (int i = 0; i < destModels.size(); i++) {
      AbstractFigureComponent locModel = destModels.get(i);
      String action = actions.get(i);
      Map<String, String> properties = new HashMap<>();
      if (!propertiesList.isEmpty()) {
        properties = propertiesList.get(i);
      }
      Location location = getKernel().getTCSObject(Location.class, locModel.getName());
      DestinationCreationTO destination;
      if (location == null) {
        Point point = getKernel().getTCSObject(Point.class, locModel.getName());
        destination = new DestinationCreationTO(point.getName(), action)
            .setDestLocationName(point.getName());
      }
      else {
        destination = new DestinationCreationTO(location.getName(), action)
            .setDestLocationName(location.getName())
            .setProperties(properties);
      }
      destinations.add(destination);
    }

    try {
      TransportOrder tOrder = getKernel()
          .createTransportOrder(new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(),
                                                             destinations)
              .setDeadline(ZonedDateTime.ofInstant(Instant.ofEpochMilli(deadline),
                                                   ZoneId.systemDefault()))
              .setIntendedVehicleName(vModel == null ? null : vModel.getName()));

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      LOG.warn("Unexpected exception", e);
    }
  }

  /**
   * Creates a new transport order by copying the given one.
   *
   * @param pattern The transport order that server as a pattern.
   */
  public void createTransportOrder(TransportOrder pattern) {
    requireNonNull(pattern, "pattern");

    try {
      TransportOrder tOrder = getKernel().createTransportOrder(
          new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(), copyDestinations(pattern))
              .setDeadline(ZonedDateTime.ofInstant(Instant.ofEpochMilli(pattern.getDeadline()),
                                                   ZoneId.systemDefault()))
              .setIntendedVehicleName(pattern.getIntendedVehicle() == null
                  ? null
                  : pattern.getIntendedVehicle().getName())
              .setProperties(pattern.getProperties()));

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      LOG.warn("Unexpected exception", e);
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

    try {
      TransportOrder t = getKernel().createTransportOrder(
          new TransportOrderCreationTO(
              "TOrder-" + UUID.randomUUID(),
              Collections.singletonList(new DestinationCreationTO(pointModel.getName(),
                                                                  DriveOrder.Destination.OP_MOVE)))
              .setDeadline(ZonedDateTime.now())
              .setIntendedVehicleName(vModel.getName())
      );

      getKernel().activateTransportOrder(t.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      LOG.warn("Unexpected exception", e);
    }
  }

  private List<DestinationCreationTO> copyDestinations(TransportOrder original) {
    List<DestinationCreationTO> result = new LinkedList<>();
    for (DriveOrder driveOrder : original.getAllDriveOrders()) {
      result.add(copyDestination(driveOrder));
    }
    return result;
  }

  private DestinationCreationTO copyDestination(DriveOrder driveOrder) {
    return new DestinationCreationTO(driveOrder.getDestination().getDestination().getName(),
                                     driveOrder.getDestination().getOperation())
        .setProperties(driveOrder.getDestination().getProperties());
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
