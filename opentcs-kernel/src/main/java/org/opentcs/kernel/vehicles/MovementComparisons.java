// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
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
      if (!itStepsA.next().equalsInMovement(itStepsB.next())) {
        return false;
      }
    }

    return true;
  }
}
