/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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

    Approvals.verify(jsonBinder.toJson(to),
                     Approvals.NAMES.withParameters("orientationAngle-" + orientationAngle));
  }

  private VehicleStatusMessage createVehicleStatusMessage(long sequenceNo) {
    return new VehicleStatusMessage()
        .setSequenceNumber(sequenceNo)
        .setCreationTimeStamp(Instant.EPOCH)
        .setVehicleName("some-vehicle")
        .setTransportOrderName("some-transport-order")
        .setPosition("some-point")
        .setPrecisePosition(new VehicleStatusMessage.PrecisePosition(1, 2, 3))
        .setPaused(false)
        .setState(Vehicle.State.IDLE)
        .setProcState(Vehicle.ProcState.IDLE)
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
        );
  }

  private OrderStatusMessage createOrderStatusMessage(long sequenceNo) {
    return new OrderStatusMessage()
        .setSequenceNumber(sequenceNo)
        .setCreationTimeStamp(Instant.EPOCH)
        .setOrderName("some-order")
        .setProcessingVehicleName("some-vehicle")
        .setOrderState(OrderStatusMessage.OrderState.BEING_PROCESSED)
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
