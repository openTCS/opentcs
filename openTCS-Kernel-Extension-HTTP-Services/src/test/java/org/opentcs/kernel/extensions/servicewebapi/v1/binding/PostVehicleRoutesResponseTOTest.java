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
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getvehicleroutes.RouteTO;

/**
 * Unit tests for {@link PostVehicleRoutesResponseTO}.
 */
class PostVehicleRoutesResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    Approvals.verify(jsonBinder.toJson(new PostVehicleRoutesResponseTO()
        .setRoutes(
            List.of(
                new RouteTO()
                    .setDestinationPoint("C")
                    .setCosts(1376)
                    .setSteps(
                        List.of(
                            new RouteTO.Step()
                                .setSourcePoint("A")
                                .setDestinationPoint("B")
                                .setPath("A --- B")
                                .setVehicleOrientation("FORWARD"),
                            new RouteTO.Step()
                                .setSourcePoint("B")
                                .setDestinationPoint("C")
                                .setPath("B --- C")
                                .setVehicleOrientation("FORWARD")
                        )
                    ),
                new RouteTO()
                    .setDestinationPoint("E")
                    .setCosts(-1)
                    .setSteps(null),
                new RouteTO()
                    .setDestinationPoint("F")
                    .setCosts(4682)
                    .setSteps(
                        List.of(
                            new RouteTO.Step()
                                .setSourcePoint("D")
                                .setDestinationPoint("E")
                                .setPath("D --- E")
                                .setVehicleOrientation("BACKWARD"),
                            new RouteTO.Step()
                                .setSourcePoint("E")
                                .setDestinationPoint("F")
                                .setPath("E --- F")
                                .setVehicleOrientation("UNDEFINED")
                        )
                    )
            )
        )
    ));
  }
}
