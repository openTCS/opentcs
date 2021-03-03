/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.kernel.controlcenter.ChooseModelDialog;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * A starter class for starting the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Singleton
public final class KernelStarter {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(KernelStarter.class.getName());
  /**
   * The ResourceBundle.
   */
  private static final ResourceBundle bundle =
      ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/Bundle");
  /**
   * The kernel we're working with.
   */
  private final LocalKernel kernel;
  /**
   * The kernel extensions to be registered.
   */
  private final Set<KernelExtension> extensions;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param extensions The kernel extensions to be registered.
   */
  @Inject
  protected KernelStarter(LocalKernel kernel,
                          @KernelExtension.Permanent Set<KernelExtension> extensions) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.extensions = Objects.requireNonNull(extensions, "extensions is null");
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @param args The command line arguments.
   */
  private void startKernel(String[] args) {

    // Parse the command line arguments
    boolean chooseModel = false;
    boolean loadModel = false;
    boolean printUsage = false;
    for (String arg:args) {
      switch (arg) {
        case "-loadmodel":
          loadModel = true;
          break;
        case "-choosemodel":
          chooseModel = true;
          break;
        case "-help":
        case "-h":
          printUsage = true;
          break;
        default:
          log.warning("Unknown argument '" + arg +"' will be ingnored.");
          printUsage = true;
          break;
      }
    }
    if(loadModel && chooseModel) {
      log.warning(
          "-loadmodel option is ignored because -choosemodel was given, too");
      loadModel = false;
    }
    if(printUsage) {
      printUsage();
    }
    log.fine("Finished parsing command line arguments.");

    // Register kernel extensions.
    for (KernelExtension extension : extensions) {
      kernel.addKernelExtension(extension);
    }

    // Start local kernel.
    kernel.initialize();
    log.fine("Kernel initialized.");
    Optional<String> savedModelName;
    try {
      savedModelName = Optional.ofNullable(kernel.getModelName());
    }
    catch (IOException exc) {
        throw new IllegalStateException("Unhandled exception loading model",
                                        exc);
    }
    boolean modelingMode = false;

    // Show ChooseModelDialog and see if we should load a model
    if (chooseModel) {
      ChooseModelDialog chooseModelDialog;
      chooseModelDialog =
          new ChooseModelDialog(savedModelName);
      chooseModelDialog.setVisible(true);
      String modelName;
      modelName = chooseModelDialog.savedModelSelected() ?
          savedModelName.get() : null;
      modelingMode = chooseModelDialog.modelingSelected();
      if (modelName == null) {
        loadModel = false;
      }
      else {
        loadModel = true;
      }
    }
    // Load the saved model if there is one
    if(loadModel && savedModelName.isPresent()) {
      try {
        log.fine("Loading model: " + savedModelName.get());
        kernel.loadModel();
        log.info("Loaded model: " + savedModelName.get());
        if (!modelingMode) {
          kernel.setState(Kernel.State.OPERATING);
        }
      }
      catch (IOException exc) {
        throw new IllegalStateException("Unhandled exception loading model",
                                        exc);
      }
    }
    // Load an empty model in modelling mode
    else {
      kernel.createModel(Kernel.DEFAULT_MODEL_NAME);
    }
  }

  /**
   * Print a help message to the log.
   */
  private void printUsage() {
    log.info("Usage: java org.opentcs.kernel.KernelStarter [-loadmodel|-choosemodel]");
    log.info("\t-loadmodel\t\tIf there is a saved model, load it.");
    log.info("\t-choosemodel\tShow a dialog for choosing between the saved and an empty model.");
    log.info("\t-h -help\t\tPrint this help message to the log.");
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(true));

    Environment.logSystemInfo();
    log.fine("Setting up openTCS kernel " + Environment.getVersionString() + "...");
    Injector injector = Guice.createInjector(new KernelInjectionModule());
    KernelStarter starter = injector.getInstance(KernelStarter.class);
    starter.startKernel(args);
  }
}
