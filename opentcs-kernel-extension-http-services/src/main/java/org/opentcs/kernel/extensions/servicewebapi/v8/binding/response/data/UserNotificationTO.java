// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
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
