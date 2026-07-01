// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * Tests for {@link PutEnvironmentalEntityPoseTO}.
 */
class PutEnvironmentalEntityPoseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutEnvironmentalEntityPoseTO to = new PutEnvironmentalEntityPoseTO(
        new TripleTO(1, 2, 3),
        135.123
    );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
