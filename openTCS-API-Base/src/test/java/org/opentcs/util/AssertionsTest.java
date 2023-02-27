/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Assertions}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssertionsTest {

  public AssertionsTest() {
  }

  @Test
  public void checkArgumentShouldFormatIntegerMessageTemplateArgument() {
    try {
      Assertions.checkArgument(false, "%s", 123);
    }
    catch (IllegalArgumentException exc) {
      assertEquals("123", exc.getMessage());
    }
  }

  @Test
  public void checkInRangeShouldIncludeBoundaries() {
    assertEquals(22, Assertions.checkInRange(22, 22, 24));
    assertEquals(23, Assertions.checkInRange(23, 22, 24));
    assertEquals(24, Assertions.checkInRange(24, 22, 24));
  }

  @Test
  public void checkInRangeShouldFailOnLessThanMinimum() {
    assertThrows(IllegalArgumentException.class,
                 () -> org.opentcs.util.Assertions.checkInRange(21, 22, 24));
  }

  @Test
  public void checkInRangeShouldFailOnMoreThanMaximum() {
    assertThrows(IllegalArgumentException.class,
                 () -> org.opentcs.util.Assertions.checkInRange(25, 22, 24));
  }
}
