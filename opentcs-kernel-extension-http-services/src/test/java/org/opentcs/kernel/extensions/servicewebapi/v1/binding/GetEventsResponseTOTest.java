// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.PeripheralJobStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.VehicleStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Unit tests for {@link GetEventsResponseTO}.
 */
class GetEventsResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, 90.0})
  void jsonSample(double orientationAngle) {
    GetEventsResponseTO to
        = new GetEventsResponseTO()
            .setTimeStamp(Instant.EPOCH)
            .setStatusMessages(
                List.of(
                    createVehicleStatusMessage(0).setOrientationAngle(orientationAngle),
                    createOrderStatusMessage(1),
                    createPeripheralJobStatusMessage(2)
                )
            );

    Approvals.verify(
        jsonBinder.toJson(to),
        Approvals.NAMES.withParameters("orientationAngle-" + orientationAngle)
    );
  }

  private VehicleStatusMessage createVehicleStatusMessage(long sequenceNo) {
    return new VehicleStatusMessage()
        .setSequenceNumber(sequenceNo)
        .setCreationTimeStamp(Instant.EPOCH)
        .setVehicleName("some-vehicle")
        .setProperties(List.of(new Property("some-key", "some-value")))
        .setTransportOrderName("some-transport-order")
        .setBoundingBox(new BoundingBoxTO(500, 400, 500, new CoupleTO(20, 20)))
        .setEnergyLevelGood(95)
        .setEnergyLevelCritical(20)
        .setEnergyLevelSufficientlyRecharged(40)
        .setEnergyLevelFullyRecharged(80)
        .setEnergyLevel(70)
        .setIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .setPosition("some-point")
        .setPrecisePosition(new VehicleStatusMessage.PrecisePosition(1, 2, 3))
        .setPaused(false)
        .setState(Vehicle.State.IDLE)
        .setStateTimestamp(Instant.parse("2025-01-29T11:12:43.000Z"))
        .setProcState(Vehicle.ProcState.IDLE)
        .setProcStateTimestamp(Instant.parse("2025-01-29T11:58:02.000Z"))
        .setAllocatedResources(
            List.of(
                List.of("some-path", "some-point"),
                List.of("some-other-path", "some-other-point")
            )
        )
        .setClaimedResources(
            List.of(
                List.of("some-path", "some-point"),
                List.of("some-other-path", "some-other-point")
            )
        )
        .setAcceptableOrderTypes(List.of(new AcceptableOrderTypeTO("some-order-type", 1)))
        .setEnvelopeKey("some-envelope-key");
  }

  private OrderStatusMessage createOrderStatusMessage(long sequenceNo) {
    return new OrderStatusMessage()
        .setSequenceNumber(sequenceNo)
        .setDispensable(true)
        .setCreationTimeStamp(Instant.EPOCH)
        .setOrderName("some-order")
        .setOrderType("some-order-type")
        .setOrderState(OrderStatusMessage.OrderState.BEING_PROCESSED)
        .setIntendedVehicle("some-intended-vehicle")
        .setProcessingVehicleName("some-vehicle")
        .setPeripheralReservationToken("some-peripheral-reservation-token")
        .setWrappingSequence("some-wrapping-sequence")
        .setDestinations(
            List.of(
                new DestinationState()
                    .setLocationName("some-location")
                    .setOperation("some-operation")
                    .setState(DestinationState.State.TRAVELLING)
                    .setProperties(
                        List.of(
                            new Property("some-key", "some-value"),
                            new Property("some-other-key", "some-other-value")
                        )
                    )
            )
        )
        .setProperties(
            List.of(
                new Property("some-key", "some-value"),
                new Property("some-other-key", "some-other-value")
            )
        );
  }

  private PeripheralJobStatusMessage createPeripheralJobStatusMessage(long sequenceNo) {
    return new PeripheralJobStatusMessage()
        .setSequenceNumber(sequenceNo)
        .setCreationTimeStamp(Instant.EPOCH)
        .setName("some-peripheral-job")
        .setReservationToken("some-token")
        .setRelatedVehicle("some-vehicle")
        .setRelatedTransportOrder("some-order")
        .setPeripheralOperation(
            new PeripheralOperationDescription()
                .setOperation("some-operation")
                .setLocationName("some-location")
                .setExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
                .setCompletionRequired(true)
        )
        .setState(PeripheralJob.State.BEING_PROCESSED)
        .setCreationTime(Instant.EPOCH)
        .setFinishedTime(Instant.MAX)
        .setProperties(
            List.of(
                new Property("some-key", "some-value"),
                new Property("some-other-key", "some-other-value")
            )
        );
  }
}
