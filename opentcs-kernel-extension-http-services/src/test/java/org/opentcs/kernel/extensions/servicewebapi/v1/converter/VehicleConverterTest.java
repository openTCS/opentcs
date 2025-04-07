// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.VehicleStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.util.Colors;

/**
 * Tests for {@link VehicleConverter}.
 */
class VehicleConverterTest {

  private VehicleConverter vehicleConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyTOList;
  private List<Property> propertyList;

  @BeforeEach
  void setUp() {
    PropertyConverter propertyConverter = mock();
    vehicleConverter = new VehicleConverter(propertyConverter, new AcceptableOrderTypeConverter());

    propertyMap = Map.of("some-key", "some-value");
    propertyTOList = List.of(new PropertyTO("some-key", "some-value"));
    propertyList = List.of(new Property("some-key", "some-value"));
    Set<AcceptableOrderType> acceptableOrderTypes = Set.of(new AcceptableOrderType("order-1", 0));
    List<AcceptableOrderTypeTO> acceptableOrderTypeList = List.of(
        new AcceptableOrderTypeTO("order-1", 0)
    );
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyTOList);
    when(propertyConverter.toProperties(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyTOList)).thenReturn(propertyMap);
  }

  @Test
  void checkToVehicleCreationTOs() {
    VehicleTO vehicleTo = new VehicleTO("V1")
        .setBoundingBox(new BoundingBoxTO(500, 100, 700, new CoupleTO(0, 0)))
        .setEnergyLevelGood(90)
        .setEnergyLevelCritical(30)
        .setEnergyLevelFullyRecharged(90)
        .setEnergyLevelSufficientlyRecharged(30)
        .setMaxVelocity(1000)
        .setMaxReverseVelocity(1000)
        .setLayout(new VehicleTO.Layout())
        .setProperties(propertyTOList);

    List<VehicleCreationTO> result = vehicleConverter.toVehicleCreationTOs(List.of(vehicleTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("V1"));
    assertThat(result.get(0).getBoundingBox().getLength(), is(500L));
    assertThat(result.get(0).getBoundingBox().getWidth(), is(100L));
    assertThat(result.get(0).getBoundingBox().getHeight(), is(700L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getX(), is(0L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getY(), is(0L));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelGood(), is(90));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelCritical(), is(30));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged(), is(90));
    assertThat(
        result.get(0).getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged(), is(30)
    );
    assertThat(result.get(0).getMaxVelocity(), is(1000));
    assertThat(result.get(0).getMaxReverseVelocity(), is(1000));
    assertThat(result.get(0).getLayout().getRouteColor(), is(Colors.decodeFromHexRGB("#00FF00")));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }

  @Test
  void checkToVehicleTOs() {
    Vehicle vehicle = new Vehicle("V1")
        .withBoundingBox(new BoundingBox(500, 100, 700))
        .withEnergyLevelThresholdSet(new EnergyLevelThresholdSet(30, 90, 30, 90))
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000)
        .withLayout(new Vehicle.Layout())
        .withProperties(propertyMap);

    List<VehicleTO> result = vehicleConverter.toVehicleTOs(Set.of(vehicle));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("V1"));
    assertThat(result.get(0).getBoundingBox().getLength(), is(500L));
    assertThat(result.get(0).getBoundingBox().getWidth(), is(100L));
    assertThat(result.get(0).getBoundingBox().getHeight(), is(700L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getX(), is(0L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getY(), is(0L));
    assertThat(result.get(0).getEnergyLevelGood(), is(90));
    assertThat(result.get(0).getEnergyLevelCritical(), is(30));
    assertThat(result.get(0).getEnergyLevelFullyRecharged(), is(90));
    assertThat(result.get(0).getEnergyLevelSufficientlyRecharged(), is(30));
    assertThat(result.get(0).getMaxVelocity(), is(1000));
    assertThat(result.get(0).getMaxReverseVelocity(), is(1000));
    assertThat(result.get(0).getLayout().getRouteColor(), is(Colors.encodeToHexRGB(Color.RED)));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyTOList));
  }

  @Test
  void checkToGetVehicleResponseTO() {
    Vehicle vehicle = new Vehicle("V1")
        .withBoundingBox(new BoundingBox(500, 100, 700))
        .withEnergyLevelThresholdSet(new EnergyLevelThresholdSet(30, 90, 30, 90))
        .withEnergyLevel(50)
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withPaused(true)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withTransportOrder(new TransportOrder("some-order", List.of()).getReference())
        .withCurrentPosition(new Point("some-point").getReference())
        .withPose(new Pose(new Triple(1, 2, 3), 4))
        .withState(Vehicle.State.EXECUTING)
        .withAllocatedResources(List.of(Set.of(new Point("some-other-point").getReference())))
        .withClaimedResources(List.of(Set.of(new Point("yet-another-point").getReference())))
        .withEnvelopeKey("some-key")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-type", 3)))
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000)
        .withLayout(new Vehicle.Layout())
        .withProperties(propertyMap);

    GetVehicleResponseTO result = vehicleConverter.toGetVehicleResponseTO(vehicle);

    assertThat(result.getName(), is("V1"));
    assertThat(result.getBoundingBox().getLength(), is(500L));
    assertThat(result.getBoundingBox().getWidth(), is(100L));
    assertThat(result.getBoundingBox().getHeight(), is(700L));
    assertThat(result.getBoundingBox().getReferenceOffset().getX(), is(0L));
    assertThat(result.getBoundingBox().getReferenceOffset().getY(), is(0L));
    assertThat(result.getEnergyLevelGood(), is(90));
    assertThat(result.getEnergyLevelCritical(), is(30));
    assertThat(result.getEnergyLevelFullyRecharged(), is(90));
    assertThat(result.getEnergyLevelSufficientlyRecharged(), is(30));
    assertThat(result.getEnergyLevel(), is(50));
    assertThat(result.getIntegrationLevel(), is(Vehicle.IntegrationLevel.TO_BE_UTILIZED));
    assertThat(result.isPaused(), is(true));
    assertThat(result.getProcState(), is(Vehicle.ProcState.PROCESSING_ORDER));
    assertThat(result.getTransportOrder(), is("some-order"));
    assertThat(result.getCurrentPosition(), is("some-point"));
    assertThat(result.getPrecisePosition().getX(), is(1L));
    assertThat(result.getPrecisePosition().getY(), is(2L));
    assertThat(result.getPrecisePosition().getZ(), is(3L));
    assertThat(result.getOrientationAngle(), is(4.0));
    assertThat(result.getState(), is(Vehicle.State.EXECUTING));
    assertThat(result.getAllocatedResources(), is(List.of(List.of("some-other-point"))));
    assertThat(result.getClaimedResources(), is(List.of(List.of("yet-another-point"))));
    assertThat(result.getEnvelopeKey(), is("some-key"));
    assertThat(
        result.getAcceptableOrderTypes(),
        is(List.of(new AcceptableOrderTypeTO("some-type", 3)))
    );
    assertThat(result.getProperties(), is(aMapWithSize(1)));
    assertThat(result.getProperties(), is(propertyMap));
  }

  @Test
  void checkToVehicleStatusMessage() {
    Vehicle vehicle = new Vehicle("V1")
        .withBoundingBox(new BoundingBox(500, 100, 700))
        .withEnergyLevelThresholdSet(new EnergyLevelThresholdSet(30, 90, 30, 90))
        .withEnergyLevel(50)
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withPaused(true)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withTransportOrder(new TransportOrder("some-order", List.of()).getReference())
        .withCurrentPosition(new Point("some-point").getReference())
        .withPose(new Pose(new Triple(1, 2, 3), 4))
        .withState(Vehicle.State.EXECUTING)
        .withAllocatedResources(List.of(Set.of(new Point("some-other-point").getReference())))
        .withClaimedResources(List.of(Set.of(new Point("yet-another-point").getReference())))
        .withEnvelopeKey("some-key")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-type", 3)))
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000)
        .withLayout(new Vehicle.Layout())
        .withProperties(propertyMap);

    VehicleStatusMessage message = vehicleConverter.toVehicleStatusMessage(
        vehicle, 555, Instant.EPOCH
    );

    assertThat(message.getVehicleName(), is("V1"));
    assertThat(message.getBoundingBox().getLength(), is(500L));
    assertThat(message.getBoundingBox().getWidth(), is(100L));
    assertThat(message.getBoundingBox().getHeight(), is(700L));
    assertThat(message.getBoundingBox().getReferenceOffset().getX(), is(0L));
    assertThat(message.getBoundingBox().getReferenceOffset().getY(), is(0L));
    assertThat(message.getEnergyLevelGood(), is(90));
    assertThat(message.getEnergyLevelCritical(), is(30));
    assertThat(message.getEnergyLevelFullyRecharged(), is(90));
    assertThat(message.getEnergyLevelSufficientlyRecharged(), is(30));
    assertThat(message.getEnergyLevel(), is(50));
    assertThat(message.getIntegrationLevel(), is(Vehicle.IntegrationLevel.TO_BE_UTILIZED));
    assertThat(message.isPaused(), is(true));
    assertThat(message.getProcState(), is(Vehicle.ProcState.PROCESSING_ORDER));
    assertThat(message.getTransportOrderName(), is("some-order"));
    assertThat(message.getPrecisePosition().getX(), is(1L));
    assertThat(message.getPrecisePosition().getY(), is(2L));
    assertThat(message.getPrecisePosition().getZ(), is(3L));
    assertThat(message.getOrientationAngle(), is(4.0));
    assertThat(message.getState(), is(Vehicle.State.EXECUTING));
    assertThat(message.getAllocatedResources(), is(List.of(List.of("some-other-point"))));
    assertThat(message.getClaimedResources(), is(List.of(List.of("yet-another-point"))));
    assertThat(message.getEnvelopeKey(), is("some-key"));
    assertThat(
        message.getAcceptableOrderTypes(),
        is(List.of(new AcceptableOrderTypeTO("some-type", 3)))
    );
    assertThat(message.getProperties().size(), is(1));
    assertThat(message.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(message.getProperties().getFirst().getValue(), is("some-value"));
  }
}
