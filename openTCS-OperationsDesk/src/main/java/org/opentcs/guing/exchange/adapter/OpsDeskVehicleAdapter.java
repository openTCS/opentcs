/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.exchange.DriveOrderHistory;
import org.opentcs.guing.model.FigureDecorationDetails;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.TransportOrdersContainer;

/**
 * An adapter for vehicles specific to the Operations Desk application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OpsDeskVehicleAdapter
    extends VehicleAdapter {

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

      List<ModelComponent> c = composeDriveOrderComponents(transportOrder.getCurrentDriveOrder(),
                                                           vehicle.getRouteProgressIndex(),
                                                           systemModel);
      vehicleModel.setDriveOrderComponents(c);
      vehicleModel.setDriveOrderState(transportOrder.getState());
    }
    else {
      Set<FigureDecorationDetails> finishedComponents
          = driveOrderHistory.updateDriveOrderComponents(vehicleModel.getName(), new HashSet<>());
      for (FigureDecorationDetails component : finishedComponents) {
        component.removeVehicleModel(vehicleModel);
      }

      vehicleModel.setDriveOrderComponents(null);
    }
  }

  @Nullable
  private TransportOrder getTransportOrder(TCSObjectService objectService,
                                           TCSObjectReference<TransportOrder> ref)
      throws CredentialsException {
    if (ref == null) {
      return null;
    }
//    return objectService.fetchObject(TransportOrder.class, ref);
    return transportOrdersContainer.getTransportOrder(ref.getName()).orElse(null);
  }

  /**
   * Extracts the left over course elements from a drive order and progress.
   *
   * @param driveOrder The <code>DriveOrder</code>.
   * @param routeProgressIndex Index of the current position in the drive order.
   * @return List containing the left over course elements or <code>null</code>
   * if driveOrder is <code>null</code>.
   */
  private List<ModelComponent> composeDriveOrderComponents(@Nullable DriveOrder driveOrder,
                                                           int routeProgressIndex,
                                                           SystemModel systemModel) {
    if (driveOrder == null) {
      return null;
    }

    List<ModelComponent> result = new LinkedList<>();
    List<Route.Step> lSteps = driveOrder.getRoute().getSteps();

    ProcessAdapter adapter;
    for (int i = lSteps.size() - 1; i >= 0; i--) {
      if (i == routeProgressIndex) {
        break;
      }

      Route.Step step = lSteps.get(i);
      Path path = step.getPath();
      Point point = step.getDestinationPoint();
      PointModel pointModel = systemModel.getPointModel(point.getName());
      result.add(0, pointModel);

      if (path != null) {
        PathModel pathModel = systemModel.getPathModel(path.getName());
        result.add(0, pathModel);
      }
    }

    TCSObjectReference<?> ref = driveOrder.getDestination().getDestination();
    ModelComponent pointOrLocationModel = systemModel.getModelComponent(ref.getName());
    if (pointOrLocationModel != null) {
      result.add(pointOrLocationModel);
    }

    return result;
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
