/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.util.Map;
import java.util.TreeMap;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests ConfigurationStore class.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
@Deprecated
public class ConfigurationStoreTest {

  /**
   * This store's namespace.
   */
  private String namespace;
  /**
   * This store's configuration items, mapped by their keys.
   */
  private Map<String, ConfigurationItem> configurationItems =
      new TreeMap<>();
  /**
   * Configuration store instance.
   */
  private ConfigurationStore store;

  @Before
  public void setUp() {
    namespace = "testConfigurationStore";
    store = ConfigurationStore.getStore(namespace);
    configurationItems = null;
  }

  @After
  public void tearDown() {
    configurationItems = null;
    store = null;
    namespace = null;
  }

  @Test
  public void testConfigurationStore() {
    assertNotNull(store);
  }

  @Test
  public void testgetNamespace() {
    assertEquals(store.getNamespace(), namespace);
  }

  @Test
  public void testgetBoolean() {
    boolean result = store.getBoolean("testKeyboolean", true);
    assertTrue(result);
  }

  @Test
  public void testgetByte() {
    byte value = 10;
    byte result = store.getByte("testKeybyte", value);
    assertEquals(result, value);
  }

  @Test
  public void testgetInteger() {
    int value = 100;
    int result = store.getInt("testKeyint", value);
    assertEquals(result, value);
  }

  @Test
  public void testgetShort() {
    short value = 2;
    short result = store.getShort("testKeyshort", value);
    assertEquals(result, value);
  }

  @Test
  public void testgetLong() {
    long value = 100000000;
    long result = store.getLong("testKeylong", value);
    assertEquals(result, value);
  }

  @Test
  public void testgetFloat() {
    float value = 1 / 3;
    float result = store.getFloat("testKeyfloat", value);
    assertNotNull(result);
  }

  @Test
  public void testgetDouble() {
    Double value = 360000000.0;
    Double result = store.getDouble("testKeyDouble", value);
    assertEquals(result, value);
  }

  @Test
  public void testgetString() {
    String value = "test string";
    String result = store.getString("testKeystring", value);
    assertEquals(result, value);
  }

  /*@Test
  public void testconfigurationItems() {
    configurationItems = store.getConfigurationItems();
    System.out.println("Following are the configuration Items : \n");
    for (String key : configurationItems.keySet()) {
      System.out.println("-->  " + key + "\n");
    }
    assertNotNull(this);
  }*/
}
