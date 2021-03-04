/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers creation of a batch of orders if the number of transport orders
 * in progress drop to or below a given threshold.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class ThresholdOrderGenTrigger
    implements EventListener<TCSEvent>,
               OrderGenerationTrigger {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ThresholdOrderGenTrigger.class);
  /**
   * The kernel we talk to.
   */
  private final Kernel kernel;
  /**
   * The orders that we know are in the system.
   */
  private final Set<TransportOrder> knownOrders = new LinkedHashSet<>();
  /**
   * The threshold for order generation. If the number of orders "in progress"
   * drops to or below this number, a batch of new orders is generated.
   */
  private final int threshold;
  /**
   * The instance actually creating the new orders.
   */
  private final OrderBatchCreator orderBatchCreator;

  /**
   * Creates a new ThresholdOrderGenTrigger.
   *
   * @param kernel The kernel we talk to.
   * @param threshold The threshold when new order are being created
   * @param orderBatchCreator The order batch creator
   */
  public ThresholdOrderGenTrigger(final Kernel kernel,
                                  final int threshold,
                                  final OrderBatchCreator orderBatchCreator) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.threshold = threshold;
    this.orderBatchCreator = Objects.requireNonNull(orderBatchCreator,
                                                    "orderBatchCreator is null");
  }

  @Override
  public void setTriggeringEnabled(boolean enabled) {
    synchronized (knownOrders) {
      if (enabled) {
        // Remember all orders that are not finished, failed etc.
        for (TransportOrder curOrder : kernel.getTCSObjects(TransportOrder.class)) {
          if (!curOrder.getState().isFinalState()) {
            knownOrders.add(curOrder);
          }
        }
        kernel.addEventListener(this);
        if (knownOrders.size() <= threshold) {
          triggerOrderGeneration();
        }
      }
      else {
        kernel.removeEventListener(this);
        knownOrders.clear();
      }
    }
  }

  @Override
  public void triggerOrderGeneration()
      throws KernelRuntimeException {
    knownOrders.addAll(orderBatchCreator.createOrderBatch());
  }

  @Override
  public void processEvent(TCSEvent event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObjectEvent objEvent = (TCSObjectEvent) event;
    if (!(objEvent.getCurrentOrPreviousObjectState() instanceof TransportOrder)) {
      return;
    }

    synchronized (knownOrders) {
      TransportOrder eventOrder
          = (TransportOrder) objEvent.getCurrentOrPreviousObjectState();
      // If a new order was created, add it to the set of known orders.
      if (TCSObjectEvent.Type.OBJECT_CREATED.equals(objEvent.getType())) {
        knownOrders.add(eventOrder);
      }
      // If an order was removed, remove it here, too.
      else if (TCSObjectEvent.Type.OBJECT_REMOVED.equals(objEvent.getType())) {
        knownOrders.remove(eventOrder);
      }
      // If an order was modified, check if it's NOT "in progress". If it's not,
      // i.e. if it's now finished, failed etc., remove it here, too.
      else if (eventOrder.getState().isFinalState()) {
        knownOrders.remove(eventOrder);
      }
      // Now let's check if the number of orders "in progress" has dropped below
      // the threshold. If so, create a new batch of orders.
      if (knownOrders.size() <= threshold) {
        LOG.debug("orderCount = " + knownOrders.size() + ", triggering...");
        trigger();
      }
    }
  }

  private void trigger() {
    try {
      triggerOrderGeneration();
    }
    catch (KernelRuntimeException exc) {
      LOG.warn("Exception triggering order generation, terminating triggering", exc);
      setTriggeringEnabled(false);

    }
  }

}
