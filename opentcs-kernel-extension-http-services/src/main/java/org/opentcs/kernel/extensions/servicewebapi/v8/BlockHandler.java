// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Block;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.BlockConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.BlockTO;

/**
 * Handles requests related to blocks.
 */
public class BlockHandler {

  private final InternalTCSObjectService objectService;
  private final BlockConverter blockConverter;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve block instances.
   * @param blockConverter Provides methods to convert blocks to their web API representation.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public BlockHandler(
      InternalTCSObjectService objectService,
      BlockConverter blockConverter,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.blockConverter = requireNonNull(blockConverter, "blockConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Finds all blocks and filters them depending on the given parameters.
   *
   * @param names The names of the blocks to be retrieved. If a named block could not be found, it
   * will simply be omitted from the result. Filtering for this parameter is disabled if the
   * provided list is empty.
   * @return A list of blocks that match the filter.
   */
  @Nonnull
  public List<BlockTO> getBlocks(
      @Nonnull
      List<String> names
  ) {
    requireNonNull(names, "names");
    if (names.isEmpty()) {
      // Optimization - With no filter, filtering is not necessary.
      return executorWrapper.callAndWait(
          () -> objectService.stream(Block.class)
              .map(blockConverter::convert)
              .sorted(Comparator.comparing(BlockTO::getName))
              .toList()
      );
    }

    return executorWrapper.callAndWait(
        () -> objectService.stream(Block.class)
            .filter(Filters.objectNameMatchesOneOf(names))
            .map(blockConverter::convert)
            .sorted(Comparator.comparing(BlockTO::getName))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the block with the given name.
   *
   * @param name The name of the requested block.
   * @return A single block with the given name.
   * @throws ObjectUnknownException If a block with the given name does not exist.
   */
  @Nonnull
  public BlockTO getBlockByName(
      @Nonnull
      String name
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(
        () -> objectService.fetch(Block.class, name)
            .map(blockConverter::convert)
            .orElseThrow(() -> new ObjectUnknownException("Unknown block: " + name))
    );
  }
}
