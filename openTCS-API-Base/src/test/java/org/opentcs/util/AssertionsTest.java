/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import org.junit.*;

/**
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
      Assert.assertEquals("123", exc.getMessage());
    }
  }
  
  @Test
  public void checkInRangeShouldIncludeBoundaries() {
    Assert.assertEquals(22, Assertions.checkInRange(22, 22, 24));
    Assert.assertEquals(23, Assertions.checkInRange(23, 22, 24));
    Assert.assertEquals(24, Assertions.checkInRange(24, 22, 24));
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkInRangeShouldFailOnLessThanMinimum() {
    Assertions.checkInRange(21, 22, 24);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkInRangeShouldFailOnMoreThanMaximum() {
    Assertions.checkInRange(25, 22, 24);
  }
}
