/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * Stores reservations of orders for vehicles.
 */
public class OrderReservationPool {

  /**
   * Reservations of orders for vehicles.
   */
  private final Map<TCSObjectReference<TransportOrder>, TCSObjectReference<Vehicle>> reservations
      = Collections.synchronizedMap(new HashMap<>());

  /**
   * Creates a new instance.
   */
  @Inject
  public OrderReservationPool() {
  }

  /**
   * Clears all reservations.
   */
  public void clear() {
    reservations.clear();
  }

  /**
   * Checks whether there is a reservation of the given transport order for any vehicle.
   *
   * @param orderRef A reference to the transport order.
   * @return <code>true</code> if, and only if, there is a reservation.
   */
  public boolean isReserved(@Nonnull TCSObjectReference<TransportOrder> orderRef) {
    return reservations.containsKey(orderRef);
  }

  public void addReservation(@Nonnull TCSObjectReference<TransportOrder> orderRef,
                             @Nonnull TCSObjectReference<Vehicle> vehicleRef) {
    reservations.put(orderRef, vehicleRef);
  }

  public void removeReservation(@Nonnull TCSObjectReference<TransportOrder> orderRef) {
    reservations.remove(orderRef);
  }

  public void removeReservations(@Nonnull TCSObjectReference<Vehicle> vehicleRef) {
    reservations.values().removeIf(value -> vehicleRef.equals(value));
  }

  public List<TCSObjectReference<TransportOrder>> findReservations(
      @Nonnull TCSObjectReference<Vehicle> vehicleRef) {
    return reservations.entrySet().stream()
        .filter(entry -> vehicleRef.equals(entry.getValue()))
        .map(entry -> entry.getKey())
        .collect(Collectors.toList());
  }
}
