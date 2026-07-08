// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VisualLayoutTO;

/**
 * Provides methods to convert {@link VisualLayout}s to their web API representation.
 */
public class VisualLayoutConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public VisualLayoutConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link VisualLayout} to its web API representation.
   *
   * @param visualLayout The visual layout to convert.
   * @return The visual layout point.
   */
  public VisualLayoutTO convert(VisualLayout visualLayout) {
    return modelMapper.map(visualLayout, VisualLayoutTO.class);
  }

  private void configureModelMapper() {
    modelMapper.typeMap(VisualLayout.class, VisualLayoutTO.class);
  }
}
