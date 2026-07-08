// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

/**
 * Unit tests for {@link PostVehicleRouteComputationQueryRequestTO}.
 */
class PostVehicleRouteComputationQueryRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    Approvals.verify(
        jsonBinder.toJson(
            new PostVehicleRouteComputationQueryRequestTO(List.of("C", "F"))
                .setSourcePoint("A")
                .setResourcesToAvoid(List.of("A", "B"))
        )
    );
  }
}
