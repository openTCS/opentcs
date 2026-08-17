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
      description = {
          "Set to a list of VDA5050 specification versions for which to enable the vehicle driver.",
          "Valid versions to be used here are: `1.1`, `2.0`. Set to an empty string to disable it.",
          "When disabled, the kernel will not use the driver at all, even if vehicles are "
              + "configured for it."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_enable"
  )
  List<String> enabledVersions();

  @ConfigurationEntry(
      type = "Map of operating modes to the integration level",
      description = {
          "Set to a map of VDA5050 operating mode names to openTCS integration level names.",
          "Whenever the vehicle's reported operating mode changes to a value given in the map, the "
              + "vehicle's integration level in openTCS will be changed to the one given for that "
              + "operating mode.",
          "Valid values for operating mode names are `TEACHIN`, `SERVICE`, `MANUAL`, "
              + "`SEMIAUTOMATIC` and `AUTOMATIC`.",
          "Valid values for integration level names are `TO_BE_UTILIZED`, `TO_BE_RESPECTED`, "
              + "`TO_BE_NOTICED`, `TO_BE_IGNORED`; the value `LEAVE_UNCHANGED`, which is the "
              + "default for every operating mode, can be used to explicitly state that the "
              + "integration level should not be changed.",
          "Note that for setting a vehicle's integration level to `TO_BE_NOTICED` or "
              + "`TO_BE_IGNORED`, openTCS requires the vehicle to not be processing any transport "
              + "order, so it usually makes sense to also configure the driver to explicitly "
              + "withdraw any transport order in these cases (see below.)"
      },
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_1_changeLevel"
  )
  Map<ConfigOperatingMode, ConfigIntegrationLevel> onOpModeChangeDoUpdateIntegrationLevel();

  @ConfigurationEntry(
      type = "Map of operating modes to a boolean",
      description = {
          "Set to a map of VDA5050 operating mode names to boolean values.",
          "Whenever the vehicle's reported operating mode changes to a value given in the map, the "
              + "vehicle's transport order may be withdrawn; if it is withdrawn, this happens "
              + "forcibly.",
          "Valid values for operating mode names are `TEACHIN`, `SERVICE`, `MANUAL`, "
              + "`SEMIAUTOMATIC` and `AUTOMATIC`.",
          "Valid assignment values are `true` (do withdraw the transport order forcibly) and "
              + "`false` (do not withdraw the transport order), with `false` being the default for "
              + "every operating mode.",
          "Note that it is recommended to always set `MANUAL=true`. While setting it to `false` is "
              + "generally possible, a vehicle that adheres to the VDA5050 specification will "
              + "clear its order buffer and thus stop processing the current order when switching "
              + "to/from manual mode; withdrawing the order ensures that the vehicle's state in "
              + "openTCS reflects this, too."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_2_withdrawOrder"
  )
  Map<ConfigOperatingMode, Boolean> onOpModeChangeDoWithdrawOrder();

  @ConfigurationEntry(
      type = "Map of operating modes to a boolean",
      description = {
          "Set to a map of VDA5050 operating mode names to boolean values.",
          "Whenever the vehicle's reported operating mode changes to a value given in the map, the "
              + "vehicle's last known position may be reset.",
          "Valid values for operating mode names are `TEACHIN`, `SERVICE`, `MANUAL`, "
              + "`SEMIAUTOMATIC` and `AUTOMATIC`.",
          "Valid assignment values are 'true' (do reset the last known position) and 'false' (do "
              + "not reset the last known position), with 'false' being the default for every "
              + "operating mode."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
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
