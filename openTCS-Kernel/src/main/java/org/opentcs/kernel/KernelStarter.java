/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.BindingAnnotation;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.kernel.controlcenter.ChooseModelDialog;

/**
 * Initializes an openTCS kernel instance.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Singleton
public class KernelStarter {

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
   * Whether to automatically load a persisted model into the kernel.
   */
  private final boolean loadModelOnStartup;
  /**
   * Whether to show the startup dialog for the user.
   */
  private final boolean showStartupDialog;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param extensions The kernel extensions to be registered.
   * @param loadModelOnStartup Whether to automatically load a persisted model
   * into the kernel.
   * @param showStartupDialog Whether to show the startup dialog for the user.
   */
  @Inject
  protected KernelStarter(LocalKernel kernel,
                          @KernelExtension.Permanent Set<KernelExtension> extensions,
                          @LoadModelOnStartup boolean loadModelOnStartup,
                          @ShowStartupDialog boolean showStartupDialog) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.extensions = Objects.requireNonNull(extensions, "extensions is null");
    this.showStartupDialog = showStartupDialog;
    this.loadModelOnStartup = loadModelOnStartup && !showStartupDialog;
    if (loadModelOnStartup && showStartupDialog) {
      log.warning("Model will not be loaded automatically because startup "
          + "dialog is to be shown");
    }
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   */
  public void startKernel() {
    // Register kernel extensions.
    for (KernelExtension extension : extensions) {
      kernel.addKernelExtension(extension);
    }

    // Start local kernel.
    kernel.initialize();
    log.fine("Kernel initialized.");
    String savedModelName;
    try {
      savedModelName = kernel.getModelName();
    }
    catch (IOException exc) {
      throw new IllegalStateException("Unhandled exception loading model",
                                      exc);
    }
    boolean modelingMode = false;

    boolean loadModel = loadModelOnStartup;
    // Show ChooseModelDialog and see if we should load a model
    if (showStartupDialog) {
      ChooseModelDialog chooseModelDialog;
      chooseModelDialog
          = new ChooseModelDialog(savedModelName);
      chooseModelDialog.setVisible(true);
      String modelName = chooseModelDialog.savedModelSelected()
          ? savedModelName : null;
      modelingMode = chooseModelDialog.modelingSelected();

      loadModel = modelName != null;
    }
    // Load the saved model if there is one
    if (loadModel && savedModelName != null) {
      try {
        log.fine("Loading model: " + savedModelName);
        kernel.loadModel();
        log.info("Loaded model: " + savedModelName);
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
   * Annotation type for marking/binding the "load model on startup" parameter.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface LoadModelOnStartup {
    // Nothing here.
  }

  /**
   * Annotation type for marking/binding the "show user dialog on startup"
   * parameter.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ShowStartupDialog {
    // Nothing here.
  }
}
