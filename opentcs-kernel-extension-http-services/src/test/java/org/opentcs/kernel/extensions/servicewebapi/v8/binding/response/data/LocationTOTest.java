// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * Tests for {@link LocationTO}.
 */
class LocationTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createLocationMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createLocationFull()));
  }

  private LocationTO createLocationMinimal() {
    return new LocationTO()
        .setName("some-location")
        .setProperties(Map.of())
        .setType("some-type")
        .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
        .setAttachedLinks(List.of())
        .setLocked(true)
        .setPeripheralInformation(
            new LocationTO.PeripheralInformationTO()
                .setReservationToken(null)
                .setState(LocationTO.PeripheralInformationTO.StateTO.NO_PERIPHERAL)
                .setProcState(LocationTO.PeripheralInformationTO.ProcStateTO.IDLE)
                .setPeripheralJob(null)
        )
        .setLayout(
            new LocationTO.LayoutTO()
                .setLabelOffset(new CoupleTO().setX(4).setY(5))
                .setLocationRepresentation(LocationRepresentationTO.DEFAULT)
                .setLayerId(6)
        );
  }

  private LocationTO createLocationFull() {
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
        .setLocked(true)
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
