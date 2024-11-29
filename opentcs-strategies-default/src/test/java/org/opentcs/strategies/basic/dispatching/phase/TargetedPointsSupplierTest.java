// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for {@link TargetedPointsSupplier}.
 */
public class TargetedPointsSupplierTest {

  private TargetedPointsSupplier targetedPointsSupplier;
  private TCSObjectService objectService;

  @BeforeEach
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    targetedPointsSupplier = new TargetedPointsSupplier(objectService);
  }

  @Test
  public void testGetTargetedPoints() {
    Point point1 = new Point("P1");
    Point point2 = new Point("P2");
    Point point3 = new Point("P3");
    Point point4 = new Point("P4");
    Point point5 = new Point("P5");
    Point point6 = new Point("P6");

    TransportOrder order1 = new TransportOrder(
        "T1",
        List.of(
            new DriveOrder(
                new Destination(point1.getReference())
            ).withRoute(
                new Route(
                    List.of(
                        new Step(null, null, point2, Vehicle.Orientation.FORWARD, 0, 1),
                        new Step(null, null, point1, Vehicle.Orientation.FORWARD, 0, 1)
                    )
                )
            )
        )
    ).withState(TransportOrder.State.BEING_PROCESSED);

    TransportOrder order2 = new TransportOrder(
        "T2",
        List.of(
            new DriveOrder(
                new Destination(point4.getReference())
            ).withRoute(
                new Route(
                    List.of(
                        new Step(null, null, point3, Vehicle.Orientation.FORWARD, 0, 1),
                        new Step(null, null, point4, Vehicle.Orientation.FORWARD, 0, 1)
                    )
                )
            ),
            new DriveOrder(
                new Destination(point6.getReference())
            ).withRoute(
                new Route(
                    List.of(
                        new Step(null, null, point5, Vehicle.Orientation.FORWARD, 0, 1),
                        new Step(null, null, point6, Vehicle.Orientation.FORWARD, 0, 1)
                    )
                )
            )
        )
    ).withState(TransportOrder.State.BEING_PROCESSED);

    when(objectService.fetchObjects(eq(TransportOrder.class), any()))
        .thenReturn(Set.of(order1, order2));

    Set<Point> targetedPoints = targetedPointsSupplier.getTargetedPoints();
    assertThat(targetedPoints, hasSize(2));
    assertThat(targetedPoints, containsInAnyOrder(point1, point6));
  }
}
