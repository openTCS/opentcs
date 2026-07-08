// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A login request with a username and a password.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class PostLoginRequestTO {
  @Nonnull
  private String username;
  @Nonnull
  private String password;
}
// CHECKSTYLE:ON
