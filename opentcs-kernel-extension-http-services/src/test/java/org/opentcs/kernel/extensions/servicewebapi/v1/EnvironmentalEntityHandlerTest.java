// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetEnvironmentalEntityResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostEnvironmentalEntityRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutEnvironmentalEntityEnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutEnvironmentalEntityPoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.EnvironmentalEntityConverter;

/**
 * Unit tests for {@link EnvironmentalEntityHandler}.
 */
class EnvironmentalEntityHandlerTest {

  private InternalPlantModelService plantModelService;
  private EnvironmentalEntityService environmentalEntityService;
  private KernelExecutorWrapper executorWrapper;
  private EnvironmentalEntityHandler handler;

  @BeforeEach
  void setUp() {
    plantModelService = mock();
    environmentalEntityService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new EnvironmentalEntityHandler(
        plantModelService,
        environmentalEntityService,
        executorWrapper,
        new EnvironmentalEntityConverter()
    );
  }

  @Test
  void retrieveAllEnvironmentalEntitiesSortedByName() {
    // Arrange
    EnvironmentalEntity entityB = createEntity("entity-b");
    EnvironmentalEntity entityA = createEntity("entity-a");
    given(plantModelService.stream(EnvironmentalEntity.class))
        .willReturn(Stream.of(entityB, entityA));

    // Act
    List<GetEnvironmentalEntityResponseTO> result = handler.getAllEnvironmentalEntities();

    // Assert
    assertThat(result)
        .extracting(GetEnvironmentalEntityResponseTO::getName)
        .containsExactly("entity-a", "entity-b");
    then(plantModelService).should().stream(EnvironmentalEntity.class);
  }

