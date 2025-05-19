// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.time.Instant;
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
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.VehicleStatusMessage;
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
    to.setIntegrationLevel(toVehicleIntegrationLevel(vehicle.getIntegrationLevel()));
    to.setPaused(vehicle.isPaused());
    to.setProcState(toVehicleProcState(vehicle.getProcState()));
    to.setProcStateTimestamp(vehicle.getProcStateTimestamp());
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
    to.setState(toVehicleState(vehicle.getState()));
    to.setStateTimestamp(vehicle.getStateTimestamp());
    to.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    to.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    to.setEnvelopeKey(vehicle.getEnvelopeKey());
    to.setAcceptableOrderTypes(
        orderTypeConverter.toAcceptableOrderTypeTOs(vehicle.getAcceptableOrderTypes())
    );
    return to;
  }

  public VehicleStatusMessage toVehicleStatusMessage(
      Vehicle vehicle,
      long sequenceNumber,
      Instant creationTimeStamp
  ) {
    VehicleStatusMessage vehicleMessage = new VehicleStatusMessage();
    vehicleMessage.setSequenceNumber(sequenceNumber);
    vehicleMessage.setCreationTimeStamp(creationTimeStamp);
    vehicleMessage.setVehicleName(vehicle.getName());
    vehicleMessage.setProperties(pConverter.toProperties(vehicle.getProperties()));
    vehicleMessage.setTransportOrderName(
        vehicle.getTransportOrder() == null ? null : vehicle.getTransportOrder().getName()
    );
    vehicleMessage.setBoundingBox(
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
    vehicleMessage.setEnergyLevelCritical(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical()
    );
    vehicleMessage.setEnergyLevelGood(vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood());
    vehicleMessage.setEnergyLevelSufficientlyRecharged(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged()
    );
    vehicleMessage.setEnergyLevelFullyRecharged(
        vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
    );
    vehicleMessage.setEnergyLevel(vehicle.getEnergyLevel());
    vehicleMessage.setIntegrationLevel(toVehicleIntegrationLevel(vehicle.getIntegrationLevel()));
    vehicleMessage.setPosition(
        vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName()
    );
    vehicleMessage.setPaused(vehicle.isPaused());
    vehicleMessage.setState(toVehicleState(vehicle.getState()));
    vehicleMessage.setStateTimestamp(vehicle.getStateTimestamp());
    vehicleMessage.setProcState(toVehicleProcState(vehicle.getProcState()));
    vehicleMessage.setProcStateTimestamp(vehicle.getProcStateTimestamp());
    if (vehicle.getPose().getPosition() != null) {
      vehicleMessage.setPrecisePosition(
          new VehicleStatusMessage.PrecisePosition(
              vehicle.getPose().getPosition().getX(),
              vehicle.getPose().getPosition().getY(),
              vehicle.getPose().getPosition().getZ()
          )
      );
    }
    else {
      vehicleMessage.setPrecisePosition(null);
    }
    vehicleMessage.setOrientationAngle(vehicle.getPose().getOrientationAngle());
    vehicleMessage.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    vehicleMessage.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    vehicleMessage.setEnvelopeKey(vehicle.getEnvelopeKey());
    vehicleMessage.setAcceptableOrderTypes(
        orderTypeConverter.toAcceptableOrderTypeTOs(vehicle.getAcceptableOrderTypes())
    );
    return vehicleMessage;
  }

  private VehicleTO.IntegrationLevel toVehicleIntegrationLevel(
      Vehicle.IntegrationLevel integrationLevel
  ) {
    return switch (integrationLevel) {
      case TO_BE_IGNORED -> VehicleTO.IntegrationLevel.TO_BE_IGNORED;
      case TO_BE_NOTICED -> VehicleTO.IntegrationLevel.TO_BE_NOTICED;
      case TO_BE_UTILIZED -> VehicleTO.IntegrationLevel.TO_BE_UTILIZED;
      case TO_BE_RESPECTED -> VehicleTO.IntegrationLevel.TO_BE_RESPECTED;
    };
  }

  private VehicleTO.State toVehicleState(Vehicle.State state) {
    return switch (state) {
      case IDLE -> VehicleTO.State.IDLE;
      case CHARGING -> VehicleTO.State.CHARGING;
      case EXECUTING -> VehicleTO.State.EXECUTING;
      case UNKNOWN -> VehicleTO.State.UNKNOWN;
      case UNAVAILABLE -> VehicleTO.State.UNAVAILABLE;
      case ERROR -> VehicleTO.State.ERROR;
    };
  }

  private VehicleTO.ProcState toVehicleProcState(Vehicle.ProcState procState) {
    return switch (procState) {
      case IDLE -> VehicleTO.ProcState.IDLE;
      case AWAITING_ORDER -> VehicleTO.ProcState.AWAITING_ORDER;
      case PROCESSING_ORDER -> VehicleTO.ProcState.PROCESSING_ORDER;
    };
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
