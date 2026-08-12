// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;

/**
 * Unit tests for {@link MessageValidator}.
 */
public class MessageValidatorTest {

  private MessageValidator messageValidator;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
  }

  @Test
  public void handleValidMessage() {
    assertDoesNotThrow(
        () -> messageValidator.validate(
            validConnectionMessage(),
            Connection.class
        )
    );

    assertDoesNotThrow(
        () -> MessageValidator.ACCEPTING_ALL.validate(validConnectionMessage(), Connection.class)
    );
  }

  @Test
  public void handleMessageWithMissingConnectionState() {
    assertThrows(
        IllegalArgumentException.class,
        () -> messageValidator.validate(
            connectionMessageWithNullConnectionState(),
            Connection.class
        )
    );

    assertDoesNotThrow(
        () -> MessageValidator.ACCEPTING_ALL.validate(
            connectionMessageWithNullConnectionState(),
            Connection.class
        )
    );
  }

  @Test
  public void handleEmptyMessage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> messageValidator.validate(
            "",
            Connection.class
        )
    );

    assertDoesNotThrow(
        () -> MessageValidator.ACCEPTING_ALL.validate("", Connection.class)
    );
  }

  @Test
  public void handleMessageWithExtraProperty() {
    assertThrows(
        IllegalArgumentException.class,
        () -> messageValidator.validate(
            connectionMessageWithExtraProperty(),
            Connection.class
        )
    );

    assertDoesNotThrow(
        () -> MessageValidator.ACCEPTING_ALL.validate(
            connectionMessageWithExtraProperty(),
            Connection.class
        )
    );
  }

  private static String validConnectionMessage() {
    return """
        {
          "headerId" : 0,
          "timestamp" : "1970-01-01T00:00:00Z",
          "version" : "version",
          "manufacturer" : "manufacturer",
          "serialNumber" : "serial-number",
          "connectionState" : "ONLINE"
        }""";
  }

  private static String connectionMessageWithNullConnectionState() {
    return """
        {
          "headerId" : 0,
          "timestamp" : "1970-01-01T00:00:00Z",
          "version" : "version",
          "manufacturer" : "manufacturer",
          "serialNumber" : "serial-number",
          "connectionState" : null
        }""";
  }

  private static String connectionMessageWithExtraProperty() {
    return """
        {
          "headerId" : 0,
          "timestamp" : "1970-01-01T00:00:00Z",
          "version" : "version",
          "manufacturer" : "manufacturer",
          "serialNumber" : "serial-number",
          "connectionState" : "ONLINE",
          "extraContent" : "something"
        }""";
  }
}
