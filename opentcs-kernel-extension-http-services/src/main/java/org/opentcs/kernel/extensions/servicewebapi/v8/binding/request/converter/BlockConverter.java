// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.BlockTO;
import org.opentcs.util.Colors;

/**
 * Includes the conversion methods for all Block classes.
 */
public class BlockConverter {

  @Inject
  public BlockConverter() {
  }

  public List<BlockCreationTO> toBlockCreationTOs(List<BlockTO> blocks) {
    return blocks.stream()
        .map(
            block -> new BlockCreationTO(block.getName())
                .withProperties(Optional.ofNullable(block.getProperties()).orElse(Map.of()))
                .withMemberNames(block.getMemberNames())
                .withType(convertToBlockType(block.getType()))
                .withLayout(
                    new BlockCreationTO.Layout(
                        Colors.decodeFromHexRGB(block.getLayout().getColor())
                    )
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private BlockCreationTO.Type convertToBlockType(BlockTO.Type type) {
    return switch (type) {
      case SAME_DIRECTION_ONLY -> BlockCreationTO.Type.SAME_DIRECTION_ONLY;
      case SINGLE_VEHICLE_ONLY -> BlockCreationTO.Type.SINGLE_VEHICLE_ONLY;
    };
  }
}
