/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

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
                    .setExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
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
