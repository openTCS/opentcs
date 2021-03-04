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
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.CompositeTransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
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
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
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
   * A collection of predicates for filtering transport orders.
   */
  private final CompositeTransportOrderSelectionVeto transportOrderSelectionVeto;

  private final TransportOrderUtil transportOrderUtil;

  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignFreeOrdersPhase(
      TCSObjectService objectService,
      Router router,
      ProcessabilityChecker processabilityChecker,
      OrderReservationPool orderReservationPool,
      CompositeVehicleComparator vehicleComparator,
      CompositeOrderComparator orderComparator,
      CompositeOrderCandidateComparator orderCandidateComparator,
      CompositeVehicleCandidateComparator vehicleCandidateComparator,
      CompositeTransportOrderSelectionVeto transportOrderSelectionVeto,
      TransportOrderUtil transportOrderUtil,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.objectService = requireNonNull(objectService, "objectService");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.vehicleComparator = requireNonNull(vehicleComparator, "vehicleComparator");
    this.orderComparator = requireNonNull(orderComparator, "orderComparator");
    this.orderCandidateComparator = requireNonNull(orderCandidateComparator,
                                                   "orderCandidateComparator");
    this.vehicleCandidateComparator = requireNonNull(vehicleCandidateComparator,
                                                     "vehicleCandidateComparator");
    this.transportOrderSelectionVeto = requireNonNull(transportOrderSelectionVeto,
                                                      "transportOrderSelectionVeto");
    this.configuration = requireNonNull(configuration, "configuration");
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
                                                                this::availableForAnyOrder);
    if (availableVehicles.isEmpty()) {
      LOG.debug("No vehicles available, skipping potentially expensive fetching of orders.");
      return;
    }
    Set<TransportOrder> availableOrders = objectService.fetchObjects(TransportOrder.class,
                                                                     this::dispatchable);

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
        .filter(candidate -> processabilityChecker.checkProcessability(vehicle,
                                                                       candidate.getTransportOrder()))
        .sorted(orderCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate));
  }

  private void tryAssignVehicle(TransportOrder order) {
    LOG.debug("Trying to find vehicle for transport order '{}'...", order.getName());

    objectService.fetchObjects(Vehicle.class,
                               vehicle -> availableForOrder(vehicle, order))
        .stream()
        .map(vehicle -> computeCandidate(vehicle, order))
        .filter(optCandidate -> optCandidate.isPresent())
        .map(optCandidate -> optCandidate.get())
        .filter(candidate -> processabilityChecker.checkProcessability(candidate.getVehicle(),
                                                                       order))
        .sorted(vehicleCandidateComparator)
        .findFirst()
        .ifPresent(candidate -> assignOrder(candidate));
  }

  private void assignOrder(AssignmentCandidate candidate) {
    // If the vehicle currently has a (dispensable) order, we may not assign the new one here
    // directly, but must abort the old one (DefaultDispatcher.abortOrder()) and wait for the
    // vehicle's ProcState to become IDLE.
    if (candidate.getVehicle().getTransportOrder() == null) {
      LOG.debug("Assigning transport order '{}' to vehicle '{}' to ...",
                candidate.getVehicle().getName(),
                candidate.getTransportOrder().getName());
      transportOrderUtil.assignTransportOrder(candidate.getVehicle(),
                                              candidate.getTransportOrder(),
                                              candidate.getDriveOrders());
    }
    else {
      LOG.debug("Reserving transport order '{}' for vehicle '{}' ",
                candidate.getTransportOrder().getName(),
                candidate.getVehicle().getName());
      // Remember that the new order is reserved for this vehicle.
      orderReservationPool.addReservation(candidate.getTransportOrder().getReference(),
                                          candidate.getVehicle().getReference());
      transportOrderUtil.abortOrder(candidate.getVehicle(), false, false, false);
    }
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle, TransportOrder order) {
    return computeCandidate(vehicle, objectService.fetchObject(Point.class,
                                                               vehicle.getCurrentPosition()),
                            order);
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
    return dispatchable(order)
        && orderAssignableToVehicle(order, vehicle);
  }

  private boolean dispatchable(TransportOrder order) {
    // We only want to check dispatchable transport orders.
    // Filter out transport orders that are intended for other vehicles.
    // Also filter out all transport orders with reservations. We assume that a check for reserved
    // orders has been performed already, and if any had been found, we wouldn't have been called.
    // Also filter out any transport orders that we have a veto condition for.
    return order.hasState(TransportOrder.State.DISPATCHABLE)
        && !partOfOtherVehiclesSequence(order)
        && !orderReservationPool.isReserved(order.getReference())
        && !transportOrderSelectionVeto.test(order);
  }

  private boolean partOfOtherVehiclesSequence(TransportOrder order) {
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = objectService.fetchObject(OrderSequence.class,
                                                    order.getWrappingSequence());
      if (seq != null && seq.getProcessingVehicle() != null) {
        return true;
      }
    }
    return false;
  }

  private boolean availableForOrder(Vehicle vehicle, TransportOrder order) {
    return availableForAnyOrder(vehicle)
        && orderAssignableToVehicle(order, vehicle);
  }

  private boolean orderAssignableToVehicle(TransportOrder order, Vehicle vehicle) {
    return order.getIntendedVehicle() == null
        || Objects.equals(order.getIntendedVehicle(), vehicle.getReference());
  }

  private boolean availableForAnyOrder(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && !vehicle.isEnergyLevelCritical()
        && !needsMoreCharging(vehicle)
        && (processesNoOrder(vehicle)
            || processesDispensableOrder(vehicle))
        && !hasOrderReservation(vehicle);
  }

  private boolean needsMoreCharging(Vehicle vehicle) {
    return configuration.keepRechargingUntilGood()
        && vehicle.hasState(Vehicle.State.CHARGING)
        && vehicle.isEnergyLevelDegraded();
  }

  private boolean processesNoOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.CHARGING));
  }

  private boolean processesDispensableOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && objectService.fetchObject(TransportOrder.class, vehicle.getTransportOrder())
            .isDispensable();
  }

  private boolean hasOrderReservation(Vehicle vehicle) {
    return !orderReservationPool.findReservations(vehicle.getReference()).isEmpty();
  }

}
