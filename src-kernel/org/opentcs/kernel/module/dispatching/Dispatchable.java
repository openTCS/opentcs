/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.dispatching;

import java.util.Objects;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * An object in the dispatcher's queue.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class Dispatchable {

  /**
   * The actual dispatchable object.
   */
  private final Object dispatchable;

  /**
   * Creates a new instance for the given vehicle.
   *
   * @param vehicle The dispatchable vehicle.
   */
  Dispatchable(final Vehicle vehicle) {
    dispatchable = Objects.requireNonNull(vehicle, "vehicle is null");
  }

  /**
   * Creates a new instance for the given order.
   *
   * @param order The dispatchable order.
   */
  Dispatchable(final TransportOrder order) {
    dispatchable = Objects.requireNonNull(order, "order is null");
  }

  /**
   * Creates a new instance for the given withdrawal.
   *
   * @param withdrawal The dispatchable withdrawal.
   */
  Dispatchable(final Withdrawal withdrawal) {
    dispatchable = Objects.requireNonNull(withdrawal, "withdrawal is null");
  }

  /**
   * Returns the actual dispatchable object.
   *
   * @return The actual dispatchable object.
   */
  public Object getDispatchable() {
    return dispatchable;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.dispatchable);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Dispatchable other = (Dispatchable) obj;
    if (!Objects.equals(this.dispatchable, other.dispatchable)) {
      return false;
    }
    return true;
  }
}
