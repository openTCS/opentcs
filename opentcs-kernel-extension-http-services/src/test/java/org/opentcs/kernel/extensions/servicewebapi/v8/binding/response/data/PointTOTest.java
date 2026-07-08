// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * Tests for {@link PointTO}.
 */
class PointTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createPointMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createPointFull()));
  }

  private PointTO createPointMinimal() {
    return new PointTO()
        .setName("some-point")
        .setProperties(Map.of())
        .setPose(
            new PoseTO()
                .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
                .setOrientationAngle(null)
        )
        .setType(PointTO.TypeTO.HALT_POSITION)
        .setIncomingPaths(List.of())
        .setOutgoingPaths(List.of())
        .setAttachedLinks(List.of())
        .setOccupyingVehicle(null)
        .setVehicleEnvelopes(Map.of())
        .setMaxVehicleBoundingBox(
            new BoundingBoxTO()
                .setLength(1)
                .setWidth(2)
                .setHeight(3)
                .setReferenceOffset(new CoupleTO().setX(4).setY(5))
        )
        .setLayout(
            new PointTO.LayoutTO()
                .setLabelOffset(new CoupleTO().setX(6).setY(7))
                .setLayerId(8)
        );
  }

  private PointTO createPointFull() {
    return new PointTO()
        .setName("some-point")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setPose(
            new PoseTO()
                .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
                .setOrientationAngle(4.0)
        )
        .setType(PointTO.TypeTO.HALT_POSITION)
        .setIncomingPaths(List.of("some-path"))
        .setOutgoingPaths(List.of("some-other-path"))
        .setAttachedLinks(
            List.of(
                new LinkTO()
                    .setLocation("some-location")
                    .setPoint("some-point")
                    .setAllowedOperations(List.of("some-operation"))
            )
        )
        .setOccupyingVehicle("some-vehicle")
        .setVehicleEnvelopes(
            Map.of(
                "some-envelope",
                new EnvelopeTO().setVertices(
                    List.of(
                        new CoupleTO().setX(5).setY(6)
                    )
                )
            )
        )
        .setMaxVehicleBoundingBox(
            new BoundingBoxTO()
                .setLength(7)
                .setWidth(8)
                .setHeight(9)
                .setReferenceOffset(new CoupleTO().setX(10).setY(11))
        )
        .setLayout(
            new PointTO.LayoutTO()
                .setLabelOffset(new CoupleTO().setX(12).setY(13))
                .setLayerId(14)
        );
  }
}
