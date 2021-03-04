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
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.customizations.kernel.ActiveInAllModes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes an openTCS kernel instance.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
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
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * The kernel extensions to be registered.
   */
  private final Set<KernelExtension> extensions;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param plantModelService The plant model service.
   * @param extensions The kernel extensions to be registered.
   */
  @Inject
  protected KernelStarter(LocalKernel kernel,
                          InternalPlantModelService plantModelService,
                          @ActiveInAllModes Set<KernelExtension> extensions) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.extensions = requireNonNull(extensions, "extensions");
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

    plantModelService.loadPlantModel();
    LOG.info("Loaded model named '{}'.", plantModelService.getModelName());

    kernel.setState(Kernel.State.OPERATING);
  }
}
