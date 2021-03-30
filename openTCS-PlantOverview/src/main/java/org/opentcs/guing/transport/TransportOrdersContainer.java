/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a set of all transport orders existing on the kernel side.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrdersContainer
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrdersContainer.class);
  /**
   * Where we get events from.
   */
  private final EventBus eventBus;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The transport orders.
   */
  private final Map<String, TransportOrder> transportOrders = new HashMap<>();
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param eventBus Where this instance subscribes for events.
   * @param portalProvider Provides a access to a portal.
   */
  @Inject
  public TransportOrdersContainer(@ApplicationEventBus EventBus eventBus,
                                  SharedKernelServicePortalProvider portalProvider) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    
    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    
    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      handleObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof OperationModeChangeEvent) {
      initOrders();
    }
    else if (event instanceof SystemModelTransitionEvent) {
      initOrders();
    }
    else if (event instanceof KernelStateChangeEvent) {
      initOrders();
    }
  }

  /**
   * Returns the transport order with the given name, if it exists.
   *
   * @param name The name of the transport order.
   * @return The transport order with the given name, if it exists.
   */
  public Optional<TransportOrder> getTransportOrder(@Nonnull String name) {
    requireNonNull(name, "name");

    return Optional.ofNullable(transportOrders.get(name));
  }

  private void initOrders() {
    setTransportOrders(fetchOrdersIfOnline());
  }

  private void handleObjectEvent(TCSObjectEvent evt) {
    if (evt.getCurrentOrPreviousObjectState() instanceof TransportOrder) {
      switch (evt.getType()) {
        case OBJECT_CREATED:
          transportOrderAdded((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_MODIFIED:
          transportOrderChanged((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_REMOVED:
          transportOrderRemoved((TransportOrder) evt.getCurrentOrPreviousObjectState());
          break;
        default:
          LOG.warn("Unhandled event type: {}", evt.getType());
      }
    }
  }

  private void transportOrderAdded(TransportOrder order) {
    transportOrders.put(order.getName(), order);
  }

  private void transportOrderChanged(TransportOrder order) {
    transportOrders.put(order.getName(), order);
  }

  private void transportOrderRemoved(TransportOrder order) {
    transportOrders.remove(order.getName());
  }

  private void setTransportOrders(Set<TransportOrder> newOrders) {
    transportOrders.clear();
    for (TransportOrder order : newOrders) {
      transportOrders.put(order.getName(), order);
    }
  }

  private Set<TransportOrder> fetchOrdersIfOnline() {
    if (portalProvider.portalShared()) {
      try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getTransportOrderService()
            .fetchObjects(TransportOrder.class);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching transport orders", exc);
      }
    }

    return new HashSet<>();
  }

}
