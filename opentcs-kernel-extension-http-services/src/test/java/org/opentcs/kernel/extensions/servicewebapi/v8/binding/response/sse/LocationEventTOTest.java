// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * Tests for {@link LocationEventTO}.
 */
class LocationEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    LocationEventTO to = new LocationEventTO(
        createLocation(false),
        createLocation(true)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private LocationTO createLocation(boolean locked) {
    return new LocationTO()
        .setName("some-location")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setType("some-type")
        .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
        .setAttachedLinks(
            List.of(
                new LinkTO()
                    .setLocation("some-location")
                    .setPoint("some-point")
                    .setAllowedOperations(List.of("some-operation"))
            )
        )
        .setLocked(locked)
        .setPeripheralInformation(
            new LocationTO.PeripheralInformationTO()
                .setReservationToken("some-reservation-token")
                .setState(LocationTO.PeripheralInformationTO.StateTO.EXECUTING)
                .setProcState(LocationTO.PeripheralInformationTO.ProcStateTO.PROCESSING_JOB)
                .setPeripheralJob("some-peripheral-job")
        )
        .setLayout(
            new LocationTO.LayoutTO()
                .setLabelOffset(new CoupleTO().setX(4).setY(5))
                .setLocationRepresentation(LocationRepresentationTO.DEFAULT)
                .setLayerId(6)
        );
  }
}
