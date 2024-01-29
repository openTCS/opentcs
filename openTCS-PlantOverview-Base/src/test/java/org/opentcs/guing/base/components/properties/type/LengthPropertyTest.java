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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.guing.base.components.properties.type.LengthProperty.Unit;
import org.opentcs.guing.base.model.AbstractModelComponent;

/**
 * A test for a length property.
 */
class LengthPropertyTest {

  private LengthProperty property;

  @ParameterizedTest
  @ValueSource(strings = {"mm", "cm", "m", "km"})
  void testValidUnits(String unit) {
    property = new LengthProperty(new DummyComponent());
    assertTrue(property.isPossibleUnit(unit));
  }

  @ParameterizedTest
  @MethodSource("paramsFactory")
  void testPropertyConversion(Unit unit, Object result) {
    property = new LengthProperty(new DummyComponent(), 10, Unit.CM);
    property.convertTo(unit);
    assertEquals(result, property.getValue());
    assertEquals(unit, property.getUnit());
  }

  @Test
  void testPropertyRange() {
    property = new LengthProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(Double.MAX_VALUE, property.getValidRange().getMax(), 0);
  }

  static Object[][] paramsFactory() {
    return new Object[][] {{Unit.MM, 100.0},
                           {Unit.CM, 10.0},
                           {Unit.M, 0.1},
                           {Unit.KM, 0.0001}};
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
