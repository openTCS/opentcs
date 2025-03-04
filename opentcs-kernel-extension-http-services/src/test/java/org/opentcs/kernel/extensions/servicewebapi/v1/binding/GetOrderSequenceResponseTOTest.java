// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 */
class GetOrderSequenceResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    GetOrderSequenceResponseTO to = new GetOrderSequenceResponseTO("some-order-sequence")
        .setType("Charge")
        .setOrders(List.of("some-order", "another-order", "order-3"))
        .setCreationTime(Instant.EPOCH)
        .setFinishedTime(Instant.MAX)
        .setFinishedIndex(3)
        .setComplete(false)
        .setFinished(false)
        .setFailureFatal(true)
        .setIntendedVehicle("some-vehicle")
        .setProcessingVehicle(null)
        .setProperties(
            List.of(
                new Property("some-key", "some-value"),
                new Property("another-key", "another-value")
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
