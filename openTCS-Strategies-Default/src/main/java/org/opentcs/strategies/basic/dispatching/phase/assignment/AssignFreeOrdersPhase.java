/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.phase.OrderFilterResult;
import org.opentcs.strategies.basic.dispatching.phase.VehicleFilterResult;
import org.opentcs.strategies.basic.dispatching.selection.orders.CompositeTransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.orders.IsFreelyDispatchableToAnyVehicle;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.IsAvailableForAnyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns transport orders to vehicles that are currently not processing any and are not bound to
 * any order sequences.
 */
public class AssignFreeOrdersPhase
    implements Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignFreeOrdersPhase.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * A collection of predicates for filtering vehicles.
   */
  private final CompositeVehicleSelectionFilter vehicleSelectionFilter;

  private final IsAvailableForAnyOrder isAvailableForAnyOrder;

  private final IsFreelyDispatchableToAnyVehicle isFreelyDispatchableToAnyVehicle;
  /**
   * A collection of predicates for filtering transport orders.
   */
  private final CompositeTransportOrderSelectionFilter transportOrderSelectionFilter;
  /**
   * Handles assignments of transport orders to vehicles.
   */
  private final OrderAssigner orderAssigner;
  /**
   * Provides methods to check and update the dispatching status of transport orders.
   */
  private final DispatchingStatusMarker dispatchingStatusMarker;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignFreeOrdersPhase(
      TCSObjectService objectService,
      CompositeVehicleSelectionFilter vehicleSelectionFilter,
      IsAvailableForAnyOrder isAvailableForAnyOrder,
      IsFreelyDispatchableToAnyVehicle isFreelyDispatchableToAnyVehicle,
      CompositeTransportOrderSelectionFilter transportOrderSelectionFilter,
      OrderAssigner orderAssigner,
      DispatchingStatusMarker dispatchingStatusMarker) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.vehicleSelectionFilter = requireNonNull(vehicleSelectionFilter, "vehicleSelectionFilter");
    this.isAvailableForAnyOrder = requireNonNull(isAvailableForAnyOrder, "isAvailableForAnyOrder");
    this.isFreelyDispatchableToAnyVehicle = requireNonNull(isFreelyDispatchableToAnyVehicle,
                                                           "isFreelyDispatchableToAnyVehicle");
    this.transportOrderSelectionFilter = requireNonNull(transportOrderSelectionFilter,
                                                        "transportOrderSelectionFilter");
    this.orderAssigner = requireNonNull(orderAssigner, "orderAssigner");
    this.dispatchingStatusMarker = requireNonNull(dispatchingStatusMarker,
                                                  "dispatchingStatusMarker");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    Map<Boolean, List<VehicleFilterResult>> vehiclesSplitByFilter
        = objectService.fetchObjects(Vehicle.class, isAvailableForAnyOrder)
            .stream()
            .map(vehicle -> new VehicleFilterResult(vehicle, vehicleSelectionFilter.apply(vehicle)))
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    Collection<Vehicle> availableVehicles = vehiclesSplitByFilter.get(Boolean.TRUE).stream()
        .map(VehicleFilterResult::getVehicle)
        .collect(Collectors.toList());

    if (availableVehicles.isEmpty()) {
      LOG.debug("No vehicles available, skipping potentially expensive fetching of orders.");
      return;
    }

    // Select only dispatchable orders first, then apply the composite filter, handle
    // the orders that can be tried as usual and mark the others as filtered (if they aren't, yet).
    Map<Boolean, List<OrderFilterResult>> ordersSplitByFilter
        = objectService.fetchObjects(TransportOrder.class, isFreelyDispatchableToAnyVehicle)
            .stream()
            .map(order -> new OrderFilterResult(order, transportOrderSelectionFilter.apply(order)))
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    markNewlyFilteredOrders(ordersSplitByFilter.get(Boolean.FALSE));

    orderAssigner.tryAssignments(availableVehicles,
                                 ordersSplitByFilter.get(Boolean.TRUE).stream()
                                     .map(OrderFilterResult::getOrder)
                                     .collect(Collectors.toList()));
  }

  private void markNewlyFilteredOrders(Collection<OrderFilterResult> filterResults) {
    filterResults.stream()
        .filter(filterResult
            -> (!dispatchingStatusMarker.isOrderMarkedAsDeferred(filterResult.getOrder())
                || dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(filterResult)))
        .forEach(dispatchingStatusMarker::markOrderAsDeferred);
  }
}
