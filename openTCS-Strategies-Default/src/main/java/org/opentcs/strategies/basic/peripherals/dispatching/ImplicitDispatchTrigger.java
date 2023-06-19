/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event listener that triggers the peripheral job dispatcher on certain events.
 */
public class ImplicitDispatchTrigger
    implements EventHandler {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ImplicitDispatchTrigger.class);
  /**
   * The dispatcher in use.
   */
  private final PeripheralJobDispatcher dispatcher;

  /**
   * Creates a new instance.
   *
   * @param dispatcher The dispatcher in use.
   */
  @Inject
  public ImplicitDispatchTrigger(PeripheralJobDispatcher dispatcher) {
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObjectEvent objectEvent = (TCSObjectEvent) event;
    if (objectEvent.getType() == TCSObjectEvent.Type.OBJECT_MODIFIED
        && objectEvent.getCurrentOrPreviousObjectState() instanceof TransportOrder) {
      checkTransportOrderChange((TransportOrder) objectEvent.getPreviousObjectState(),
                                (TransportOrder) objectEvent.getCurrentObjectState());
    }
  }

  private void checkTransportOrderChange(TransportOrder oldOrder, TransportOrder newOrder) {
    if (newOrder.getState() != oldOrder.getState()
        && newOrder.getState() == TransportOrder.State.FAILED) {
      LOG.debug("Dispatching for {}...", newOrder);
      dispatcher.dispatch();
    }
  }
}
