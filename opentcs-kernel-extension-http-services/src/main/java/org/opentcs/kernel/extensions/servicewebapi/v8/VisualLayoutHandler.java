// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.VisualLayoutConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VisualLayoutTO;

/**
 * Handles requests related to visual layouts.
 */
public class VisualLayoutHandler {

  private final InternalTCSObjectService objectService;
  private final VisualLayoutConverter visualLayoutConverter;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve visual layout instances.
   * @param visualLayoutConverter Provides methods to convert visual layouts to their web API
   * representation.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public VisualLayoutHandler(
      InternalTCSObjectService objectService,
      VisualLayoutConverter visualLayoutConverter,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.visualLayoutConverter = requireNonNull(visualLayoutConverter, "visualLayoutConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Retrieves the visual layout.
   *
   * @return The visual layout.
   * @throws ObjectUnknownException If a visual layout with the given name does not exist.
   */
  @Nonnull
  public VisualLayoutTO getVisualLayout()
      throws ObjectUnknownException {
    return executorWrapper.callAndWait(
        () -> objectService.stream(VisualLayout.class)
            .map(visualLayoutConverter::convert)
            .findFirst()
            .orElseThrow(() -> new ObjectUnknownException("There's no visual layout"))
    );
  }
}
