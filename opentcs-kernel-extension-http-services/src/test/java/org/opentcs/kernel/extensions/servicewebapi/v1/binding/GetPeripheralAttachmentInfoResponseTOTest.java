// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;

/**
 * Unit tests for {@link GetVehicleAttachmentInfoResponseTO}.
 */
class GetPeripheralAttachmentInfoResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    GetPeripheralAttachmentInfoResponseTO to
        = new GetPeripheralAttachmentInfoResponseTO(
            "Location001",
            "com.example.someperipheraldriver.descriptionclass",
            List.of(
                "com.example.someperipheraldriver.descriptionclass",
                "com.example.someotherperipheraldriver.descriptionclass2"
            )
        );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
