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
    // Register kernel extensions.
    for (KernelExtension extension : extensions) {
      kernel.addKernelExtension(extension);
    }

    // Start local kernel.
    kernel.initialize();
    log.fine("Kernel initialized.");
    
    // Parse the command line arguments, e.g. the name of the model to load
    boolean chooseModel = false;
    String modelName = null;
    for (int i = 0; i < args.length; i++) {
      String curArgument = args[i];
      if (curArgument.equals("-loadmodel")) {
        i++;
        if (args.length > i) {
          modelName = args[i];
        }
        else {
          log.severe("No model name given for '-loadmodel'.");
        }
      }
      else if (curArgument.equals("-loaddefaultmodel")) {
        modelName = kernel.getDefaultModelName();
        if ("".equals(modelName)) {
          modelName = null;
          log.severe("Default model not configured.");
        }
      }
      else if (curArgument.equals("-choosemodel")) {
        chooseModel = true;
      }
    }
    log.fine("Finished parsing command line arguments.");
    if ((modelName != null) || chooseModel) {
      boolean modelingMode = false;
      if (chooseModel) {
        ChooseModelDialog chooseModelWindow = new ChooseModelDialog(kernel);
        chooseModelWindow.setVisible(true);
        modelName = chooseModelWindow.getModelName();
        modelingMode = chooseModelWindow.modelingSelected();
        if (modelName == null) {
          log.info("No model chosen by user, remaining in modelling mode.");
        }
      }
      if (modelName != null) {
        try {
          log.fine("Loading model: " + modelName);
          kernel.loadModel(modelName);
          log.info("Loaded model: " + modelName);
          if (!modelingMode) {
            kernel.setState(Kernel.State.OPERATING);
          }
        }
        catch (IOException exc) {
          throw new IllegalStateException("Unhandled exception loading model",
                                          exc);
        }
      }
      else {
        kernel.createModel(Kernel.DEFAULT_MODEL_NAME);
      }
    }
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
