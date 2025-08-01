// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import org.modelmapper.ModelMapper;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.OrderSequenceEventTO;

/**
 * Provides methods to convert {@link OrderSequence}s to their SSE representation.
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
   * Converts an {@link OrderSequence} to its SSE representation.
   *
   * @param orderSequence The order sequence to convert.
   * @return The converted order sequence.
   */
  public OrderSequenceEventTO.OrderSequenceTO convert(OrderSequence orderSequence) {
    return modelMapper.map(orderSequence, OrderSequenceEventTO.OrderSequenceTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.typeMap(OrderSequence.class, OrderSequenceEventTO.OrderSequenceTO.class);
  }
}
