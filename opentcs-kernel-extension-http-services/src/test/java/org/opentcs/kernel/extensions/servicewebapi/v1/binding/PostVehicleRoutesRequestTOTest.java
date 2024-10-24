// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;

/**
 * Unit tests for {@link PostVehicleRoutesRequestTO}.
 */
class PostVehicleRoutesRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    Approvals.verify(
        jsonBinder.toJson(
            new PostVehicleRoutesRequestTO(List.of("C", "F"))
                .setSourcePoint("A")
                .setResourcesToAvoid(List.of("A", "B"))
        )
    );
  }
}
