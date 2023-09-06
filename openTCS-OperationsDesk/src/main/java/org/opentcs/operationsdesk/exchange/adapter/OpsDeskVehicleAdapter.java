/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.exchange.SchedulingHistory;
import org.opentcs.guing.common.exchange.adapter.VehicleAdapter;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for vehicles specific to the Operations Desk application.
 */
public class OpsDeskVehicleAdapter
    extends VehicleAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpsDeskVehicleAdapter.class);
  /**
   * Keeps track of resources/components allocated and claimed by vehicles.
   */
  private final SchedulingHistory schedulingHistory;
  /**
   * Maintains a set of all transport orders.
   */
  private final TransportOrdersContainer transportOrdersContainer;

  @Inject
  public OpsDeskVehicleAdapter(SchedulingHistory schedulingHistory,
                               @Nonnull TransportOrdersContainer transportOrdersContainer) {
    this.schedulingHistory = requireNonNull(schedulingHistory, "schedulingHistory");
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
      vehicleModel.setCurrentDriveOrderPath(
          getCurrentDriveOrderPath(transportOrder.getCurrentDriveOrder(),
                                   vehicle.getRouteProgressIndex(),
                                   systemModel)
      );
      vehicleModel.setDriveOrderDestination(
          getCurrentDriveOrderDestination(transportOrder.getCurrentDriveOrder(),
                                          systemModel)
      );

      vehicleModel.setDriveOrderState(transportOrder.getState());
    }
    else {
      vehicleModel.setCurrentDriveOrderPath(null);
      vehicleModel.setDriveOrderDestination(null);
    }

    Set<FigureDecorationDetails> allocatedAndClaimedComponents
        = getAllocatedAndClaimedComponents(vehicle, systemModel);
    for (FigureDecorationDetails component : allocatedAndClaimedComponents) {
      component.addVehicleModel(vehicleModel);
    }

    Set<FigureDecorationDetails> noLongerAllocatedOrClaimedComponents = schedulingHistory
        .updateAllocatedAndClaimedComponents(vehicleModel.getName(), allocatedAndClaimedComponents);
    for (FigureDecorationDetails component : noLongerAllocatedOrClaimedComponents) {
      component.removeVehicleModel(vehicleModel);
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

    List<Route.Step> routeSteps = driveOrder.getRoute().getSteps();
    return systemModel.getPointModel(
        routeSteps.get(routeSteps.size() - 1).getDestinationPoint().getName()
    );
  }

  private Set<FigureDecorationDetails> getAllocatedAndClaimedComponents(Vehicle vehicle,
                                                                        SystemModel systemModel) {
    return Stream.concat(vehicle.getAllocatedResources().stream(),
                         vehicle.getClaimedResources().stream())
        .flatMap(resourceSet -> resourceSet.stream())
        .filter(this::isResourceRepresentedByFigureDecorationDetails)
        .map(res -> (FigureDecorationDetails) systemModel.getModelComponent(res.getName()))
        .collect(Collectors.toSet());
  }

  private boolean isResourceRepresentedByFigureDecorationDetails(TCSResourceReference<?> ref) {
    return ref.getReferentClass().isAssignableFrom(Point.class)
        || ref.getReferentClass().isAssignableFrom(Path.class)
        || ref.getReferentClass().isAssignableFrom(Location.class);
  }
}
