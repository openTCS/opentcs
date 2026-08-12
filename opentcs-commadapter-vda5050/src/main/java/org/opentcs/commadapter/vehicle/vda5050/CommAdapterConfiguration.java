// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import org.opentcs.data.model.Vehicle.IntegrationLevel;

/**
 * Provides methods to configure the version 1.1 communication adapter.
 */
@ConfigurationPrefix(CommAdapterConfiguration.PREFIX)
public interface CommAdapterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "commadapter.vehicle.vda5050";

  @ConfigurationEntry(
      type = "List of VDA5050 version numbers",
      description = "See driver documentation.",
      orderKey = "0_enable"
  )
  List<String> enabledVersions();

  @ConfigurationEntry(
      type = "Map of operating modes to the integration level",
      description = "See driver documentation.",
      orderKey = "1_1_changeLevel"
  )
  Map<ConfigOperatingMode, ConfigIntegrationLevel> onOpModeChangeDoUpdateIntegrationLevel();

  @ConfigurationEntry(
      type = "Map of operating modes to a boolean",
      description = "See driver documentation.",
      orderKey = "1_2_withdrawOrder"
  )
  Map<ConfigOperatingMode, Boolean> onOpModeChangeDoWithdrawOrder();

  @ConfigurationEntry(
      type = "Map of operating modes to a boolean",
      description = "See driver documentation.",
      orderKey = "1_3_resetPosition"
  )
  Map<ConfigOperatingMode, Boolean> onOpModeChangeDoResetPosition();

  /**
   * Vehicle operating modes.
   */
  enum ConfigOperatingMode {
    /**
     * Teach-in mode.
     */
    TEACHIN,
    /**
     * Service mode.
     */
    SERVICE,
    /**
     * Manual mode.
     */
    MANUAL,
    /**
     * Semi-automatic mode.
     */
    SEMIAUTOMATIC,
    /**
     * Automatic mode.
     */
    AUTOMATIC,
  }

  /**
   * Vehicle integration levels.
   */
  enum ConfigIntegrationLevel {
    /**
     * Integration level "to be utilized".
     */
    TO_BE_UTILIZED,
    /**
     * Integration level "to be respected".
     */
    TO_BE_RESPECTED,
    /**
     * Integration level "to be noticed".
     */
    TO_BE_NOTICED,
    /**
     * Integration level "to be ignored".
     */
    TO_BE_IGNORED,
    /**
     * Leave the integration level unchanged.
     */
    LEAVE_UNCHANGED;

    public Optional<IntegrationLevel> toIntegrationLevel() {
      switch (this) {
        case TO_BE_UTILIZED:
          return Optional.of(IntegrationLevel.TO_BE_UTILIZED);
        case TO_BE_RESPECTED:
          return Optional.of(IntegrationLevel.TO_BE_RESPECTED);
        case TO_BE_NOTICED:
          return Optional.of(IntegrationLevel.TO_BE_NOTICED);
        case TO_BE_IGNORED:
          return Optional.of(IntegrationLevel.TO_BE_IGNORED);
        case LEAVE_UNCHANGED:
        default:
          return Optional.empty();
      }
    }
  }
}
