/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.guing.base.components.properties.type.AngleProperty.Unit;
import org.opentcs.guing.base.model.AbstractModelComponent;

/**
 * A test for an angle property.
 * For Degrees, min value is 0 and max value is 360.
 * For Radians, min value is 0 and max value is 2*PI ~ 6.283185
 */
class AnglePropertyTest {

  private AngleProperty property;

  @ParameterizedTest
  @ValueSource(strings = {"deg", "rad"})
  void testValidUnits(String unit) {
    property = new AngleProperty(new DummyComponent());
    assertTrue(property.isPossibleUnit(unit));
  }

  @Test
  void testPropertyConversionDegToRad() {
    // 180 deg = PI rad
    property = new AngleProperty(new DummyComponent(), 180, Unit.DEG);
    property.convertTo(Unit.RAD);
    assertEquals(Math.PI, (double) property.getValue(), 0);
    assertEquals(Unit.RAD, property.getUnit());
  }

  @Test
  void testPropertyConversionRadToDeg() {
    // 3.7168 rad ~ 212.96 deg
    property = new AngleProperty(new DummyComponent(), 3.7168, Unit.RAD);
    property.convertTo(Unit.DEG);
    assertEquals(212.96, (double) property.getValue(), 0.01);
    assertEquals(Unit.DEG, property.getUnit());
  }

  @Test
  void testPropertyRange() {
    property = new AngleProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(Double.MAX_VALUE, property.getValidRange().getMax(), 0);
  }

  @Test
  void shouldStayInRangeDeg() {
    property = new AngleProperty(new DummyComponent(), 540, Unit.DEG);
    assertEquals(180.0, property.getValue());
    assertEquals(Unit.DEG, property.getUnit());
  }

  @Test
  void shouldStayInRangeRad() {
    property = new AngleProperty(new DummyComponent(), 10, Unit.RAD);
    assertEquals(3.716, (double) property.getValue(), 0.001);
    assertEquals(AngleProperty.Unit.RAD, property.getUnit());
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
