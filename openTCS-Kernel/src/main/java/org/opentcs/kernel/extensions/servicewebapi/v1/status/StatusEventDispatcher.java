/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.ServiceWebApiConfiguration;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.StatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.StatusMessageList;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.VehicleStatusMessage;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;

/**
 * Provides descriptions of recent events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatusEventDispatcher
    implements Lifecycle,
               EventHandler {

  /**
   * The interface configuration.
   */
  private final ServiceWebApiConfiguration configuration;
  /**
   * Where we register for application events.
   */
  private final EventSource eventSource;
  /**
   * The events collected.
   */
  private final SortedMap<Long, StatusMessage> events = new TreeMap<>();
  /**
   * The number of events collected so far.
   */
  private long eventCount;
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  @Inject
  public StatusEventDispatcher(ServiceWebApiConfiguration configuration,
                               @ApplicationEventBus EventSource eventSource) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.eventSource = requireNonNull(eventSource, "eventSource");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    synchronized (events) {
      eventCount = 0;
      events.clear();
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

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObject<?> object = ((TCSObjectEvent) event).getCurrentOrPreviousObjectState();
    if (object instanceof TransportOrder) {
      synchronized (events) {
        addOrderStatusMessage((TransportOrder) object, eventCount);
        eventCount++;
        cleanUpEvents();
        events.notifyAll();
      }
    }
    else if (object instanceof Vehicle) {
      synchronized (events) {
        addVehicleStatusMessage((Vehicle) object, eventCount);
        eventCount++;
        cleanUpEvents();
        events.notifyAll();
      }
    }
  }

  /**
   * Provides a list of events within the given range, waiting at most <code>timeout</code>
   * milliseconds for new events if there currently aren't any.
   *
   * @param minSequenceNo The minimum sequence number for accepted events.
   * @param maxSequenceNo The maximum sequence number for accepted events.
   * @param timeout The maximum time to wait for events (in ms) if there currently aren't any.
   * @return A list of events within the given range.
   */
  public StatusMessageList fetchEvents(long minSequenceNo, long maxSequenceNo, long timeout)
      throws IllegalArgumentException {
    checkInRange(minSequenceNo, 0, Long.MAX_VALUE, "minSequenceNo");
    checkInRange(maxSequenceNo, minSequenceNo, Long.MAX_VALUE, "maxSequenceNo");
    checkInRange(timeout, 0, Long.MAX_VALUE, "timeout");

    StatusMessageList result = new StatusMessageList();
    synchronized (events) {
      Collection<StatusMessage> messages = events.subMap(minSequenceNo, maxSequenceNo).values();
      if (messages.isEmpty()) {
        try {
          events.wait(timeout);
        }
        catch (InterruptedException exc) {
          // XXX Do something.
        }
      }
      messages = events.subMap(minSequenceNo, maxSequenceNo).values();
      result.getStatusMessages().addAll(messages);
    }
    return result;
  }

  private void addOrderStatusMessage(TransportOrder order, long sequenceNumber) {
    events.put(sequenceNumber, OrderStatusMessage.fromTransportOrder(order, sequenceNumber));
  }

  private void addVehicleStatusMessage(Vehicle vehicle, long sequenceNumber) {
    events.put(sequenceNumber, VehicleStatusMessage.fromVehicle(vehicle, sequenceNumber));
  }

  private void cleanUpEvents() {
    // XXX Sanitize maxEventCount
    int maxEventCount = configuration.statusEventsCapacity();
    while (events.size() > maxEventCount) {
      events.remove(events.firstKey());
    }
  }
}
