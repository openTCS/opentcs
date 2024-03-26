/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Path;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

/**
 * Listens to path lock events and updates the routing topology.
 */
public class PathLockEventListener
    implements EventHandler,
               Lifecycle {

  /**
   * The kernel configuration.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The router service.
   */
  private final RouterService routerService;
  /**
   * The event bus.
   */
  private final EventBus eventBus;
  /**
   * The dispatcher.
   */
  private final DispatcherService dispatcher;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param configuration The kernel configuration.
   * @param routerService The router service.
   * @param eventBus The event bus.
   * @param dispatcher The dispatcher.
   */
  @Inject
  public PathLockEventListener(KernelApplicationConfiguration configuration,
                               RouterService routerService,
                               @ApplicationEventBus EventBus eventBus,
                               DispatcherService dispatcher) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.routerService = requireNonNull(routerService, "routerService");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
    eventBus.subscribe(this);
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
    initialized = false;
    eventBus.unsubscribe(this);
  }

  @Override
  public void onEvent(Object eventObject) {
    if (!configuration.updateRoutingTopologyOnPathLockChange()) {
      return;
    }

    if (!(eventObject instanceof TCSObjectEvent)) {
      return;
    }

    TCSObjectEvent event = (TCSObjectEvent) eventObject;
    if (hasPathLockChanged(event)) {
      routerService.updateRoutingTopology(
          Set.of(((Path) event.getCurrentObjectState()).getReference())
      );

      if (configuration.rerouteOnRoutingTopologyUpdate()) {
        dispatcher.rerouteAll(ReroutingType.REGULAR);
      }
    }
  }

  private boolean hasPathLockChanged(TCSObjectEvent event) {
    return event.getCurrentObjectState() instanceof Path
        && event.getType() == TCSObjectEvent.Type.OBJECT_MODIFIED
        && ((Path) event.getCurrentObjectState()).isLocked()
        != ((Path) event.getPreviousObjectState()).isLocked();
  }
}
