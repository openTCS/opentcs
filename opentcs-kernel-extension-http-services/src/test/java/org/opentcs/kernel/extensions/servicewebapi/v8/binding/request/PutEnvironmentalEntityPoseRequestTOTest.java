// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.TripleTO;

/**
 * Tests for {@link PutEnvironmentalEntityPoseRequestTO}.
 */
class PutEnvironmentalEntityPoseRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutEnvironmentalEntityPoseRequestTO to = new PutEnvironmentalEntityPoseRequestTO(
        new TripleTO(1, 2, 3),
        135.123
    );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
