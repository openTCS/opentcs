/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.user;

import java.io.Serializable;
import java.util.Set;

/**
 * Instances of this class store user account data, including name, password
 * and granted permissions of the user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserAccount
implements Serializable, Comparable<UserAccount> {
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
   * @param name The user's name.
   * @param pass The user's password.
   * @param perms The user's permissions.
   */
  public UserAccount(String name, String pass, Set<UserPermission> perms) {
    if (name == null) {
      throw new NullPointerException("name is null");
    }
    if (pass == null) {
      throw new NullPointerException("pass is null");
    }
    if (perms == null) {
      throw new NullPointerException("perms is null");
    }
    userName = name;
    password = pass;
    permissions = perms;
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
   * @param newPermissions The user's new permissions.
   */
  public void setPermissions(Set<UserPermission> newPermissions) {
    if (newPermissions == null) {
      throw new NullPointerException("newPermissions is null");
    }
    permissions = newPermissions;
  }
  
  // Implementation of interface Comparable<UserAccount> starts here.

  @Override
  public int compareTo(UserAccount o) {
    return userName.compareTo(o.userName);
  }
}
