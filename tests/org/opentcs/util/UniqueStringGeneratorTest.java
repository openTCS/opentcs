package org.opentcs.util;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * A test case for class UniqueStringGenerator.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UniqueStringGeneratorTest {
  
  private static final String PREFIX = "TestPrefix";
  
  private static final String PATTERN_ONE_DIGIT = "0";
  
  private static final String PATTERN_TWO_DIGITS = "00";
  
  private UniqueStringGenerator generator;

  @Before
  public void setUp() {
    generator = new UniqueStringGenerator();
  }

  @After
  public void tearDown() {
    generator = null;
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
