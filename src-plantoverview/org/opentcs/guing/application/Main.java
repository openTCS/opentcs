/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jhotdraw.app.Application;
import org.opentcs.guing.util.ApplicationConfiguration;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * The plant overview application's entry point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Main {

  /**
   * This class's logger.
   */
  private static final Logger log = Logger.getLogger(Main.class.getName());
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
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String args[]) {
    // Make sure we log everything, especially uncaught exceptions leading to
    // threads terminating.
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(true));
    Environment.logSystemInfo();

    Injector injector = Guice.createInjector(new ApplicationInjectionModule());

    initialize(injector.getInstance(ApplicationConfiguration.class));

    Main main = injector.getInstance(Main.class);
    main.startApplication();
  }

  /**
   * Initializes the application according to the given configuration.
   *
   * @param appConfig The configuration.
   */
  private static void initialize(ApplicationConfiguration appConfig) {
    Locale.setDefault(appConfig.getLocale());
    // Look and feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException |
        IllegalAccessException | UnsupportedLookAndFeelException ex) {
      log.log(Level.WARNING, "Could not set look-and-feel", ex);
    }
    // Show tooltips for 30 seconds (Default: 4 sec)
    ToolTipManager.sharedInstance().setDismissDelay(30 * 1000);
  }

  /**
   * Creates a new instance.
   *
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   */
  @Inject
  public Main(ProgressIndicator progressIndicator,
              Application application,
              OpenTCSView opentcsView) {
    this.progressIndicator = requireNonNull(progressIndicator,
                                            "progressIndicator");
    this.application = requireNonNull(application, "application");
    this.opentcsView = requireNonNull(opentcsView, "opentcsView");
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

  private void startApplication() {
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
}
