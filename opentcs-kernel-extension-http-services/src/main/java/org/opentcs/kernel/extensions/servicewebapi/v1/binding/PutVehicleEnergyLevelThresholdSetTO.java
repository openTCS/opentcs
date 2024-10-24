// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An update for a vehicle's energy level threshold set.
 */
public class PutVehicleEnergyLevelThresholdSetTO {

  private int energyLevelCritical;
  private int energyLevelGood;
  private int energyLevelSufficientlyRecharged;
  private int energyLevelFullyRecharged;

  @JsonCreator
  public PutVehicleEnergyLevelThresholdSetTO(
      @JsonProperty(value = "energyLevelCritical", required = true)
      int energyLevelCritical,
      @JsonProperty(value = "energyLevelGood", required = true)
      int energyLevelGood,
      @JsonProperty(value = "energyLevelSufficientlyRecharged", required = true)
      int energyLevelSufficientlyRecharged,
      @JsonProperty(value = "energyLevelFullyRecharged", required = true)
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

  public PutVehicleEnergyLevelThresholdSetTO setEnergyLevelCritical(int energyLevelCritical) {
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public PutVehicleEnergyLevelThresholdSetTO setEnergyLevelGood(int energyLevelGood) {
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public PutVehicleEnergyLevelThresholdSetTO setEnergyLevelSufficientlyRecharged(
      int energyLevelSufficientlyRecharged
  ) {
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    return this;
  }

  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  public PutVehicleEnergyLevelThresholdSetTO setEnergyLevelFullyRecharged(
      int energyLevelFullyRecharged
  ) {
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    return this;
  }
}
