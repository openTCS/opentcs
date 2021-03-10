/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.data.model.Point;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ParkingPositionToPriorityFunctionTest {

  private ParkingPositionToPriorityFunction priorityFunction;

  @Before
  public void setUp() {
    priorityFunction = new ParkingPositionToPriorityFunction();
  }

  @Test
  public void returnsNullForNonParkingPosition() {
    Point point = new Point("Some point")
        .withType(Point.Type.HALT_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "1");

    assertThat(priorityFunction.apply(point), is(nullValue()));
  }

  @Test
  public void returnsNullForParkingPositionWithoutPriorityProperty() {
    Point point = new Point("Some point").withType(Point.Type.PARK_POSITION);

    assertThat(priorityFunction.apply(point), is(nullValue()));
  }

  @Test
  public void returnsNullForParkingPositionWithNonDecimalProperty() {
    Point point = new Point("Some point")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "abc");

    assertThat(priorityFunction.apply(point), is(nullValue()));
  }

  @Test
  public void returnsPriorityForParkingPositionWithDecimalProperty() {
    Point point = new Point("Some point")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "23");

    assertThat(priorityFunction.apply(point), is(23));
  }

}
