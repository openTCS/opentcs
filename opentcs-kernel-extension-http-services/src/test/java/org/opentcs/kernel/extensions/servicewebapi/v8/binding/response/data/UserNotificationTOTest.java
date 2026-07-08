// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.time.Instant;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;

/**
 * Tests for {@link UserNotificationTO}.
 */
public class UserNotificationTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createUserNotificationMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createUserNotificationFull()));
  }

  private UserNotificationTO createUserNotificationMinimal() {
    return new UserNotificationTO()
        .setText("notificationText")
        .setLevel(UserNotificationTO.LevelTO.INFORMATIONAL)
        .setTimestamp(Instant.EPOCH);
  }

  private UserNotificationTO createUserNotificationFull() {
    return new UserNotificationTO()
        .setSource("source")
        .setText("notificationText")
        .setLevel(UserNotificationTO.LevelTO.INFORMATIONAL)
        .setTimestamp(Instant.EPOCH);
  }
}
