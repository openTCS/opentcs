// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.Location;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link Location}s to their web API representation.
 */
public class LocationConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public LocationConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link Location} to its web API representation.
   *
   * @param location The location to convert.
   * @return The converted location.
   */
  public LocationTO convert(Location location) {
    return modelMapper.map(location, LocationTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.tcsResourceReferenceConverter());

    modelMapper.typeMap(Location.class, LocationTO.class);
  }
}
