/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
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
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 * Checks whether a specific vehicle is assigned to/intended for a given transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignedVehicleSelectionStrategy
    implements OrderVehicleSelectionStrategy,
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
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignedVehicleSelectionStrategy(@Nonnull LocalKernel kernel,
                                          @Nonnull Router router,
                                          @Nonnull ProcessabilityChecker processabilityChecker) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.router = requireNonNull(router, "router");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
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
  public VehicleOrderSelection selectVehicle(TransportOrder order) {
    requireNonNull(order, "order");

    // If the order belongs to an order sequence, check if a vehicle is already processing it or, if
    // not, if the sequence is intended for a specific vehicle.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class, order.getWrappingSequence());
      if (seq.getProcessingVehicle() != null) {
        return selectIfAvailable(seq.getProcessingVehicle(), order);
      }
      else if (seq.getIntendedVehicle() != null) {
        return selectIfAvailable(seq.getIntendedVehicle(), order);
      }
    }
    // If there's no order sequence, but the order itself is intended for a specific vehicle, take that.
    else if (order.getIntendedVehicle() != null) {
      return selectIfAvailable(order.getIntendedVehicle(), order);
    }

    return null;
  }

  @Nullable
  private VehicleOrderSelection selectIfAvailable(TCSObjectReference<Vehicle> vRefIntended,
                                                  TransportOrder order) {
    Vehicle vehicle = kernel.getTCSObject(Vehicle.class, vRefIntended);

    if (processabilityChecker.availableForTransportOrder(vehicle, order)) {
      Point curPosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
      // Get a route for the vehicle, starting at it's current position.
      Optional<List<DriveOrder>> tmpDriveOrders = router.getRoute(vehicle, curPosition, order);
      if (tmpDriveOrders.isPresent()) {
        return new VehicleOrderSelection(order, vehicle, tmpDriveOrders.get());
      }
    }

    return new VehicleOrderSelection(order, null, null);
  }

}
