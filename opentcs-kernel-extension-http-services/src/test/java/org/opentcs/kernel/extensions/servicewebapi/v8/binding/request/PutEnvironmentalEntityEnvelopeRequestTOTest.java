// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;

/**
 * Tests for {@link PutEnvironmentalEntityEnvelopeRequestTO}.
 */
class PutEnvironmentalEntityEnvelopeRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutEnvironmentalEntityEnvelopeRequestTO to = new PutEnvironmentalEntityEnvelopeRequestTO(
        List.of(
            new CoupleTO(1, 2),
            new CoupleTO(3, 4),
            new CoupleTO(5, 6)
        )
    );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
