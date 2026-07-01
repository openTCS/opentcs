// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.documentation.developers_guide;

import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;

/**
 * Test for the developer documentation to demonstrate how environmental entities can be handled.
 * This test merely exists for the documentation to refer to a compiling example.
 */
class HandleEnvironmentalEntitiesTest {

  private EnvironmentalEntityService envEntityService;

  @BeforeEach
  void setUp() {
    envEntityService = mock(EnvironmentalEntityService.class);
  }

  @Test
  void createEnvironmentalEntity() {
    // Note: Keep these lines to a maximum of 80 characters for the documentation!

    // tag::documentation_createEnvironmentalEntity[]
    // Describes a square area.
    EnvelopeCreationTO envelopeTO = new EnvelopeCreationTO(
        List.of(
            new CoupleCreationTO(0, 0),
            new CoupleCreationTO(1000, 0),
            new CoupleCreationTO(1000, 1000),
            new CoupleCreationTO(0, 1000),
            new CoupleCreationTO(0, 0)
        )
    );
    // The area's reference point is at (1000, 1000), and it is rotated
    // by 45 degrees.
    PoseCreationTO poseTO = new PoseCreationTO(
        new TripleCreationTO(1000, 1000, 0),
        45.0
    );

    EnvironmentalEntityCreationTO entityTO = new EnvironmentalEntityCreationTO(
        "MyEnvironmentalEntity",
        envelopeTO,
        poseTO
    )
        .withIncompleteName(true)
        .withType(EnvironmentalEntityCreationTO.Type.ZONE);

    EnvironmentalEntity entity = envEntityService.createEnvironmentalEntity(entityTO);
    // end::documentation_createEnvironmentalEntity[]

  }

  @Test
  void updateEnvironmentalEntityIntegrationLevel() {
    // tag::documentation_updateEnvironmentalEntityIntegrationLevel[]
    EnvironmentalEntity entity = someEntity();
    envEntityService.updateEnvironmentalEntityIntegrationLevel(
        entity.getReference(),
        EnvironmentalEntity.IntegrationLevel.TO_BE_IGNORED
    );
    // end::documentation_updateEnvironmentalEntityIntegrationLevel[]
  }

  @Test
  void retireEnvironmentalEntity() {
    // tag::documentation_retireEnvironmentalEntity[]
    EnvironmentalEntity entity = someEntity();
    envEntityService.markEnvironmentalEntityRetired(entity.getReference());
    // end::documentation_retireEnvironmentalEntity[]
  }

  private EnvironmentalEntity someEntity() {
    return new EnvironmentalEntity(
        "someEntity",
        new Envelope(
            List.of(
                new Couple(0, 0),
                new Couple(1000, 0),
                new Couple(1000, 1000),
                new Couple(0, 1000),
                new Couple(0, 0)
            )
        ),
        new Pose(new Triple(1000, 1000, 0), 45.0)
    );
  }
}
