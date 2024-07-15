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
 * Unit tests for {@link GetVehicleAttachmentInfoResponseTO}.
 */
class GetVehicleAttachmentInfoResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    GetVehicleAttachmentInfoResponseTO to
        = new GetVehicleAttachmentInfoResponseTO()
            .setVehicleName("some-vehicle")
            .setAvailableCommAdapters(
                List.of(
                    "com.example.somedriver.descriptionclass",
                    "com.example.someotherdriver.descriptionclass"
                )
            )
            .setAttachedCommAdapter("com.example.somedriver.descriptionclass");

    Approvals.verify(jsonBinder.toJson(to));
  }

}
