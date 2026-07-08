// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;

/**
 * Unit tests for {@link PostPeripheralJobRequestTO}.
 */
class PostPeripheralJobRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PostPeripheralJobRequestTO to
        = new PostPeripheralJobRequestTO()
            .setIncompleteName(true)
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
            .setProperties(
                List.of(
                    new Property("some-key", "some-value"),
                    new Property("some-other-key", "some-other-value")
                )
            );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
