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

/**
 * The plant overview application's entry point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PlantOverviewStarter {

  /**
   * The key of the (optional) system property for the initial mode.
   */
  private static final String PROP_INITIAL_MODE = "opentcs.initialmode";
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
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   */
  @Inject
  public PlantOverviewStarter(ProgressIndicator progressIndicator,
                              Application application,
                              OpenTCSView opentcsView) {
    this.progressIndicator = requireNonNull(progressIndicator,
                                            "progressIndicator");
    this.application = requireNonNull(application, "application");
    this.opentcsView = requireNonNull(opentcsView, "opentcsView");
  }

  public void startPlantOverview() {
    opentcsView.init();
    setInitialMode();
    progressIndicator.initialize();
    progressIndicator.setProgress(0, "Start openTCS visualization");
    // XXX We currently do this to iteratively eliminate (circular) references
    // to the OpenTCSView instance. This should eventually go away.
    OpenTCSView.setInstance(opentcsView);
    progressIndicator.setProgress(5, "Launch openTCS visualization application");
    opentcsView.setApplication(application);
    // Start the view.
    application.show(opentcsView);
    progressIndicator.terminate();
  }

  /**
   * Sets the application's initial mode of operation, either by reading it from
   * the system properties, or, failing that, by letting the user select it in a
   * dialog.
   */
  private void setInitialMode() {
    final OperationMode initialMode;

    String modeProp = System.getProperty(PROP_INITIAL_MODE);
    if (modeProp != null) {
      if (modeProp.toLowerCase().equals("operating")) {
        initialMode = OperationMode.OPERATING;
      }
      else {
        initialMode = OperationMode.MODELLING;
      }
    }
    else {
      ChooseStateDialog chooseStateDialog = new ChooseStateDialog();
      chooseStateDialog.setVisible(true);
      initialMode = chooseStateDialog.getSelection();
    }

    opentcsView.switchPlantOverviewState(initialMode);
  }
}
