/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.Comparators;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.configuration.ItemConstraintBoolean;

/**
 * Provides helper methods for the dispatcher.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class Assignments {

  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore CONFIG_STORE =
      ConfigurationStore.getStore(Assignments.class.getName());
  /**
   * Whether assignment of orders is obligatory, even if the vehicle is already
   * at the destination position and there's not explicit operation to be
   * executed.
   */
  private static final boolean MUST_ASSIGN_ALL;

  static {
    MUST_ASSIGN_ALL = CONFIG_STORE.getBoolean(
        "mustAssignAll",
        false,
        "Whether assignment of orders is obligatory, even if the vehicle is "
        + "already at the destination position and there's no explicit "
        + "operation to be executed",new ItemConstraintBoolean());
  }

  /**
   * Prevents undesired instantiation.
   */
  private Assignments() {
  }

  /**
   * Returns the sequence of operations to be executed when processing the
   * given transport order.
   *
   * @param order The transport order from which to extract the sequence of
   * operations.
   * @return The sequence of operations to be executed when processing the
   * given transport order.
   */
  public static List<String> getOperations(TransportOrder order) {
    assert order != null;
    List<String> result = new LinkedList<>();
    for (DriveOrder curDriveOrder : order.getFutureDriveOrders()) {
      result.add(curDriveOrder.getDestination().getOperation());
    }
    return result;
  }

  /**
   * Checks if the given drive order must be processed or could/should be left
   * out.
   * Orders that should be left out are those with destinations at which the
   * vehicle is already present and which require no destination operation.
   *
   * @param driveOrder The drive order to be processed.
   * @param vehicle The vehicle that would process the order.
   * @return <code>true</code> if, and only if, the given drive order must be
   * processed; <code>false</code> if the order should/must be left out.
   */
  public static boolean mustAssign(DriveOrder driveOrder, Vehicle vehicle) {
    assert vehicle != null;
    // Removing a vehicle's drive order is always allowed.
    if (driveOrder == null) {
      return true;
    }
    // Check if all orders are to be assigned.
    if (MUST_ASSIGN_ALL) {
      return true;
    }
    Point destPoint = driveOrder.getRoute().getFinalDestinationPoint();
    String destOp = driveOrder.getDestination().getOperation();
    // We use startsWith(OP_NOP) here because that makes it possible to have
    // multiple different operations ("NOP.*") that all do nothing.
    if (destPoint.getReference().equals(vehicle.getCurrentPosition())
        && (destOp.startsWith(DriveOrder.Destination.OP_NOP)
            || destOp.equals(DriveOrder.Destination.OP_MOVE))) {
      return false;
    }
    return true;
  }

  /**
   * Finds a set of orders in the given set of orders that are available for
   * dispatching to the given vehicle.
   * If this method finds any transport orders that are explicitly intended for
   * the given vehicle, those are returned; if not, all orders that are not
   * explicitly intended for any other vehicle are returned instead.
   *
   * @param orders The transport orders to filter.
   * @param vehicle The vehicle to select dispatchable orders for.
   * @return A set of orders that are available for dispatching to the given
   * vehicle.
   */
  public static SortedSet<TransportOrder> getOrdersForVehicle(
      Set<TransportOrder> orders,
      Vehicle vehicle) {
    requireNonNull(orders, "orders");
    requireNonNull(vehicle, "vehicle");

    TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    SortedSet<TransportOrder> transportOrders = new TreeSet<>(Comparators.ordersByDeadline());
    SortedSet<TransportOrder> vehicleSpecificOrders = new TreeSet<>(Comparators.ordersByDeadline());
    // Get all transport orders that are ready to be dispatched.
    for (TransportOrder curOrder : orders) {
      if (curOrder.hasState(TransportOrder.State.DISPATCHABLE)) {
        TCSObjectReference<Vehicle> intendedVehicle = curOrder.getIntendedVehicle();
        // If the order is free for processing by any vehicle, add it to the
        // set of 'usual' orders.
        if (intendedVehicle == null) {
          transportOrders.add(curOrder);
        }
        // If the order is intended to be processed by the vehicle being
        // dispatched, add it to the set of specific orders.
        else if (vehicleRef.equals(intendedVehicle)) {
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

  /**
   * Finds a set of orders in the given set of orders that are available for
   * dispatching to the given vehicle.
   * This method first looks for an unfinished order sequence assigned to the
   * given vehicle and either returns the next unfinished order in the sequence
   * or <code>null</code>, if the next order isn't available, yet. If there
   * isn't any order sequence being processed by the vehicle, fall back to
   * selecting all dispatchable orders.
   * 
   * @param sequences The order sequences to filter.
   * @param orders The transport orders to filter.
   * @param vehicle The vehicle to select dispatchable orders for.
   * @return The next order(s) to be processed, or an empty set, if no orders
   * for the vehicle were found, or <code>null</code>, if the vehicle shouln't
   * do anything.
   */
  public static SortedSet<TransportOrder> getOrdersForVehicle(
      Set<OrderSequence> sequences,
      Set<TransportOrder> orders,
      Vehicle vehicle) {
    Objects.requireNonNull(sequences, "sequences is null");
    Objects.requireNonNull(orders, "orders is null");
    Objects.requireNonNull(vehicle, "vehicle is null");

    TCSObjectReference<Vehicle> vRef = vehicle.getReference();
    SortedSet<TransportOrder> result = new TreeSet<>(Comparators.ordersByDeadline());

    // Check if there's an order sequence being processed by the given
    // vehicle that is not finished, yet. We have to finish that first, so the
    // next order in that sequence has priority.
    for (OrderSequence seq : sequences) {
      if (result.isEmpty() && !seq.isFinished()
          && vRef.equals(seq.getProcessingVehicle())) {
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

    // If we didn't find any sequence to be processed first, try to find another
    // order to take care of.
    if (result.isEmpty()) {
      return getOrdersForVehicle(orders, vehicle);
    }
    else {
      return result;
    }
  }
}
