/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 * A test case for class UniqueStringGenerator.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UniqueStringGeneratorTest {

  private static final String PREFIX = "TestPrefix";

  private static final String PATTERN_ONE_DIGIT = "0";

  private static final String PATTERN_TWO_DIGITS = "00";

  private UniqueStringGenerator<Object> generator;

  @Before
  public void setUp() {
    generator = new UniqueStringGenerator<>();
  }

  @Test
  public void testRepeatedGenerationWithoutModification() {
    String generatedString = generator.getUniqueString(PREFIX,
                                                       PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
    generatedString = generator.getUniqueString(PREFIX, PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
  }

  @Test
  public void shouldProvideConfiguredPatterns() {
    final String NAME_PATTERN_PREFIX = "SomePrefix";
    final String NAME_PATTERN_PREFIX2 = "AnotherPrefix";
    final Object selector = new Object();
    final Object selector2 = new Object();
    
    generator.registerNamePattern(selector, NAME_PATTERN_PREFIX, "0000");
    generator.registerNamePattern(selector2, NAME_PATTERN_PREFIX2, "0000");

    assertEquals(NAME_PATTERN_PREFIX + "0001",
                 generator.getUniqueString(selector));
    assertEquals(NAME_PATTERN_PREFIX2 + "0001",
                 generator.getUniqueString(selector2));
  }

  @Test
  public void testRepeatedGenerationWithAddition() {
    String generatedString = generator.getUniqueString(PREFIX,
                                                       PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "01", generatedString);
    generator.addString(generatedString);
    generatedString = generator.getUniqueString(PREFIX, PATTERN_TWO_DIGITS);
    assertEquals(PREFIX + "02", generatedString);
  }

  @Test
  public void testNullPrefix() {
    String generatedString = generator.getUniqueString(null,
                                                       PATTERN_ONE_DIGIT);
    assertEquals("1", generatedString);
  }

}
