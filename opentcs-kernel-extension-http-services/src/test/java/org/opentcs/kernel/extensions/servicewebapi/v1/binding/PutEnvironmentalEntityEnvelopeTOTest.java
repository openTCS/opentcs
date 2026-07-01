// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;

/**
 * Tests for {@link PutEnvironmentalEntityEnvelopeTO}.
 */
class PutEnvironmentalEntityEnvelopeTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutEnvironmentalEntityEnvelopeTO to = new PutEnvironmentalEntityEnvelopeTO(
        List.of(
            new CoupleTO(1, 2),
            new CoupleTO(3, 4),
            new CoupleTO(5, 6)
        )
    );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
