// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
      extends
        AbstractModelComponent {
  }
}
