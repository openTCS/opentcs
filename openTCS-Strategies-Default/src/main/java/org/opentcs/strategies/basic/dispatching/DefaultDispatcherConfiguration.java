/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultDispatcher}
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(DefaultDispatcherConfiguration.PREFIX)
public interface DefaultDispatcherConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultdispatcher";

  @ConfigurationEntry(
      type = "List of strings",
      description = {"Keys by which to prioritize transport orders for assignment.",
                     "Possible values:",
                     "BY_AGE: Sort by age, oldest first.",
                     "BY_DEADLINE: Sort by deadline, most urgent first.",
                     "BY_NAME: Sort by name, lexicographically."},
      orderKey = "0_assign")
  List<String> orderPriorities();

  @ConfigurationEntry(
      type = "List of strings",
      description = {"Keys by which to prioritize vehicles for assignment.",
                     "Possible values:",
                     "BY_ENERGY_LEVEL: Sort by energy level, highest first.",
                     "IDLE_FIRST: Sort vehicles with state IDLE first.",
                     "BY_NAME: Sort by name, lexicographically."},
      orderKey = "0_assign")
  List<String> vehiclePriorities();

  @ConfigurationEntry(
      type = "List of strings",
      description = {"Keys by which to prioritize vehicle candidates for assignment.",
                     "Possible values:",
                     "BY_ENERGY_LEVEL: Sort by energy level of the vehicle, highest first.",
                     "BY_COMPLETE_ROUTING_COSTS: Sort by complete routing costs, lowest first.",
                     "BY_INITIAL_ROUTING_COSTS: Sort by routing costs for the first destination.",
                     "BY_VEHICLE_NAME: Sort by vehicle name, lexicographically."},
      orderKey = "0_assign")
  List<String> vehicleCandidatePriorities();

  @ConfigurationEntry(
      type = "List of strings",
      description = {"Keys by which to prioritize transport order candidates for assignment.",
                     "Possible values:",
                     "BY_AGE: Sort by transport order age, oldest first.",
                     "BY_DEADLINE: Sort by transport order deadline, most urgent first.",
                     "BY_COMPLETE_ROUTING_COSTS: Sort by complete routing costs, lowest first.",
                     "BY_INITIAL_ROUTING_COSTS: Sort by routing costs for the first destination.",
                     "BY_ORDER_NAME: Sort by transport order name, lexicographically."},
      orderKey = "0_assign")
  List<String> orderCandidatePriorities();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether orders to the current position with no operation should be assigned.",
      orderKey = "1_orders_special")
  boolean assignRedundantOrders();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether unroutable incoming transport orders should be marked as UNROUTABLE.",
      orderKey = "1_orders_special")
  boolean dismissUnroutableTransportOrders();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create parking orders idle vehicles.",
      orderKey = "2_idle_0")
  boolean parkIdleVehicles();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create recharge orders for idle vehicles.",
      orderKey = "2_idle_1")
  boolean rechargeIdleVehicles();

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval between redispatching of vehicles.",
      orderKey = "3_misc")
  long idleVehicleRedispatchingInterval();

}
