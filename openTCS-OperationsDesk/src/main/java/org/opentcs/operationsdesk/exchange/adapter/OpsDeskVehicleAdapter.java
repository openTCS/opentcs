/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import static java.util.Objects.requireNonNull;
import static org.opentcs.data.order.TransportOrder.State.WITHDRAWN;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.exchange.AllocationHistory;
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
   * Keeps track of the resources claimed and allocated by vehicles.
   */
  private final AllocationHistory allocationHistory;
  /**
   * Maintains a set of all transport orders.
   */
  private final TransportOrdersContainer transportOrdersContainer;

  @Inject
  public OpsDeskVehicleAdapter(AllocationHistory allocationHistory,
                               @Nonnull TransportOrdersContainer transportOrdersContainer) {
    this.allocationHistory = requireNonNull(allocationHistory, "allocationHistory");
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
      vehicleModel.setCurrentDriveOrderPath(getCurrentDriveOrderPath(vehicle, systemModel));
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

    updateAllocationStates(vehicle, systemModel, vehicleModel);
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

  private PathModel getCurrentDriveOrderPath(Vehicle vehicle, SystemModel systemModel) {
    if (!vehicle.isProcessingOrder()) {
      return null;
    }

    return Stream.concat(vehicle.getAllocatedResources().stream(),
                         vehicle.getClaimedResources().stream())
        .dropWhile(
            resources -> !containsPointWithName(resources, vehicle.getCurrentPosition().getName())
        )
        // Skip the resource set containing the vehicle's current position.
        .skip(1)
        // Get the resource set after the one containing the vehicle's current position.
        .findFirst()
        .map(resourceSet -> extractPath(resourceSet))
        .map(path -> systemModel.getPathModel(path.getName()))
        .orElse(null);
  }

  private boolean containsPointWithName(Set<TCSResourceReference<?>> resources, String pointName) {
    return resources.stream()
        .filter(resource -> resource.getReferentClass().isAssignableFrom(Point.class))
        .anyMatch(resource -> Objects.equals(resource.getName(), pointName));
  }

  private TCSResourceReference<?> extractPath(Set<TCSResourceReference<?>> resources) {
    return resources.stream()
        .filter(resource -> resource.getReferentClass().isAssignableFrom(Path.class))
        .findFirst()
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

  private void updateAllocationStates(Vehicle vehicle,
                                      SystemModel systemModel,
                                      VehicleModel vehicleModel) {
    AllocationHistory.Entry entry = allocationHistory.updateHistory(vehicle);

    for (FigureDecorationDetails component
             : toFigureDecorationDetails(entry.getCurrentClaimedResources(), systemModel)) {
      component.updateAllocationState(vehicleModel, AllocationState.CLAIMED);
    }

    for (FigureDecorationDetails component
             : toFigureDecorationDetails(entry.getCurrentAllocatedResourcesAhead(), systemModel)) {
      if (vehicleModel.getDriveOrderState() == WITHDRAWN) {
        component.updateAllocationState(vehicleModel, AllocationState.ALLOCATED_WITHDRAWN);
      }
      else {
        component.updateAllocationState(vehicleModel, AllocationState.ALLOCATED);
      }
    }

    for (FigureDecorationDetails component
             : toFigureDecorationDetails(entry.getCurrentAllocatedResourcesBehind(), systemModel)) {
      component.updateAllocationState(vehicleModel, AllocationState.ALLOCATED);
    }

    for (FigureDecorationDetails component
             : toFigureDecorationDetails(entry.getPreviouslyClaimedOrAllocatedResources(),
                                         systemModel)) {
      component.clearAllocationState(vehicleModel);
    }
  }

  private Set<FigureDecorationDetails> toFigureDecorationDetails(
      Set<TCSResourceReference<?>> resources,
      SystemModel systemModel) {
    return resources.stream()
        .map(res -> systemModel.getModelComponent(res.getName()))
        .filter(modelComponent -> modelComponent instanceof FigureDecorationDetails)
        .map(modelComponent -> (FigureDecorationDetails) modelComponent)
        .collect(Collectors.toSet());
  }
}
