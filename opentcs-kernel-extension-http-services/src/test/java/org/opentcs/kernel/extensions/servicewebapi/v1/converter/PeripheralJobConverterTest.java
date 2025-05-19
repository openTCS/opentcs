// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.PeripheralJobStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralJobStateTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;

/**
 * Tests for {@link PeripheralJobConverter}.
 */
public class PeripheralJobConverterTest {
  private final PeripheralJobConverter peripheralJobConverter = new PeripheralJobConverter(
      new PeripheralOperationConverter()
  );

  @Test
  void checkToPeripheralJobStatusMessage() {
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            new Location("L1", new LocationType("LT1").getReference()).getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedVehicle(new Vehicle("V1").getReference())
        .withRelatedTransportOrder(new TransportOrder("T1", List.of()).getReference())
        .withState(PeripheralJob.State.TO_BE_PROCESSED)
        .withCreationTime(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH)
        .withProperty("some-prop-key", "some-prop-value");

    PeripheralJobStatusMessage statusMessage = peripheralJobConverter.toPeripheralJobStatusMessage(
        job, 1L, Instant.EPOCH
    );
    assertThat(statusMessage.getName(), is("some-job"));
    assertThat(statusMessage.getReservationToken(), is("some-token"));
    assertThat(statusMessage.getPeripheralOperation().getOperation(), is("some-operation"));
    assertThat(statusMessage.getPeripheralOperation().getLocationName(), is("L1"));
    assertThat(
        statusMessage.getPeripheralOperation().getExecutionTrigger(), is(
            PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION
        )
    );
    assertThat(statusMessage.getPeripheralOperation().isCompletionRequired(), is(true));
    assertThat(
        statusMessage.getState(), is(PeripheralJobStateTO.TO_BE_PROCESSED)
    );
    assertThat(statusMessage.getRelatedVehicle(), is("V1"));
    assertThat(statusMessage.getRelatedTransportOrder(), is("T1"));
    assertThat(statusMessage.getCreationTime(), is(Instant.EPOCH));
    assertThat(statusMessage.getFinishedTime(), is(Instant.EPOCH));
    assertThat(statusMessage.getProperties().size(), is(1));
    assertThat(statusMessage.getProperties().getFirst().getKey(), is("some-prop-key"));
    assertThat(statusMessage.getProperties().getFirst().getValue(), is("some-prop-value"));
  }

  @Test
  void checkToGetPeripheralJobResponse() {
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            new Location("L1", new LocationType("LT1").getReference()).getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedVehicle(new Vehicle("V1").getReference())
        .withRelatedTransportOrder(new TransportOrder("T1", List.of()).getReference())
        .withState(PeripheralJob.State.TO_BE_PROCESSED)
        .withCreationTime(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH)
        .withProperty("some-prop-key", "some-prop-value");

    GetPeripheralJobResponseTO response = peripheralJobConverter.toGetPeripheralJobResponseTO(job);
    assertThat(response.getName(), is("some-job"));
    assertThat(response.getReservationToken(), is("some-token"));
    assertThat(response.getPeripheralOperation().getOperation(), is("some-operation"));
    assertThat(response.getPeripheralOperation().getLocationName(), is("L1"));
    assertThat(
        response.getPeripheralOperation().getExecutionTrigger(), is(
            PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION
        )
    );
    assertThat(response.getPeripheralOperation().isCompletionRequired(), is(true));
    assertThat(response.getState(), is(PeripheralJobStateTO.TO_BE_PROCESSED));
    assertThat(response.getRelatedVehicle(), is("V1"));
    assertThat(response.getRelatedTransportOrder(), is("T1"));
    assertThat(response.getCreationTime(), is(Instant.EPOCH));
    assertThat(response.getFinishedTime(), is(Instant.EPOCH));
    assertThat(response.getProperties().size(), is(1));
    assertThat(response.getProperties().getFirst().getKey(), is("some-prop-key"));
    assertThat(response.getProperties().getFirst().getValue(), is("some-prop-value"));
  }
}
