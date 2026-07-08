// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.EnvironmentalEntityEventTO;

/**
 * Provides methods to convert {@link EnvironmentalEntity}s to their SSE representation.
 */
public class EnvironmentalEntityConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public EnvironmentalEntityConverter() {
    modelMapper.validate();
  }

  /**
   * Converts an {@link EnvironmentalEntity} to its SSE representation.
   *
   * @param envEntity The environmental entity to convert.
   * @return The converted entity.
   */
  public EnvironmentalEntityEventTO.EnvironmentalEntityTO convert(EnvironmentalEntity envEntity) {
    return modelMapper.map(envEntity, EnvironmentalEntityEventTO.EnvironmentalEntityTO.class);
  }
}
