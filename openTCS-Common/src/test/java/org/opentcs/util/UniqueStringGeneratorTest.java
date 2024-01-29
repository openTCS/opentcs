/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test case for class UniqueStringGenerator.
 */
class UniqueStringGeneratorTest {

  private static final String PREFIX = "TestPrefix";

  private static final String PATTERN_ONE_DIGIT = "0";

  private static final String PATTERN_TWO_DIGITS = "00";

  private UniqueStringGenerator<Object> generator;

  @BeforeEach
  void setUp() {
    generator = new UniqueStringGenerator<>();
  }

  @Test
  void testRepeatedGenerationWithoutModification() {
    String generatedString = generator.getUniqueString(PREFIX,
                                                       PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
    generatedString = generator.getUniqueString(PREFIX, PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
  }

  @Test
  void shouldProvideConfiguredPatterns() {
    final String namePatternPrefix = "SomePrefix";
    final String namePatterPrefix2 = "AnotherPrefix";
    final Object selector = new Object();
    final Object selector2 = new Object();

    generator.registerNamePattern(selector, namePatternPrefix, "0000");
    generator.registerNamePattern(selector2, namePatterPrefix2, "0000");

    assertEquals(namePatternPrefix + "0001",
                 generator.getUniqueString(selector));
    assertEquals(namePatterPrefix2 + "0001",
                 generator.getUniqueString(selector2));
  }

  @Test
  void testRepeatedGenerationWithAddition() {
    String generatedString = generator.getUniqueString(PREFIX,
                                                       PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
    generator.addString(generatedString);
    generatedString = generator.getUniqueString(PREFIX, PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "02", generatedString);
  }

  @Test
  void testNullPrefix() {
    String generatedString = generator.getUniqueString(null,
                                                       PATTERN_ONE_DIGIT);
    assertEquals("1", generatedString);
  }

  @Test
  void shouldHaveString() {
    generator.addString("some string");
    assertTrue(generator.hasString("some string"));
  }

  @Test
  void shouldNotHaveString() {
    generator.addString("some string");
    assertFalse(generator.hasString("some other string"));
  }

}
