/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks whether vehicles should <em>not</em> get a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NoOrderSelectionStrategy
    implements VehicleOrderSelectionStrategy,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(NoOrderSelectionStrategy.class);
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public NoOrderSelectionStrategy() {
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
    requireNonNull(vehicle, "vehicle");

    if (!vehicle.hasState(Vehicle.State.IDLE) && !vehicle.hasState(Vehicle.State.CHARGING)) {
      LOG.debug("{}: Vehicle state is not idle/charging but {} - leaving it alone.",
                vehicle.getName(),
                vehicle.getState());
      return new VehicleOrderSelection(null, vehicle, null);
    }

    if (vehicle.isEnergyLevelDegraded() && vehicle.hasState(Vehicle.State.CHARGING)) {
      LOG.debug("{}: Energy level degraded, vehicle charging - leaving it alone.",
                vehicle.getName());
      return new VehicleOrderSelection(null, vehicle, null);
    }
    return null;
  }

}
