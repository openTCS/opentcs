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
import org.opentcs.guing.model.AbstractModelComponent;

/**
 * A test for a percent property.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PercentPropertyTest {

  private PercentProperty property;

  @Test
  public void testPropertyRange() {
    property = new PercentProperty(new DummyComponent());
    assertEquals(0, property.getValidRange().getMin(), 0);
    assertEquals(100, property.getValidRange().getMax(), 0);
  }

  private class DummyComponent
      extends AbstractModelComponent {
  }
}
