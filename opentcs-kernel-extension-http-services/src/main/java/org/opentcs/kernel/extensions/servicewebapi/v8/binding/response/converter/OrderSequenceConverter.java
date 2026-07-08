// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.OrderSequenceTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link OrderSequence}s to their web API representation.
 */
public class OrderSequenceConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public OrderSequenceConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts an {@link OrderSequence} to its web API representation.
   *
   * @param orderSequence The order sequence to convert.
   * @return The converted order sequence.
   */
  public OrderSequenceTO convert(OrderSequence orderSequence) {
    return modelMapper.map(orderSequence, OrderSequenceTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.instantConverter());
    modelMapper.typeMap(OrderSequence.class, OrderSequenceTO.class);
  }
}