  @Test
  void retrieveEnvironmentalEntityByName() {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity");
    given(plantModelService.fetch(EnvironmentalEntity.class, "some-entity"))
        .willReturn(Optional.of(entity));

    // Act & Assert: happy path
    GetEnvironmentalEntityResponseTO result = handler.getEnvironmentalEntityByName("some-entity");
    assertThat(result)
        .returns("some-entity", from(GetEnvironmentalEntityResponseTO::getName));
    then(plantModelService).should().fetch(EnvironmentalEntity.class, "some-entity");

    // Act & Assert: unknown environmental entity
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getEnvironmentalEntityByName("some-other-entity"));
  }

  @Test
  void createEnvironmentalEntity() {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity")
        .withType(EnvironmentalEntity.Type.ZONE)
        .withIntegrationLevel(EnvironmentalEntity.IntegrationLevel.TO_BE_NOTICED)
        .withLayout(new EnvironmentalEntity.Layout().withLayerId(42))
        .withProperties(Map.of("some-key", "some-value"))
        .withCreatedTime(Instant.now());
    given(
        environmentalEntityService.createEnvironmentalEntity(
            org.mockito.ArgumentMatchers.any(EnvironmentalEntityCreationTO.class)
        )
    )
        .willReturn(entity);

    PostEnvironmentalEntityRequestTO request = new PostEnvironmentalEntityRequestTO(
        new PostEnvironmentalEntityRequestTO.EnvelopeTO()
            .setVertices(testEnvelopeVerticesTO()),
        new PostEnvironmentalEntityRequestTO.PoseTO()
            .setPosition(new TripleTO(11, 22, 33))
            .setOrientationAngle(45.0)
    )
        .setIncompleteName(true)
        .setType(PostEnvironmentalEntityRequestTO.Type.ZONE)
        .setIntegrationLevel(PostEnvironmentalEntityRequestTO.IntegrationLevel.TO_BE_NOTICED)
        .setLayout(new PostEnvironmentalEntityRequestTO.LayoutTO().setLayerId(42))
        .setProperties(Map.of("some-key", "some-value"));

    // Act
    GetEnvironmentalEntityResponseTO result = handler.createEnvironmentalEntity(
        "some-entity", request
    );

    // Assert
    assertThat(result)
        .returns("some-entity", from(GetEnvironmentalEntityResponseTO::getName))
        .returns(
            GetEnvironmentalEntityResponseTO.Type.ZONE, from(
                GetEnvironmentalEntityResponseTO::getType
            )
        )
        .returns(
            GetEnvironmentalEntityResponseTO.IntegrationLevel.TO_BE_NOTICED,
            from(GetEnvironmentalEntityResponseTO::getIntegrationLevel)
        );

    ArgumentCaptor<EnvironmentalEntityCreationTO> captor
        = ArgumentCaptor.forClass(EnvironmentalEntityCreationTO.class);
    then(environmentalEntityService).should().createEnvironmentalEntity(captor.capture());
    assertThat(captor.getValue())
        .returns("some-entity", from(EnvironmentalEntityCreationTO::getName))
        .returns(true, from(EnvironmentalEntityCreationTO::hasIncompleteName))
        .returns(
            EnvironmentalEntityCreationTO.Type.ZONE, from(EnvironmentalEntityCreationTO::getType)
        )
        .returns(
            EnvironmentalEntityCreationTO.IntegrationLevel.TO_BE_NOTICED,
            from(EnvironmentalEntityCreationTO::getIntegrationLevel)
        )
        .returns(42, from(to -> to.getLayout().getLayerId()))
        .returns(
            Map.of("some-key", "some-value"), from(EnvironmentalEntityCreationTO::getProperties)
        );
    assertThat(captor.getValue().getEnvelope().getVertices()).hasSize(4);
    assertThat(captor.getValue().getPose())
        .returns(45.0, from(PoseCreationTO::getOrientationAngle));
    assertThat(captor.getValue().getPose().getPosition())
        .returns(11L, from(TripleCreationTO::getX))
        .returns(22L, from(TripleCreationTO::getY))
        .returns(33L, from(TripleCreationTO::getZ));
  }

  @Test
  void updateEnvironmentalEntityEnvelope() {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity");
    given(plantModelService.fetch(EnvironmentalEntity.class, "some-entity"))
        .willReturn(Optional.of(entity));

    PutEnvironmentalEntityEnvelopeTO request
        = new PutEnvironmentalEntityEnvelopeTO(testEnvelopeVerticesTO());

    // Act & Assert: happy path
    handler.putEnvironmentalEntityEnvelope("some-entity", request);

    ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);
    then(environmentalEntityService)
        .should()
        .updateEnvironmentalEntityEnvelope(
            eq(entity.getReference()),
            captor.capture()
        );
    assertThat(captor.getValue().getVertices()).containsExactlyElementsOf(testEnvelopeVertices());

    // Act & Assert: unknown environmental entity
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putEnvironmentalEntityEnvelope(
                "some-other-entity",
                new PutEnvironmentalEntityEnvelopeTO(testEnvelopeVerticesTO())
            )
        );
  }

  @Test
  void updateEnvironmentalEntityPose() {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity");
    given(plantModelService.fetch(EnvironmentalEntity.class, "some-entity"))
        .willReturn(Optional.of(entity));

    PutEnvironmentalEntityPoseTO request = new PutEnvironmentalEntityPoseTO(
        new TripleTO(111, 222, 333),
        90.0
    );

    // Act & Assert: happy path
    handler.putEnvironmentalEntityPose("some-entity", request);

    ArgumentCaptor<Pose> captor = ArgumentCaptor.forClass(Pose.class);
    then(environmentalEntityService)
        .should()
        .updateEnvironmentalEntityPose(
            eq(entity.getReference()),
            captor.capture()
        );
    assertThat(captor.getValue())
        .returns(90.0, from(Pose::getOrientationAngle));
    assertThat(captor.getValue().getPosition())
        .returns(111L, from(Triple::getX))
        .returns(222L, from(Triple::getY))
        .returns(333L, from(Triple::getZ));

    // Act & Assert: unknown environmental entity
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putEnvironmentalEntityPose(
                "some-other-entity",
                new PutEnvironmentalEntityPoseTO(new TripleTO(1, 2, 3), 0.0)
            )
        );
  }

  @ParameterizedTest
  @EnumSource(EnvironmentalEntity.IntegrationLevel.class)
  void updateEnvironmentalEntityIntegrationLevel(
      EnvironmentalEntity.IntegrationLevel integrationLevel
  ) {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity");
    given(plantModelService.fetch(EnvironmentalEntity.class, "some-entity"))
        .willReturn(Optional.of(entity));

    // Act
    handler.putEnvironmentalEntityIntegrationLevel("some-entity", integrationLevel.name());

    // Assert
    then(environmentalEntityService)
        .should()
        .updateEnvironmentalEntityIntegrationLevel(entity.getReference(), integrationLevel);
  }

  @Test
  void throwOnUpdateIntegrationLevelForUnknownEnvironmentalEntity() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putEnvironmentalEntityIntegrationLevel(
                "some-unknown-entity",
                EnvironmentalEntity.IntegrationLevel.TO_BE_RESPECTED.name()
            )
        );
  }

  @Test
  void retireEnvironmentalEntity() {
    // Arrange
    EnvironmentalEntity entity = createEntity("some-entity");
    given(plantModelService.fetch(EnvironmentalEntity.class, "some-entity"))
        .willReturn(Optional.of(entity));

    // Act & Assert: happy path
    handler.putEnvironmentalEntityRetired("some-entity");
    then(environmentalEntityService)
        .should()
        .markEnvironmentalEntityRetired(entity.getReference());

    // Act & Assert: unknown environmental entity
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putEnvironmentalEntityRetired("some-other-entity"));
  }

  private EnvironmentalEntity createEntity(String name) {
    return new EnvironmentalEntity(
        name,
        new Envelope(testEnvelopeVertices()),
        new Pose(new Triple(1, 2, 3), 0.0)
    );
  }

  private List<Couple> testEnvelopeVertices() {
    return List.of(
        new Couple(0, 0),
        new Couple(100, 0),
        new Couple(100, 100),
        new Couple(0, 0)
    );
  }

  private List<CoupleTO> testEnvelopeVerticesTO() {
    return List.of(
        new CoupleTO(0, 0),
        new CoupleTO(100, 0),
        new CoupleTO(100, 100),
        new CoupleTO(0, 0)
    );
  }
}
