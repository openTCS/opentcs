/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.model.AbstractModelComponent;

/**
 * A test for a speed property.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SpeedPropertyTest {

  private SpeedProperty property;

  @ParameterizedTest
  @ValueSource(strings = {"mm/s", "m/s", "km/h"})
  public void testValidUnits(String unit) {
    property = new SpeedProperty(new DummyComponent());
    assertTrue(property.isPossibleUnit(unit));
  }

  @ParameterizedTest
  @MethodSource("paramsFactory")
  public void testPropertyConversion(Unit unit, Object result) {
    property = new SpeedProperty(new DummyComponent(), 10000.0, Unit.MM_S);
    property.convertTo(unit);
    assertEquals(result, property.getValue());
    assertEquals(unit, property.getUnit());
  }

  @Test
  public void testPropertyRange() {
    property = new SpeedProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(Double.MAX_VALUE, property.getValidRange().getMax(), 0);
  }

  public static Object[][] paramsFactory() {
    return new Object[][] {{Unit.MM_S, 10000.0},
                           {Unit.M_S, 10.0},
                           {Unit.KM_H, 36.0}};
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
