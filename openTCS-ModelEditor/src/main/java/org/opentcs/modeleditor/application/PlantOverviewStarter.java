/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.app.Application;
import org.opentcs.guing.common.application.ProgressIndicator;
import org.opentcs.guing.common.application.StartupProgressStatus;
import org.opentcs.guing.common.event.EventLogger;

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
   * Provides logging for events published on the application event bus.
   */
  private final EventLogger eventLogger;

  /**
   * Creates a new instance.
   *
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   * @param eventLogger Provides logging for events published on the application event bus.
   */
  @Inject
  public PlantOverviewStarter(ProgressIndicator progressIndicator,
                              Application application,
                              OpenTCSView opentcsView,
                              EventLogger eventLogger) {
    this.progressIndicator = requireNonNull(progressIndicator, "progressIndicator");
    this.application = requireNonNull(application, "application");
    this.opentcsView = requireNonNull(opentcsView, "opentcsView");
    this.eventLogger = requireNonNull(eventLogger, "eventLogger");
  }

  public void startPlantOverview() {
    eventLogger.initialize();

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
