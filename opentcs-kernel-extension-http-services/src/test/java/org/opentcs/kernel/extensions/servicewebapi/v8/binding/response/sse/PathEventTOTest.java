// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;

/**
 * Tests for {@link PathEventTO}.
 */
class PathEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PathEventTO to = new PathEventTO(
        createPath(false),
        createPath(true)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private PathTO createPath(boolean locked) {
    return new PathTO()
        .setName("some-path")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setSourcePoint("some-source-point")
        .setDestinationPoint("some-destination-point")
        .setLength(1)
        .setMaxVelocity(2)
        .setMaxReverseVelocity(3)
        .setPeripheralOperations(
            List.of(
                new PathTO.PeripheralOperationTO()
                    .setLocation("some-location")
                    .setOperation("some-operation")
                    .setExecutionTrigger(
                        PathTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_ALLOCATION
                    )
                    .setCompletionRequired(true)
            )
        )
        .setLocked(locked)
        .setVehicleEnvelopes(
            Map.of(
                "some-envelope",
                new EnvelopeTO().setVertices(
                    List.of(
                        new CoupleTO().setX(4).setY(5)
                    )
                )
            )
        )
        .setLayout(
            new PathTO.LayoutTO()
                .setConnectionType(PathTO.LayoutTO.ConnectionTypeTO.DIRECT)
                .setControlPoints(
                    List.of(
                        new CoupleTO().setX(6).setY(7)
                    )
                )
                .setLayerId(8)
        );
  }
}
