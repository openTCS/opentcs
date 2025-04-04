// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MapValueExtractor}.
 */
class MapValueExtractorTest {

  private MapValueExtractor mapValueExtractor;

  @BeforeEach
  void setUp() {
    mapValueExtractor = new MapValueExtractor();
  }

  @Test
  void returnsEmptyOptionalForMissingStringEntry() {
    var result = mapValueExtractor.extractString("key", Map.of());
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsExtractedString() {
    var result = mapValueExtractor.extractString("key", Map.of("key", "value"));
    assertThat(result).isPresent();
    assertThat(result).hasValue("value");
  }

  @Test
  void returnsEmptyOptionalForMissingOrUnparsableIntegerEntry() {
    // Test for missing parameter
    var result = mapValueExtractor.extractInteger("key", Map.of());
    assertThat(result).isNotPresent();

    // Test for unparsable parameter
    result = mapValueExtractor.extractInteger("key", Map.of("key", "notAnInteger"));
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsExtractedInteger() {
    var result = mapValueExtractor.extractInteger("key", Map.of("key", "123"));
    assertThat(result).isPresent();
    assertThat(result).hasValue(123);
  }

  @Test
  void returnsEmptyOptionalForMissingOrUnparsableLongEntry() {
    // Test for missing entry
    var result = mapValueExtractor.extractLong("key", Map.of());
    assertThat(result).isNotPresent();

    // Test for unparsable entry
    result = mapValueExtractor.extractLong("key", Map.of("key", "notALong"));
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsExtractedLong() {
    var result = mapValueExtractor.extractLong("key", Map.of("key", "456"));
    assertThat(result).isPresent();
    assertThat(result).hasValue(456L);
  }

  @Test
  void returnsEmptyOptionalForMissingOrUnparsableDoubleEntry() {
    // Test for missing entry
    var result = mapValueExtractor.extractDouble("key", Map.of());
    assertThat(result).isNotPresent();

    // Test for unparsable entry
    result = mapValueExtractor.extractDouble("key", Map.of("key", "notADouble"));
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsExtractedDouble() {
    var result = mapValueExtractor.extractDouble("key", Map.of("key", "789.0"));
    assertThat(result).isPresent();
    assertThat(result).hasValue(789.0);
  }

  @Test
  void returnsEmptyOptionalForMissingBooleanEntry() {
    var result = mapValueExtractor.extractBoolean("key", Map.of());
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsOptionalContainingFalseForUnparsableBooleanEntry() {
    var result = mapValueExtractor.extractBoolean("key", Map.of("key", "notABoolean"));
    assertThat(result).isPresent();
    assertThat(result).hasValue(false);
  }

  @Test
  void returnsExtractedBoolean() {
    var result = mapValueExtractor.extractBoolean("key", Map.of("key", "true"));
    assertThat(result).isPresent();
    assertThat(result).hasValue(true);
  }

  @Test
  void returnsEmptyOptionalForMissingOrUnmappableEnumEntry() {
    // Test for missing entry
    var result = mapValueExtractor.extractEnum(
        "key",
        Map.of(),
        TestEnum.class
    );
    assertThat(result).isNotPresent();

    // Test for unmappable entry
    result = mapValueExtractor.extractEnum(
        "key",
        Map.of("key", "notAnEnumElement"),
        TestEnum.class
    );
    assertThat(result).isNotPresent();
  }

  @Test
  void returnsExtractedEnum() {
    var result = mapValueExtractor.extractEnum(
        "key",
        Map.of("key", "ELEMENT"),
        TestEnum.class
    );
    assertThat(result).isPresent();
    assertThat(result).hasValue(TestEnum.ELEMENT);
  }

  private enum TestEnum {
    ELEMENT;
  }
}
