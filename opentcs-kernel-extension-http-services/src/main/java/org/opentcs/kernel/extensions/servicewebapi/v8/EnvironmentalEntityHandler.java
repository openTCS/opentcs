// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostEnvironmentalEntityRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutEnvironmentalEntityEnvelopeRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PutEnvironmentalEntityPoseRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.EnvironmentalEntityConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.EnvironmentalEntityTO;

/**
 * Handles requests related to environmental entities.
 */
public class EnvironmentalEntityHandler {

  private final InternalTCSObjectService objectService;
  private final EnvironmentalEntityService environmentalEntityService;
  private final EnvironmentalEntityConverter environmentalEntityConverter;
  private final KernelExecutorWrapper executorWrapper;


  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve objects.
   * @param environmentalEntityService Used to create and modify environmental entities.
   * @param environmentalEntityConverter Used to convert environmental entity instances to their web
   * API representation.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public EnvironmentalEntityHandler(
      InternalTCSObjectService objectService,
      EnvironmentalEntityService environmentalEntityService,
      EnvironmentalEntityConverter environmentalEntityConverter,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.environmentalEntityService = requireNonNull(
        environmentalEntityService,
        "environmentalEntityService"
    );
    this.environmentalEntityConverter
        = requireNonNull(environmentalEntityConverter, "environmentalEntityConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public EnvironmentalEntity createEnvironmentalEntity(
      String name,
      PostEnvironmentalEntityRequestTO request
  )
      throws ObjectExistsException {
    requireNonNull(name, "name");
    requireNonNull(request, "request");

    EnvironmentalEntityCreationTO to = new EnvironmentalEntityCreationTO(
        name,
        toEnvelopeCreationTO(request.getEnvelope()),
        toPoseCreationTO(request.getPose())
    )
        .withIncompleteName(request.isIncompleteName())
        .withType(toCreationType(request.getType()))
        .withIntegrationLevel(toCreationIntegrationLevel(request.getIntegrationLevel()))
        .withProperties(request.getProperties() == null ? Map.of() : request.getProperties());

    return executorWrapper.callAndWait(() -> {
      return environmentalEntityService.createEnvironmentalEntity(to);
    });
  }

  /**
   * Find all environmental entities.
   *
   * @return A list of environmental entities.
   */
  public List<EnvironmentalEntityTO> getEnvironmentalEntitiesState() {
    return executorWrapper.callAndWait(
        () -> objectService.stream(EnvironmentalEntity.class)
            .map(environmentalEntityConverter::convert)
            .sorted(Comparator.comparing(EnvironmentalEntityTO::getName))
            .toList()
    );
  }

  /**
   * Find the environmental entity with the given name.
   *
   * @param name The name of the requested entity.
   * @return A single entity that has the given name.
   * @throws ObjectUnknownException If an entity with the given name does not exist.
   */
  public EnvironmentalEntityTO getEnvironmentalEntityStateByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return objectService.fetch(EnvironmentalEntity.class, name)
          .map(environmentalEntityConverter::convert)
          .orElseThrow(() -> new ObjectUnknownException("Unknown environmental entity: " + name));
    });
  }

  public void putEnvironmentalEntityEnvelope(
      String name,
      PutEnvironmentalEntityEnvelopeRequestTO request
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(request, "request");

    executorWrapper.callAndWait(() -> {
      environmentalEntityService.updateEnvironmentalEntityEnvelope(
          objectService.fetch(EnvironmentalEntity.class, name)
              .orElseThrow(
                  () -> new ObjectUnknownException("Unknown environmental entity: " + name)
              ).getReference(),
          toEnvelope(request)
      );
    });
  }

  public void putEnvironmentalEntityPose(
      String name,
      PutEnvironmentalEntityPoseRequestTO request
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(request, "request");

    executorWrapper.callAndWait(() -> {
      environmentalEntityService.updateEnvironmentalEntityPose(
          objectService.fetch(EnvironmentalEntity.class, name)
              .orElseThrow(
                  () -> new ObjectUnknownException("Unknown environmental entity: " + name)
              ).getReference(),
          new Pose(
              new Triple(
                  request.getPosition().getX(),
                  request.getPosition().getY(),
                  request.getPosition().getZ()
              ),
              request.getOrientationAngle()
          )
      );
    });
  }

  public void putEnvironmentalEntityIntegrationLevel(String name, String value)
      throws ObjectUnknownException,
        IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      environmentalEntityService.updateEnvironmentalEntityIntegrationLevel(
          objectService.fetch(EnvironmentalEntity.class, name)
              .orElseThrow(
                  () -> new ObjectUnknownException("Unknown environmental entity: " + name)
              ).getReference(),
          EnvironmentalEntity.IntegrationLevel.valueOf(value)
      );
    });
  }

  public void putEnvironmentalEntityRetired(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      environmentalEntityService.markEnvironmentalEntityRetired(
          objectService.fetch(EnvironmentalEntity.class, name)
              .orElseThrow(
                  () -> new ObjectUnknownException("Unknown environmental entity: " + name)
              ).getReference()
      );
    });
  }

  private Envelope toEnvelope(PutEnvironmentalEntityEnvelopeRequestTO request) {
    return new Envelope(
        request.getVertices().stream()
            .map(couple -> new Couple(couple.getX(), couple.getY()))
            .toList()
    );
  }

  private EnvelopeCreationTO toEnvelopeCreationTO(
      PostEnvironmentalEntityRequestTO.EnvelopeTO envelope
  ) {
    return new EnvelopeCreationTO(
        envelope.getVertices().stream()
            .map(couple -> new CoupleCreationTO(couple.getX(), couple.getY()))
            .toList()
    );
  }

  private PoseCreationTO toPoseCreationTO(PostEnvironmentalEntityRequestTO.PoseTO pose) {
    return new PoseCreationTO(
        new TripleCreationTO(
            pose.getPosition().getX(),
            pose.getPosition().getY(),
            pose.getPosition().getZ()
        ),
        pose.getOrientationAngle()
    );
  }

  private EnvironmentalEntityCreationTO.Type toCreationType(
      PostEnvironmentalEntityRequestTO.Type type
  ) {
    return switch (type) {
      case OBJECT -> EnvironmentalEntityCreationTO.Type.OBJECT;
      case ZONE -> EnvironmentalEntityCreationTO.Type.ZONE;
    };
  }

  private EnvironmentalEntityCreationTO.IntegrationLevel toCreationIntegrationLevel(
      PostEnvironmentalEntityRequestTO.IntegrationLevel integrationLevel
  ) {
    return switch (integrationLevel) {
      case TO_BE_RESPECTED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_RESPECTED;
      case TO_BE_NOTICED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_NOTICED;
      case TO_BE_IGNORED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_IGNORED;
    };
  }
}
