/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultDispatcher}.
 */
@ConfigurationPrefix(DefaultDispatcherConfiguration.PREFIX)
public interface DefaultDispatcherConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultdispatcher";

  @ConfigurationEntry(
      type = "Comma-separated list of strings",
      description = {"Keys by which to prioritize transport orders for assignment.",
                     "Possible values:",
                     "BY_AGE: Sort by age, oldest first.",
                     "BY_DEADLINE: Sort by deadline, most urgent first.",
                     "DEADLINE_AT_RISK_FIRST: Sort orders with deadlines at risk first.",
                     "BY_NAME: Sort by name, lexicographically."},
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_assign")
  List<String> orderPriorities();

  @ConfigurationEntry(
      type = "Comma-separated list of strings",
      description = {"Keys by which to prioritize vehicles for assignment.",
                     "Possible values:",
                     "BY_ENERGY_LEVEL: Sort by energy level, highest first.",
                     "IDLE_FIRST: Sort vehicles with state IDLE first.",
                     "BY_NAME: Sort by name, lexicographically."},
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_assign")
  List<String> vehiclePriorities();

  @ConfigurationEntry(
      type = "Comma-separated list of strings",
      description = {"Keys by which to prioritize vehicle candidates for assignment.",
                     "Possible values:",
                     "BY_ENERGY_LEVEL: Sort by energy level of the vehicle, highest first.",
                     "IDLE_FIRST: Sort vehicles with state IDLE first.",
                     "BY_COMPLETE_ROUTING_COSTS: Sort by complete routing costs, lowest first.",
                     "BY_INITIAL_ROUTING_COSTS: Sort by routing costs for the first destination.",
                     "BY_VEHICLE_NAME: Sort by vehicle name, lexicographically."},
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_assign")
  List<String> vehicleCandidatePriorities();

  @ConfigurationEntry(
      type = "Comma-separated list of strings",
      description = {"Keys by which to prioritize transport order candidates for assignment.",
                     "Possible values:",
                     "BY_AGE: Sort by transport order age, oldest first.",
                     "BY_DEADLINE: Sort by transport order deadline, most urgent first.",
                     "DEADLINE_AT_RISK_FIRST: Sort orders with deadlines at risk first.",
                     "BY_COMPLETE_ROUTING_COSTS: Sort by complete routing costs, lowest first.",
                     "BY_INITIAL_ROUTING_COSTS: Sort by routing costs for the first destination.",
                     "BY_ORDER_NAME: Sort by transport order name, lexicographically."},
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_assign")
  List<String> orderCandidatePriorities();

  @ConfigurationEntry(
      type = "Integer",
      description = "The time window (in ms) before its deadline in which an order becomes urgent.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_assign_special_0")
  long deadlineAtRiskPeriod();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether orders to the current position with no operation should be assigned.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_orders_special_0")
  boolean assignRedundantOrders();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether unroutable incoming transport orders should be marked as UNROUTABLE.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_orders_special_1")
  boolean dismissUnroutableTransportOrders();

  @ConfigurationEntry(
      type = "String",
      description = {
        "The strategy to use when rerouting of a vehicle results in no route at all.",
        "The vehicle then continues to use the previous route in the configured way.",
        "Possible values:",
        "IGNORE_PATH_LOCKS: Stick to the previous route, ignoring path locks.",
        "PAUSE_IMMEDIATELY: Do not send further orders to the vehicle; wait for another rerouting "
        + "opportunity.",
        "PAUSE_AT_PATH_LOCK: Send further orders to the vehicle only until it reaches a locked "
        + "path; then wait for another rerouting opportunity."
      },
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_orders_special_2")
  ReroutingImpossibleStrategy reroutingImpossibleStrategy();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create parking orders for idle vehicles.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_park_0")
  boolean parkIdleVehicles();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to consider parking position priorities when creating parking orders.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_park_1")
  boolean considerParkingPositionPriorities();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to repark vehicles to parking positions with higher priorities.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_park_2")
  boolean reparkVehiclesToHigherPriorityPositions();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create recharge orders for idle vehicles.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "3_recharge_0")
  boolean rechargeIdleVehicles();

  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether vehicles must be recharged until they are fully charged.",
                     "If false, vehicle must only be recharged until sufficiently charged."},
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "3_recharge_1")
  boolean keepRechargingUntilFullyCharged();

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval between redispatching of vehicles.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "9_misc")
  long idleVehicleRedispatchingInterval();

  /**
   * The available strategies for situations in which rerouting is not possible.
   */
  enum ReroutingImpossibleStrategy {
    /**
     * Stick to the previous route, ignoring path locks.
     */
    IGNORE_PATH_LOCKS,
    /**
     * Do not send further orders to the vehicle; wait for another rerouting opportunity.
     */
    PAUSE_IMMEDIATELY,
    /**
     * Send further orders to the vehicle only until it reaches a locked path; then wait for another
     * rerouting opportunity.
     */
    PAUSE_AT_PATH_LOCK;
  }
}
