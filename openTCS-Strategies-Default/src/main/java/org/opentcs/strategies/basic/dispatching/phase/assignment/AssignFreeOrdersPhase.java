/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import java.util.Comparator;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
import org.opentcs.strategies.basic.dispatching.selection.CompositeAssignmentCandidateSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.CompositeTransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.CompositeVehicleSelectionFilter;
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
    Set<Vehicle> availableVehicles = objectService.fetchObjects(Vehicle.class,
                                                                vehicleSelectionFilter);
    if (availableVehicles.isEmpty()) {
      LOG.debug("No vehicles available, skipping potentially expensive fetching of orders.");
      return;
    }
    Set<TransportOrder> availableOrders = objectService.fetchObjects(TransportOrder.class,
                                                                     transportOrderSelectionFilter);

    LOG.debug("Available for dispatching: {} transport orders and {} vehicles.",
              availableOrders.size(),
              availableVehicles.size());

    if (availableVehicles.size() < availableOrders.size()) {
      availableVehicles.stream()
          .sorted(vehicleComparator)
          .forEach(vehicle -> tryAssignOrder(vehicle));
    }
    else {
      availableOrders.stream()
          .sorted(orderComparator)
          .forEach(order -> tryAssignVehicle(order));
    }
  }

  private void tryAssignOrder(Vehicle vehicle) {
    LOG.debug("Trying to find transport order for vehicle '{}'...", vehicle.getName());

    Point vehiclePosition = objectService.fetchObject(Point.class, vehicle.getCurrentPosition());

    objectService.fetchObjects(TransportOrder.class,
                               order -> dispatchableForVehicle(order, vehicle))
        .stream()
        .map(order -> computeCandidate(vehicle, vehiclePosition, order))
        .filter(optCandidate -> optCandidate.isPresent())
        .map(optCandidate -> optCandidate.get())
        .filter(assignmentCandidateSelectionFilter)
        .sorted(orderCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate));
  }

  private void tryAssignVehicle(TransportOrder order) {
    LOG.debug("Trying to find vehicle for transport order '{}'...", order.getName());

    objectService.fetchObjects(Vehicle.class,
                               vehicle -> availableForOrder(vehicle, order))
        .stream()
        .map(vehicle -> computeCandidate(vehicle,
                                         objectService.fetchObject(Point.class,
                                                                   vehicle.getCurrentPosition()),
                                         order))
        .filter(optCandidate -> optCandidate.isPresent())
        .map(optCandidate -> optCandidate.get())
        .filter(assignmentCandidateSelectionFilter)
        .sorted(vehicleCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate));
  }

  private void assignOrder(AssignmentCandidate candidate) {
    // If the vehicle currently has a (dispensable) order, we may not assign the new one here
    // directly, but must abort the old one (DefaultDispatcher.abortOrder()) and wait for the
    // vehicle's ProcState to become IDLE.
    if (candidate.getVehicle().getTransportOrder() == null) {
      LOG.debug("Assigning transport order '{}' to vehicle '{}'...",
                candidate.getTransportOrder().getName(),
                candidate.getVehicle().getName());
      transportOrderUtil.assignTransportOrder(candidate.getVehicle(),
                                              candidate.getTransportOrder(),
                                              candidate.getDriveOrders());
    }
    else {
      LOG.debug("Reserving transport order '{}' for vehicle '{}'...",
                candidate.getTransportOrder().getName(),
                candidate.getVehicle().getName());
      // Remember that the new order is reserved for this vehicle.
      orderReservationPool.addReservation(candidate.getTransportOrder().getReference(),
                                          candidate.getVehicle().getReference());
      transportOrderUtil.abortOrder(candidate.getVehicle(), false, false, false);
    }
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle,
                                                         Point vehiclePosition,
                                                         TransportOrder order) {
    return router.getRoute(vehicle, vehiclePosition, order)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }

  private boolean dispatchableForVehicle(TransportOrder order, Vehicle vehicle) {
    // We only want to check dispatchable transport orders.
    // Filter out transport orders that are intended for other vehicles.
    return transportOrderSelectionFilter.test(order)
        && orderAssignableToVehicle(order, vehicle);
  }

  private boolean availableForOrder(Vehicle vehicle, TransportOrder order) {
    return vehicleSelectionFilter.test(vehicle)
        && orderAssignableToVehicle(order, vehicle);
  }

  private boolean orderAssignableToVehicle(TransportOrder order, Vehicle vehicle) {
    return order.getIntendedVehicle() == null
        || Objects.equals(order.getIntendedVehicle(), vehicle.getReference());
  }
}
