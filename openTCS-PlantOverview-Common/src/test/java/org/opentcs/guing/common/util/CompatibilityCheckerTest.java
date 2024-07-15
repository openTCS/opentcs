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

import org.junit.jupiter.api.Test;

/**
 */
class CompatibilityCheckerTest {

  @Test
  void acceptCompatibleVersion() {
    // "21.0.3" is a string that is returned by Eclipse Temurin 21 and Oracle JDK 21
    assertThat(CompatibilityChecker.versionCompatibleWithDockingFrames("21.0.3"), is(true));
  }

  @Test
  void refuseIncompatibleVersion() {
    // "21" is a string that may be returned by some other Java distribution
    assertThat(CompatibilityChecker.versionCompatibleWithDockingFrames("21"), is(false));
  }
}
