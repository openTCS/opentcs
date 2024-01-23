/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.phase.AssignmentState;
import org.opentcs.strategies.basic.dispatching.phase.CandidateFilterResult;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles assignments of transport orders to vehicles.
 */
public class OrderAssigner {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderAssigner.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * Defines the order of vehicles when there are less vehicles than transport orders.
   */
  private final Comparator<Vehicle> vehicleComparator;
  /**
   * Defines the order of transport orders when there are less transport orders than vehicles.
   */
  private final Comparator<TransportOrder> orderComparator;
  /**
   * Sorts candidates when looking for a transport order to be assigned to a vehicle.
   */
  private final Comparator<AssignmentCandidate> orderCandidateComparator;
  /**
   * Sorts candidates when looking for a vehicle to be assigned to a transport order.
   */
  private final Comparator<AssignmentCandidate> vehicleCandidateComparator;
  /**
   * A collection of predicates for filtering assignment candidates.
   */
  private final CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter;

  private final TransportOrderUtil transportOrderUtil;
  /**
   * Provides methods to check and update the dispatching status of transport orders.
   */
  private final DispatchingStatusMarker dispatchingStatusMarker;

  @Inject
  public OrderAssigner(
      TCSObjectService objectService,
      Router router,
      OrderReservationPool orderReservationPool,
      CompositeVehicleComparator vehicleComparator,
      CompositeOrderComparator orderComparator,
      CompositeOrderCandidateComparator orderCandidateComparator,
      CompositeVehicleCandidateComparator vehicleCandidateComparator,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      TransportOrderUtil transportOrderUtil,
      DispatchingStatusMarker dispatchingStatusMarker) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.vehicleComparator = requireNonNull(vehicleComparator, "vehicleComparator");
    this.orderComparator = requireNonNull(orderComparator, "orderComparator");
    this.orderCandidateComparator = requireNonNull(orderCandidateComparator,
                                                   "orderCandidateComparator");
    this.vehicleCandidateComparator = requireNonNull(vehicleCandidateComparator,
                                                     "vehicleCandidateComparator");
    this.assignmentCandidateSelectionFilter = requireNonNull(assignmentCandidateSelectionFilter,
                                                             "assignmentCandidateSelectionFilter");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.dispatchingStatusMarker = requireNonNull(dispatchingStatusMarker,
                                                  "dispatchingStatusMarker");
  }

  /**
   * Tries to assign the given tranpsort orders to the given vehicles.
   *
   * @param availableVehicles The vehicles available for order assignment.
   * @param availableOrders The transport order available to be assigned to a vehicle.
   */
  public void tryAssignments(Collection<Vehicle> availableVehicles,
                             Collection<TransportOrder> availableOrders) {
    LOG.debug("Available for dispatching: {} transport orders and {} vehicles.",
              availableOrders.size(),
              availableVehicles.size());

    AssignmentState assignmentState = new AssignmentState();
    if (availableVehicles.size() < availableOrders.size()) {
      availableVehicles.stream()
          .sorted(vehicleComparator)
          .forEach(vehicle -> tryAssignOrder(vehicle, availableOrders, assignmentState));
    }
    else {
      availableOrders.stream()
          .sorted(orderComparator)
          .forEach(order -> tryAssignVehicle(order, availableVehicles, assignmentState));
    }

    assignmentState.getFilteredOrders().values().stream()
        .filter(filterResult -> !assignmentState.wasAssignedToVehicle(filterResult.getOrder()))
        .filter(dispatchingStatusMarker::haveDeferralReasonsForOrderChanged)
        .forEach(dispatchingStatusMarker::markOrderAsDeferred);

    availableOrders.stream()
        .filter(order -> (!assignmentState.wasFiltered(order)
                          && !assignmentState.wasAssignedToVehicle(order)))
        .filter(dispatchingStatusMarker::isOrderMarkedAsDeferred)
        .forEach(dispatchingStatusMarker::markOrderAsResumed);
  }

  private void tryAssignOrder(Vehicle vehicle,
                              Collection<TransportOrder> availableOrders,
                              AssignmentState assignmentState) {
    LOG.debug("Trying to find transport order for vehicle '{}'...", vehicle.getName());

    Point vehiclePosition = objectService.fetchObject(Point.class, vehicle.getCurrentPosition());

    Map<Boolean, List<CandidateFilterResult>> ordersSplitByFilter
        = availableOrders.stream()
            .filter(order -> (!assignmentState.wasAssignedToVehicle(order)
                              && orderAssignableToVehicle(order, vehicle)))
            .map(order -> computeCandidate(vehicle, vehiclePosition, order))
            .filter(optCandidate -> optCandidate.isPresent())
            .map(optCandidate -> optCandidate.get())
            .map(
                candidate -> new CandidateFilterResult(
                    candidate,
                    assignmentCandidateSelectionFilter.apply(candidate)
                )
            )
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    ordersSplitByFilter.get(Boolean.FALSE).stream()
        .map(CandidateFilterResult::toFilterResult)
        .forEach(filterResult -> assignmentState.addFilteredOrder(filterResult));

    ordersSplitByFilter.get(Boolean.TRUE).stream()
        .map(CandidateFilterResult::getCandidate)
        .sorted(orderCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate, assignmentState));
  }

  private void tryAssignVehicle(TransportOrder order,
                                Collection<Vehicle> availableVehicles,
                                AssignmentState assignmentState) {
    LOG.debug("Trying to find vehicle for transport order '{}'...", order.getName());

    Map<Boolean, List<CandidateFilterResult>> ordersSplitByFilter
        = availableVehicles.stream()
            .filter(vehicle -> (!assignmentState.wasAssignedToOrder(vehicle)
                                && orderAssignableToVehicle(order, vehicle)))
            .map(
                vehicle -> computeCandidate(
                    vehicle,
                    objectService.fetchObject(Point.class, vehicle.getCurrentPosition()),
                    order
                )
            )
            .filter(optCandidate -> optCandidate.isPresent())
            .map(optCandidate -> optCandidate.get())
            .map(
                candidate -> new CandidateFilterResult(
                    candidate,
                    assignmentCandidateSelectionFilter.apply(candidate)
                )
            )
            .collect(Collectors.partitioningBy(filterResult -> !filterResult.isFiltered()));

    ordersSplitByFilter.get(Boolean.FALSE).stream()
        .map(CandidateFilterResult::toFilterResult)
        .forEach(filterResult -> assignmentState.addFilteredOrder(filterResult));

    ordersSplitByFilter.get(Boolean.TRUE).stream()
        .map(CandidateFilterResult::getCandidate)
        .sorted(vehicleCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate, assignmentState));
  }

  private void assignOrder(AssignmentCandidate candidate, AssignmentState assignmentState) {
    // If the vehicle currently has a (dispensable) order, we may not assign the new one here
    // directly, but must abort the old one (DefaultDispatcher.abortOrder()) and wait for the
    // vehicle's ProcState to become IDLE.
    if (candidate.getVehicle().getTransportOrder() == null) {
      LOG.debug("Assigning transport order '{}' to vehicle '{}'...",
                candidate.getTransportOrder().getName(),
                candidate.getVehicle().getName());
      dispatchingStatusMarker.markOrderAsAssigned(candidate.getTransportOrder(),
                                                  candidate.getVehicle());
      transportOrderUtil.assignTransportOrder(candidate.getVehicle(),
                                              candidate.getTransportOrder(),
                                              candidate.getDriveOrders());
      assignmentState.getAssignedCandidates().add(candidate);
    }
    else {
      LOG.debug("Reserving transport order '{}' for vehicle '{}'...",
                candidate.getTransportOrder().getName(),
                candidate.getVehicle().getName());
      // Remember that the new order is reserved for this vehicle.
      dispatchingStatusMarker.markOrderAsReserved(candidate.getTransportOrder(),
                                                  candidate.getVehicle());
      orderReservationPool.addReservation(candidate.getTransportOrder().getReference(),
                                          candidate.getVehicle().getReference());
      assignmentState.getReservedCandidates().add(candidate);
      transportOrderUtil.abortOrder(candidate.getVehicle(), false);
    }
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle,
                                                         Point vehiclePosition,
                                                         TransportOrder order) {
    return router.getRoute(vehicle, vehiclePosition, order)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }

  private boolean orderAssignableToVehicle(TransportOrder order, Vehicle vehicle) {
    return (order.getIntendedVehicle() == null
            || Objects.equals(order.getIntendedVehicle(), vehicle.getReference()))
        && (vehicle.getAllowedOrderTypes().contains(order.getType())
            || vehicle.getAllowedOrderTypes().contains(OrderConstants.TYPE_ANY));
  }
}
