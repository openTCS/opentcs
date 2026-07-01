// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetEnvironmentalEntityResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostEnvironmentalEntityRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * Provides conversion subroutines for environmental entities.
 */
public class EnvironmentalEntityConverter {

  /**
   * Creates a new instance.
   */
  public EnvironmentalEntityConverter() {
  }

  public GetEnvironmentalEntityResponseTO toGetEnvironmentalEntityResponse(
      EnvironmentalEntity entity
  ) {
    if (entity == null) {
      return null;
    }

    GetEnvironmentalEntityResponseTO response = new GetEnvironmentalEntityResponseTO();
    response.setName(entity.getName());
    response.setProperties(Map.copyOf(entity.getProperties()));
    response.setHistory(convertObjectHistory(entity.getHistory()));
    response.setEnvelope(convertEnvelope(entity.getEnvelope()));
    response.setPose(convertPose(entity.getPose()));
    response.setType(convertType(entity.getType()));
    response.setIntegrationLevel(convertIntegrationLevel(entity.getIntegrationLevel()));
    response.setLayout(
        new GetEnvironmentalEntityResponseTO.LayoutTO()
            .setLayerId(entity.getLayout().getLayerId())
    );
    response.setRetired(entity.isRetired());
    response.setCreatedTime(entity.getCreatedTime());
    response.setRetiredTime(entity.getRetiredTime());

    return response;
  }

  public EnvironmentalEntityCreationTO toEnvironmentalEntityCreationTO(
      String name,
      PostEnvironmentalEntityRequestTO entity
  ) {
    requireNonNull(name, "name");
    requireNonNull(entity, "entity");

    return new EnvironmentalEntityCreationTO(
        name,
        new EnvelopeCreationTO(
            entity.getEnvelope().getVertices().stream()
                .map(vertex -> new CoupleCreationTO(vertex.getX(), vertex.getY()))
                .toList()
        ),
        new PoseCreationTO(
            new TripleCreationTO(
                entity.getPose().getPosition().getX(),
                entity.getPose().getPosition().getY(),
                entity.getPose().getPosition().getZ()
            ),
            entity.getPose().getOrientationAngle()
        )
    )
        .withIncompleteName(entity.isIncompleteName())
        .withType(toCreationType(entity.getType()))
        .withIntegrationLevel(toCreationIntegrationLevel(entity.getIntegrationLevel()))
        .withLayout(toCreationLayout(entity.getLayout()))
        .withProperties(entity.getProperties());
  }

  private ObjectHistoryTO convertObjectHistory(ObjectHistory history) {
    return new ObjectHistoryTO(
        history.getEntries()
            .stream()
            .map(
                entry -> new ObjectHistoryTO.ObjectHistoryEntryTO(
                    entry.getTimestamp(),
                    entry.getEventCode(),
                    entry.getSupplements()
                )
            ).toList()
    );
  }

  private GetEnvironmentalEntityResponseTO.EnvelopeTO convertEnvelope(Envelope envelope) {
    return new GetEnvironmentalEntityResponseTO.EnvelopeTO()
        .setVertices(
            envelope.getVertices().stream()
                .map(vertex -> new CoupleTO(vertex.getX(), vertex.getY()))
                .toList()
        );
  }

  private GetEnvironmentalEntityResponseTO.PoseTO convertPose(Pose pose) {
    return new GetEnvironmentalEntityResponseTO.PoseTO()
        .setPosition(
            new TripleTO(
                pose.getPosition().getX(),
                pose.getPosition().getY(),
                pose.getPosition().getZ()
            )
        )
        .setOrientationAngle(pose.getOrientationAngle());
  }

  private GetEnvironmentalEntityResponseTO.Type convertType(EnvironmentalEntity.Type type) {
    return switch (type) {
      case OBJECT -> GetEnvironmentalEntityResponseTO.Type.OBJECT;
      case ZONE -> GetEnvironmentalEntityResponseTO.Type.ZONE;
    };
  }

  private GetEnvironmentalEntityResponseTO.IntegrationLevel convertIntegrationLevel(
      EnvironmentalEntity.IntegrationLevel level
  ) {
    return switch (level) {
      case TO_BE_IGNORED -> GetEnvironmentalEntityResponseTO.IntegrationLevel.TO_BE_IGNORED;
      case TO_BE_NOTICED -> GetEnvironmentalEntityResponseTO.IntegrationLevel.TO_BE_NOTICED;
      case TO_BE_RESPECTED -> GetEnvironmentalEntityResponseTO.IntegrationLevel.TO_BE_RESPECTED;
    };
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
      case TO_BE_IGNORED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_IGNORED;
      case TO_BE_NOTICED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_NOTICED;
      case TO_BE_RESPECTED -> EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_RESPECTED;
    };
  }

  private EnvironmentalEntityCreationTO.Layout toCreationLayout(
      PostEnvironmentalEntityRequestTO.LayoutTO layout
  ) {
    return new EnvironmentalEntityCreationTO.Layout(layout.getLayerId());
  }
}
