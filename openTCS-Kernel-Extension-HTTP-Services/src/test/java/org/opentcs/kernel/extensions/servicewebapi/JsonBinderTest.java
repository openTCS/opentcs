/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import org.approvaltests.Approvals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JsonBinder}.
 */
class JsonBinderTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void writeAndParseObject() {
    String json = jsonBinder.toJson(new TestObject().setName("some-name"));

    Approvals.verify(json);

    TestObject parsedObject = jsonBinder.fromJson(json, TestObject.class);

    assertThat(parsedObject.getName(), is(equalTo("some-name")));
  }

  @Test
  void writeAndParseThrowable() {
    Approvals.verify(jsonBinder.toJson(new TestException("some-message")));
  }

  private static class TestObject {

    private String name;

    public String getName() {
      return name;
    }

    public TestObject setName(String name) {
      this.name = name;
      return this;
    }
  }

  private static class TestException
      extends Exception {

    TestException(String message) {
      super(message);
    }

  }
}
