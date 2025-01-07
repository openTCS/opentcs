// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.util.Colors;

/**
 * Includes the conversion methods for all Vehicle classes.
 */
public class VehicleConverter {

  private final PropertyConverter pConverter;
  private final AcceptableOrderTypeConverter orderTypeConverter;

  @Inject
  public VehicleConverter(
      PropertyConverter pConverter,
      AcceptableOrderTypeConverter orderTypeConverter
  ) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
    this.orderTypeConverter = requireNonNull(orderTypeConverter, "orderTypeConverter");
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

  public List<VehicleTO> toVehicleTOs(Set<Vehicle> vehicles) {
    return vehicles.stream()
        .map(
            vehicle -> new VehicleTO(vehicle.getName())
                .setBoundingBox(
                    new BoundingBoxTO(
                        vehicle.getBoundingBox().getLength(),
                        vehicle.getBoundingBox().getWidth(),
                        vehicle.getBoundingBox().getHeight(),
                        new CoupleTO(
                            vehicle.getBoundingBox().getReferenceOffset().getX(),
                            vehicle.getBoundingBox().getReferenceOffset().getY()
                        )
                    )
                )
                .setEnergyLevelCritical(
                    vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical()
                )
                .setEnergyLevelGood(vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood())
                .setEnergyLevelFullyRecharged(
                    vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
                )
                .setEnergyLevelSufficientlyRecharged(
                    vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged()
                )
                .setMaxVelocity(vehicle.getMaxVelocity())
                .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
                .setLayout(
                    new VehicleTO.Layout()
                        .setRouteColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor()))
                )
                .setProperties(pConverter.toPropertyTOs(vehicle.getProperties()))
        )
        .sorted(Comparator.comparing(VehicleTO::getName))
        .collect(Collectors.toList());
  }

  /**
   * Converts the given vehicle to a {@link GetVehicleResponseTO} instance.
   *
   * @param vehicle The vehicle.
   * @return A new {@link GetVehicleResponseTO} instance filled with data from the given vehicle.
   */
  public GetVehicleResponseTO toGetVehicleResponseTO(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    GetVehicleResponseTO to = new GetVehicleResponseTO();
    to.setName(vehicle.getName());
    to.setProperties(vehicle.getProperties());
    to.setBoundingBox(
        new BoundingBoxTO(
            vehicle.getBoundingBox().getLength(),
            vehicle.getBoundingBox().getWidth(),
            vehicle.getBoundingBox().getHeight(),
            new CoupleTO(
                vehicle.getBoundingBox().getReferenceOffset().getX(),
                vehicle.getBoundingBox().getReferenceOffset().getY()
            )
        )
    );
    to.setEnergyLevelCritical(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical()
    );
    to.setEnergyLevelGood(vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood());
    to.setEnergyLevelSufficientlyRecharged(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged()
    );
    to.setEnergyLevelFullyRecharged(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
    );
    to.setEnergyLevel(vehicle.getEnergyLevel());
    to.setIntegrationLevel(vehicle.getIntegrationLevel());
    to.setPaused(vehicle.isPaused());
    to.setProcState(vehicle.getProcState());
    to.setTransportOrder(nameOfNullableReference(vehicle.getTransportOrder()));
    to.setCurrentPosition(nameOfNullableReference(vehicle.getCurrentPosition()));
    if (vehicle.getPose().getPosition() != null) {
      to.setPrecisePosition(
          new GetVehicleResponseTO.PrecisePosition(
              vehicle.getPose().getPosition().getX(),
              vehicle.getPose().getPosition().getY(),
              vehicle.getPose().getPosition().getZ()
          )
      );
    }
    else {
      to.setPrecisePosition(null);
    }
    to.setOrientationAngle(vehicle.getPose().getOrientationAngle());
    to.setState(vehicle.getState());
    to.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    to.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    to.setEnvelopeKey(vehicle.getEnvelopeKey());
    to.setAcceptableOrderTypes(
        orderTypeConverter.toAcceptableOrderTypeTOs(vehicle.getAcceptableOrderTypes())
    );
    return to;
  }

  private String nameOfNullableReference(
      @Nullable
      TCSObjectReference<?> reference
  ) {
    return reference == null ? null : reference.getName();
  }

  private List<List<String>> toListOfListOfNames(
      List<Set<TCSResourceReference<?>>> resources
  ) {
    List<List<String>> result = new ArrayList<>(resources.size());

    for (Set<TCSResourceReference<?>> resSet : resources) {
      result.add(
          resSet.stream()
              .map(TCSObjectReference::getName)
              .collect(Collectors.toList())
      );
    }

    return result;
  }
}
