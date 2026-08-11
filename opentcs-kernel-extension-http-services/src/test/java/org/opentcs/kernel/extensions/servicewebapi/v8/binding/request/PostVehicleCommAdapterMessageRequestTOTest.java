// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

/**
 * Tests for {@link PostVehicleCommAdapterMessageRequestTO}.
 */
class PostVehicleCommAdapterMessageRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    Approvals.verify(
        jsonBinder.toJson(
            new PostVehicleCommAdapterMessageRequestTO(
                "some-type",
                Map.of(
                    "key1", "value1",
                    "key2", "value2"
                )
            )
        )
    );
  }
}
