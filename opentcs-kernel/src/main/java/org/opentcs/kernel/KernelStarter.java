// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.customizations.kernel.ActiveInAllModes;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes an openTCS kernel instance.
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
   * The kernel's executor service.
   */
  private final ScheduledExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel we're working with.
   * @param plantModelService The plant model service.
   * @param extensions The kernel extensions to be registered.
   * @param kernelExecutor The kernel's executor service.
   */
  @Inject
  protected KernelStarter(
      LocalKernel kernel,
      InternalPlantModelService plantModelService,
      @ActiveInAllModes
      Set<KernelExtension> extensions,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor
  ) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.extensions = requireNonNull(extensions, "extensions");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  /**
   * Initializes the system and starts the openTCS kernel including modules.
   *
   * @throws IOException If there was a problem loading model data.
   */
  public void startKernel()
      throws IOException {
    kernelExecutor.submit(() -> {
      // Register kernel extensions.
      for (KernelExtension extension : extensions) {
        kernel.addKernelExtension(extension);
      }

      // Start local kernel.
      kernel.initialize();
      LOG.debug("Kernel initialized.");

      plantModelService.loadPlantModel();

      kernel.setState(Kernel.State.OPERATING);
    });
  }
}
