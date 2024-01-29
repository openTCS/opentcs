/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;

/**
 */
class PutVehicleAllowedOrderTypesTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutVehicleAllowedOrderTypesTO to
        = new PutVehicleAllowedOrderTypesTO(List.of("some-orderType",
                                                   "another-orderType",
                                                   "orderType-3"));

    Approvals.verify(jsonBinder.toJson(to));
  }
}
