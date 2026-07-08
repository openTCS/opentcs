// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetEnvironmentalEntityResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostEnvironmentalEntityRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutEnvironmentalEntityEnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutEnvironmentalEntityPoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.EnvironmentalEntityConverter;

/**
 * Handles requests related to environmental entities.
 */
public class EnvironmentalEntityHandler {

  private final InternalPlantModelService plantModelService;
  private final EnvironmentalEntityService environmentalEntityService;
  private final KernelExecutorWrapper executorWrapper;
  private final EnvironmentalEntityConverter environmentalEntityConverter;

  @Inject
  public EnvironmentalEntityHandler(
      InternalPlantModelService plantModelService,
      EnvironmentalEntityService environmentalEntityService,
      KernelExecutorWrapper executorWrapper,
      EnvironmentalEntityConverter environmentalEntityConverter
  ) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.environmentalEntityService
        = requireNonNull(environmentalEntityService, "environmentalEntityService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.environmentalEntityConverter
        = requireNonNull(environmentalEntityConverter, "environmentalEntityConverter");
  }

  public List<GetEnvironmentalEntityResponseTO> getAllEnvironmentalEntities() {
    return plantModelService.stream(EnvironmentalEntity.class)
        .map(environmentalEntityConverter::toGetEnvironmentalEntityResponse)
        .sorted(Comparator.comparing(GetEnvironmentalEntityResponseTO::getName))
        .collect(Collectors.toList());
  }

  public GetEnvironmentalEntityResponseTO getEnvironmentalEntityByName(String name) {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return plantModelService.fetch(EnvironmentalEntity.class, name)
          .map(environmentalEntityConverter::toGetEnvironmentalEntityResponse)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));
    });
  }

  public GetEnvironmentalEntityResponseTO createEnvironmentalEntity(
      String name,
      PostEnvironmentalEntityRequestTO entity
  )
      throws ObjectExistsException,
        KernelRuntimeException,
        IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(entity, "entity");

    EnvironmentalEntityCreationTO to = environmentalEntityConverter.toEnvironmentalEntityCreationTO(
        name,
        entity
    );

    return executorWrapper.callAndWait(() -> {
      return environmentalEntityConverter.toGetEnvironmentalEntityResponse(
          environmentalEntityService.createEnvironmentalEntity(to)
      );
    });
  }

  public void putEnvironmentalEntityEnvelope(
      String name, PutEnvironmentalEntityEnvelopeTO envelope
  ) {
    requireNonNull(name, "name");
    requireNonNull(envelope, "envelope");

    executorWrapper.callAndWait(() -> {
      EnvironmentalEntity entity = plantModelService.fetch(EnvironmentalEntity.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));

      environmentalEntityService.updateEnvironmentalEntityEnvelope(
          entity.getReference(),
          new Envelope(
              envelope.getVertices().stream()
                  .map(vertex -> new Couple(vertex.getX(), vertex.getY()))
                  .toList()
          )
      );
    });
  }

  public void putEnvironmentalEntityPose(String name, PutEnvironmentalEntityPoseTO pose) {
    requireNonNull(name, "name");
    requireNonNull(pose, "pose");

    executorWrapper.callAndWait(() -> {
      EnvironmentalEntity entity = plantModelService.fetch(EnvironmentalEntity.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));

      environmentalEntityService.updateEnvironmentalEntityPose(
          entity.getReference(),
          new Pose(
              new Triple(
                  pose.getPosition().getX(),
                  pose.getPosition().getY(),
                  pose.getPosition().getZ()
              ),
              pose.getOrientationAngle()
          )
      );
    });
  }

  public void putEnvironmentalEntityIntegrationLevel(String name, String value) {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      EnvironmentalEntity entity = plantModelService.fetch(EnvironmentalEntity.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));

      environmentalEntityService.updateEnvironmentalEntityIntegrationLevel(
          entity.getReference(),
          EnvironmentalEntity.IntegrationLevel.valueOf(value)
      );
    });
  }

  public void putEnvironmentalEntityRetired(String name) {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      EnvironmentalEntity entity = plantModelService.fetch(EnvironmentalEntity.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));

      environmentalEntityService.markEnvironmentalEntityRetired(entity.getReference());
    });
  }
}
