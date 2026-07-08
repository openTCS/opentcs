// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.LocationType;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTypeTO;

/**
 * Provides methods to convert {@link LocationType}s to their web API representation.
 */
public class LocationTypeConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public LocationTypeConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link LocationType} to its web API representation.
   *
   * @param locationType The location type to convert.
   * @return The converted location type.
   */
  public LocationTypeTO convert(LocationType locationType) {
    return modelMapper.map(locationType, LocationTypeTO.class);
  }

  private void configureModelMapper() {
    modelMapper.typeMap(LocationType.class, LocationTypeTO.class);
  }
}
