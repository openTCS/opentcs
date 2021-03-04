/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.CompositeTransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderSelectionStrategy
    implements VehicleOrderSelectionStrategy,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderSelectionStrategy.class);
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
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
   * Sorts dispatchable transport orders by their priority.
   */
  private final Comparator<TransportOrder> orderComparator;
  /**
   * A collection of predicates for filtering transport orders.
   */
  private final CompositeTransportOrderSelectionVeto transportOrderSelectionVeto;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public TransportOrderSelectionStrategy(
      LocalKernel kernel,
      Router router,
      ProcessabilityChecker processabilityChecker,
      OrderReservationPool orderReservationPool,
      @OrderComparator Comparator<TransportOrder> orderComparator,
      CompositeTransportOrderSelectionVeto transportOrderSelectionVeto) {
    this.router = requireNonNull(router, "router");
    this.kernel = requireNonNull(kernel, "kernel");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.orderComparator = requireNonNull(orderComparator, "orderComparator");
    this.transportOrderSelectionVeto = requireNonNull(transportOrderSelectionVeto,
                                                      "transportOrderSelectionVeto");
  }

  @Override
  public void initialize() {
    if (initialized) {
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
    if (!initialized) {
      return;
    }
    initialized = false;
  }

  @Nullable
  @Override
  public VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle) {
    Set<TransportOrder> transportOrders = findOrdersFor(vehicle);
    if (transportOrders == null) {
      // The vehicle should not get ANY order.
      return new VehicleOrderSelection(null, vehicle, null);
    }
    // Filter out all transport orders with reservations. (If there was a
    // reservation for this vehicle, we would have found it above.)
    Iterator<TransportOrder> iter = transportOrders.iterator();
    while (iter.hasNext()) {
      if (orderReservationPool.isReserved(iter.next().getReference())) {
        iter.remove();
      }
    }

    Point vehiclePosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
    // Assuming the transport orders are sorted correctly, we can now just
    // grab the first one that can be processed by the given vehicle.
    Iterator<TransportOrder> orderIter = transportOrders.iterator();
    TransportOrder selectedOrder = null;
    Optional<List<DriveOrder>> driveOrders = Optional.empty();
    while (selectedOrder == null && orderIter.hasNext()) {
      TransportOrder curOrder = orderIter.next();
      boolean canProcess;
      // Get a route for the vehicle.
      driveOrders = router.getRoute(vehicle, vehiclePosition, curOrder);
      LOG.debug("driveOrders {}", driveOrders);
      canProcess = driveOrders.isPresent();
      if (!canProcess) {
        LOG.debug("{}: No route for order {}", vehicle.getName(), curOrder);
        kernel.addTransportOrderRejection(curOrder.getReference(),
                                          new Rejection(vehicle.getReference(), "Unroutable"));
      }
      // Check if the vehicle can process the order right now.
      canProcess = canProcess && processabilityChecker.checkProcessability(vehicle, curOrder);
      // If the vehicle can process this order, choose it.
      if (canProcess) {
        selectedOrder = curOrder;
      }
    }

    // If no processable order was found, we're done here.
    if (selectedOrder == null) {
      return null;
    }

    // If the vehicle's energy level is critical and the selected order (if any) is not mandatory,
    // create an order to recharge and assign that instead.
    if (vehicle.isEnergyLevelCritical() && !isPartOfActiveSequence(selectedOrder)) {
      LOG.debug("{}: Energy level critical, not assigning processable transport order.",
                vehicle.getName());
      return null;
    }

    return new VehicleOrderSelection(selectedOrder, vehicle, driveOrders.get());
  }

  /**
   * Finds a set of orders in the given set of orders that are available for dispatching to the
   * given vehicle.
   * This method first looks for an unfinished order sequence assigned to the given vehicle and
   * either returns the next unfinished order in the sequence or <code>null</code>, if the next
   * order isn't available, yet. If there isn't any order sequence being processed by the vehicle,
   * fall back to selecting all dispatchable orders.
   *
   * @param sequences The order sequences to filter.
   * @param orders The transport orders to filter.
   * @param vehicle The vehicle to select dispatchable orders for.
   * @return The next order(s) to be processed, or an empty set, if no orders for the vehicle were
   * found, or <code>null</code>, if the vehicle shouln't do anything.
   */
  @Nullable
  private SortedSet<TransportOrder> findOrdersFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    Set<TransportOrder> orders = kernel.getTCSObjects(TransportOrder.class,
                                                      transportOrderSelectionVeto.negate());

    SortedSet<TransportOrder> result = findNextOrderInSameSequence(orders, vehicle);
    if (result == null || !result.isEmpty()) {
      return result;
    }

    // If we didn't find any sequence to be processed first, try to find another order.
    return findDispatchableOrders(orders, vehicle);
  }

  private SortedSet<TransportOrder> findNextOrderInSameSequence(Set<TransportOrder> orders,
                                                                Vehicle vehicle) {
    SortedSet<TransportOrder> result = new TreeSet<>(orderComparator);

    Set<OrderSequence> sequences = kernel.getTCSObjects(OrderSequence.class);
    // Check if there's an order sequence being processed by the given
    // vehicle that is not finished, yet. We have to finish that first, so the
    // next order in that sequence has priority.
    for (OrderSequence seq : sequences) {
      if (result.isEmpty() && !seq.isFinished()
          && vehicle.getReference().equals(seq.getProcessingVehicle())) {
        // The sequence is not finished, yet. If there currently isn't any
        // order in the sequence to be processed, the vehicle shouldn't do
        // anything -> return null; if there is an unprocessed order in the
        // sequence, put that into the result set to be returned.
        TCSObjectReference<TransportOrder> oRef = seq.getNextUnfinishedOrder();
        if (oRef == null) {
          return null;
        }
        else {
          for (TransportOrder order : orders) {
            if (oRef.equals(order.getReference())) {
              result.add(order);
              break;
            }
          }
        }
      }
    }

    return result;
  }

  /**
   * Finds a set of orders in the given set of orders that are available for dispatching to the
   * given vehicle.
   * If this method finds any transport orders that are explicitly intended for the given vehicle,
   * those are returned; if not, all orders that are not explicitly intended for any other vehicle
   * are returned instead.
   *
   * @param orders The transport orders to filter.
   * @param vehicle The vehicle to select dispatchable orders for.
   * @return A set of orders that are available for dispatching to the given vehicle.
   */
  private SortedSet<TransportOrder> findDispatchableOrders(Set<TransportOrder> orders,
                                                           Vehicle vehicle) {
    requireNonNull(orders, "orders");
    requireNonNull(vehicle, "vehicle");

    TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    SortedSet<TransportOrder> transportOrders = new TreeSet<>(orderComparator);
    SortedSet<TransportOrder> vehicleSpecificOrders = new TreeSet<>(orderComparator);
    // Get all transport orders that are ready to be dispatched.
    for (TransportOrder curOrder : orders) {
      if (curOrder.hasState(TransportOrder.State.DISPATCHABLE)
          && !isPartOfActiveSequence(curOrder)) {
        // If the order is free for processing by any vehicle, add it to the
        // set of 'usual' orders.
        if (curOrder.getIntendedVehicle() == null) {
          transportOrders.add(curOrder);
        }
        // If the order is intended to be processed by the vehicle being
        // dispatched, add it to the set of specific orders.
        else if (vehicleRef.equals(curOrder.getIntendedVehicle())) {
          vehicleSpecificOrders.add(curOrder);
        }
      }
    }
    // If there are orders that this vehicle is intended to process, they
    // have priority over the 'usual' ones.
    if (!vehicleSpecificOrders.isEmpty()) {
      return vehicleSpecificOrders;
    }
    else {
      return transportOrders;
    }
  }

  private boolean isPartOfActiveSequence(TransportOrder order) {
    if (order.getWrappingSequence() == null) {
      return false;
    }
    OrderSequence seq = kernel.getTCSObject(OrderSequence.class, order.getWrappingSequence());
    if (seq != null && seq.getProcessingVehicle() != null) {
      return true;
    }
    return false;
  }

  /**
   * A binding annotation for the comparator sorting transport orders.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface OrderComparator {
  }
}
