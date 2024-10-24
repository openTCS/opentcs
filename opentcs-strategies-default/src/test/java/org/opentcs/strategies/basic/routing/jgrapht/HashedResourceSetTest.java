// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;

/**
 * Tests for {@link HashedResourceSet}.
 */
class HashedResourceSetTest {

  private HashedResourceSet<Point> set;

  @BeforeEach
  void setUp() {
    set = new HashedResourceSet<>(points -> 13);
  }

  @Test
  void isInitiallyEmpty() {
    assertTrue(set.isEmpty());
    assertThat(set.getResources()).isEmpty();
    assertThat(set.getHash()).isEqualTo(13);
  }

  @Test
  void setsResourcesAndHash() {
    set.overrideResources(Set.of(new Point("1"), new Point("2"), new Point("3")));

    assertFalse(set.isEmpty());
    assertThat(set.getResources()).hasSize(3);
    assertThat(set.getHash()).isEqualTo(13);
  }

  @Test
  void updatesResources() {
    Point originalPoint = new Point("1");
    Point updatedPoint = originalPoint.withProperty("some-key", "some-value");

    set.overrideResources(Set.of(originalPoint, new Point("2"), new Point("3")));
    assertThat(set.getResources()).hasSize(3);
    assertThat(set.getHash()).isEqualTo(13);
    assertThat(set.getResources()).anyMatch(
        point -> Objects.equals(point.getName(), "1") && point.getProperties().isEmpty()
    );

    set.updateResources(Set.of(updatedPoint));
    assertThat(set.getResources()).hasSize(3);
    assertThat(set.getHash()).isEqualTo(13);
    assertThat(set.getResources()).anyMatch(
        point -> Objects.equals(point.getName(), "1")
            && Objects.equals(point.getProperty("some-key"), "some-value")
    );
  }

  @Test
  void clearsResources() {
    set.updateResources(Set.of(new Point("1"), new Point("2"), new Point("3")));
    assertFalse(set.isEmpty());

    set.clear();
    assertTrue(set.isEmpty());
  }
}
