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
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;
import org.opentcs.guing.util.ResourceBundleUtil;

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
   * Creates a new instance.
   *
   * @param configuration The application's configuration.
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   */
  @Inject
  public PlantOverviewStarter(PlantOverviewApplicationConfiguration configuration,
                              ProgressIndicator progressIndicator,
                              Application application,
                              OpenTCSView opentcsView) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.progressIndicator = requireNonNull(progressIndicator, "progressIndicator");
    this.application = requireNonNull(application, "application");
    this.opentcsView = requireNonNull(opentcsView, "opentcsView");
  }

  public void startPlantOverview() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    opentcsView.init();
    opentcsView.switchPlantOverviewState(initialMode());
    progressIndicator.initialize();
    progressIndicator.setProgress(0, bundle.getString(
                                  "PlantOverviewStarter.progress.startPlantOverview"));
    // XXX We currently do this to iteratively eliminate (circular) references
    // to the OpenTCSView instance. This should eventually go away.
    OpenTCSView.setInstance(opentcsView);
    progressIndicator.setProgress(5, bundle.getString(
                                  "PlantOverviewStarter.progress.showPlantOverview"));
    opentcsView.setApplication(application);
    // Start the view.
    application.show(opentcsView);
    progressIndicator.terminate();
  }

  private OperationMode initialMode() {
    switch (configuration.initialMode()) {
      case MODELLING:
        return OperationMode.MODELLING;
      case OPERATING:
        return OperationMode.OPERATING;
      default:
        ChooseStateDialog chooseStateDialog = new ChooseStateDialog();
        chooseStateDialog.setVisible(true);
        return chooseStateDialog.getSelection();
    }
  }
}
