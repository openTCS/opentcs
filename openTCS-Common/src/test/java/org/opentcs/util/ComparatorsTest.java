/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * Unit tests for {@link Comparators}.
 */
class ComparatorsTest {

  @Test
  void compareObjectsByName() {
    Comparator<TCSObject<?>> comparator = Comparators.objectsByName();

    assertThat(comparator.compare(new Point("1"), new Point("1")),
               is(0));

    assertThat(comparator.compare(new Point("1"), new Point("2")),
               is(-1));
    assertThat(comparator.compare(new Point("2"), new Point("1")),
               is(1));

    assertThat(comparator.compare(new Point("01"), new Point("02")),
               is(-1));
    assertThat(comparator.compare(new Point("02"), new Point("01")),
               is(1));

    assertThat(comparator.compare(new Point("point01"), new Point("point02")),
               is(-1));
    assertThat(comparator.compare(new Point("point02"), new Point("point01")),
               is(1));

    assertThat(comparator.compare(new Point("point-01"), new Point("point-02")),
               is(-1));
    assertThat(comparator.compare(new Point("point-02"), new Point("point-01")),
               is(1));
  }

  @Test
  void compareReferencesByName() {
    Comparator<TCSObjectReference<?>> comparator = Comparators.referencesByName();

    assertThat(
        comparator.compare(
            new Point("1").getReference(),
            new Point("1").getReference()
        ),
        is(0)
    );

    assertThat(
        comparator.compare(
            new Point("1").getReference(),
            new Point("2").getReference()
        ),
        is(-1)
    );
    assertThat(
        comparator.compare(
            new Point("2").getReference(),
            new Point("1").getReference()
        ),
        is(1)
    );

    assertThat(
        comparator.compare(
            new Point("01").getReference(),
            new Point("02").getReference()
        ),
        is(-1)
    );
    assertThat(
        comparator.compare(
            new Point("02").getReference(),
            new Point("01").getReference()
        ),
        is(1)
    );

    assertThat(
        comparator.compare(
            new Point("point01").getReference(),
            new Point("point02").getReference()
        ),
        is(-1)
    );
    assertThat(
        comparator.compare(
            new Point("point02").getReference(),
            new Point("point01").getReference()
        ),
        is(1)
    );

    assertThat(
        comparator.compare(
            new Point("point-01").getReference(),
            new Point("point-02").getReference()
        ),
        is(-1)
    );
    assertThat(
        comparator.compare(
            new Point("point-02").getReference(),
            new Point("point-01").getReference()
        ),
        is(1)
    );
  }

  @Test
  void compareOrdersByAge() {
    Comparator<TransportOrder> comparator = Comparators.ordersByAge();

    TransportOrder order1 = new TransportOrder("order-1", List.of());
    TransportOrder order2 = new TransportOrder("order-2", List.of());

    assertThat(
        comparator.compare(
            order1.withCreationTime(Instant.ofEpochMilli(1000)),
            order2.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            order1.withCreationTime(Instant.ofEpochMilli(2000)),
            order2.withCreationTime(Instant.ofEpochMilli(1000))
        ),
        is(1)
    );

    // Compares by name if age is same:
    assertThat(
        comparator.compare(
            order1.withCreationTime(Instant.ofEpochMilli(2000)),
            order2.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            order2.withCreationTime(Instant.ofEpochMilli(2000)),
            order1.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(1)
    );
  }

  @Test
  void compareOrdersByDeadline() {
    Comparator<TransportOrder> comparator = Comparators.ordersByDeadline();

    TransportOrder order1 = new TransportOrder("order-1", List.of())
        .withCreationTime(Instant.ofEpochMilli(1000));
    TransportOrder order2 = new TransportOrder("order-2", List.of())
        .withCreationTime(Instant.ofEpochMilli(2000));

    assertThat(
        comparator.compare(
            order1.withDeadline(Instant.ofEpochMilli(5000)),
            order2.withDeadline(Instant.ofEpochMilli(7000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            order1.withDeadline(Instant.ofEpochMilli(7000)),
            order2.withDeadline(Instant.ofEpochMilli(5000))
        ),
        is(1)
    );

    // Compares by age if deadline is same:
    assertThat(
        comparator.compare(
            order1.withDeadline(Instant.ofEpochMilli(5000)),
            order2.withDeadline(Instant.ofEpochMilli(5000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            order2.withDeadline(Instant.ofEpochMilli(5000)),
            order1.withDeadline(Instant.ofEpochMilli(5000))
        ),
        is(1)
    );
  }

  @Test
  void comparePeripheralJobsByAge() {
    Comparator<PeripheralJob> comparator = Comparators.jobsByAge();

    LocationType locType = new LocationType("some-loc-type");
    Location location = new Location("some-location", locType.getReference());

    PeripheralJob job1 = new PeripheralJob(
        "job-1",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    PeripheralJob job2 = new PeripheralJob(
        "job-2",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );

    assertThat(
        comparator.compare(
            job1.withCreationTime(Instant.ofEpochMilli(1000)),
            job2.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            job1.withCreationTime(Instant.ofEpochMilli(2000)),
            job2.withCreationTime(Instant.ofEpochMilli(1000))
        ),
        is(1)
    );

    // Compare by name if age is same:
    assertThat(
        comparator.compare(
            job1.withCreationTime(Instant.ofEpochMilli(2000)),
            job2.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(-1)
    );

    assertThat(
        comparator.compare(
            job2.withCreationTime(Instant.ofEpochMilli(2000)),
            job1.withCreationTime(Instant.ofEpochMilli(2000))
        ),
        is(1)
    );

  }
}
