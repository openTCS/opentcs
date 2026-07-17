// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.EnvironmentalEntityTO;

/**
 * Provides methods to convert {@link EnvironmentalEntity}s to their web API representation.
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
   * Converts a {@link EnvironmentalEntity} to its web API representation.
   *
   * @param entity The entity to convert.
   * @return The converted entity.
   */
  public EnvironmentalEntityTO convert(EnvironmentalEntity entity) {
    return modelMapper.map(entity, EnvironmentalEntityTO.class);
  }
}
