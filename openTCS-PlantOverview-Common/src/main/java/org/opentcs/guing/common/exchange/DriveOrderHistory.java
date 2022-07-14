/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opentcs.guing.base.model.FigureDecorationDetails;

/**
 * Keeps track of the drive order components of vehicles.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DriveOrderHistory {

  /**
   * The drive order components mapped to the vehicle name.
   */
  private final Map<String, Set<FigureDecorationDetails>> driveOrderComponents = new HashMap<>();

  /**
   * Remembers the given set of components as the new drive order components for the given
   * vehicle name and returns the set difference of the old and the new drive order components
   * (e.g. the components that are no longer part of the vehicle's drive order).
   *
   * @param vehicleName The name of the vehicle.
   * @param newDriveOrderComponents The new drive order components.
   * @return The set difference of the old and the new drive order components.
   */
  public Set<FigureDecorationDetails> updateDriveOrderComponents(
      String vehicleName,
      Set<FigureDecorationDetails> newDriveOrderComponents) {
    Set<FigureDecorationDetails> oldDriveOrderComponents
        = getDriveOrderComponents(vehicleName);
    Set<FigureDecorationDetails> finishedDriveOrderComponents
        = new HashSet<>(oldDriveOrderComponents);

    finishedDriveOrderComponents.removeAll(newDriveOrderComponents);

    oldDriveOrderComponents.clear();
    oldDriveOrderComponents.addAll(newDriveOrderComponents);

    return finishedDriveOrderComponents;
  }

  private Set<FigureDecorationDetails> getDriveOrderComponents(String vehicleName) {
    if (!driveOrderComponents.containsKey(vehicleName)) {
      driveOrderComponents.put(vehicleName, new HashSet<>());
    }

    return driveOrderComponents.get(vehicleName);
  }
}
