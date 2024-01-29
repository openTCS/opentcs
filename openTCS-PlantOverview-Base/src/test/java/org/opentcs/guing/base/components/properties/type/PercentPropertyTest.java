/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.opentcs.guing.base.model.AbstractModelComponent;

/**
 * A test for a percent property.
 */
class PercentPropertyTest {

  private PercentProperty property;

  @Test
  void testPropertyRange() {
    property = new PercentProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(100, property.getValidRange().getMax(), 0);
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
