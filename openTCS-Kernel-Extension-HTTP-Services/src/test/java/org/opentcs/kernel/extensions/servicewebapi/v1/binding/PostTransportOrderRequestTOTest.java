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
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.posttransportorder.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Unit tests for {@link PostTransportOrderRequestTO}.
 */
class PostTransportOrderRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PostTransportOrderRequestTO to
        = new PostTransportOrderRequestTO()
            .setIncompleteName(true)
            .setDispensable(true)
            .setDeadline(Instant.EPOCH)
            .setIntendedVehicle("some-vehicle")
            .setPeripheralReservationToken("some-token")
            .setWrappingSequence("some-sequence")
            .setType("some-type")
            .setDestinations(
                List.of(
                    new Destination()
                        .setLocationName("some-location")
                        .setOperation("some-operation")
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
            )
            .setDependencies(
                List.of(
                    "some-other-order",
                    "another-order"
                )
            );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
