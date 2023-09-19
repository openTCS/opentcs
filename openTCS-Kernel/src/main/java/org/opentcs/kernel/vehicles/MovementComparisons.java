/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.order.Route.Step;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Provides methods for comparing movements represented by {@link MovementCommand}s and
 * {@link Step}s.
 */
public class MovementComparisons {

  private MovementComparisons() {
  }

  /**
   * Compares the two given movement commands, ignoring rerouting-related properties.
   *
   * @param commandA The first movement command.
   * @param commandB The second movement command.
   * @return {@code true}, if the given movement commands are equal (ignoring rerouting-related
   * properties), otherwise {@code false}.
   */
  public static boolean equalsInMovement(MovementCommand commandA, MovementCommand commandB) {
    requireNonNull(commandA, "commandA");
    requireNonNull(commandB, "commandB");

    return MovementComparisons.equalsInMovement(commandA.getStep(), commandB.getStep())
        && Objects.equals(commandA.getOperation(), commandB.getOperation());
  }

  /**
   * Compares the two given lists of steps, ignoring rerouting-related properties.
   *
   * @param stepsA The first list of steps.
   * @param stepsB The second list of steps.
   * @return {@code true}, if the given lists of steps are equal (ignoring rerouting-related
   * properties), otherwise {@code false}.
   */
  public static boolean equalsInMovement(List<Step> stepsA, List<Step> stepsB) {
    requireNonNull(stepsA, "stepsA");
    requireNonNull(stepsB, "stepsB");

    if (stepsA.size() != stepsB.size()) {
      return false;
    }

    Iterator<Step> itStepsA = stepsA.iterator();
    Iterator<Step> itStepsB = stepsB.iterator();

    while (itStepsA.hasNext()) {
      if (!MovementComparisons.equalsInMovement(itStepsA.next(), itStepsB.next())) {
        return false;
      }
    }

    return true;
  }

  private static boolean equalsInMovement(Step stepA, Step stepB) {
    return Objects.equals(stepA.getSourcePoint(), stepB.getSourcePoint())
        && Objects.equals(stepA.getDestinationPoint(), stepB.getDestinationPoint())
        && Objects.equals(stepA.getPath(), stepB.getPath())
        && Objects.equals(stepA.getVehicleOrientation(), stepB.getVehicleOrientation())
        && Objects.equals(stepA.getRouteIndex(), stepB.getRouteIndex());
  }
}
