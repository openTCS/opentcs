/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.*;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompatibilityCheckerTest {

  @Test
  public void acceptCompatibleVersion() {
    // "13.0.2" is a string that is returned by AdoptOpenJDK 13
    assertThat(CompatibilityChecker.versionCompatibleWithDockingFrames("13.0.2"), is(true));
  }
  
  @Test
  public void refuseIncompatibleVersion() {
    // "13" is a string that is returned by Oracle JDK 13
    assertThat(CompatibilityChecker.versionCompatibleWithDockingFrames("13"), is(false));
  }
}
