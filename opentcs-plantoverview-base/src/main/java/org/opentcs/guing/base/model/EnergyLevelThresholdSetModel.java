// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;

/**
 * A representation of an {@link EnergyLevelThresholdSet}.
 */
public class EnergyLevelThresholdSetModel {

  private final int energyLevelCritical;
  private final int energyLevelGood;
  private final int energyLevelSufficientlyRecharged;
  private final int energyLevelFullyRecharged;

  /**
   * Creates a new instance.
   *
   * @param energyLevelCritical The value at/below which the vehicle's energy level is considered
   * "critical".
   * @param energyLevelGood The value at/above which the vehicle's energy level is considered
   * "good".
   * @param energyLevelSufficientlyRecharged The value at/above which the vehicle's energy level
   * is considered fully recharged.
   * @param energyLevelFullyRecharged The value at/above which the vehicle's energy level is
   * considered sufficiently recharged.
   */
  public EnergyLevelThresholdSetModel(
      int energyLevelCritical,
      int energyLevelGood,
      int energyLevelSufficientlyRecharged,
      int energyLevelFullyRecharged
  ) {
    this.energyLevelCritical = energyLevelCritical;
    this.energyLevelGood = energyLevelGood;
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
  }

  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }
}
