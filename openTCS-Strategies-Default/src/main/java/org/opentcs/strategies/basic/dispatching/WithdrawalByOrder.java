/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import org.opentcs.data.order.TransportOrder;

/**
 * The description of a withdrawal.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class WithdrawalByOrder {

  /**
   * The transport order to be withdrawn.
   */
  private final TransportOrder order;
  /**
   * Indicates whether the order should be aborted immediately instead of
   * withdrawn.
   */
  private final boolean immediateAbort;
  /**
   * Indicates whether the processing state of the vehicle processing the order
   * is to be set to UNAVAILABLE.
   */
  private final boolean disablingVehicle;

  /**
   * Creates a new instance.
   *
   * @param newVehicle The vehicle whose order is to be withdrawn.
   * @param immediateAbort Whether to abort the order immediately instead of
   * withdrawing it.
   * @param disableVehicle Whether to disable the vehicle after withdrawing the
   * order.
   */
  WithdrawalByOrder(TransportOrder newOrder,
                    boolean immediateAbort,
                    boolean disableVehicle) {
    this.order = requireNonNull(newOrder, "newOrder");
    this.immediateAbort = immediateAbort;
    this.disablingVehicle = disableVehicle;
  }

  /**
   * Returns the transport order that is to be withdrawn.
   *
   * @return The transport order that is to be withdrawn.
   */
  public TransportOrder getOrder() {
    return order;
  }

  /**
   * Indicates whether to abort the vehicle's transport order immediately
   * instead of withdrawing it.
   *
   * @return <code>true</code> if, and only if, the order is to be aborted
   * immediately instead of withdrawn.
   */
  public boolean isImmediateAbort() {
    return immediateAbort;
  }

  /**
   * Indicates whether to disable the vehicle after withdrawing the order.
   *
   * @return <code>true</code> if, and only if, the vehicle is to be disabled
   * after withdrawing the order.
   */
  public boolean isDisablingVehicle() {
    return disablingVehicle;
  }
}
