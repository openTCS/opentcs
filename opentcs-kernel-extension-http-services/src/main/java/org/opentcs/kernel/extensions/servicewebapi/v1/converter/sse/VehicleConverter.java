// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import java.util.Comparator;
import org.modelmapper.ModelMapper;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.VehicleEventTO;

/**
 * Provides methods to convert {@link Vehicle}s to their SSE representation.
 */
public class VehicleConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public VehicleConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts a {@link Vehicle} to its SSE representation.
   *
   * @param vehicle The vehicle to convert.
   * @return The converted vehicle.
   */
  public VehicleEventTO.VehicleTO convert(Vehicle vehicle) {
    return modelMapper.map(vehicle, VehicleEventTO.VehicleTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.colorConverter());

    modelMapper.typeMap(Vehicle.class, VehicleEventTO.VehicleTO.class)
        .addMappings(
            mapper -> mapper.using(
                Converters.setToListConverter(
                    Comparator.comparing(AcceptableOrderType::getName),
                    VehicleEventTO.AcceptableOrderTypeTO.class,
                    modelMapper
                )
            ).map(
                Vehicle::getAcceptableOrderTypes,
                VehicleEventTO.VehicleTO::setAcceptableOrderTypes
            )
        )
        .addMappings(
            mapper -> mapper.using(Converters.tcsObjectReferenceListConverter()).map(
                Vehicle::getClaimedResources,
                VehicleEventTO.VehicleTO::setClaimedResources
            )
        )
        .addMappings(
            mapper -> mapper.using(Converters.tcsObjectReferenceListConverter()).map(
                Vehicle::getAllocatedResources,
                VehicleEventTO.VehicleTO::setAllocatedResources
            )
        );
  }
}
