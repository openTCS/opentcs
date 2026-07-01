// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.strategies.basic.util.CustomGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows allocations only if requested resources are not occupied/covered by environmental
 * entities.
 */
public class EnvironmentalEntitiesModule
    implements
      Scheduler.Module {

  private static final Logger LOG = LoggerFactory.getLogger(EnvironmentalEntitiesModule.class);
  private final InternalTCSObjectService objectService;
  private final CustomGeometryFactory geometryFactory;
  private final Object globalSyncObject;
  private boolean initialized;

  @Inject
  public EnvironmentalEntitiesModule(
      @Nonnull
      InternalTCSObjectService objectService,
      @Nonnull
      CustomGeometryFactory geometryFactory,
      @Nonnull
      @GlobalSyncObject
      Object globalSyncObject
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.geometryFactory = requireNonNull(geometryFactory, "geometryFactory");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void setAllocationState(
      @Nonnull
      Scheduler.Client client,
      @NonNull
      Set<TCSResource<?>> alloc,
      @NonNull
      List<Set<TCSResource<?>>> remainingClaim
  ) {

  }

  @Override
  public boolean mayAllocate(
      @Nonnull
      Scheduler.Client client,
      @NonNull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      List<EnvironmentalEntity> entitiesToConsider = objectService.stream(EnvironmentalEntity.class)
          .filter(envEntity -> !envEntity.isRetired())
          .filter(
              envEntity -> envEntity.getIntegrationLevel()
                  == EnvironmentalEntity.IntegrationLevel.TO_BE_RESPECTED
          )
          .toList();
      // In case we do not have any environmental entities, shortcut to allowing the request.
      if (entitiesToConsider.isEmpty()) {
        return true;
      }

      List<Point> requestedPoints = resources.stream()
          .filter(resource -> resource instanceof Point)
          .map(resource -> (Point) resource)
          .toList();
      // Check whether any point is inside the envelope of any env entity.
      for (Point point : requestedPoints) {
        for (EnvironmentalEntity envEntity : entitiesToConsider) {
          if (enclosesPoint(envEntity, point)) {
            LOG.debug(
                "{}: Point '{}' unavailable as it is covered by entity '{}'.",
                client.getId(),
                point.getName(),
                envEntity.getName()
            );
            return false;
          }
        }
      }
    }

    return true;
  }

  @Override
  public void prepareAllocation(
      @Nonnull
      Scheduler.Client client,
      @NonNull
      Set<TCSResource<?>> resources
  ) {

  }

  @Override
  public boolean hasPreparedAllocation(
      @Nonnull
      Scheduler.Client client,
      @NonNull
      Set<TCSResource<?>> resources
  ) {
    return true;
  }

  @Override
  public void allocationReleased(
      @Nonnull
      Scheduler.Client client,
      @NonNull
      Set<TCSResource<?>> resources
  ) {

  }

  private boolean enclosesPoint(EnvironmentalEntity envEntity, Point point) {
    return geometryFactory.createTransformationForPose(envEntity.getPose())
        .transform(geometryFactory.createPolygonOrEmptyGeometry(envEntity.getEnvelope()))
        .covers(geometryFactory.createPoint(point));
  }
}
