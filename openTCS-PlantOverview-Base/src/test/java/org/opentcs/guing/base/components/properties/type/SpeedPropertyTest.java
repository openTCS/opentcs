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
import org.opentcs.guing.base.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.base.model.AbstractModelComponent;

/**
 * A test for a speed property.
 */
class SpeedPropertyTest {

  private SpeedProperty property;

  @ParameterizedTest
  @ValueSource(strings = {"mm/s", "m/s", "km/h"})
  void testValidUnits(String unit) {
    property = new SpeedProperty(new DummyComponent());
    assertTrue(property.isPossibleUnit(unit));
  }

  @ParameterizedTest
  @MethodSource("paramsFactory")
  void testPropertyConversion(Unit unit, Object result) {
    property = new SpeedProperty(new DummyComponent(), 10000.0, Unit.MM_S);
    property.convertTo(unit);
    assertEquals(result, property.getValue());
    assertEquals(unit, property.getUnit());
  }

  @Test
  void testPropertyRange() {
    property = new SpeedProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(Double.MAX_VALUE, property.getValidRange().getMax(), 0);
  }

  static Object[][] paramsFactory() {
    return new Object[][] {{Unit.MM_S, 10000.0},
                           {Unit.M_S, 10.0},
                           {Unit.KM_H, 36.0}};
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
