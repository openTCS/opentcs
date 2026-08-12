// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Limits.UINT32_MAX_VALUE;
import static org.opentcs.util.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Describes an AGVs battery state.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatteryState
    implements
      Serializable {

  /**
   * State of Charge (in percent). Range: [0 ... 100]
   * <p>
   * If an AGV only provides values for good or bad battery levels, these will be indicated
   * as 20% (bad) and 80% (good).
   */
  private Double batteryCharge;
  /**
   * [Optional] The battery voltage (in V).
   */
  private Double batteryVoltage;
  /**
   * [Optional] State of health (in percent). Range: [0 ... 100]
   */
  private Long batteryHealth;
  /**
   * Whether the AGV is charging or nor.
   */
  private Boolean charging;
  /**
   * [Optional] Estimated reach with actual state of charge. Range: [0 ... infinity]
   */
  private Long reach;

  @JsonCreator
  public BatteryState(
      @Nonnull
      @JsonProperty(required = true, value = "batteryCharge")
      Double batteryCharge,
      @Nonnull
      @JsonProperty(required = true, value = "charging")
      Boolean charging
  ) {
    this.batteryCharge = requireNonNull(batteryCharge, "batteryCharge");
    this.charging = requireNonNull(charging, "charging");
  }

  public Double getBatteryCharge() {
    return batteryCharge;
  }

  public BatteryState setBatteryCharge(
      @Nonnull
      Double batteryCharge
  ) {
    this.batteryCharge = requireNonNull(batteryCharge, "batteryCharge");
    return this;
  }

  public Double getBatteryVoltage() {
    return batteryVoltage;
  }

  public BatteryState setBatteryVoltage(Double batteryVoltage) {
    this.batteryVoltage = batteryVoltage;
    return this;
  }

  public Long getBatteryHealth() {
    return batteryHealth;
  }

  public BatteryState setBatteryHealth(Long batteryHealth) {
    if (batteryHealth != null) {
      checkInRange(batteryHealth, 0, 100, "batteryHealth");
    }
    this.batteryHealth = batteryHealth;
    return this;
  }

  public Boolean isCharging() {
    return charging;
  }

  public BatteryState setCharging(
      @Nonnull
      Boolean charging
  ) {
    this.charging = requireNonNull(charging, "charging");
    return this;
  }

  public Long getReach() {
    return reach;
  }

  public BatteryState setReach(Long reach) {
    if (reach != null) {
      checkInRange(reach, 0, UINT32_MAX_VALUE, "reach");
    }
    this.reach = reach;
    return this;
  }

  @Override
  public String toString() {
    return "BatteryState{" + "batteryCharge=" + batteryCharge
        + ", batteryVoltage=" + batteryVoltage
        + ", batteryHealth=" + batteryHealth
        + ", charging=" + charging
        + ", reach=" + reach
        + '}';
  }

}
