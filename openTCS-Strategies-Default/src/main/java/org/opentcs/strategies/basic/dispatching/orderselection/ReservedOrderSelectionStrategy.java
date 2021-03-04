/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ReservedOrderSelectionStrategy
    implements VehicleOrderSelectionStrategy,
               Lifecycle {

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
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public ReservedOrderSelectionStrategy(LocalKernel kernel,
                                        Router router,
                                        ProcessabilityChecker processabilityChecker,
                                        OrderReservationPool orderReservationPool) {
    this.router = requireNonNull(router, "router");
    this.kernel = requireNonNull(kernel, "kernel");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
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

  /**
   * Returns the order reserved for the given vehicle, if one such order exists
   * and it is in an assignable state (i.e. not in a final state and not
   * withdrawn).
   *
   * @param vehicle The vehicle for which to check for a reserved order.
   * @return The order reserved for the given vehicle, if such an order exists.
   */
  @Nullable
  @Override
  public VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    // Check if there's an order reserved for this vehicle that is in an
    // assignable state. If yes, return that.
    TransportOrder transportOrder
        = orderReservationPool.findReservations(vehicle.getReference()).stream()
            .findFirst()
            .map(orderRef -> kernel.getTCSObject(TransportOrder.class, orderRef))
            .filter(order -> !order.getState().isFinalState() && !order.hasState(TransportOrder.State.WITHDRAWN))
            .orElse(null);
    if (transportOrder == null) {
      return null;
    }
    // Make sure there are no reservations left for the vehicle.
    orderReservationPool.removeReservations(vehicle.getReference());
    // Make sure that the vehicle can actually process the reserved order.
    if (!processabilityChecker.checkProcessability(vehicle, transportOrder)) {
      // XXX Should we mark the order as failed in this case?
      return null;
    }
    Point vehiclePosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
    Optional<List<DriveOrder>> driveOrders = router.getRoute(vehicle, vehiclePosition, transportOrder);
    if (!driveOrders.isPresent()) {
      return null;
    }
    return new VehicleOrderSelection(transportOrder, vehicle, driveOrders.get());
  }

}
