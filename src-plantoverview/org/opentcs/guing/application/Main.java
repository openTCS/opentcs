/**
 * (c): Fraunhofer IML.
 *
 */
package org.opentcs.guing.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jhotdraw.app.Application;
import org.opentcs.guing.exchange.ConnectToServerDialog;
import org.opentcs.guing.exchange.ConnectionParamSet;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.util.Environment;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * The plant overview application's entry point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Main {

  /**
   * This class's configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  /**
   * This class's logger.
   */
  private static final Logger log = Logger.getLogger(Main.class.getName());
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
   * A manager for the kernel proxy/connection.
   */
  private final KernelProxyManager kernelProxyManager;

  static {
    switch (configStore.getString("LANGUAGE", "ENGLISH")) {
      case "GERMAN":
        Locale.setDefault(Locale.GERMAN);
        break;

      case "ENGLISH":
        Locale.setDefault(Locale.ENGLISH);
        break;

      default:
        Locale.setDefault(Locale.ENGLISH);
    }
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
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String args[]) {
    // Make sure we log everything, especially uncaught exceptions leading to
    // threads terminating.
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(true));
    Environment.logSystemInfo();
    
    // Currently the kernel needs to be connected to before starting/injecting
    // application parts.
    connectKernel();
    
    Injector injector = Guice.createInjector(new ApplicationInjectionModule());
    Main main = injector.getInstance(Main.class);
    main.startApplication();
  }

  /**
   * Creates a new instance.
   *
   * @param progressIndicator The progress indicator to be used.
   * @param application The application to be used.
   * @param opentcsView The view to be used.
   * @param kernelProxyManager The proxy/connection manager to be used.
   */
  @Inject
  public Main(ProgressIndicator progressIndicator,
              Application application,
              OpenTCSView opentcsView,
              KernelProxyManager kernelProxyManager) {
    this.progressIndicator = Objects.requireNonNull(progressIndicator);
    this.application = Objects.requireNonNull(application);
    this.opentcsView = Objects.requireNonNull(opentcsView);
    this.kernelProxyManager = Objects.requireNonNull(kernelProxyManager);
  }

  /**
   * Connects the kernel.
   */
  private static void connectKernel() {
    // If connection parameters are given in the system properties, try
    // connecting with them.
    ConnectionParamSet connParamSet
        = ConnectionParamSet.getParamSet(System.getProperties());
    KernelProxyManager localkernelProxyManager = DefaultKernelProxyManager.instance();
    if (connParamSet != null) {
      localkernelProxyManager.connect(connParamSet);
    }

    // If we are not connected, yet, show a dialog for entering the connection
    // parameters.
    if (!localkernelProxyManager.isConnected()) {
      ConnectToServerDialog dialog = new ConnectToServerDialog(localkernelProxyManager);
      dialog.setVisible(true);

      if (dialog.getReturnStatus() != ConnectToServerDialog.RET_OK) {
        log.info("User cancelled kernel connection dialog, terminating...");
        System.exit(0);
      }
    }
  }

  private void startApplication() {
    progressIndicator.initialize();
    progressIndicator.setProgress(0, "Start openTCS visualization");
    // XXX We currently do this to iteratively eliminate (circular) references
    // to the OpenTCSView instance. This should eventually go away.
    OpenTCSView.setInstance(opentcsView);
    // XXX connectKernel() removed
    progressIndicator.setProgress(5, "Launch openTCS visualization application");
    opentcsView.setApplication(application);
    // Start the view.
    application.show(opentcsView);
    progressIndicator.terminate();
  }
}
