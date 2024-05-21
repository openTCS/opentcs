/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides common kernel configuration entries.
 */
@ConfigurationPrefix(KernelApplicationConfiguration.PREFIX)
public interface KernelApplicationConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "kernelapp";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically enable drivers on startup.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_startup_0")
  boolean autoEnableDriversOnStartup();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically enable peripheral drivers on startup.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_startup_1")
  boolean autoEnablePeripheralDriversOnStartup();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving modelling state.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "2_autosave")
  boolean saveModelOnTerminateModelling();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving operating state.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "2_autosave")
  boolean saveModelOnTerminateOperating();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly update the router's topology when a path is (un)locked.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "3_topologyUpdate")
  boolean updateRoutingTopologyOnPathLockChange();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether vehicles should be rerouted immediately on topology changes.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "4_reroute_1")
  boolean rerouteOnRoutingTopologyUpdate();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether vehicles should be rerouted as soon as they finish a drive order.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "4_reroute_2")
  boolean rerouteOnDriveOrderFinished();

  @ConfigurationEntry(
      type = "String",
      description = {
        "The type of how vehicle resources (i.e., paths, points and locations allocated by "
        + "vehicles) are managed.",
        "Possible values:",
        "LENGTH_IGNORED: Resources are _always_ released up to (excluding) a vehicle's current"
        + "position. This type can be useful when you primarily want to utilize vehicle "
        + "envelopes for traffic management.",
        "LENGTH_RESPECTED: Only resources that are no longer \"covered\" by a vehicle (according "
        + "to the length of the vehicle and the length of the paths behind it) are released. This "
        + "is the \"classic\" way resources were managed before vehicle envelopes were introduced."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "5_resource_management_1")
  VehicleResourceManagementType vehicleResourceManagementType();

  /**
   * Defines the different types of how vehicle resources (i.e., paths, points and locations
   * allocated by vehicles) are managed.
   */
  enum VehicleResourceManagementType {
    /**
     * When releasing resources, the length of a vehicle is ignored.
     * <p>
     * Resources are <em>always</em> released up to (excluding) a vehicle's current position.
     * </p>
     * <p>
     * This type can be useful when you primarily want to utilize vehicle envelopes for traffic
     * management.
     * </p>
     */
    LENGTH_IGNORED,
    /**
     * When releasing resources, the length of a vehicle is respected.
     * <p>
     * Only resources that are no longer "covered" by a vehicle (according to the length of the
     * vehicle and the length of the paths behind it) are released. This is the "classic" way
     * resources were managed before vehicle envelopes were introduced.
     * </p>
     */
    LENGTH_RESPECTED;
  }
}
