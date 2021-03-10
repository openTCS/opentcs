/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_ASSIGNED_TO_VEHICLE;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_DEFERRED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_DISPATCHING_RESUMED;
import static org.opentcs.data.order.TransportOrderHistoryCodes.ORDER_RESERVED_FOR_VEHICLE;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.phase.AssignmentState;
import org.opentcs.strategies.basic.dispatching.phase.CandidateFilterResult;
import org.opentcs.strategies.basic.dispatching.phase.OrderFilterResult;
import org.opentcs.strategies.basic.dispatching.phase.VehicleFilterResult;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.orders.CompositeTransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.orders.IsFreelyDispatchableToAnyVehicle;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeVehicleSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.IsAvailableForAnyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns transport orders to vehicles that are currently not processing any and are not bound to
 * any order sequences.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignFreeOrdersPhase
    implements Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignFreeOrdersPhase.class);
  /**
   * The object service
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
   * A collection of predicates for filtering assignment candidates.
   */
  private final CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter;

  private final TransportOrderUtil transportOrderUtil;

  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignFreeOrdersPhase(
      TCSObjectService objectService,
      Router router,
      OrderReservationPool orderReservationPool,
      CompositeVehicleComparator vehicleComparator,
      CompositeOrderComparator orderComparator,
      CompositeOrderCandidateComparator orderCandidateComparator,
      CompositeVehicleCandidateComparator vehicleCandidateComparator,
      CompositeVehicleSelectionFilter vehicleSelectionFilter,
      IsAvailableForAnyOrder isAvailableForAnyOrder,
      IsFreelyDispatchableToAnyVehicle isFreelyDispatchableToAnyVehicle,
      CompositeTransportOrderSelectionFilter transportOrderSelectionFilter,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      TransportOrderUtil transportOrderUtil) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.vehicleComparator = requireNonNull(vehicleComparator, "vehicleComparator");
    this.orderComparator = requireNonNull(orderComparator, "orderComparator");
    this.orderCandidateComparator = requireNonNull(orderCandidateComparator,
                                                   "orderCandidateComparator");
    this.vehicleCandidateComparator = requireNonNull(vehicleCandidateComparator,
                                                     "vehicleCandidateComparator");
    this.vehicleSelectionFilter = requireNonNull(vehicleSelectionFilter, "vehicleSelectionFilter");
    this.isAvailableForAnyOrder = requireNonNull(isAvailableForAnyOrder, "isAvailableForAnyOrder");
    this.isFreelyDispatchableToAnyVehicle = requireNonNull(isFreelyDispatchableToAnyVehicle,
                                                           "isFreelyDispatchableToAnyVehicle");
    this.transportOrderSelectionFilter = requireNonNull(transportOrderSelectionFilter,
                                                        "transportOrderSelectionFilter");
    this.assignmentCandidateSelectionFilter = requireNonNull(assignmentCandidateSelectionFilter,
                                                             "assignmentCandidateSelectionFilter");
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
        = objectService.fetchObjects(Vehicle.class, isAvailableForAnyOrder)
            .stream()
            .map(order -> new VehicleFilterResult(order, vehicleSelectionFilter.apply(order)))
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

    tryAssignments(availableVehicles,
                   ordersSplitByFilter.get(Boolean.TRUE).stream()
                       .map(OrderFilterResult::getOrder)
                       .collect(Collectors.toList()));
  }

  private void tryAssignments(Collection<Vehicle> availableVehicles,
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
        .filter(this::filterReasonsChanged)
        .forEach(this::doMarkAsFiltered);

    availableOrders.stream()
        .filter(order -> (!assignmentState.wasFiltered(order)
                          && !assignmentState.wasAssignedToVehicle(order)))
        .filter(this::markedAsFiltered)
        .forEach(this::doUnmarkAsFiltered);
  }

  private void markNewlyFilteredOrders(Collection<OrderFilterResult> filterResults) {
    filterResults.stream()
        .filter(filterResult -> (!markedAsFiltered(filterResult.getOrder())
                                 || filterReasonsChanged(filterResult)))
        .forEach(filterResult -> doMarkAsFiltered(filterResult));
  }

  private boolean markedAsFiltered(TransportOrder order) {
    return lastRelevantDeferredHistoryEntry(order).isPresent();
  }

  private Optional<ObjectHistory.Entry> lastRelevantDeferredHistoryEntry(TransportOrder order) {
    return order.getHistory().getEntries().stream()
        .filter(entry -> equalsAny(entry.getEventCode(),
                                   ORDER_DISPATCHING_DEFERRED,
                                   ORDER_DISPATCHING_RESUMED))
        .reduce((firstEntry, secondEntry) -> secondEntry)
        .filter(entry -> entry.getEventCode().equals(ORDER_DISPATCHING_DEFERRED));
  }

  @SuppressWarnings("unchecked")
  private boolean filterReasonsChanged(OrderFilterResult filterResult) {
    Collection<String> newReasons = filterResult.getFilterReasons();
    Collection<String> oldReasons = lastRelevantDeferredHistoryEntry(filterResult.getOrder())
        .map(entry -> (Collection<String>) entry.getSupplement())
        .orElse(new ArrayList<>());

    return newReasons.size() != oldReasons.size()
        || !newReasons.containsAll(oldReasons);
  }

  private void doMarkAsFiltered(OrderFilterResult filterResult) {
    objectService.appendObjectHistoryEntry(
        filterResult.getOrder().getReference(),
        new ObjectHistory.Entry(
            ORDER_DISPATCHING_DEFERRED,
            Collections.unmodifiableList(new ArrayList<>(filterResult.getFilterReasons()))
        )
    );
  }

  private void doUnmarkAsFiltered(TransportOrder order) {
    objectService.appendObjectHistoryEntry(
        order.getReference(),
        new ObjectHistory.Entry(
            ORDER_DISPATCHING_RESUMED,
            Collections.unmodifiableList(new ArrayList<>())
        )
    );
  }

  private boolean equalsAny(String string, String... others) {
    return Arrays.asList(others).stream()
        .anyMatch(other -> string.equals(other));
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
            .map(candidate -> new CandidateFilterResult(candidate, assignmentCandidateSelectionFilter.apply(candidate)))
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
            .map(vehicle -> computeCandidate(vehicle,
                                             objectService.fetchObject(Point.class,
                                                                       vehicle.getCurrentPosition()),
                                             order))
            .filter(optCandidate -> optCandidate.isPresent())
            .map(optCandidate -> optCandidate.get())
            .map(candidate -> new CandidateFilterResult(candidate, assignmentCandidateSelectionFilter.apply(candidate)))
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
      doMarkAsAssigned(candidate.getTransportOrder(), candidate.getVehicle());
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
      doMarkAsReserved(candidate.getTransportOrder(), candidate.getVehicle());
      orderReservationPool.addReservation(candidate.getTransportOrder().getReference(),
                                          candidate.getVehicle().getReference());
      assignmentState.getReservedCandidates().add(candidate);
      transportOrderUtil.abortOrder(candidate.getVehicle(), false, false, false);
    }
  }

  private void doMarkAsAssigned(TransportOrder order, Vehicle vehicle) {
    objectService.appendObjectHistoryEntry(
        order.getReference(),
        new ObjectHistory.Entry(ORDER_ASSIGNED_TO_VEHICLE, vehicle.getName())
    );
  }

  private void doMarkAsReserved(TransportOrder order, Vehicle vehicle) {
    objectService.appendObjectHistoryEntry(
        order.getReference(),
        new ObjectHistory.Entry(ORDER_RESERVED_FOR_VEHICLE, vehicle.getName())
    );
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle,
                                                         Point vehiclePosition,
                                                         TransportOrder order) {
    return router.getRoute(vehicle, vehiclePosition, order)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }

  private boolean orderAssignableToVehicle(TransportOrder order, Vehicle vehicle) {
    return order.getIntendedVehicle() == null
        || Objects.equals(order.getIntendedVehicle(), vehicle.getReference());
  }
}
