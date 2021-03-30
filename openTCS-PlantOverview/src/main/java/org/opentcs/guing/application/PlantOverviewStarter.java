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
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;

/**
 * The plant overview application's entry point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PlantOverviewStarter {

  /**
   * The application's configuration.
   */
  private final PlantOverviewApplicationConfiguration configuration;
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
   * Creates a new instance.
   *
   * @param configuration The application's configuration.
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   * @param kernelEventFetcher Fetches events from the kernel, if connected, and publishes them via
   * the local event bus.
   * @param eventDispatcher Dispatches openTCS event from kernel objects to corresponding model
   * components.
   * @param transportOrdersContainer Maintains a set of all transport orders existing on the kernel
   * side.
   */
  @Inject
  public PlantOverviewStarter(PlantOverviewApplicationConfiguration configuration,
                              ProgressIndicator progressIndicator,
                              Application application,
                              OpenTCSView opentcsView,
                              EventLogger eventLogger,
                              KernelEventFetcher kernelEventFetcher,
                              OpenTCSEventDispatcher eventDispatcher,
                              AttributeAdapterRegistry attributeAdapterRegistry,
                              TransportOrdersContainer transportOrdersContainer) {
    this.configuration = requireNonNull(configuration, "configuration");
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
  }

  public void startPlantOverview() {
    eventLogger.initialize();
    kernelEventFetcher.initialize();
    eventDispatcher.initialize();
    attributeAdapterRegistry.initialize();
    transportOrdersContainer.initialize();

    opentcsView.init();
    opentcsView.switchPlantOverviewState(initialMode());
    progressIndicator.initialize();
    progressIndicator.setProgress(StartupProgressStatus.START_PLANT_OVERVIEW);
    // XXX We currently do this to iteratively eliminate (circular) references
    // to the OpenTCSView instance. This should eventually go away.
    OpenTCSView.setInstance(opentcsView);
    progressIndicator.setProgress(StartupProgressStatus.SHOW_PLANT_OVERVIEW);
    opentcsView.setApplication(application);
    // Start the view.
    application.show(opentcsView);
    progressIndicator.terminate();
  }

  private OperationMode initialMode() {
    switch (configuration.initialMode()) {
      case OPERATING:
        return OperationMode.OPERATING;
      case MODELLING:
      default:
        return OperationMode.MODELLING;
    }
  }
}
