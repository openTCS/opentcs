// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;

/**
 * Unit tests for {@link ClassMatcher}.
 */
class ClassMatcherTest {

  @Test
  void confirmGivenClasses() {
    ClassMatcher classMatcher = new ClassMatcher(Point.class, Path.class);

    assertTrue(classMatcher.test(new Point("some-point")));
    assertTrue(
        classMatcher.test(
            new Path(
                "some-path",
                new Point("src-point").getReference(),
                new Point("dst-point").getReference()
            )
        )
    );
  }
}
