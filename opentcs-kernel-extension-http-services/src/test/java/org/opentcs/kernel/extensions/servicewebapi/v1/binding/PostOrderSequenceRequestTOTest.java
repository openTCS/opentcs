// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

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
        .setType("Transport")
        .setIntendedVehicle("some-vehicle")
        .setFailureFatal(true)
        .setProperties(
            List.of(
                new Property("some-key", "some-value"),
                new Property("another-key", "another-value")
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
