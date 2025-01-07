// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;

/**
 * Tests for {@link PutVehicleAcceptableOrderTypesTO}.
 */
class PutVehicleAcceptableOrderTypesTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutVehicleAcceptableOrderTypesTO to
        = new PutVehicleAcceptableOrderTypesTO(
            List.of(
                new AcceptableOrderTypeTO("some-orderType", 0),
                new AcceptableOrderTypeTO("another-orderType", 1),
                new AcceptableOrderTypeTO("orderType-3", 2)
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }
}
