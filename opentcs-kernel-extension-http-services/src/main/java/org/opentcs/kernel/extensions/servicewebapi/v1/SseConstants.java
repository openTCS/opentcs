// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.Set;

/**
 * Contains constants related to server-sent events (SSE).
 */
public abstract class SseConstants {

  /**
   * The event type for events regarding vehicles.
   */
  public static final String EVENT_TYPE_VEHICLES = "/events/vehicles";
  /**
   * The event type for events regarding transport orders.
   */
  public static final String EVENT_TYPE_TRANSPORT_ORDERS = "/events/transportOrders";
  /**
   * The event type for events regarding order sequences.
   */
  public static final String EVENT_TYPE_ORDER_SEQUENCES = "/events/orderSequences";
  /**
   * The event type for events regarding peripheral jobs.
   */
  public static final String EVENT_TYPE_PERIPHERAL_JOBS = "/events/peripheralJobs";
  /**
   * A set of all event types supported by the SSE API.
   */
  public static final Set<String> SUPPORTED_EVENTS = Set.of(
      EVENT_TYPE_VEHICLES,
      EVENT_TYPE_TRANSPORT_ORDERS,
      EVENT_TYPE_ORDER_SEQUENCES,
      EVENT_TYPE_PERIPHERAL_JOBS
  );

  /**
   * Prevents instantiation.
   */
  private SseConstants() {
  }
}
