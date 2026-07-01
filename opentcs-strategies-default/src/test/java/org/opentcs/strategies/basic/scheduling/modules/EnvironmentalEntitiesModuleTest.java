// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.util.CustomGeometryFactory;

/**
 * Unit tests for {@link EnvironmentalEntitiesModule}.
 */
class EnvironmentalEntitiesModuleTest {

  private InternalTCSObjectService objectService;
  private EnvironmentalEntitiesModule module;
  private Scheduler.Client client;

  @BeforeEach
  void setUp() {
    objectService = mock(InternalTCSObjectService.class);
    module = new EnvironmentalEntitiesModule(
        objectService, new CustomGeometryFactory(), new Object()
    );
    client = new SampleClient();
  }

  @Test
  void shouldAllowAllocationWhenNoEnvironmentalEntityIsPresent() {
    when(objectService.stream(eq(EnvironmentalEntity.class)))
        .thenAnswer(invocation -> Stream.empty());

    assertTrue(module.mayAllocate(client, Set.of(createPoint("point-1", 0, 0))));
  }

  @Test
  void shouldAllowAllocationWhenEntitiesAreNotToBeRespected() {
    EnvironmentalEntity entity = createEntity(
        "entity-1",
        envelopeFrom(0, 0, 10, 10),
        EnvironmentalEntity.IntegrationLevel.TO_BE_NOTICED
    );

    when(objectService.stream(eq(EnvironmentalEntity.class)))
        .thenAnswer(invocation -> Stream.of(entity));

    assertTrue(module.mayAllocate(client, Set.of(createPoint("point-1", 5, 5))));
  }

  @Test
  void shouldAllowAllocationWhenEntitiesAreRetired() {
    EnvironmentalEntity entity = createEntity(
        "entity-1",
        envelopeFrom(0, 0, 10, 10),
        EnvironmentalEntity.IntegrationLevel.TO_BE_RESPECTED
    )
        .withRetired(true)
        .withRetiredTime(Instant.now());

    when(objectService.stream(eq(EnvironmentalEntity.class)))
        .thenAnswer(invocation -> Stream.of(entity));

    assertTrue(module.mayAllocate(client, Set.of(createPoint("point-1", 5, 5))));
  }

  @Test
  void shouldDenyAllocationWhenPointIsInsideRespectedEntityEnvelope() {
    EnvironmentalEntity entity = createEntity(
        "entity-1",
        envelopeFrom(0, 0, 10, 10),
        EnvironmentalEntity.IntegrationLevel.TO_BE_RESPECTED
    );

    when(objectService.stream(eq(EnvironmentalEntity.class)))
        .thenAnswer(invocation -> Stream.of(entity));

    // Somewhere inside the area.
    assertFalse(module.mayAllocate(client, Set.of(createPoint("point-1", 5, 5))));
    // On an edge.
    assertFalse(module.mayAllocate(client, Set.of(createPoint("point-1", 5, 10))));
    // At a corner.
    assertFalse(module.mayAllocate(client, Set.of(createPoint("point-1", 0, 0))));
  }

  @Test
  void shouldAllowAllocationWhenPointIsOutsideRespectedEntityEnvelope() {
    EnvironmentalEntity entity = createEntity(
        "entity-1",
        envelopeFrom(0, 0, 10, 10),
        EnvironmentalEntity.IntegrationLevel.TO_BE_RESPECTED
    );

    when(objectService.stream(eq(EnvironmentalEntity.class)))
        .thenAnswer(invocation -> Stream.of(entity));

    // Away from the entity.
    assertTrue(module.mayAllocate(client, Set.of(createPoint("point-1", 20, 20))));
    // Right next to the entity.
    assertTrue(module.mayAllocate(client, Set.of(createPoint("point-1", 5, 11))));
  }

  private Point createPoint(String name, int x, int y) {
    return new Point(name).withPose(new Pose(new Triple(x, y, 0), 0));
  }

  private EnvironmentalEntity createEntity(
      String name,
      Envelope envelope,
      EnvironmentalEntity.IntegrationLevel integrationLevel
  ) {
    return new EnvironmentalEntity(name, envelope, new Pose(new Triple(0, 0, 0), 0))
        .withIntegrationLevel(integrationLevel);
  }

  private Envelope envelopeFrom(int minX, int minY, int maxX, int maxY) {
    return new Envelope(
        List.of(
            new Couple(minX, minY),
            new Couple(maxX, minY),
            new Couple(maxX, maxY),
            new Couple(minX, maxY),
            new Couple(minX, minY)
        )
    );
  }

  private static class SampleClient
      implements
        Scheduler.Client {

    @Override
    public String getId() {
      return "SampleClient";
    }

    @Override
    public TCSObjectReference<Vehicle> getRelatedVehicle() {
      return null;
    }

    @Override
    public boolean onAllocation(
        @Nonnull
        Set<TCSResource<?>> resources
    ) {
      return true;
    }
  }
}
