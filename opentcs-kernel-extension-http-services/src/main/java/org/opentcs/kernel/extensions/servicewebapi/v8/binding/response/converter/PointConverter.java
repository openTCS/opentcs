// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.Point;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link Point}s to their web API representation.
 */
public class PointConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public PointConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link Point} to its web API representation.
   *
   * @param point The point to convert.
   * @return The converted point.
   */
  public PointTO convert(Point point) {
    return modelMapper.map(point, PointTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.tcsResourceReferenceConverter());
    modelMapper.addConverter(Converters.nanToNullConverter());

    modelMapper.typeMap(Point.class, PointTO.class)
        .addMappings(
            mapper -> mapper.using(
                Converters.tcsObjectReferenceSetConverter()
            ).map(
                Point::getIncomingPaths,
                PointTO::setIncomingPaths
            )
        )
        .addMappings(
            mapper -> mapper.using(
                Converters.tcsObjectReferenceSetConverter()
            ).map(
                Point::getOutgoingPaths,
                PointTO::setOutgoingPaths
            )
        );
  }
}
