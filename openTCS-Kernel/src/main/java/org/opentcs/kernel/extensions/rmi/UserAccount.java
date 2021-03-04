/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import java.util.Set;

/**
 * Instances of this class store user account data, including name, password
 * and granted permissions of the user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserAccount
    implements Serializable {

  /**
   * The user's name.
   */
  private final String userName;
  /**
   * The user's password.
   */
  private String password;
  /**
   * The user's permissions.
   */
  private Set<UserPermission> permissions;

  /**
   * Creates a new instance of UserAccount.
   *
   * @param userName The user's name.
   * @param password The user's password.
   * @param perms The user's permissions.
   */
  public UserAccount(String userName, String password, Set<UserPermission> perms) {
    this.userName = requireNonNull(userName, "userName");
    this.password = requireNonNull(password, "password");
    this.permissions = requireNonNull(perms, "perms");
  }

  /**
   * Return the user's name.
   *
   * @return The user's name.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Return the user's password.
   *
   * @return The user's password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the user's password.
   *
   * @param pass The user's password.
   */
  public void setPassword(String pass) {
    password = pass;
  }

  /**
   * Returns the user's permissions.
   *
   * @return The user's permissions.
   */
  public Set<UserPermission> getPermissions() {
    return permissions;
  }

  /**
   * Set the user's permissions.
   *
   * @param permissions The user's new permissions.
   */
  public void setPermissions(Set<UserPermission> permissions) {
    this.permissions = requireNonNull(permissions, "permissions");
  }
}
