// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

/**
 * Tests for {@link EnvironmentalEntityEventTO}.
 */
class EnvironmentalEntityEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    EnvironmentalEntityEventTO to = new EnvironmentalEntityEventTO(
        createEntity(),
        createEntity()
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private EnvironmentalEntityEventTO.EnvironmentalEntityTO createEntity() {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new EnvironmentalEntityEventTO.EnvironmentalEntityTO()
        .setName("entity-1")
        .setProperties(new TreeMap<>(Map.of("key-1", "value-1", "key-2", "value-2")))
        .setHistory(
            new ObjectHistoryTO().setEntries(
                List.of(
                    new ObjectHistoryTO.ObjectHistoryEntryTO()
                        .setEventCode("event-code-1")
                        .setTimestamp(time)
                        .setSupplements(List.of("supplement-1", "supplement-2"))
                )
            )
        )
        .setEnvelope(
            new EnvironmentalEntityEventTO.EnvelopeTO()
                .setVertices(
                    List.of(
                        new EnvironmentalEntityEventTO.CoupleTO().setX(1).setY(2),
                        new EnvironmentalEntityEventTO.CoupleTO().setX(3).setY(4),
                        new EnvironmentalEntityEventTO.CoupleTO().setX(5).setY(6),
                        new EnvironmentalEntityEventTO.CoupleTO().setX(1).setY(2)
                    )
                )
        )
        .setPose(
            new EnvironmentalEntityEventTO.PoseTO()
                .setPosition(new EnvironmentalEntityEventTO.TripleTO().setX(1).setY(2).setZ(0))
                .setOrientationAngle(123.45)
        )
        .setType(EnvironmentalEntityEventTO.TypeTO.OBJECT)
        .setIntegrationLevel(EnvironmentalEntityEventTO.IntegrationLevelTO.TO_BE_NOTICED)
        .setLayout(new EnvironmentalEntityEventTO.LayoutTO().setLayerId(23))
        .setCreatedTime(time)
        .setRetiredTime(time.plus(1, ChronoUnit.HOURS));
  }
}
