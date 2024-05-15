/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.cfg4j;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.configuration.ConfigurationBindingProvider;

/**
 * Tests for reading configuration entries with cfg4j.
 */
public class SampleConfigurationTest {

  private ConfigurationBindingProvider input;

  @BeforeEach
  void setUp() {
    input = cfg4jConfigurationBindingProvider();
  }

  @Test
  void testBoolean() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.simpleBoolean(), is(true));
  }

  @Test
  void testInteger() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.simpleInteger(), is(600));
  }

  @Test
  void testString() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.simpleString(), is("HelloWorld"));
  }

  @Test
  void testEnum() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.simpleEnum(), is(SampleConfigurationTest.DummyEnum.ORDER));
  }

  @Test
  void testStringList() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.stringList(), equalTo(List.of("A", "B", "C")));
  }

  @Test
  void testStringMap() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.stringMap(), equalTo(Map.of("A", "1", "B", "2", "C", "3")));
  }

  @Test
  void testEnumMap() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.enumMap(), equalTo(Map.of(DummyEnum.ORDER, "1", DummyEnum.POINT, "2")));
  }

  @Test
  void testObjectList() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.objectList(), equalTo(List.of(new DummyClass("A", "B", 1),
                                                    new DummyClass("C", "D", 2))));
  }

  @Test
  void testStringConstructor() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.stringConstructor(), equalTo(new DummyClass("A", "B", 1)));
  }

  @Test
  void testClassPath() {
    SampleConfig config = input.get(SampleConfig.PREFIX, SampleConfig.class);
    assertThat(config.classPath(), is(DummyClass.class));
  }

  private static ConfigurationBindingProvider cfg4jConfigurationBindingProvider() {
    try {
      return new Cfg4jConfigurationBindingProvider(
          Paths.get(Thread.currentThread().getContextClassLoader()
              .getResource("org/opentcs/configuration/cfg4j/sampleConfig.properties").toURI())
      );
    }
    catch (URISyntaxException ex) {
      Logger.getLogger(SampleConfigurationTest.class.getName()).log(Level.SEVERE, null, ex);
      assertFalse(true);
      return null;
    }
  }

  public interface SampleConfig {

    /**
     * This configuration's prefix.
     */
    String PREFIX = "sampleConfig";

    boolean simpleBoolean();

    int simpleInteger();

    String simpleString();

    SampleConfigurationTest.DummyEnum simpleEnum();

    List<String> stringList();

    Map<String, String> stringMap();

    Map<DummyEnum, String> enumMap();

    List<DummyClass> objectList();

    DummyClass stringConstructor();

    Class<DummyClass> classPath();

  }

  public enum DummyEnum {
    /**
     * Vehicle.
     */
    VEHICLE,
    /**
     * Point.
     */
    POINT,
    /**
     * Order.
     */
    ORDER
  }
}
