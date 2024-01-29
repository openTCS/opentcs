/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link ReservationPool}.
 */
class ReservationPoolTest {

  private Scheduler.Client client;
  private ReservationPool reservationPool;

  @BeforeEach
  void setUp() {
    client = new TestClient();
    reservationPool = new ReservationPool();
  }

  @Test
  void claimIsEmptyInitially() {
    assertThat(reservationPool.getClaim(client), is(empty()));
  }

  @Test
  void claimIsEmptyAfterClear() {
    Set<TCSResource<?>> resources = Set.of(new Point("point1"), new Point("point2"));
    List<Set<TCSResource<?>>> claim = List.of(resources);

    reservationPool.setClaim(client, claim);
    reservationPool.clear();

    assertThat(reservationPool.getClaim(client), is(empty()));
  }

  @Test
  void disallowUnclaimingResourcesNotPreviouslyClaimed() {
    Set<TCSResource<?>> resources = Set.of(new Point("point1"), new Point("point2"));
    List<Set<TCSResource<?>>> claim = List.of(resources);

    reservationPool.setClaim(client, claim);

    assertThatIllegalArgumentException().isThrownBy(
        () -> reservationPool.unclaim(client, Set.of())
    );

    reservationPool.unclaim(client, resources);
  }

  @Test
  void returnClaimedResources() {
    Set<TCSResource<?>> resources = Set.of(new Point("point1"), new Point("point2"));
    List<Set<TCSResource<?>>> claim = List.of(resources);

    reservationPool.setClaim(client, claim);

    List<Set<TCSResource<?>>> claimedResources = reservationPool.getClaim(client);

    assertThat(claimedResources, hasSize(1));
    assertThat(claimedResources.get(0), hasItems(new Point("point1"), new Point("point2")));
  }

  @Test
  void confirmNextClaim() {
    Set<TCSResource<?>> resources = Set.of(new Point("point1"), new Point("point2"));
    List<Set<TCSResource<?>>> claim = List.of(resources);

    reservationPool.setClaim(client, claim);

    assertThat(
        reservationPool.isNextInClaim(client, Set.of(new Point("point1"), new Point("point2"))),
        is(true)
    );
  }

  @Test
  void allocatedResourcesIsEmptyInitially() {
    assertThat(reservationPool.allocatedResources(client), is(empty()));
    assertThat(reservationPool.getAllocations(), is(anEmptyMap()));
  }

  @Test
  void allocatedResourcesIsEmptyAfterClear() {
    reservationPool.getReservationEntry(new Point("point1")).allocate(client);
    reservationPool.clear();

    assertThat(reservationPool.allocatedResources(client), is(empty()));
  }

  @Test
  void reflectAllocatedResources() {
    reservationPool.getReservationEntry(new Point("point1")).allocate(client);

    assertThat(reservationPool.allocatedResources(client), hasSize(1));
    assertThat(reservationPool.getAllocations(), is(aMapWithSize(1)));
  }

  @Test
  void allocatedResourcesIsEmptyAfterFreeAll() {
    reservationPool.getReservationEntry(new Point("point1")).allocate(client);
    reservationPool.freeAll(client);

    assertThat(reservationPool.allocatedResources(client), is(empty()));
    assertThat(reservationPool.getAllocations(), is(anEmptyMap()));
  }

  /**
   * A dummy client for cases in which we need to provide a client but do not have a real one.
   */
  private static class TestClient
      implements Scheduler.Client {

    @Override
    public String getId() {
      return getClass().getName();
    }

    @Override
    public TCSObjectReference<Vehicle> getRelatedVehicle() {
      return null;
    }

    @Override
    public boolean allocationSuccessful(Set<TCSResource<?>> resources) {
      return false;
    }

    @Override
    public void allocationFailed(Set<TCSResource<?>> resources) {
    }
  }
}
