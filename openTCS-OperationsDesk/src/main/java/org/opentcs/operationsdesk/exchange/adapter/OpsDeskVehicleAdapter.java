/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.exchange.DriveOrderHistory;
import org.opentcs.guing.common.exchange.adapter.VehicleAdapter;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for vehicles specific to the Operations Desk application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OpsDeskVehicleAdapter
    extends VehicleAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpsDeskVehicleAdapter.class);

  /**
   * Keeps track of the drive order components of vehicles.
   */
  private final DriveOrderHistory driveOrderHistory;
  /**
   * Maintains a set of all transport orders.
   */
  private final TransportOrdersContainer transportOrdersContainer;

  @Inject
  public OpsDeskVehicleAdapter(DriveOrderHistory driveOrderHistory,
                               @Nonnull TransportOrdersContainer transportOrdersContainer) {
    this.driveOrderHistory = requireNonNull(driveOrderHistory, "driveOrderHistory");
    this.transportOrdersContainer = requireNonNull(transportOrdersContainer,
                                                   "transportOrdersContainer");
  }

  @Override
  protected void updateModelDriveOrder(TCSObjectService objectService,
                                       Vehicle vehicle,
                                       VehicleModel vehicleModel,
                                       SystemModel systemModel)
      throws CredentialsException {
    TransportOrder transportOrder = getTransportOrder(objectService, vehicle.getTransportOrder());

    if (transportOrder != null) {
      Set<FigureDecorationDetails> driveOrderComponents
          = getDriveOrderComponents(transportOrder.getCurrentDriveOrder(),
                                    vehicle.getRouteProgressIndex(),
                                    systemModel);
      for (FigureDecorationDetails component : driveOrderComponents) {
        component.addVehicleModel(vehicleModel);
      }

      Set<FigureDecorationDetails> finishedComponents
          = driveOrderHistory.updateDriveOrderComponents(vehicleModel.getName(),
                                                         driveOrderComponents);
      for (FigureDecorationDetails component : finishedComponents) {
        component.removeVehicleModel(vehicleModel);
      }

      vehicleModel.setCurrentDriveOrderPath(getCurrentDriveOrderPath(transportOrder.getCurrentDriveOrder(),
                                                                     vehicle.getRouteProgressIndex(),
                                                                     systemModel));
      vehicleModel.setDriveOrderDestination(getCurrentDriveOrderDestination(transportOrder.getCurrentDriveOrder(),
                                                                            systemModel));

      vehicleModel.setDriveOrderState(transportOrder.getState());
    }
    else {
      Set<FigureDecorationDetails> finishedComponents
          = driveOrderHistory.updateDriveOrderComponents(vehicleModel.getName(), new HashSet<>());
      for (FigureDecorationDetails component : finishedComponents) {
        component.removeVehicleModel(vehicleModel);
      }
      vehicleModel.setCurrentDriveOrderPath(null);
      vehicleModel.setDriveOrderDestination(null);
    }
  }

  @Nullable
  private TransportOrder getTransportOrder(TCSObjectService objectService,
                                           TCSObjectReference<TransportOrder> ref)
      throws CredentialsException {
    if (ref == null) {
      return null;
    }
    return transportOrdersContainer.getTransportOrder(ref.getName()).orElse(null);
  }

  private PathModel getCurrentDriveOrderPath(@Nullable DriveOrder driveOrder,
                                             int routeProgressIndex,
                                             SystemModel systemModel) {
    if (driveOrder == null) {
      return null;
    }
    return driveOrder.getRoute().getSteps().stream()
        .skip(Math.max(0, routeProgressIndex + 1))
        .map(step -> step.getPath())
        .filter(path -> path != null)
        .findFirst()
        .map(path -> systemModel.getPathModel(path.getName()))
        .orElse(null);
  }

  private PointModel getCurrentDriveOrderDestination(@Nullable DriveOrder driveOrder,
                                                     SystemModel systemModel) {
    if (driveOrder == null) {
      return null;
    }
    return systemModel.getPointModel(driveOrder.getDestination().getDestination().getName());
  }

  private Set<FigureDecorationDetails> getDriveOrderComponents(@Nullable DriveOrder driveOrder,
                                                               int routeProgressIndex,
                                                               SystemModel systemModel) {
    if (driveOrder == null) {
      return new HashSet<>();
    }

    Set<FigureDecorationDetails> result = new HashSet<>();
    driveOrder.getRoute().getSteps().stream()
        .skip(routeProgressIndex + 1)
        .flatMap(step -> Stream.of(step.getPath(), step.getDestinationPoint()))
        .filter(pointOrPath -> pointOrPath != null) // paths may be null
        .forEach(pointOrPath -> {
          if (pointOrPath instanceof Point) {
            result.add(systemModel.getPointModel(pointOrPath.getName()));
          }
          else if (pointOrPath instanceof Path) {
            result.add(systemModel.getPathModel(pointOrPath.getName()));
          }
        });

    TCSObjectReference<?> ref = driveOrder.getDestination().getDestination();
    if (ref.getReferentClass().isAssignableFrom(Location.class)) {
      result.add(systemModel.getLocationModel(ref.getName()));
    }

    return result;
  }
}
