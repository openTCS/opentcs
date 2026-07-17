// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * Tests for {@link EnvironmentalEntityTO}.
 */
public class EnvironmentalEntityTOTest {
  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    Approvals.verify(jsonBinder.toJson(createEntity()));
  }

  private EnvironmentalEntityTO createEntity() {
    return new EnvironmentalEntityTO()
        .setName("entity-1")
        .setProperties(Map.of("some-key", "some-value"))
        .setHistory(
            new ObjectHistoryTO().setEntries(
                List.of(
                    new ObjectHistoryTO.ObjectHistoryEntryTO()
                        .setTimestamp(Instant.EPOCH)
                        .setEventCode("some-code")
                        .setSupplements(List.of("supplement-1", "supplement-2"))
                )
            )
        )
        .setEnvelope(
            new EnvelopeTO()
                .setVertices(
                    List.of(
                        new CoupleTO().setX(1).setY(2),
                        new CoupleTO().setX(3).setY(4),
                        new CoupleTO().setX(5).setY(6),
                        new CoupleTO().setX(1).setY(2)
                    )
                )
        )
        .setPose(
            new PoseTO()
                .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
                .setOrientationAngle(123.45)
        )
        .setType(EnvironmentalEntityTO.Type.OBJECT)
        .setIntegrationLevel(EnvironmentalEntityTO.IntegrationLevel.TO_BE_NOTICED)
        .setLayout(new EnvironmentalEntityTO.LayoutTO().setLayerId(23))
        .setRetired(false)
        .setCreatedTime(Instant.EPOCH)
        .setRetiredTime(Instant.EPOCH);
  }
}
