// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import java.util.Comparator;
import org.modelmapper.ModelMapper;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link Vehicle}s to their web API representation.
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
   * Converts a {@link Vehicle} to its web API representation.
   *
   * @param vehicle The vehicle to convert.
   * @return The converted vehicle.
   */
  public VehicleTO convert(Vehicle vehicle) {
    return modelMapper.map(vehicle, VehicleTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.colorConverter());
    modelMapper.addConverter(Converters.nanToNullConverter());

    modelMapper.typeMap(Vehicle.class, VehicleTO.class)
        .addMappings(
            mapper -> mapper.using(
                Converters.setToListConverter(
                    Comparator.comparing(AcceptableOrderType::getName),
                    VehicleTO.AcceptableOrderTypeTO.class,
                    modelMapper
                )
            ).map(
                Vehicle::getAcceptableOrderTypes,
                VehicleTO::setAcceptableOrderTypes
            )
        )
        .addMappings(
            mapper -> mapper.using(Converters.resourceConverter()).map(
                Vehicle::getClaimedResources,
                VehicleTO::setClaimedResources
            )
        )
        .addMappings(
            mapper -> mapper.using(Converters.resourceConverter()).map(
                Vehicle::getAllocatedResources,
                VehicleTO::setAllocatedResources
            )
        );
  }
}
