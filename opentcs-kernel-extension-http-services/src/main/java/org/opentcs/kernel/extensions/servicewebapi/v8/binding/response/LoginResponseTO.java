// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.UserPermission;

/**
 * A successful login response with information on the user and session.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class LoginResponseTO {
  @Nonnull
  private User user;
  @Nonnull
  private Session session;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class User {
    @Nonnull
    private String username;
    @Nonnull
    private List<UserPermission> permissions;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Session {
    @Nonnull
    private String sessionId;
    @Nonnull
    private Instant creationTime;
    @Nonnull
    private Instant expirationTime;
  }
}
// CHECKSTYLE:ON
