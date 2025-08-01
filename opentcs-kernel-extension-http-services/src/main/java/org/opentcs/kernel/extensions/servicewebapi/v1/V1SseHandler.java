// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import io.javalin.http.sse.SseClient;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.opentcs.common.LoggingScheduledThreadPoolExecutor;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse.EventConverter;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles connections to the Server-Sent Events API version 1.
 * <p>
 * This class listens for application events and broadcasts them to connected clients based on their
 * "subscription" preferences.
 */
public class V1SseHandler
    implements
      Lifecycle,
      EventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(V1SseHandler.class);
  /**
   * The queue of connected clients.
   */
  private final Queue<SseConnection> connections = new ConcurrentLinkedQueue<>();
  private final EventSource eventSource;
  private final EventConverter eventConverter;
  private final JsonBinder jsonBinder;
  /**
   * The executor service used for precessing kernel events.
   */
  private final ExecutorService executor = new LoggingScheduledThreadPoolExecutor(
      1,
      runnable -> {
        Thread thread = new Thread(runnable, "sseExecutor");
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
        return thread;
      }
  );
  /**
   * Indicates whether this handler is initialized.
   */
  private boolean initialized = false;

  /**
   * Creates a new instance.
   *
   * @param eventSource Where we register for application events.
   * @param eventConverter Converts application events to events to be sent via SSE.
   * @param jsonBinder Converts objects to JSON.
   */
  @Inject
  public V1SseHandler(
      @ApplicationEventBus
      EventSource eventSource,
      EventConverter eventConverter,
      JsonBinder jsonBinder
  ) {
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.eventConverter = requireNonNull(eventConverter, "eventConverter");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventSource.subscribe(this);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventSource.unsubscribe(this);

    for (SseConnection connection : connections) {
      connection.client().close();
    }
    connections.clear();

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    executor.submit(() -> {
      if (event instanceof TCSObjectEvent tcsObjectEvent) {
        handleObjectEvent(tcsObjectEvent);
      }
    });
  }

  /**
   * Handles a new SSE connection.
   *
   * @param client The client that connected.
   */
  public void handleSseConnection(SseClient client) {
    LOG.info("Client connected: {}", client);

    // Add the client to the queue of connections.
    SseConnection sseConnection = new SseConnection(
        client,
        queryParamsToEventTypes(client.ctx().queryParamMap())
    );
    connections.add(sseConnection);
    // Keep the connection alive to be able to continuously broadcast events to connected clients.
    client.keepAlive();
    // Forget the client when the connection is closed.
    client.onClose(() -> {
      LOG.info("Connection to client closed: {}", client);
      connections.remove(sseConnection);
    });
  }

  private void handleObjectEvent(TCSObjectEvent event) {
    TCSObject<?> object = event.getCurrentOrPreviousObjectState();
    if (object instanceof Vehicle) {
      sendEventToClients(
          SseConstants.EVENT_TYPE_VEHICLES,
          jsonBinder.toJson(eventConverter.convertVehicleEvent(event))
      );
    }
    else if (object instanceof TransportOrder) {
      sendEventToClients(
          SseConstants.EVENT_TYPE_TRANSPORT_ORDERS,
          jsonBinder.toJson(eventConverter.convertTransportOrderEvent(event))
      );
    }
    else if (object instanceof OrderSequence) {
      sendEventToClients(
          SseConstants.EVENT_TYPE_ORDER_SEQUENCES,
          jsonBinder.toJson(eventConverter.convertOrderSequenceEvent(event))
      );
    }
    else if (object instanceof PeripheralJob) {
      sendEventToClients(
          SseConstants.EVENT_TYPE_PERIPHERAL_JOBS,
          jsonBinder.toJson(eventConverter.convertPeripheralJobEvent(event))
      );
    }
  }

  private void sendEventToClients(String eventType, String data) {
    for (SseConnection connection : connections) {
      try {
        if (connection.eventTypes().contains(eventType)) {
          connection.client().sendEvent(eventType, data);
        }
      }
      catch (Exception e) {
        LOG.warn("Failed to send event to client {}: {}", connection.client(), e.getMessage());
      }
    }
  }

  private Set<String> queryParamsToEventTypes(Map<String, List<String>> queryParamMap) {
    if (queryParamMap == null) {
      return Set.of();
    }

    return queryParamMap.entrySet().stream()
        .filter(entry -> SseConstants.SUPPORTED_EVENTS.contains(entry.getKey()))
        .map(
            entry -> Map.entry(
                entry.getKey(),
                Boolean.parseBoolean(entry.getValue().getFirst())
            )
        )
        // Only process event names that the client explicitly selected to be sent.
        .filter(Map.Entry::getValue)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  /**
   * Represents a connection to a client.
   *
   * @param client The client this connection is associated with.
   * @param eventTypes The event types the associated client is interested in.
   */
  private record SseConnection(SseClient client, Set<String> eventTypes) {

    /**
     * Creates a new instance.
     *
     * @param client The client this connection is associated with.
     * @param eventTypes The event types the associated client is interested in.
     */
    private SseConnection(SseClient client, Set<String> eventTypes) {
      this.client = requireNonNull(client, "client");
      this.eventTypes = requireNonNull(eventTypes, "eventTypes");
    }
  }
}
