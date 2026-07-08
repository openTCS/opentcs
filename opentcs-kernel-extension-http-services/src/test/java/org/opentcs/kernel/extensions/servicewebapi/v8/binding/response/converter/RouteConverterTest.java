// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.RouteTO;

/**
 * Tests for {@link RouteConverter}.
 */
class RouteConverterTest {

  private JsonBinder jsonBinder;
  private RouteConverter routeConverter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    routeConverter = new RouteConverter();
  }

  @Test
  void convert() {
    Map<TCSObjectReference<Point>, Set<Route>> routeMap = Map.of(
        new Point("point-2").getReference(),
        Set.of(
            new Route(
                List.of(
                    new Route.Step(
                        createPath("point-1 --- point-2"),
                        new Point("point-1"),
                        new Point("point-2"),
                        Vehicle.Orientation.BACKWARD,
                        0,
                        1
                    )
                )
            )
        ),
        new Point("point-3").getReference(),
        Set.of(
            new Route(
                List.of(
                    new Route.Step(
                        null,
                        null,
                        new Point("point-3"),
                        Vehicle.Orientation.FORWARD,
                        0,
                        0
                    )
                )
            )
        )
    );

    List<RouteTO> result = routeConverter.convert(routeMap);

    Approvals.verify(jsonBinder.toJson(result));
  }

  private Path createPath(String name) {
    Point dummyPoint = new Point("dummy-point");
    return new Path(name, dummyPoint.getReference(), dummyPoint.getReference());
  }
}
