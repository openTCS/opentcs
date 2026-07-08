// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.Block;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link Block}s to their web API representation.
 */
public class BlockConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public BlockConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link Block} to its web API representation.
   *
   * @param block The block to convert.
   * @return The converted block.
   */
  public BlockTO convert(Block block) {
    return modelMapper.map(block, BlockTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.colorConverter());

    modelMapper.typeMap(Block.class, BlockTO.class)
        .addMappings(
            mapper -> mapper.using(
                Converters.resourceSetConverter()
            ).map(
                Block::getMembers,
                BlockTO::setMembers
            )
        );
  }
}
