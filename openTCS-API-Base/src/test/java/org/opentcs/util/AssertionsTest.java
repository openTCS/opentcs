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
 */
class AssertionsTest {

  AssertionsTest() {
  }

  @Test
  void checkArgumentShouldFormatIntegerMessageTemplateArgument() {
    try {
      Assertions.checkArgument(false, "%s", 123);
    }
    catch (IllegalArgumentException exc) {
      assertEquals("123", exc.getMessage());
    }
  }

  @Test
  void checkArgumentShouldThrowIfExpressionIsFalse() {
    assertThrows(IllegalArgumentException.class,
                 () -> Assertions.checkArgument(false, "The given expression is not true!"));
  }

  @Test
  void checkStateShouldFormatIntegerMessageTemplateArgument() {
    try {
      Assertions.checkState(false, "%s", 456);
    }
    catch (IllegalStateException exc) {
      assertEquals("456", exc.getMessage());
    }
  }

  @Test
  void checkStateShouldThrowIfExpressionIsFalse() {
    assertThrows(IllegalStateException.class,
                 () -> Assertions.checkState(false, "The given expression is not true"));
  }

  @Test
  void checkInRangeShouldSucceedWithinBoundaries() {
    assertEquals(22, Assertions.checkInRange(22, 22, 24));
    assertEquals(23, Assertions.checkInRange(23, 22, 24));
    assertEquals(24, Assertions.checkInRange(24, 22, 24));
    assertEquals(Integer.MAX_VALUE,
                 Assertions.checkInRange(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    assertEquals(Integer.MIN_VALUE,
                 Assertions.checkInRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
  }

  @Test
  void checkInRangeShouldFailOnLessThanMinimum() {
    assertThrows(IllegalArgumentException.class,
                 () -> Assertions.checkInRange(21, 22, 24));
  }

  @Test
  void checkInRangeShouldFailOnMoreThanMaximum() {
    assertThrows(IllegalArgumentException.class,
                 () -> Assertions.checkInRange(25, 22, 24));
  }

  @Test
  void checkInRangeLongShouldSucceedWithinBoundaries() {
    assertEquals(23, Assertions.checkInRange(23, 23, 25));
    assertEquals(24, Assertions.checkInRange(24, 23, 25));
    assertEquals(25, Assertions.checkInRange(25, 23, 25));
    assertEquals(Long.MIN_VALUE,
                 Assertions.checkInRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
    assertEquals(Long.MAX_VALUE,
                 Assertions.checkInRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
  }

  @Test
  void checkInRangeLongShouldFailOnLessThanMinimum() {
    assertThrows(IllegalArgumentException.class,
                 () -> Assertions.checkInRange(22, 23, 25));
  }

  @Test
  void checkInRangeLongShouldFailOnMoreThanMaximum() {
    assertThrows(IllegalArgumentException.class,
                 () -> Assertions.checkInRange(26, 23, 25));
  }
}
