/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link VelocityController}.
 */
class VelocityControllerTest {

  private static final int MAX_DECEL = -1000;
  private static final int MAX_ACCEL = 1000;
  private static final int MAX_REV_VELO = -500;
  private static final int MAX_FWD_VELO = 500;

  private static final long WAY_LENGTH = 5000;
  private static final int MAX_VELO = 500;
  private static final String POINT_NAME = "Destination_Point";

  private VelocityController controller;

  @BeforeEach
  void setUp() {
    controller = new VelocityController(MAX_DECEL, MAX_ACCEL, MAX_REV_VELO, MAX_FWD_VELO);
  }

  @Test
  void initialControllerHasNoWayEntries() {
    assertFalse(controller.hasWayEntries());
    assertNull(controller.getCurrentWayEntry());
  }

  @Test
  void throwOnAddingNullWayEntry() {
    assertThrows(NullPointerException.class, () -> controller.addWayEntry(null));
  }

  @Test
  void throwOnAdvancingWithNegativeTime() {
    assertThrows(IllegalArgumentException.class, () -> controller.advanceTime(-1));
  }

  @Test
  void advanceTimeAdvancesTime() {
    long timeBefore = controller.getCurrentTime();

    controller.advanceTime(5);

    assertThat(controller.getCurrentTime(), is(timeBefore + 5));
  }

  @Test
  void vehicleDoesNotChangePositionWhilePaused() {
    VelocityController.WayEntry wayEntry
        = new VelocityController.WayEntry(WAY_LENGTH,
                                          MAX_VELO,
                                          POINT_NAME,
                                          Vehicle.Orientation.FORWARD);
    controller.addWayEntry(wayEntry);
    controller.setVehiclePaused(true);

    int posBefore = controller.getCurrentPosition();

    controller.advanceTime(5);

    assertThat(controller.getCurrentPosition(), is(posBefore));
  }

  @Test
  void processWayEntriesInGivenOrder() {
    controller = new VelocityController(MAX_DECEL, 1000, MAX_REV_VELO, 500);

    VelocityController.WayEntry firstEntry
        = new VelocityController.WayEntry(1, MAX_VELO, POINT_NAME, Vehicle.Orientation.FORWARD);
    VelocityController.WayEntry secondEntry
        = new VelocityController.WayEntry(10000, MAX_VELO, POINT_NAME, Vehicle.Orientation.FORWARD);
    controller.addWayEntry(firstEntry);
    controller.addWayEntry(secondEntry);

    assertThat(controller.getCurrentWayEntry(), is(sameInstance(firstEntry)));
    assertSame(firstEntry, controller.getCurrentWayEntry());

    controller.advanceTime(100);

    assertThat(controller.getCurrentWayEntry(), is(sameInstance(secondEntry)));
    assertSame(secondEntry, controller.getCurrentWayEntry());
  }

  @Test
  void accelerateToMaxVelocityLimitedByController() {
    final int maxFwdVelocity = 500; // mm/s
    final int maxAcceleration = 250; // mm/s^2
    controller = new VelocityController(MAX_DECEL, maxAcceleration, MAX_REV_VELO, maxFwdVelocity);

    // Way point with enough length to reach the maximum velocity and with a higher velocity limit
    // than the vehicle's own maximum velocity.
    VelocityController.WayEntry wayEntry
        = new VelocityController.WayEntry(100000,
                                          2 * maxFwdVelocity,
                                          POINT_NAME,
                                          Vehicle.Orientation.FORWARD);
    controller.addWayEntry(wayEntry);

    controller.advanceTime(1000);

    // Reach 250 mm/s in 1s:
    assertThat(controller.getCurrentVelocity(), is(250));

    controller.advanceTime(1000);

    // Reach max. velocity of 500 mm/s in 2s:
    assertThat(controller.getCurrentVelocity(), is(500));

    controller.advanceTime(1000);

    // Stay at max velocity:
    assertThat(controller.getCurrentVelocity(), is(500));
  }

  @Test
  void accelerateToMaxVelocityLimitedByWayEntry() {
    final int maxFwdVelocity = 500; // mm/s
    final int maxAcceleration = 500; // mm/s^2
    controller = new VelocityController(MAX_DECEL, maxAcceleration, MAX_REV_VELO, maxFwdVelocity);

    // Way point with a velocity limit less than the vehicle's own maximum velocity.
    VelocityController.WayEntry wayEntry
        = new VelocityController.WayEntry(10000,
                                          250,
                                          POINT_NAME,
                                          Vehicle.Orientation.FORWARD);
    controller.addWayEntry(wayEntry);

    for (int i = 0; i < 10; i++) {
      controller.advanceTime(100);
    }

    // Velocity could be 500 mm/s after one second, but should be limited to 250 mm/s.
    assertThat(controller.getCurrentVelocity(), is(250));
  }
}
