/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;

/**
 * The result of trying to assign a set of vehicles/transport orders.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AssignmentState {

  private final List<AssignmentCandidate> assignedCandidates = new ArrayList<>();
  private final List<AssignmentCandidate> reservedCandidates = new ArrayList<>();
  private final Map<TransportOrder, OrderFilterResult> filteredOrders = new HashMap<>();

  public AssignmentState() {
  }

  public List<AssignmentCandidate> getAssignedCandidates() {
    return assignedCandidates;
  }

  public List<AssignmentCandidate> getReservedCandidates() {
    return reservedCandidates;
  }

  public Map<TransportOrder, OrderFilterResult> getFilteredOrders() {
    return filteredOrders;
  }

  public void addFilteredOrder(OrderFilterResult filterResult) {
    TransportOrder order = filterResult.getOrder();
    OrderFilterResult result = filteredOrders.getOrDefault(order,
                                                      new OrderFilterResult(order, new ArrayList<>()));
    result.getFilterReasons().addAll(filterResult.getFilterReasons());
    filteredOrders.put(order, result);
  }

  /**
   * Checks whether the given transport order is still assignable, taking into account the current
   * assignment results.
   *
   * @param order The transport order to check.
   * @return {@code true}, if the given transport order was not yet assigned or reserved, otherwise
   * {@code false}.
   */
  public boolean wasAssignedToVehicle(TransportOrder order) {
    return Stream.concat(assignedCandidates.stream(), reservedCandidates.stream())
        .anyMatch(candidate -> Objects.equals(candidate.getTransportOrder(), order));
  }

  /**
   * Checks whether the given vehicle is still assignable, taking into account the current
   * assignment results.
   *
   * @param vehicle The vehicle to check.
   * @return {@code true}, if a transport order was not yet assigned to or reserved for the given
   * vehicle, otherwise {@code false}.
   */
  public boolean wasAssignedToOrder(Vehicle vehicle) {
    return Stream.concat(assignedCandidates.stream(), reservedCandidates.stream())
        .anyMatch(candidate -> Objects.equals(candidate.getVehicle(), vehicle));
  }
  
  public boolean wasFiltered(TransportOrder order) {
    return filteredOrders.containsKey(order);
  }
}
