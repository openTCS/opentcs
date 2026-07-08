// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

/**
 */
class PostOrderSequenceRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PostOrderSequenceRequestTO to = new PostOrderSequenceRequestTO()
        .setIncompleteName(true)
        .setOrderTypes(List.of("some-type"))
        .setIntendedVehicle("some-vehicle")
        .setFailureFatal(true)
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "another-key", "another-value"
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
