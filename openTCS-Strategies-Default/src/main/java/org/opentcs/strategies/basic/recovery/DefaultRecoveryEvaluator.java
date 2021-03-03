/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.queries.QueryRecoveryStatus;
import org.opentcs.components.kernel.RecoveryEvaluator;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 * Evaluates the recovery status based on the sum of the squares of all vehicles' energy levels
 * divided by the number of vehicles.
 * (NES = Normalized Energy Squares)
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultRecoveryEvaluator
    implements RecoveryEvaluator {

  /**
   * The local kernel.
   */
  private final LocalKernel kernel;
  /**
   * The minimum NES value for a positive recovery status.
   */
  private final double threshold;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new NESRecoveryEvaluator.
   *
   * @param kernel The local kernel.
   * @param threshold The minimum NES value interpreted as a positive recovery
   * status.
   */
  @Inject
  public DefaultRecoveryEvaluator(LocalKernel kernel,
                                  @Threshold double threshold) {
    this.kernel = requireNonNull(kernel, "kernel is null");
    this.threshold = threshold;
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
  public QueryRecoveryStatus evaluateRecovery() {
    boolean recovered = (currentNESValue() >= threshold) && allOrdersFinished();
    return new QueryRecoveryStatus(recovered);
  }

  /**
   * Computes the current NES value.
   *
   * @return The current NES value.
   */
  private double currentNESValue() {
    double result = 0;
    Set<Vehicle> vehicles = kernel.getTCSObjects(Vehicle.class);
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
    for (TransportOrder order : kernel.getTCSObjects(TransportOrder.class)) {
      if (!order.getState().isFinalState()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Annotation type for injecting the threshold.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Threshold {
    // Nothing here.
  }
}
