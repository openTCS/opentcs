/*
 * AutoGrowingArrayListTest.java
 *
 * Created on February 1, 2008, 10:03 PM
 */
package org.opentcs.kernel.workingset;

import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * A test case for AutoGrowingArrayList.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AutoGrowingArrayListTest {

  /**
   * Verify that automatic growth works correctly.
   */
  @Test
  public void testIfListGrowsAutomatically() {
    AutoGrowingArrayList<String> list = new AutoGrowingArrayList<>();
    list.set(0, "A");
    assertEquals(1, list.size());

    list.add("B");
    list.add("C");
    assertEquals(3, list.size());

    list.set(25, "Z");
    assertEquals(26, list.size());

    list.set(24, "Y");
    assertEquals(26, list.size());
  }
}
