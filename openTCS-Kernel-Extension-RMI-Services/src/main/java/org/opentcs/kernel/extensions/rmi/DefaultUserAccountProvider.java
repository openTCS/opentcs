/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.opentcs.common.GuestUserCredentials;

/**
 * The default impelementation of {@link UserAccountProvider}.
 * Provides only one (guest) user account.
 *
 * @see GuestUserCredentials
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultUserAccountProvider
    implements UserAccountProvider {

  public DefaultUserAccountProvider() {
  }

  @Override
  public Set<UserAccount> getUserAccounts() {
    return new HashSet<>(Arrays.asList(new UserAccount(GuestUserCredentials.USER,
                                                       GuestUserCredentials.PASSWORD,
                                                       EnumSet.allOf(UserPermission.class))));
  }
}
