/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.app.Application;
import org.opentcs.guing.event.EventLogger;
import org.opentcs.guing.exchange.AttributeAdapterRegistry;
import org.opentcs.guing.exchange.KernelEventFetcher;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.transport.TransportOrdersContainer;
import org.opentcs.guing.peripherals.jobs.PeripheralJobsContainer;

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
   * Creates a new instance.
   *
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   * @param kernelEventFetcher Fetches events from the kernel, if connected, and publishes them via
   * the local event bus.
   * @param eventDispatcher Dispatches openTCS event from kernel objects to corresponding model
   * components.
   * @param transportOrdersContainer Maintains a set of all transport orders existing on the kernel
   * side.
   * @param peripheralJobsContainer Maintains a set of all peripheral jobs existing on the kernel
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
                              PeripheralJobsContainer peripheralJobsContainer) {
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
  }

  public void startPlantOverview() {
    eventLogger.initialize();
    kernelEventFetcher.initialize();
    eventDispatcher.initialize();
    attributeAdapterRegistry.initialize();
    transportOrdersContainer.initialize();
    peripheralJobsContainer.initialize();

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
