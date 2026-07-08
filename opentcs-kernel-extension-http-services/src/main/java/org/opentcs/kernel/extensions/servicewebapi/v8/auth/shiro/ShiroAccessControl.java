// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.auth.shiro;

import static java.util.Objects.requireNonNull;

import io.javalin.security.RouteRole;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AccessControl;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AuthenticationException;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.UserPermission;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.LoginResponseTO;

/**
 * An implementation of {@link AccessControl} utilizing the Shiro security framework.
 */
public class ShiroAccessControl
    implements
      AccessControl {

  /**
   * Creates a new instance.
   */
  public ShiroAccessControl() {
  }

  @Override
  public LoginResponseTO login(
      @Nonnull
      String username,
      @Nonnull
      String password
  )
      throws AuthenticationException {
    requireNonNull(username, "username");
    requireNonNull(password, "password");

    if (SecurityUtils.getSubject().isAuthenticated()) {
      return getLoginResponse();
    }

    try {
      UsernamePasswordToken token = new UsernamePasswordToken(username, password);
      SecurityUtils.getSubject().login(token);
    }
    catch (org.apache.shiro.authc.AuthenticationException e) {
      throw new AuthenticationException(e);
    }
    return getLoginResponse();
  }

  @Override
  public Optional<LoginResponseTO> getLoginInformation() {
    if (!isLoggedIn()) {
      return Optional.empty();
    }

    return Optional.of(getLoginResponse());
  }

  @Override
  public boolean logout() {
    try {
      SecurityUtils.getSubject().logout();
    }
    catch (IllegalStateException e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isLoggedIn() {
    return SecurityUtils.getSubject().isAuthenticated();
  }

  @Override
  public boolean isAuthorized(
      @Nonnull
      Set<RouteRole> permissions
  ) {
    requireNonNull(permissions, "permissions");
    return permissions
        .stream()
        .filter(routeRole -> routeRole instanceof UserPermission)
        .map(routeRole -> (UserPermission) routeRole)
        .allMatch(permission -> SecurityUtils.getSubject().isPermitted(permission.name()));
  }

  private LoginResponseTO getLoginResponse() {
    Subject subject = SecurityUtils.getSubject();
    Instant creationTime = subject.getSession().getStartTimestamp().toInstant();
    Instant lastTimeAccess = subject.getSession().getLastAccessTime().toInstant();
    return new LoginResponseTO()
        .setUser(
            new LoginResponseTO.User()
                .setUsername(subject.getPrincipal().toString())
                .setPermissions(
                    Arrays.stream(UserPermission.values())
                        .filter(permission -> subject.isPermitted(permission.name()))
                        .toList()
                )
        )
        .setSession(
            new LoginResponseTO.Session()
                .setSessionId(subject.getSession().getId().toString())
                .setCreationTime(creationTime)
                .setExpirationTime(lastTimeAccess.plusMillis(subject.getSession().getTimeout()))
        );
  }
}
