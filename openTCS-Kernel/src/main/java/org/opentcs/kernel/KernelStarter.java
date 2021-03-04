/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.io.IOException;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.kernel.ActiveInAllModes;
import org.opentcs.kernel.controlcenter.ChooseModelDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(KernelStarter.class);
  /**
   * The kernel we're working with.
   */
  private final LocalKernel kernel;
  /**
   * The kernel extensions to be registered.
   */
  private final Set<KernelExtension> extensions;
  /**
   * This class's configuration.
   */
  private final KernelApplicationConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param extensions The kernel extensions to be registered.
   * @param configuration This class's configuration.
   */
  @Inject
  protected KernelStarter(LocalKernel kernel,
                          @ActiveInAllModes Set<KernelExtension> extensions,
                          KernelApplicationConfiguration configuration) {
    this.kernel = requireNonNull(kernel, "kernel is null");
    this.extensions = requireNonNull(extensions, "extensions is null");
    this.configuration = requireNonNull(configuration, "configuration");
    
    if (configuration.selectModelOnStartup()) {
      LOG.debug("Model will not be loaded automatically because startup dialog is to be shown");
    }
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @throws IOException If there was a problem loading model data.
   */
  public void startKernel()
      throws IOException {
    // Register kernel extensions.
    for (KernelExtension extension : extensions) {
      kernel.addKernelExtension(extension);
    }

    // Start local kernel.
    kernel.initialize();
    LOG.debug("Kernel initialized.");
    String savedModelName = kernel.getPersistentModelName();
    boolean modelingMode = false;

    boolean loadModel = configuration.loadModelOnStartup() && !configuration.selectModelOnStartup();
    // Show ChooseModelDialog and see if we should load a model
    if (configuration.selectModelOnStartup()) {
      ChooseModelDialog chooseModelDialog = new ChooseModelDialog(savedModelName);
      chooseModelDialog.setVisible(true);
      String modelName = chooseModelDialog.savedModelSelected() ? savedModelName : null;
      modelingMode = chooseModelDialog.modelingSelected();

      loadModel = modelName != null;
    }
    // Load the saved model if there is one
    if (loadModel && savedModelName != null) {
      LOG.debug("Loading model: " + savedModelName);
      kernel.loadPlantModel();
      LOG.info("Loaded model: " + savedModelName);
      if (!modelingMode) {
        kernel.setState(Kernel.State.OPERATING);
      }
    }
    // Load an empty model in modelling mode
    else {
      kernel.createPlantModel(new PlantModelCreationTO(Kernel.DEFAULT_MODEL_NAME));
    }
  }
}
