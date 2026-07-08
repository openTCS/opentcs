// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.UserNotificationTO;

/**
 * Tests for {@link UserNotificationConverter}.
 */
public class UserNotificationConverterTest {
  private JsonBinder jsonBinder;
  private UserNotificationConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new UserNotificationConverter();
  }

  @Test
  void convert() {
    UserNotification userNotification = new UserNotification(
        "source",
        "notification-text",
        UserNotification.Level.INFORMATIONAL
    );

    UserNotificationTO result = converter.convert(userNotification);

    Approvals.verify(
        jsonBinder.toJson(result),
        // Scrub timestamps in the result's JSON representation as some of them may be generated
        // during test runtime, making it impossible to use ApprovalTests.
        new Options(new TimestampScrubber())
    );
  }

  @ParameterizedTest
  @EnumSource(UserNotification.Level.class)
  void convertsUserNotificationLevels(UserNotification.Level level) {
    UserNotification userNotification = new UserNotification("notification-text", level);

    UserNotificationTO result = converter.convert(userNotification);

    UserNotificationTO.LevelTO expectedLevel = switch (level) {
      case INFORMATIONAL -> UserNotificationTO.LevelTO.INFORMATIONAL;
      case NOTEWORTHY -> UserNotificationTO.LevelTO.NOTEWORTHY;
      case IMPORTANT -> UserNotificationTO.LevelTO.IMPORTANT;
    };
    assertThat(result.getLevel(), is(expectedLevel));
  }
}
