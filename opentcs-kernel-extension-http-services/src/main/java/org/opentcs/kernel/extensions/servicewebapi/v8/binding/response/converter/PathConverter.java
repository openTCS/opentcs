// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.Path;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link Path}s to their web API representation.
 */
public class PathConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public PathConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link Path} to its web API representation.
   *
   * @param path The path to convert.
   * @return The converted path.
   */
  public PathTO convert(Path path) {
    return modelMapper.map(path, PathTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.tcsResourceReferenceConverter());

    modelMapper.typeMap(Path.class, PathTO.class);
  }
}
