/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.util.Colors;

/**
 * Includes the conversion methods for all Vehicle classes.
 */
public class VehicleConverter {

  private final PropertyConverter pConverter;

  @Inject
  public VehicleConverter(PropertyConverter pConverter) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
  }

  public List<VehicleCreationTO> toVehicleCreationTOs(List<VehicleTO> vehicles) {
    return vehicles.stream()
        .map(
            vehicle -> new VehicleCreationTO(vehicle.getName())
                .withProperties(pConverter.toPropertyMap(vehicle.getProperties()))
                .withLength(vehicle.getLength())
                .withEnergyLevelCritical(vehicle.getEnergyLevelCritical())
                .withEnergyLevelGood(vehicle.getEnergyLevelGood())
                .withEnergyLevelFullyRecharged(
                    vehicle.getEnergyLevelFullyRecharged())
                .withEnergyLevelSufficientlyRecharged(
                    vehicle.getEnergyLevelSufficientlyRecharged())
                .withMaxVelocity(vehicle.getMaxVelocity())
                .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
                .withLayout(new VehicleCreationTO.Layout(
                    Colors.decodeFromHexRGB(vehicle.getLayout().getRouteColor()))))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<VehicleTO> toVehicleTOs(Set<Vehicle> vehicles) {
    return vehicles.stream()
        .map(vehicle -> new VehicleTO(vehicle.getName())
        .setLength(vehicle.getLength())
        .setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
        .setEnergyLevelGood(vehicle.getEnergyLevelGood())
        .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
        .setEnergyLevelSufficientlyRecharged(
            vehicle.getEnergyLevelSufficientlyRecharged())
        .setMaxVelocity(vehicle.getMaxVelocity())
        .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
        .setLayout(new VehicleTO.Layout()
            .setRouteColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor())))
        .setProperties(pConverter.toPropertyTOs(vehicle.getProperties())))
        .sorted(Comparator.comparing(VehicleTO::getName))
        .collect(Collectors.toList());
  }
}
