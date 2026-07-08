// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

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
  void deserializeMinimal() {
    PostTransportOrderRequestTO to
        = new PostTransportOrderRequestTO(
            List.of(
                new PostTransportOrderRequestTO.Destination("some-location", "some-operation")
            )
        );

    String json = jsonBinder.toJson(to);
    Approvals.verify(json);

    PostTransportOrderRequestTO deserializationResult = jsonBinder.fromJson(
        json, PostTransportOrderRequestTO.class
    );
    assertThat(deserializationResult).isEqualTo(to);
  }

  @Test
  void deserializeFull() {
    PostTransportOrderRequestTO to
        = new PostTransportOrderRequestTO(
            List.of(
                new PostTransportOrderRequestTO.Destination("some-location", "some-operation")
                    .setProperties(
                        Map.of(
                            "some-key", "some-value",
                            "some-other-key", "some-other-value"
                        )
                    )
            )
        )
            .setIncompleteName(true)
            .setDispensable(true)
            .setDeadline(Instant.EPOCH)
            .setIntendedVehicle("some-vehicle")
            .setPeripheralReservationToken("some-token")
            .setWrappingSequence("some-sequence")
            .setType("some-type")
            .setProperties(
                Map.of(
                    "some-other-key", "some-other-value",
                    "some-key", "some-value"
                )
            )
            .setDependencies(
                List.of(
                    "some-other-order",
                    "another-order"
                )
            );

    String json = jsonBinder.toJson(to);
    Approvals.verify(json);

    PostTransportOrderRequestTO deserializationResult = jsonBinder.fromJson(
        json, PostTransportOrderRequestTO.class
    );
    assertThat(deserializationResult).isEqualTo(to);
  }

}
