// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO.LayoutTO.ConnectionTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO.PeripheralOperationTO.ExecutionTriggerTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;

/**
 * Tests for {@link PathTO}.
 */
class PathTOTest {
  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createPathMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createPathFull()));
  }

  private PathTO createPathMinimal() {
    return new PathTO()
        .setName("some-path")
        .setProperties(Map.of())
        .setSourcePoint("some-source-point")
        .setDestinationPoint("some-destination-point")
        .setLength(1)
        .setMaxVelocity(2)
        .setMaxReverseVelocity(3)
        .setPeripheralOperations(List.of())
        .setLocked(true)
        .setVehicleEnvelopes(Map.of())
        .setLayout(
            new PathTO.LayoutTO()
                .setConnectionType(ConnectionTypeTO.DIRECT)
                .setControlPoints(List.of())
                .setLayerId(4)
        );
  }

  private PathTO createPathFull() {
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
                    .setExecutionTrigger(ExecutionTriggerTO.AFTER_ALLOCATION)
                    .setCompletionRequired(true)
            )
        )
        .setLocked(true)
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
                .setConnectionType(ConnectionTypeTO.DIRECT)
                .setControlPoints(
                    List.of(
                        new CoupleTO().setX(6).setY(7)
                    )
                )
                .setLayerId(8)
        );
  }
}
