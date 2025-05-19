// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralJobStateTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Unit tests for {@link GetPeripheralJobResponseTO}.
 */
class GetPeripheralJobResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    GetPeripheralJobResponseTO to
        = new GetPeripheralJobResponseTO()
            .setName("some-peripheral-job")
            .setReservationToken("some-token")
            .setRelatedVehicle("some-vehicle")
            .setRelatedTransportOrder("some-order")
            .setPeripheralOperation(
                new PeripheralOperationDescription()
                    .setOperation("some-operation")
                    .setLocationName("some-location")
                    .setExecutionTrigger(PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION)
                    .setCompletionRequired(true)
            )
            .setState(PeripheralJobStateTO.BEING_PROCESSED)
            .setCreationTime(Instant.EPOCH)
            .setFinishedTime(Instant.MAX)
            .setProperties(
                List.of(
                    new Property("some-key", "some-value"),
                    new Property("some-other-key", "some-other-value")
                )
            );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
