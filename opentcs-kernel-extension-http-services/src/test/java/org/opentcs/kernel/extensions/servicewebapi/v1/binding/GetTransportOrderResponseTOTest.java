// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Unit tests for {@link GetTransportOrderResponseTO}.
 */
class GetTransportOrderResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    GetTransportOrderResponseTO to
        = new GetTransportOrderResponseTO()
            .setDispensable(true)
            .setName("some-order")
            .setPeripheralReservationToken("some-token")
            .setWrappingSequence("some-sequence")
            .setType("some-type")
            .setState(GetTransportOrderResponseTO.State.BEING_PROCESSED)
            .setIntendedVehicle("some-vehicle")
            .setProcessingVehicle("some-vehicle")
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
                    new Property("another-key", "another-value")
                )
            );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
