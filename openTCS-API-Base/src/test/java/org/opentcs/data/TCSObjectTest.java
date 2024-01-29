/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TCSObject}.
 */
class TCSObjectTest {

  @Test
  void considerReferenceForEquality() {
    TestType1 object = new TestType1("some-name");

    assertThat(object)
        .isEqualTo(object)
        .isNotEqualTo(null);
  }

  @Test
  void considerNameForEquality() {
    assertThat(new TestType1("some-name"))
        .isEqualTo(new TestType1("some-name"))
        .isNotEqualTo(new TestType1("some-other-name"));
  }

  @Test
  void considerClassForEquality() {
    assertThat(new TestType1("some-name"))
        .isEqualTo(new TestType1("some-name"))
        .isNotEqualTo(new TestType2("some-name"));
  }

  @Test
  void considerNameForHashCode() {
    assertThat(new TestType1("some-name"))
        .hasSameHashCodeAs(new TestType1("some-name"))
        .doesNotHaveSameHashCodeAs(new TestType1("some-other-name"));
  }

  @Test
  void considerClassForHashCode() {
    assertThat(new TestType1("some-name"))
        .hasSameHashCodeAs(new TestType1("some-name"))
        .doesNotHaveSameHashCodeAs(new TestType2("some-name"));
  }

  @Test
  void addProperty() {
    TestType1 object = new TestType1("some-object")
        .withProperty("some-key", "some-value");

    assertThat(object.getProperty("some-key"))
        .isEqualTo("some-value");
    assertThat(object.getProperties())
        .containsKey("some-key");
  }

  @Test
  void removePropertyViaNullValue() {
    TestType1 original = new TestType1("some-object")
        .withProperty("some-key", "some-value");

    TestType1 modified = original.withProperty("some-key", null);

    assertThat(modified.getProperty("some-key"))
        .isNull();
    assertThat(modified.getProperties())
        .doesNotContainKey("some-key");
  }

  @Test
  void filterNullValuesFromMap() {
    Map<String, String> input = new HashMap<>();
    input.put("one", "one");
    input.put("two", "two");
    input.put("null-1", null);
    input.put("null-2", null);
    input.put("three", "three");
    input.put("null-3", null);

    Map<String, String> result = TCSObject.mapWithoutNullValues(input);

    assertThat(result)
        .hasSize(3)
        .containsOnlyKeys("one", "two", "three");
  }

  @Test
  void filterNullValuesFromList() {
    List<String> result = TCSObject.listWithoutNullValues(
        Arrays.asList(
            "one",
            "two",
            null,
            null,
            "three",
            null
        )
    );

    assertThat(result)
        .hasSize(3)
        .containsExactly("one", "two", "three");
  }

  @Test
  void filterNullValuesFromSet() {
    Set<String> result = TCSObject.setWithoutNullValues(
        new HashSet<>(
            Arrays.asList(
                "one",
                "two",
                null,
                null,
                "three",
                null
            )
        )
    );

    assertThat(result)
        .hasSize(3)
        .containsExactlyInAnyOrder("one", "two", "three");
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
