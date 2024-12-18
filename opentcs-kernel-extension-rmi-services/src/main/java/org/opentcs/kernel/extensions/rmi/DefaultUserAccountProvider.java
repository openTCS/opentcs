// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
 */
public class DefaultUserAccountProvider
    implements
      UserAccountProvider {

  public DefaultUserAccountProvider() {
  }

  @Override
  public Set<UserAccount> getUserAccounts() {
    return new HashSet<>(
        Arrays.asList(
            new UserAccount(
                GuestUserCredentials.USER,
                GuestUserCredentials.PASSWORD,
                EnumSet.allOf(UserPermission.class)
            )
        )
    );
  }
}
