// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO;
import org.opentcs.util.Comparators;

/**
 * Provides methods to convert {@link TransportOrder}s to their SSE representation.
 */
public class TransportOrderConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public TransportOrderConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link TransportOrder} to its SSE representation.
   *
   * @param transportOrder The transport order to convert.
   * @return The converted transport order.
   */
  public TransportOrderEventTO.TransportOrderTO convert(TransportOrder transportOrder) {
    return modelMapper.map(transportOrder, TransportOrderEventTO.TransportOrderTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.pathConverter());
    modelMapper.addConverter(Converters.pointConverter());
    // Transport order provides multiple methods for retrieving drive orders, which prevents
    // ModelMapper from automatically determining which one to use when mapping
    // TransportOrderEventTO.TransportOrderTO.setDriveOrders(). Enabling strict matching in
    // conjunction with explicitly adding a mapping for the setDriveOrders() method solves this
    // issue.
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.typeMap(TransportOrder.class, TransportOrderEventTO.TransportOrderTO.class)
        .addMappings(
            mapper -> mapper.using(
                Converters.setToListConverter(
                    Comparators.referencesByName(),
                    String.class,
                    modelMapper
                )
            )
                .map(
                    TransportOrder::getDependencies,
                    TransportOrderEventTO.TransportOrderTO::setDependencies
                )
        )
        .addMapping(
            TransportOrder::getAllDriveOrders,
            TransportOrderEventTO.TransportOrderTO::setDriveOrders
        );
  }
}
