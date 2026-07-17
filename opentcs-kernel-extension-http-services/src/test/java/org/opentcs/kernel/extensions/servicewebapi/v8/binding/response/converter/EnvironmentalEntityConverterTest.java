// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.EnvironmentalEntityTO;

/**
 * Tests for {@link EnvironmentalEntityConverter}.
 */
class EnvironmentalEntityConverterTest {

  private JsonBinder jsonBinder;
  private EnvironmentalEntityConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new EnvironmentalEntityConverter();
  }

  @Test
  void convert() {
    EnvironmentalEntity entity = new EnvironmentalEntity(
        "entity-1",
        new Envelope(
            List.of(
                new Couple(1, 2),
                new Couple(3, 4),
                new Couple(5, 6),
                new Couple(1, 2)
            )
        ),
        new Pose(new Triple(1, 2, 3), 123.45)
    )
        .withProperties(Map.of("key-1", "value-1", "key-2", "value-2"))
        .withHistoryEntry(
            new ObjectHistory.Entry(Instant.EPOCH, "event-code-1", List.of("supplement-1"))
        )
        .withType(EnvironmentalEntity.Type.OBJECT)
        .withIntegrationLevel(EnvironmentalEntity.IntegrationLevel.TO_BE_NOTICED)
        .withLayout(new EnvironmentalEntity.Layout().withLayerId(23))
        .withRetired(true)
        .withCreatedTime(Instant.EPOCH)
        .withRetiredTime(Instant.EPOCH);

    EnvironmentalEntityTO result = converter.convert(entity);

    Approvals.verify(jsonBinder.toJson(result));
  }

  @ParameterizedTest
  @EnumSource(value = EnvironmentalEntity.Type.class)
  void convertAllTypes(EnvironmentalEntity.Type type) {
    EnvironmentalEntity entity = new EnvironmentalEntity(
        "entity-1",
        new Envelope(
            List.of(
                new Couple(1, 2),
                new Couple(3, 4),
                new Couple(5, 6),
                new Couple(1, 2)
            )
        ),
        new Pose(new Triple(1, 2, 3), 123.45)
    )
        .withType(type);

    EnvironmentalEntityTO result = converter.convert(entity);

    EnvironmentalEntityTO.Type expectedType = switch (type) {
      case OBJECT -> EnvironmentalEntityTO.Type.OBJECT;
      case ZONE -> EnvironmentalEntityTO.Type.ZONE;
    };
    assertThat(result.getType()).isEqualTo(expectedType);
  }

  @ParameterizedTest
  @EnumSource(EnvironmentalEntity.IntegrationLevel.class)
  void convertAllIntegrationLevels(EnvironmentalEntity.IntegrationLevel integrationLevel) {
    EnvironmentalEntity entity = new EnvironmentalEntity(
        "entity-1",
        new Envelope(
            List.of(
                new Couple(1, 2),
                new Couple(3, 4),
                new Couple(5, 6),
                new Couple(1, 2)
            )
        ),
        new Pose(new Triple(1, 2, 3), 123.45)
    )
        .withIntegrationLevel(integrationLevel);

    EnvironmentalEntityTO result = converter.convert(entity);

    EnvironmentalEntityTO.IntegrationLevel expectedIntegrationLevel = switch (integrationLevel) {
      case TO_BE_IGNORED -> EnvironmentalEntityTO.IntegrationLevel.TO_BE_IGNORED;
      case TO_BE_NOTICED -> EnvironmentalEntityTO.IntegrationLevel.TO_BE_NOTICED;
      case TO_BE_RESPECTED -> EnvironmentalEntityTO.IntegrationLevel.TO_BE_RESPECTED;
    };
    assertThat(result.getIntegrationLevel()).isEqualTo(expectedIntegrationLevel);
  }
}
