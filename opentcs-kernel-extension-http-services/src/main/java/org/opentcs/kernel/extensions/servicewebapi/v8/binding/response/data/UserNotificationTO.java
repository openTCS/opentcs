// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.notification.UserNotification;

/**
 * A transfer object representing a {@link UserNotification} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class UserNotificationTO {
  @Nullable
  private String source;
  @Nonnull
  private String text;
  @Nonnull
  private LevelTO level;
  @Nonnull
  private Instant timestamp;

  public enum LevelTO {
    INFORMATIONAL,
    NOTEWORTHY,
    IMPORTANT
  }
}
// CHECKSTYLE:ON
