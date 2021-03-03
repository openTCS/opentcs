/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
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
  public void checkArgument_shouldFormatIntegerMessageTemplateArgument() {
    try {
      Assertions.checkArgument(false, "%s", 123);
    }
    catch (IllegalArgumentException exc) {
      Assert.assertEquals("123", exc.getMessage());
    }
  }
  
  @Test
  public void checkInRange_shouldIncludeBoundaries() {
    Assert.assertEquals(22, Assertions.checkInRange(22, 22, 24));
    Assert.assertEquals(23, Assertions.checkInRange(23, 22, 24));
    Assert.assertEquals(24, Assertions.checkInRange(24, 22, 24));
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkInRange_shouldFailOnLessThanMinimum() {
    Assertions.checkInRange(21, 22, 24);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkInRange_shouldFailOnMoreThanMaximum() {
    Assertions.checkInRange(25, 22, 24);
  }
}
