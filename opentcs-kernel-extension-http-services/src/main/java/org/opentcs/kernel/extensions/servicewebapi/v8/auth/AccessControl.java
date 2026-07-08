// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.auth;

import io.javalin.security.RouteRole;
import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.LoginResponseTO;

/**
 * Defines methods for authentication and authorization checks.
 */
public interface AccessControl
    extends
      Serializable {

  /**
   * Tries to log in the user with the given username and password.
   *
   * @param username The username.
   * @param password The password.
   *
   * @throws AuthenticationException if a problem occurred while logging in.
   * @return A {@link LoginResponseTO} that contains information on the user and session.
   */
  LoginResponseTO login(
      @Nonnull
      String username,
      @Nonnull
      String password
  )
      throws AuthenticationException;

  /**
   * Retrieves information about the currently logged-in user.
   *
   * @return A {@link LoginResponseTO} that contains information about the currently logged-in user.
   */
  Optional<LoginResponseTO> getLoginInformation();

  /**
   * Logs the currently logged-in user out.
   *
   * @return {@code true} if the log-out was successful, otherwise return {@code false}.
   */
  boolean logout();

  /**
   * Checks whether the current user is authenticated/logged in.
   *
   * <p>
   * The access rights and authentication status of the user associated with the current request
   * are determined through the user context, which is implicitly obtained from the
   * request/session environment.
   * </p>
   *
   * @return {@code true}, if a user is authenticated, otherwise {@code false}.
   */
  boolean isLoggedIn();

  /**
   * Checks whether the current user is authorized to perform the current request
   *
   * <p>
   * A user is considered authorized if they hold all the specified permissions.
   * The access rights and authentication status of the user associated with the current request
   * are determined through the user context, which is implicitly obtained from the
   * request/session environment.
   * </p>
   *
   * @param permissions The permissions the user needs to have to be authorized for the current
   * request.
   * @return {@code true}, if a user is authorized or {@code permissions} is an empty set,
   * otherwise {@code false}.
   */
  boolean isAuthorized(
      @Nonnull
      Set<RouteRole> permissions
  );
}
