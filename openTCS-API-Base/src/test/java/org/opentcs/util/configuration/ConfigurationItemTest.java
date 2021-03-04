/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests ConfigurationItem class.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
@Deprecated
public class ConfigurationItemTest {

  /**
   * Configuration item instance.
   */
  private ConfigurationItem configItem;
  /**
   * Configuration item namespace.
   */
  private String namespace;
  /**
   * Configuration item key.
   */
  private String key;
  /**
   * Configuration item description.
   */
  private String description;
  /**
   * Configuration item value.
   */
  private String value;
  /**
   * Configuration item constraint.
   */
  private ItemConstraint constraint;

  @Before
  public void setUp() {
    namespace = "Test NameSpace";
    key = "Test key";
    description = "Test Description";
    value = "Test value";
    constraint = new ItemConstraintInteger(Integer.MIN_VALUE,Integer.MAX_VALUE,
        ConfigurationDataType.INTEGER);
    configItem = new ConfigurationItem(namespace,
                                       key,
                                       description,
                                       constraint,
                                       value);
  }

  @After
  public void tearDown() {
    configItem = null;
    namespace = null;
    key = null;
    description = null;
    value = null;
  }

  @Test
  public void testgetKey() {
    String result = configItem.getKey();
    assertEquals(result, key);
  }

  @Test
  public void testgetDescription() {
    String result = configItem.getDescription();
    assertEquals(result, description);
  }

  @Test
  public void testgetNamespace() {
    String result = configItem.getNamespace();
    assertEquals(result, namespace);
  }

  @Test
  public void testgetValue() {
    String result = configItem.getValue();
    assertEquals(result, value);
  }

  @Test
  public void testgetConstraint() {
    ConfigurationDataType result = configItem.getConstraint().getType();
    assertEquals(result, ConfigurationDataType.INTEGER);
  }

  @Test
  public void testEquals() {
    ConfigurationItem testItem =
        new ConfigurationItem("nameSpace2", "key2", null,
                              constraint, null);

    boolean result = configItem.equals(testItem);
    assertFalse(result);
  }

  @Test(expected = NullPointerException.class)
  public void testConfigurationItem() {
    ConfigurationItem testItem =
        new ConfigurationItem("nameSpace2", "key2", null,
                              null, null);
  }
}
