// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
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
    implements
      Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignFreeOrdersPhase.class);
  /**
   * The object service.
   */
  private final InternalTCSObjectService objectService;
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
   * Provides service functions for working with transport orders and their states.
   */
  private final TransportOrderUtil transportOrderUtil;
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
      InternalTCSObjectService objectService,
      CompositeVehicleSelectionFilter vehicleSelectionFilter,
      IsAvailableForAnyOrder isAvailableForAnyOrder,
      IsFreelyDispatchableToAnyVehicle isFreelyDispatchableToAnyVehicle,
      CompositeTransportOrderSelectionFilter transportOrderSelectionFilter,
      OrderAssigner orderAssigner,
      DispatchingStatusMarker dispatchingStatusMarker,
      TransportOrderUtil transportOrderUtil
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.vehicleSelectionFilter = requireNonNull(vehicleSelectionFilter, "vehicleSelectionFilter");
    this.isAvailableForAnyOrder = requireNonNull(isAvailableForAnyOrder, "isAvailableForAnyOrder");
    this.isFreelyDispatchableToAnyVehicle = requireNonNull(
        isFreelyDispatchableToAnyVehicle,
        "isFreelyDispatchableToAnyVehicle"
    );
    this.transportOrderSelectionFilter = requireNonNull(
        transportOrderSelectionFilter,
        "transportOrderSelectionFilter"
    );
    this.orderAssigner = requireNonNull(orderAssigner, "orderAssigner");
    this.dispatchingStatusMarker = requireNonNull(
        dispatchingStatusMarker,
        "dispatchingStatusMarker"
    );
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
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
        = objectService.stream(Vehicle.class)
            .filter(isAvailableForAnyOrder)
            .map(vehicle -> new VehicleFilterResult(vehicle, vehicleSelectionFilter.apply(vehicle)))
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    Collection<Vehicle> availableVehicles = vehiclesSplitByFilter.get(Boolean.TRUE).stream()
        .map(VehicleFilterResult::getVehicle)
        .collect(Collectors.toList());

    if (availableVehicles.isEmpty()) {
      LOG.debug("No vehicles available, skipping potentially expensive fetching of orders.");
      return;
    }

    //Make sure all order sequences have their first really dispatchable order
    //marked as such and skip all skippable orders.
    markFirstDispatchableOrderInUnassignedSequences();
    // Select only dispatchable orders first, then apply the composite filter, handle
    // the orders that can be tried as usual and mark the others as filtered (if they aren't, yet).
    Map<Boolean, List<OrderFilterResult>> ordersSplitByFilter
        = objectService.stream(TransportOrder.class)
            .filter(isFreelyDispatchableToAnyVehicle)
            .map(order -> new OrderFilterResult(order, transportOrderSelectionFilter.apply(order)))
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    markNewlyFilteredOrders(ordersSplitByFilter.get(Boolean.FALSE));

    orderAssigner.tryAssignments(
        availableVehicles,
        ordersSplitByFilter.get(Boolean.TRUE).stream()
            .map(OrderFilterResult::getOrder)
            .collect(Collectors.toList())
    );
  }

  private void markFirstDispatchableOrderInUnassignedSequences() {
    // In case any orders at the beginning of sequences were withdrawn, update the sequences.
    transportOrderUtil.markNewDispatchableOrders();

    objectService.fetch(
        TransportOrder.class,
        order -> order.hasState(TransportOrder.State.DISPATCHABLE)
            && order.getWrappingSequence() != null
            && !partOfAnyVehiclesSequence(order)
    )
        .forEach(
            order -> {
              transportOrderUtil
                  .nextDispatchableOrderInSequence(order.getWrappingSequence());
            }
        );
  }

  private void markNewlyFilteredOrders(Collection<OrderFilterResult> filterResults) {
    filterResults.stream()
        .filter(
            filterResult -> (!dispatchingStatusMarker.isOrderMarkedAsDeferred(
                filterResult.getOrder()
            )
                || dispatchingStatusMarker.haveDeferralReasonsForOrderChanged(filterResult))
        )
        .forEach(dispatchingStatusMarker::markOrderAsDeferred);
  }

  private boolean partOfAnyVehiclesSequence(TransportOrder order) {
    return objectService.fetch(OrderSequence.class, order.getWrappingSequence())
        .orElseThrow()
        .getProcessingVehicle() != null;
  }
}
