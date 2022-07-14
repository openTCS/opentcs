/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.app.Application;
import org.opentcs.guing.common.application.ProgressIndicator;
import org.opentcs.guing.common.application.StartupProgressStatus;
import org.opentcs.guing.common.event.EventLogger;
import org.opentcs.operationsdesk.exchange.AttributeAdapterRegistry;
import org.opentcs.operationsdesk.exchange.KernelEventFetcher;
import org.opentcs.operationsdesk.exchange.OpenTCSEventDispatcher;
import org.opentcs.operationsdesk.peripherals.jobs.PeripheralJobsContainer;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainer;
import org.opentcs.operationsdesk.transport.sequences.OrderSequencesContainer;

/**
 * The plant overview application's entry point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PlantOverviewStarter {

  /**
   * Our startup progress indicator.
   */
  private final ProgressIndicator progressIndicator;
  /**
   * The enclosing application.
   */
  private final Application application;
  /**
   * The actual document view.
   */
  private final OpenTCSView opentcsView;
  /**
   *
   */
  private final EventLogger eventLogger;
  /**
   * Fetches events from the kernel, if connected, and publishes them via the local event bus.
   */
  private final KernelEventFetcher kernelEventFetcher;
  /**
   * Dispatches openTCS event from kernel objects to corresponding model components.
   */
  private final OpenTCSEventDispatcher eventDispatcher;

  private final AttributeAdapterRegistry attributeAdapterRegistry;
  /**
   * Maintains a set of all transport orders existing on the kernel side.
   */
  private final TransportOrdersContainer transportOrdersContainer;
  /**
   * Maintains a set of all peripheral jobs existing on the kernel side.
   */
  private final PeripheralJobsContainer peripheralJobsContainer;
  /**
   * Maintains a set of all order sequences existing on the kernel side.
   */
  private final OrderSequencesContainer orderSequencesContainer;

  /**
   * Creates a new instance.
   *
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   * @param eventLogger The event logger.
   * @param kernelEventFetcher Fetches events from the kernel, if connected, and publishes them via
   * the local event bus.
   * @param eventDispatcher Dispatches openTCS event from kernel objects to corresponding model
   * components.
   * @param attributeAdapterRegistry Handles registering of model attribute adapters.
   * @param transportOrdersContainer Maintains a set of all transport orders existing on the kernel
   * side.
   * @param peripheralJobsContainer Maintains a set of all peripheral jobs existing on the kernel
   * side.
   * @param orderSequencesContainer Maintains a set of all peripheral jobs existing on the kernel
   * side.
   */
  @Inject
  public PlantOverviewStarter(ProgressIndicator progressIndicator,
                              Application application,
                              OpenTCSView opentcsView,
                              EventLogger eventLogger,
                              KernelEventFetcher kernelEventFetcher,
                              OpenTCSEventDispatcher eventDispatcher,
                              AttributeAdapterRegistry attributeAdapterRegistry,
                              TransportOrdersContainer transportOrdersContainer,
                              PeripheralJobsContainer peripheralJobsContainer,
                              OrderSequencesContainer orderSequencesContainer) {
    this.progressIndicator = requireNonNull(progressIndicator, "progressIndicator");
    this.application = requireNonNull(application, "application");
    this.opentcsView = requireNonNull(opentcsView, "opentcsView");
    this.eventLogger = requireNonNull(eventLogger, "eventLogger");
    this.kernelEventFetcher = requireNonNull(kernelEventFetcher, "kernelEventFetcher");
    this.eventDispatcher = requireNonNull(eventDispatcher, "eventDispatcher");
    this.attributeAdapterRegistry = requireNonNull(attributeAdapterRegistry,
                                                   "attributeAdapterRegistry");
    this.transportOrdersContainer = requireNonNull(transportOrdersContainer,
                                                   "transportOrdersContainer");
    this.peripheralJobsContainer = requireNonNull(peripheralJobsContainer,
                                                  "peripheralJobsContainer");
    this.orderSequencesContainer = requireNonNull(orderSequencesContainer,
                                                  "orderSequencesContainer");
  }

  public void startPlantOverview() {
    eventLogger.initialize();
    kernelEventFetcher.initialize();
    eventDispatcher.initialize();
    attributeAdapterRegistry.initialize();
    transportOrdersContainer.initialize();
    peripheralJobsContainer.initialize();
    orderSequencesContainer.initialize();

    opentcsView.init();
    progressIndicator.initialize();
    progressIndicator.setProgress(StartupProgressStatus.START_PLANT_OVERVIEW);
    progressIndicator.setProgress(StartupProgressStatus.SHOW_PLANT_OVERVIEW);
    opentcsView.setApplication(application);
    // Start the view.
    application.show(opentcsView);
    progressIndicator.terminate();
  }
}
