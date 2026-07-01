// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * Unit tests for {@link PostEnvironmentalEntityRequestTO}.
 */
class PostEnvironmentalEntityRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PostEnvironmentalEntityRequestTO to = new PostEnvironmentalEntityRequestTO(
        new PostEnvironmentalEntityRequestTO.EnvelopeTO()
            .setVertices(
                List.of(
                    new CoupleTO(1, 2),
                    new CoupleTO(3, 4),
                    new CoupleTO(5, 6),
                    new CoupleTO(7, 8)
                )
            ),
        new PostEnvironmentalEntityRequestTO.PoseTO()
            .setPosition(new TripleTO(1, 2, 3))
            .setOrientationAngle(123.456)
    )
        .setIncompleteName(true)
        .setType(PostEnvironmentalEntityRequestTO.Type.OBJECT)
        .setIntegrationLevel(PostEnvironmentalEntityRequestTO.IntegrationLevel.TO_BE_RESPECTED)
        .setLayout(new PostEnvironmentalEntityRequestTO.LayoutTO().setLayerId(23))
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
