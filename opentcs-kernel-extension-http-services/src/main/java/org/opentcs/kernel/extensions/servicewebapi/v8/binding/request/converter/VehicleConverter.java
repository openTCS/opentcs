// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VehicleTO;
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
                .withBoundingBox(
                    new BoundingBoxCreationTO(
                        vehicle.getBoundingBox().getLength(),
                        vehicle.getBoundingBox().getWidth(),
                        vehicle.getBoundingBox().getHeight()
                    ).withReferenceOffset(
                        new CoupleCreationTO(
                            vehicle.getBoundingBox().getReferenceOffset().getX(),
                            vehicle.getBoundingBox().getReferenceOffset().getY()
                        )
                    )
                )
                .withEnergyLevelThresholdSet(
                    new VehicleCreationTO.EnergyLevelThresholdSet(
                        vehicle.getEnergyLevelCritical(),
                        vehicle.getEnergyLevelGood(),
                        vehicle.getEnergyLevelSufficientlyRecharged(),
                        vehicle.getEnergyLevelFullyRecharged()
                    )
                )
                .withMaxVelocity(vehicle.getMaxVelocity())
                .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
                .withLayout(
                    new VehicleCreationTO.Layout(
                        Colors.decodeFromHexRGB(vehicle.getLayout().getRouteColor())
                    )
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
