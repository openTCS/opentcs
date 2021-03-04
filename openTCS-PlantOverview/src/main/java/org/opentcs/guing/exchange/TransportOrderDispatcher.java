/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.event.TransportOrderEvent;
import org.opentcs.util.event.EventHandler;

/**
 * A special event dispatcher for transport orders.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderDispatcher
    implements EventHandler {

  /**
   * Where this instance sends events.
   */
  private final EventHandler eventHandler;

  /**
   * Creates a new instance.
   *
   * @param eventHandler Where this instance sends events.
   */
  @Inject
  public TransportOrderDispatcher(@ApplicationEventBus EventHandler eventHandler) {
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObjectEvent objEvent = (TCSObjectEvent) event;
    if (!(objEvent.getCurrentOrPreviousObjectState() instanceof TransportOrder)) {
      return;
    }

    TransportOrder t = (TransportOrder) objEvent.getCurrentOrPreviousObjectState();

    switch (objEvent.getType()) {
      case OBJECT_CREATED:
        eventHandler.onEvent(new TransportOrderEvent(this,
                                                     t,
                                                     TransportOrderEvent.Type.ORDER_CREATED));
        break;

      case OBJECT_MODIFIED:
        eventHandler.onEvent(new TransportOrderEvent(this,
                                                     t,
                                                     TransportOrderEvent.Type.ORDER_CHANGED));
        break;

      case OBJECT_REMOVED:
        eventHandler.onEvent(new TransportOrderEvent(this,
                                                     t,
                                                     TransportOrderEvent.Type.ORDER_REMOVED));
        break;

      default:
    }
  }
}
