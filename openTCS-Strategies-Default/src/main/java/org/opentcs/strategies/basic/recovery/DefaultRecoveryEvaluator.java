/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Evaluates the recovery status based on the sum of the squares of all vehicles' energy levels
 * divided by the number of vehicles.
 * (NES = Normalized Energy Squares)
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated The definition of <em>recovered</em> is unclear. Unless it is clearly specified,
 * evaluation of a state of recovery should not be part of the API.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class DefaultRecoveryEvaluator
    implements org.opentcs.components.kernel.RecoveryEvaluator {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;
  /**
   * This class's configuration.
   */
  private final DefaultRecoveryEvaluatorConfiguration configuration;

  /**
   * Creates a new NESRecoveryEvaluator.
   *
   * @param objectService The object service.
   * @param configuration
   */
  @Inject
  public DefaultRecoveryEvaluator(TCSObjectService objectService,
                                  DefaultRecoveryEvaluatorConfiguration configuration) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    initialized = false;
  }

  @Override
  public org.opentcs.access.queries.QueryRecoveryStatus evaluateRecovery() {
    boolean recovered = (currentNESValue() >= configuration.threshold()) && allOrdersFinished();
    return new org.opentcs.access.queries.QueryRecoveryStatus(recovered);
  }

  /**
   * Computes the current NES value.
   *
   * @return The current NES value.
   */
  private double currentNESValue() {
    double result = 0;
    Set<Vehicle> vehicles = objectService.fetchObjects(Vehicle.class);
    int vehicleCount = vehicles.size();
    if (vehicleCount > 0) {
      for (Vehicle vehicle : vehicles) {
        result += vehicle.getEnergyLevel() * vehicle.getEnergyLevel();
      }
      result /= vehicleCount;
    }
    return result;
  }

  /**
   * Checks if all transport orders in the kernel are in a final state.
   *
   * @return <code>true</code> if, and only if, all transport orders are in a
   * final state.
   */
  private boolean allOrdersFinished() {
    for (TransportOrder order : objectService.fetchObjects(TransportOrder.class)) {
      if (!order.getState().isFinalState()) {
        return false;
      }
    }
    return true;
  }
}
