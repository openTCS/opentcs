// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.stream.Collectors;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.PeripheralJobStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralJobStateTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Includes the conversion methods for all PeripheralJob classes.
 */
public class PeripheralJobConverter {
  private final PeripheralOperationConverter peripheralOperationConverter;

  @Inject
  public PeripheralJobConverter(PeripheralOperationConverter peripheralOperationConverter) {
    this.peripheralOperationConverter = requireNonNull(
        peripheralOperationConverter, "peripheralOperationConverter"
    );
  }

  public PeripheralJobStatusMessage toPeripheralJobStatusMessage(
      PeripheralJob job,
      long sequenceNumber,
      Instant creationTimestamp
  ) {
    PeripheralJobStatusMessage message = new PeripheralJobStatusMessage();
    message.setSequenceNumber(sequenceNumber);
    message.setCreationTimeStamp(creationTimestamp);

    message.setName(job.getName());
    message.setReservationToken(job.getReservationToken());
    if (job.getRelatedVehicle() != null) {
      message.setRelatedVehicle(job.getRelatedVehicle().getName());
    }
    if (job.getRelatedTransportOrder() != null) {
      message.setRelatedTransportOrder(job.getRelatedTransportOrder().getName());
    }
    message.setPeripheralOperation(
        peripheralOperationConverter.toPeripheralOperationDescription(job.getPeripheralOperation())
    );
    message.setState(toPeripheralJobStateTO(job.getState()));
    message.setCreationTime(job.getCreationTime());
    message.setFinishedTime(job.getFinishedTime());
    message.setProperties(
        job.getProperties().entrySet().stream()
            .map(entry -> new Property(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList())
    );

    return message;
  }

  public GetPeripheralJobResponseTO toGetPeripheralJobResponseTO(PeripheralJob job) {
    GetPeripheralJobResponseTO state = new GetPeripheralJobResponseTO();
    state = state
        .setName(job.getName())
        .setReservationToken(job.getReservationToken())
        .setPeripheralOperation(
            peripheralOperationConverter.toPeripheralOperationDescription(
                job.getPeripheralOperation()
            )
        )
        .setState(toPeripheralJobStateTO(job.getState()))
        .setCreationTime(job.getCreationTime())
        .setFinishedTime(job.getFinishedTime())
        .setProperties(
            job.getProperties().entrySet().stream()
                .map(entry -> new Property(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
        );
    if (job.getRelatedVehicle() != null) {
      state = state.setRelatedVehicle(job.getRelatedVehicle().getName());
    }
    if (job.getRelatedTransportOrder() != null) {
      state = state.setRelatedTransportOrder(job.getRelatedTransportOrder().getName());
    }
    return state;
  }

  private PeripheralJobStateTO toPeripheralJobStateTO(
      PeripheralJob.State state
  ) {
    return switch (state) {
      case TO_BE_PROCESSED -> PeripheralJobStateTO.TO_BE_PROCESSED;
      case BEING_PROCESSED -> PeripheralJobStateTO.BEING_PROCESSED;
      case FINISHED -> PeripheralJobStateTO.FINISHED;
      case FAILED -> PeripheralJobStateTO.FAILED;
    };
  }
}
