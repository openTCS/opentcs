/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AssignedVehicleSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.vehicleselection.AvailableVehicleSelectionStrategy;

/**
 * Selects vehicles for transport orders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleSelector
    implements Lifecycle {

  /**
   * Checks whether a specific vehicle is assigned to/intended for a given transport order.
   */
  private final AssignedVehicleSelectionStrategy assignedVehicleStrategy;
  /**
   * Checks for the closest available vehicle for a given transport order.
   */
  private final AvailableVehicleSelectionStrategy availableVehicleStrategy;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param assignedVehicleStrategy Checks whether a specific vehicle is assigned to/intended for a
   * given transport order.
   * @param availableVehicleStrategy Checks for the closest available vehicle for a given transport
   * order.
   */
  @Inject
  public VehicleSelector(@Nonnull AssignedVehicleSelectionStrategy assignedVehicleStrategy,
                         @Nonnull AvailableVehicleSelectionStrategy availableVehicleStrategy) {
    this.assignedVehicleStrategy = requireNonNull(assignedVehicleStrategy,
                                                  "assignedVehicleStrategy");
    this.availableVehicleStrategy = requireNonNull(availableVehicleStrategy,
                                                   "availableVehicleStrategy");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }

    assignedVehicleStrategy.initialize();
    availableVehicleStrategy.initialize();

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

    assignedVehicleStrategy.terminate();
    availableVehicleStrategy.terminate();

    initialized = false;
  }

  @Nonnull
  public VehicleOrderSelection selectVehicle(TransportOrder order) {
    requireNonNull(order, "order");

    VehicleOrderSelection result;

    result = assignedVehicleStrategy.selectVehicle(order);
    if (result != null) {
      return result;
    }

    result = availableVehicleStrategy.selectVehicle(order);
    if (result != null) {
      return result;
    }

    return new VehicleOrderSelection(order, null, null);

  }
}
