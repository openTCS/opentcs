/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TCSObjectReference}.
 */
class TCSObjectReferenceTest {

  @Test
  void considerReferenceForEquality() {
    TestType1 object = new TestType1("some-name");

    assertThat(object.getReference())
        .isEqualTo(object.getReference())
        .isNotEqualTo(null);
  }

  @Test
  void considerNameForEquality() {
    assertThat(new TestType1("some-name").getReference())
        .isEqualTo(new TestType1("some-name").getReference())
        .isNotEqualTo(new TestType1("some-other-name").getReference());
  }

  @Test
  void considerClassForEquality() {
    assertThat(new TestType1("some-name").getReference())
        .isEqualTo(new TestType1("some-name").getReference())
        .isNotEqualTo(new TestType2("some-name").getReference())
        .isNotEqualTo(new Object());
  }

  @Test
  void considerNameForHashCode() {
    assertThat(new TestType1("some-name").getReference())
        .hasSameHashCodeAs(new TestType1("some-name").getReference())
        .doesNotHaveSameHashCodeAs(new TestType1("some-other-name").getReference());
  }

  private static class TestType1
      extends TCSObject<TestType1> {

    TestType1(String objectName) {
      super(objectName);
    }

    TestType1(String objectName, Map<String, String> properties, ObjectHistory history) {
      super(objectName, properties, history);
    }

    @Override
    public TestType1 withProperty(String key, String value) {
      return new TestType1(getName(), propertiesWith(key, value), getHistory());
    }

    @Override
    public TestType1 withProperties(Map<String, String> properties) {
      return new TestType1(getName(), properties, getHistory());
    }

    @Override
    public TestType1 withHistoryEntry(ObjectHistory.Entry entry) {
      return new TestType1(getName(), getProperties(), getHistory().withEntryAppended(entry));
    }

    @Override
    public TestType1 withHistory(ObjectHistory history) {
      return new TestType1(getName(), getProperties(), history);
    }
  }

  private static class TestType2
      extends TCSObject<TestType2> {

    TestType2(String objectName) {
      super(objectName);
    }

    TestType2(String objectName, Map<String, String> properties, ObjectHistory history) {
      super(objectName, properties, history);
    }

    @Override
    public TestType2 withProperty(String key, String value) {
      return new TestType2(getName(), propertiesWith(key, value), getHistory());
    }

    @Override
    public TestType2 withProperties(Map<String, String> properties) {
      return new TestType2(getName(), properties, getHistory());
    }

    @Override
    public TestType2 withHistoryEntry(ObjectHistory.Entry entry) {
      return new TestType2(getName(), getProperties(), getHistory().withEntryAppended(entry));
    }

    @Override
    public TestType2 withHistory(ObjectHistory history) {
      return new TestType2(getName(), getProperties(), history);
    }
  }
}
